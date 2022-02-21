package com.hmgmbh.netzfrequenzwatch.service;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;

import com.hmgmbh.netzfrequenzwatch.MainActivity;
import com.hmgmbh.netzfrequenzwatch.R;
import com.hmgmbh.netzfrequenzwatch.data.DataRepository;
import com.hmgmbh.netzfrequenzwatch.data.DownloadThread;


public class ForegroundService extends LifecycleService {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static final String TAG = "ForegroundService";

    private PowerManager.WakeLock wakeLock = null;
    private Boolean isServiceStarted = false;
    DownloadThread mDownloadThread = new DownloadThread(
           "https://www.netzfrequenz.info/verlauf-3-minuten-tennet50hertz/../json/lsdtest.php");
    DataRepository mRepo = DataRepository.getInstance();
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    boolean mAlarmActive = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if(notification == null){
            // I can't see this ever being null (as always have a default notification)
            // but just incase
            if(notification == null) {
                // alert backup is null, using 2nd backup
                notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        if (mediaPlayer == null) {
            // fallback to ringtone
            notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mediaPlayer = MediaPlayer.create(getApplicationContext(), notification);
        }
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
        }
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isServiceStarted) return START_STICKY;
        super.onStartCommand(intent, flags, startId);
//        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Netzfrequenz Überwachung")
                .setContentText("Dieser Dienst überwacht die Netzfrequenz")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Ticker text")
                .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
//                .setAutoCancel(true)
                .build();

        startForeground(1, notification);

        //do heavy work on a background thread

        startService();

        //stopSelf();

        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY;
    }

    private void startService() {
        if (isServiceStarted) return;
        Log.d(TAG, "Starting the foreground service task");
        Toast.makeText(this, "Service starting its task", Toast.LENGTH_SHORT).show();
        isServiceStarted = true;
        ServiceTracker.setServiceState(this, ServiceState.STARTED);

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock = acquire(this, PowerManager.PARTIAL_WAKE_LOCK, 10000, "ForegroundService::lock");

        // start download thread
        mDownloadThread.start();

        mRepo.frequStatus().getWarnings().observe(this, warnings -> {
            if (warnings > 0) {
                startAlarm();
//                Toast.makeText(this, "Netz Warnung!!!!!!", Toast.LENGTH_SHORT).show();
            }
        });
        mRepo.frequStatus().getNotNet().observe(this, nn -> {
            if (nn == 4) {
//                Toast.makeText(this, "kein Netzwerk!!!!!!", Toast.LENGTH_SHORT).show();
//                startAlarm();
            }
        });
    }

    private PowerManager.WakeLock acquire(Context context, int lockType, long timeout, String tag) {
//        tag = prefixTag(tag);
        try {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(lockType, tag);

            wakeLock.acquire(timeout);
            Log.d(TAG, "Acquired wakelock with tag: " + tag);

            return wakeLock;
        } catch (Exception e) {
            Log.w(TAG, "Failed to acquire wakelock with tag: " + tag, e);
            return null;
        }
    }


    private void stopService() {
        Log.d(TAG, "Stopping the foreground service");
        mDownloadThread.stopIt();
        Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show();
        try {
            if (wakeLock.isHeld()) wakeLock.release();
            stopForeground(true);
            stopSelf();
        } catch (Exception e) {
            Log.d(TAG, "Service stopped without being started: ${e.message}");
        }
        stopAlarm();
        isServiceStarted = false;
        ServiceTracker.setServiceState(this, ServiceState.STOPPED);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), ForegroundService.class);
        restartServiceIntent.setPackage(getPackageName());
        PendingIntent restartServicePendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        AlarmManager alarmService  = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent);
        Log.d(TAG, "The task has been removed".toUpperCase());
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroying the foreground service");
        stopService();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            serviceChannel.setDescription("Endless Service channel");
            serviceChannel.enableLights(true);
            serviceChannel.setLightColor(Color.RED);
            serviceChannel.enableVibration(true);
            serviceChannel.setVibrationPattern( new long[]{100l, 200l, 300l, 400l, 500l, 400l, 300l, 200l, 400l});

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    private void startAlarm() {
        if (!mAlarmActive) {
            if (mediaPlayer != null) {
                mediaPlayer.start();
            }

            if (vibrator != null) {
                long[] pattern = {0, 100, 1000};
                vibrator.vibrate(pattern, 0);
            }
            showAlarmDialog();
            mAlarmActive = true;
        }
    }

    private void stopAlarm() {
        if (mAlarmActive) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            if (vibrator != null) {
                vibrator.cancel();
            }
            mAlarmActive = false;
        }
    }
/*
    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        View view = View.inflate(context, R.layout.alarm_dialog_view, null);
        AlertDialog  dialog = builder.create();
        dialog.setView(view);
        // The 8.0 system strengthens background management and prohibits pop-up reminders in other applications and windows. If you want to pop, you must use TYPE_APPLICATION_OVERLAY
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0 new features
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY - 1);
        } else {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
        }
        dialog.show();
        Window dialogWindow = dialog.getWindow();// Get window object
        dialogWindow.setGravity(Gravity.CENTER);// Set the dialog position
    }
*/
    private void showAlarmDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes Button Clicked
                        stopAlarm();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light);
        builder.setMessage("!!!!!!!!! GEFAHR EINES BLACKOUTS !!!!!!!")
                .setPositiveButton("Ok", dialogClickListener)
                .setTitle("!!!!!!!!!! ACHTUNG !!!!!!!!!!!");
        AlertDialog alertDialog = builder.create();
        if (Build.VERSION.SDK_INT >= 25) {
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else {
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }

        int LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                 WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        alertDialog.getWindow().setAttributes(params);

        alertDialog.show();
    }
}
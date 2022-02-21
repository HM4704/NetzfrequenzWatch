package com.hmgmbh.netzfrequenzwatch.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.hmgmbh.netzfrequenzwatch.service.Actions;
import com.hmgmbh.netzfrequenzwatch.service.ServiceState;
import com.hmgmbh.netzfrequenzwatch.service.ServiceTracker;

public  class StartReceiver extends BroadcastReceiver {


    public void startService(Context context) {
        Intent serviceIntent = new Intent(context, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ForegroundService", "onReceive called ");
        if (intent.getAction() == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("ForegroundService", "onReceive  ACTION_BOOT_COMPLETED");
            if (/*ServiceTracker.getServiceState(context) == ServiceState.STARTED*/true) {
                Log.d("ForegroundService", "onReceive  ServiceState.STARTED");
                intent.setAction(Actions.START.name());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.d("ForegroundService", "Starting the service in >=26 Mode from a BroadcastReceiver");
                    startService(context);
                    return;
                }
                Log.d("ForegroundService", "Starting the service in < 26 Mode from a BroadcastReceiver");
                context.startService(intent);
            }
        }
    }
}

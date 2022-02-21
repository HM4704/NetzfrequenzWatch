package com.hmgmbh.netzfrequenzwatch;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hmgmbh.netzfrequenzwatch.service.Actions;
import com.hmgmbh.netzfrequenzwatch.service.ForegroundService;
import com.hmgmbh.netzfrequenzwatch.service.ServiceState;
import com.hmgmbh.netzfrequenzwatch.service.ServiceTracker;
import com.hmgmbh.netzfrequenzwatch.ui.main.SectionsPagerAdapter;
import com.hmgmbh.netzfrequenzwatch.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Config.context = this;

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);

        // request permission for alarm dialog in service
        getDialogPermission();
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, com.hmgmbh.netzfrequenzwatch.service.ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        }
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, com.hmgmbh.netzfrequenzwatch.service.ForegroundService.class);
        stopService(serviceIntent);
    }

    public void actionOnService(Actions action) {
        if (ServiceTracker.getServiceState(this) == ServiceState.STOPPED
                && action == Actions.STOP) return;
            Intent intent = new Intent(this, ForegroundService.class);
            intent.setAction(action.name());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                log("Starting the service in >=26 Mode")
                startForegroundService(intent);
                return;
            }
//            log("Starting the service in < 26 Mode")
            startService(intent);
    }
    private void getDialogPermission() {
        //Android6.0 + need require permission
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(intent);
                return;
            }
        }
    }
}
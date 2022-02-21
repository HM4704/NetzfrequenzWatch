package com.hmgmbh.netzfrequenzwatch.service;

import android.content.Context;
import android.content.SharedPreferences;


public class ServiceTracker {
    private static final String name = "SPYSERVICE_KEY";
    private static final String key = "SPYSERVICE_STATE";

    static public void setServiceState(Context context, ServiceState state) {
        SharedPreferences sharedPrefs = getPreferences(context);
        sharedPrefs.edit().putString(key, state.name());
        sharedPrefs.edit().apply();
    }

    static public ServiceState getServiceState(Context context)  {
        SharedPreferences sharedPrefs = getPreferences(context);
        String value = sharedPrefs.getString(key, ServiceState.STOPPED.name());
        return ServiceState.valueOf(value);
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(name, 0);
    }
}

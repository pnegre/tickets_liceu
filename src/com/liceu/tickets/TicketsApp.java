package com.liceu.tickets;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;


public class TicketsApp extends Application {
    static String TAG = "TicketsApp";
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    LiceuServer server = new LiceuServerImp();
    Database database;

    boolean alarmset = false;

    public void onCreate() {
        Log.v(TAG, "OnCreate (application)");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        initServer(sp);

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public synchronized void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                initServer(prefs);
            }
        };
        // Fer-ho així per evitar que el garbage collector elimini el listener
        sp.registerOnSharedPreferenceChangeListener(listener);

        database = new DatabaseImp(this);
        //setAlarm();
    }

    void initServer(SharedPreferences sp) {
        Log.v(TAG, "Initializing server...");
        String username = sp.getString("username", "0");
        String password = sp.getString("password", "0");
        server.init(username, password);
        Log.v(TAG, "done");
    }


    synchronized void setAlarm() {
        if (alarmset == true) return;

        Intent myIntent = new Intent(this, LiceuService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, myIntent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 60 * 60 * 2); // 2h until alarm is fired
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        alarmset = true;

        Log.v(TAG, "Set alarm");
    }


}

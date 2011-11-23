package com.liceu.tickets;

import java.util.*;
import android.util.Log;
import android.app.Application;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.content.Context;
import android.content.BroadcastReceiver;

import android.app.PendingIntent;
import android.app.AlarmManager;



public class TicketsApp extends Application
{
	static String TAG = "TicketsApp";
	SharedPreferences.OnSharedPreferenceChangeListener listener;
	
	LiceuServer server = new LiceuServerImp();
	Database database;
	
	boolean alarmset = false;
	
	public void onCreate()
	{
		Log.v(TAG, "OnCreate (application)");
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		init_server(sp);
		
		listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public synchronized void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
				init_server(prefs);
			}
		};
		// Fer-ho així per evitar que el garbage collector elimini el listener
		sp.registerOnSharedPreferenceChangeListener(listener);
		
		database = new DatabaseImp(this);
		//set_alarm();
	}
	
	void init_server(SharedPreferences sp)
	{
		Log.v(TAG, "Initializing server...");
		String username = sp.getString("username","0");
		String password = sp.getString("password","0");
		server.init(username,password);
		Log.v(TAG, "done");
	}
	
	
	
	
	synchronized void set_alarm()
	{
		if (alarmset == true) return;
		
		Intent myIntent = new Intent(this, LiceuService.class);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, myIntent, 0);

		AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, 60*60*2); // 2h until alarm is fired
		alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
		alarmset = true;
		
		Log.v(TAG, "Set alarm");
	}



}

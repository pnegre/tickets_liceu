package com.liceu.tickets;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;


public class LiceuService extends IntentService {
    static String TAG = "LiceuService";
    Thread intentThread;

    TicketsApp tapp;

    public LiceuService() {
        super("LiceuService");

        Log.v(TAG, "Service started");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Wakelock
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DoNjfdhotDimScreen");
        wl.acquire();

        Log.v(TAG, "Intent started");

        tapp = (TicketsApp) getApplication();
        if (tapp == null) {
            // It should not happen
            Log.v(TAG, "tapp null");
            wl.release();
            return;
        }

        intentThread = Thread.currentThread();

        try {
            tapp.database.refreshDataFromServer(tapp.server);
            Log.v(TAG, "Data refreshed!");
        } catch (ServerError e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.v(TAG, "EXCEPTION!");
            e.printStackTrace();
        }

        Log.v(TAG, "Intent finished");

        tapp.alarmset = false;
        tapp.setAlarm();

        wl.release();
    }

    public void onDestroy() {
        intentThread.interrupt();
    }
}

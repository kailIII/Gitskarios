package com.alorma.github.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Bernat on 24/04/2015.
 */
public class SyncService extends Service {

    private NotificationsSyncAdapter syncAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
        if (syncAdapter == null) {
            syncAdapter = new NotificationsSyncAdapter(getApplicationContext(), true, true);
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}

package com.alorma.github.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.alorma.github.BuildConfig;
import com.alorma.github.R;
import com.alorma.github.sdk.bean.dto.response.Notification;
import com.alorma.github.sdk.login.AccountsHelper;
import com.alorma.github.sdk.services.notifications.GetNotificationsClient;
import com.alorma.github.ui.activity.NotificationsActivity;
import com.alorma.gitskarios.basesdk.client.StoreCredentials;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

/**
 * Created by Bernat on 24/04/2015.
 */
public class NotificationsSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String NOTIF_SINCE = "NOTIF_SINCE";

    public NotificationsSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {

        StoreCredentials storeCredentials = new StoreCredentials(getContext());
        String authToken = AccountsHelper.getUserToken(getContext(), account);

        storeCredentials.storeToken(authToken);

        GetNotificationsClient notificationsClient;

        notificationsClient = new GetNotificationsClient(getContext());


        if (BuildConfig.DEBUG) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
            builder.setContentTitle("Syncing");
            builder.setSmallIcon(R.drawable.ic_stat_ic_launcher);
            builder.setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_launcher));
            builder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
            builder.setLights(getContext().getResources().getColor(R.color.primary), 3000, 3000);
            builder.setWhen(System.currentTimeMillis());

            NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(1, builder.build());
        }

        notificationsClient.setStoreCredentials(storeCredentials);

        List<Notification> notifications = notificationsClient.executeSync();

        if (notifications != null && notifications.size() > 0) {
            showNotification(notifications.size());
        }

        if (BuildConfig.DEBUG) {
            NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.cancel(1);
        }
    }

    private void showNotification(int size) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
        builder.setContentTitle(getContext().getString(R.string.notification_title));
        builder.setContentText(getContext().getString(R.string.notification_text, size));
        builder.setSmallIcon(R.drawable.ic_stat_ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_launcher));

        Intent intent = NotificationsActivity.launchIntent(getContext());

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(NotificationsActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        builder.setLights(getContext().getResources().getColor(R.color.primary), 3000, 3000);
        builder.setWhen(System.currentTimeMillis());
        NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(0, builder.build());
    }
}

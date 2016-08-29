package com.example.prashant.picupload;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by prashant on 29/08/16.
 */
public class UploadPicService extends IntentService {

    private String UPLOAD_URL = "http://fakeurl.com";
    private String name;
    private String KEY_NAME = "name";
    private int notificationId = 1;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    public static boolean shouldStop = false;

    public UploadPicService() {
        super(UploadPicService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;
        try {
            name = intent.getStringExtra(KEY_NAME);
            Uri filePath = intent.getData();
            //Getting the Bitmap from Gallery
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
            String image = Utils.getStringImage(bitmap);
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(image)) return;
            byte bytes[] = image.getBytes();
            createNotification();
            int bufferLength = 1024;
            for (int i = 0; i < bytes.length; i += bufferLength) {
                if (shouldStop) {
                    publishError(intent);
                    return;
                }
                int progress = (int) ((i / (float) bytes.length) * 100);
                publishProgress(progress);
                // Sleeps the thread, simulating an upload operation
                // that takes time
                try {
                    // Sleep for 0.1 seconds
                    Thread.sleep(1 * 100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void publishProgress(int progress) {
        // Sets the progress indicator to a max value, the
        // current completion percentage, and "determinate"
        // state
        if (progress == 100) removeNotification();
        mBuilder.setProgress(100, progress, false);
        // Displays the progress bar for the first time.
        mNotifyManager.notify(notificationId, mBuilder.build());
    }

    private void createNotification() {
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Uploading " + name)
                .setContentText(getString(R.string.upload_progress))
                .setSmallIcon(R.drawable.ic_notification);
    }

    private void removeNotification() {
        // When the upload is finished, updates the notification
        mBuilder.setContentTitle("Uploaded " + name)
                .setContentText(getString(R.string.upload_complete))
                // Removes the progress bar
                .setProgress(0, 0, false);
        mNotifyManager.notify(notificationId, mBuilder.build());
    }

    private void publishError(Intent intent) {
        PendingIntent retryPendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentTitle("Error while uploading " + name)
                .setContentText("Click to retry")
                .setProgress(0, 0, false)
                .setContentIntent(retryPendingIntent);
        mNotifyManager.notify(notificationId, mBuilder.build());
    }
}

package com.example.prashant.picupload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by prashant on 30/08/16.
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkStateReceiver";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getExtras() != null) {
            final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
            if (ni != null && ni.isConnectedOrConnecting()) {
                Log.i(TAG, "Network " + ni.getTypeName() + " connected");
                setUploadServiceStopState(false);
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                Log.d(TAG, "There's no network connectivity");
                setUploadServiceStopState(true);
            }
        }
    }

    private void setUploadServiceStopState(boolean state) {
        UploadPicService.shouldStop = state;
    }
}
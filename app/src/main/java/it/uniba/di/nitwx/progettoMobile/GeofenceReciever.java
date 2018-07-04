package it.uniba.di.nitwx.progettoMobile;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;


public class GeofenceReciever extends BroadcastReceiver {
    private static GeofencingClient mGeofencingClient;
    private static List<Geofence> mGeofenceList;
    private static PendingIntent mGeofencePendingIntent;
    Context contextBootReceiver;

    private static final String TAG = "GeoFence Reciever";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final PendingResult pendingResult = goAsync();
        @SuppressLint("StaticFieldLeak") AsyncTask<String, Integer, String> asyncTask = new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... params) {
                StringBuilder sb = new StringBuilder();
                sb.append("Action: " + intent.getAction() + "\n");
                sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
                Log.d(TAG, "I got here");
                // Must call finish() so the BroadcastReceiver can be recycled.
                pendingResult.finish();
                return "Test";
            }
        };
        asyncTask.execute();
    }
    /*
    @Override
    public void onReceive(final Context context, Intent intent) {


        contextBootReceiver = context;

        SharedPreferences sharedPrefs;
        SharedPreferences.Editor editor;
        if ((intent.getAction().equals("android.location.MODE_CHANGED") && isLocationModeAvailable(contextBootReceiver)) || (intent.getAction().equals("android.location.PROVIDERS_CHANGED") && isLocationServciesAvailable(contextBootReceiver))) {
            // isLocationModeAvailable for API >=19, isLocationServciesAvailable for API <19
            sharedPrefs = context.getSharedPreferences("GEO_PREFS", Context.MODE_PRIVATE);
            editor = sharedPrefs.edit();
            editor.remove("Geofences added");
            editor.commit();
            if (!isGooglePlayServicesAvailable()) {
                Log.i(TAG, "Google Play services unavailable.");
                return;
            }

            mGeofencePendingIntent = null;
            mGeofenceList = new ArrayList<Geofence>();

            mGeofencingClient = LocationServices.getGeofencingClient(contextBootReceiver);
            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId("PrimaGeofence")

                    .setCircularRegion(
                            41.315376,
                            16.261924,
                            1000
                    )
                    .setExpirationDuration(60 * 60 * 1000 )
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }
    }

    private boolean isLocationModeAvailable(Context context) {

        if (Build.VERSION.SDK_INT >= 19 && getLocationMode(context) != Settings.Secure.LOCATION_MODE_OFF) {
            return true;
        }
        else return false;
    }

    public boolean isLocationServciesAvailable(Context context) {
        if (Build.VERSION.SDK_INT < 19) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

        }
        else return false;
    }

    public int getLocationMode(Context context) {
        try {
            return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");
        SharedPreferences sharedPrefs = contextBootReceiver.getSharedPreferences("GEO_PREFS", Context.MODE_PRIVATE);
        String geofencesExist = sharedPrefs.getString("Geofences added", null);

        if (geofencesExist == null) {
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent(contextBootReceiver));

        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult((android.app.Activity) contextBootReceiver,
                        Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                Log.i(TAG, "Exception while resolving connection error.", e);
            }
        } else {
            int errorCode = connectionResult.getErrorCode();
            Log.i(TAG, "Connection to Google Play services failed with error code " + errorCode);
        }

    }

    @Override
    public void onResult(Status status) {

    }

    private boolean isGooglePlayServicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(contextBootReceiver);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, (android.app.Activity) contextBootReceiver,
                        Constants.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    static GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }


    static PendingIntent getGeofencePendingIntent(Context context) {

        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofenceService.class);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    */
}

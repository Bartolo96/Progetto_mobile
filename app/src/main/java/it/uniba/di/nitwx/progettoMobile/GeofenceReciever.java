package it.uniba.di.nitwx.progettoMobile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;


public class GeofenceReciever extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks {
    private static GeofencingClient mGeofencingClient;
    private static List<Geofence> mGeofenceList;
    private static PendingIntent mGeofencePendingIntent;
    Context contextBootReceiver;

    private static final String TAG = "GeoFence Reciever";

    @SuppressLint("MissingPermission")
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
        SharedPreferences sharedPrefs = contextBootReceiver.getSharedPreferences(Constants.PACKAGE_NAME + "GEO_PREFS", Context.MODE_PRIVATE);
        String geofencesExist = sharedPrefs.getString("Geofences added", null);

        if (geofencesExist == null) {

            mGeofencingClient = LocationServices.getGeofencingClient(contextBootReceiver);
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent(contextBootReceiver)).
                    addOnSuccessListener(onSuccessAddListener).addOnFailureListener(onFailedAddListener);

        }

    }

    OnFailureListener onFailedAddListener = new OnFailureListener() {
        @Override
        public void onFailure(Exception e) {
            e.printStackTrace();
        }
    };
    OnSuccessListener<Void> onSuccessAddListener = new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            SharedPreferences sharedPrefs = contextBootReceiver.getSharedPreferences(Constants.PACKAGE_NAME + "GEO_PREFS", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("Geofences added", "1");
            editor.commit();
            Log.d("Geofence", "Aggiunte");
        }
    };

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final PendingResult pendingResult = goAsync();
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                contextBootReceiver = context;
                SharedPreferences sharedPrefs;
                SharedPreferences.Editor editor;
                intent.getParcelableExtra();
                if ((intent.getAction().equals("android.location.MODE_CHANGED") && getLocationMode(context))) {
                    // isLocationModeAvailable for API >=19, isLocationServciesAvailable for API <19

                    sharedPrefs = context.getSharedPreferences("GEO_PREFS", Context.MODE_PRIVATE);
                    editor = sharedPrefs.edit();
                    editor.remove("Geofences added");
                    editor.commit();
                    mGeofencePendingIntent = null;
                    mGeofenceList = new ArrayList<Geofence>();
                    mGeofenceList.add(new Geofence.Builder()
                            .setRequestId(Constants.PACKAGE_NAME)
                            .setCircularRegion(
                                    41.3149307,
                                    16.2623632,
                                    4000
                            )
                            .setExpirationDuration(1000 * 60 * 60 * 60)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                    Geofence.GEOFENCE_TRANSITION_EXIT)
                            .build());

                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions((android.app.Activity)context,
                                new String[]{Manifest.permission.READ_CONTACTS},
                                Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                    }
                    else {
                        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent(contextBootReceiver)).
                                addOnSuccessListener(onSuccessAddListener).addOnFailureListener(onFailedAddListener);
                    }
                }
                return null;
            }
        };
        asyncTask.execute();
    }
    private boolean getLocationMode(Context c){
        try {
             Settings.Secure.getInt(c.getContentResolver(), Settings.Secure.LOCATION_MODE);
             return  true;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }


    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }
    private PendingIntent getGeofencePendingIntent(Context c) {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(c, GeofenceService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(c, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }
    @Override
    public void onConnectionSuspended(int i) {

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

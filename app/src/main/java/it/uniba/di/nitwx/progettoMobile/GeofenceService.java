package it.uniba.di.nitwx.progettoMobile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.common.util.WorkSourceUtil.TAG;
import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;


public class GeofenceService extends IntentService {

    private GeofencingClient mGeofencingClient;

    private List<Geofence> mGeofenceList = new ArrayList<Geofence>();

    public GeofenceService() {

        super("Geofecenservice");
        Log.d("Pollo", "please");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private class GetGeofenceTask extends AsyncTask<Context, Void, Void> {
        PendingIntent mGeofencePendingIntent;
        Context ctx;
        private PendingIntent getGeofencePendingIntent() {
            // Reuse the PendingIntent if we already have it.

            if (mGeofencePendingIntent != null) {
                return mGeofencePendingIntent;
            }
            Intent intent = new Intent(ctx, GeofenceService.class);
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
            // calling addGeofences() and removeGeofences().
            mGeofencePendingIntent = PendingIntent.getService(ctx, 0, intent, PendingIntent.
                    FLAG_UPDATE_CURRENT);
            return mGeofencePendingIntent;
        }

        private GeofencingRequest getGeofencingRequest() {
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
            builder.addGeofences(mGeofenceList);
            return builder.build();
        }


        @SuppressLint("MissingPermission")
        @Override
        protected Void doInBackground(Context... contexts) {
            ctx = contexts[0];
            mGeofencingClient = LocationServices.getGeofencingClient(ctx);
            mGeofenceList.add(new Geofence.Builder()
                    .setRequestId(Constants.PACKAGE_NAME + "Prima Geofence")
                    .setCircularRegion(
                            41.3149307,
                            16.2623632,
                            4000000
                    )
                    .setExpirationDuration(NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());

            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent());
            return null;
        }
    };

    @Override
    protected void onHandleIntent(Intent intent) {
        GetGeofenceTask task = new GetGeofenceTask();

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e("Geofence", errorMessage);
            return;
        }
        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL ) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );
            Log.d("Pollo","Prima della notica");
            createNotificationChannel();
            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails);
            Log.i("Geofence", geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e("Geofence", "geofenceTransition");
        }
        task.execute(this);
    }

    String getGeofenceTransitionDetails(Context c, int geofenceTransition, List<Geofence> triggeringGeofences) {
        StringBuilder sb = new StringBuilder();
        for (Geofence g : triggeringGeofences) {
            Log.d("Geofence",g.getRequestId().toString());
            sb.append(g.getRequestId().toString());

        }
        return sb.toString();
    }

    public void sendNotification(String detail) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, Constants.PACKAGE_NAME)
                .setSmallIcon(R.drawable.questionmark)
                .setContentTitle("Geofence Test")
                .setContentText(detail)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(22, mBuilder.build());
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = "Test developing apllication";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Constants.PACKAGE_NAME, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Geofence", "ondestroy!");
        //Intent broadcastIntent = new Intent("it.uniba.di.nitwx.progettoMobile.RestartService");
        //sendBroadcast(broadcastIntent);
    }
}


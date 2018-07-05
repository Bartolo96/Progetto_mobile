package it.uniba.di.nitwx.progettoMobile;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import static com.google.android.gms.common.util.WorkSourceUtil.TAG;

/**
 * Created by Bartolo on 03/07/2018.
 */

public class GeofenceService extends IntentService {

    public GeofenceService() {

        super("Geofecenservice");
        Log.d("Pollo","please");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("Pollo","please2");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }
        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        Log.d("Pollo","please3");
        Log.d("Pollo",""+geofenceTransition);

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
            Log.i(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e(TAG, "geofenceTransition");
        }
    }

    String getGeofenceTransitionDetails(Context c, int geofenceTransition, List<Geofence> triggeringGeofences) {
        StringBuilder sb = new StringBuilder();
        for (Geofence g : triggeringGeofences) {
            Log.d("prova",g.getRequestId().toString());
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
}


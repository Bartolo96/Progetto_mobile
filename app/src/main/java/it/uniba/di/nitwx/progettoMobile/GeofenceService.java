package it.uniba.di.nitwx.progettoMobile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;


public class GeofenceService extends IntentService {

    GeofencingClient mGeofencingClient;

    private List<Geofence> mGeofenceList = new ArrayList<>();
    PendingIntent mGeofencePendingIntent;
    Context mContext;
    AppDatabase db;

    public GeofenceService() {

        super("Geofecenservice");
        Log.d("Pollo", "please");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mContext = getApplicationContext();
        return START_STICKY;
    }


    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.

        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext.getApplicationContext(), GeofenceService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    Response.ErrorListener errorHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            error.printStackTrace();
        }
    };

    private class InsertTask extends AsyncTask<Store, Void, Void> {

        @Override
        protected Void doInBackground(Store... stores) {
            db.storeDao().instertStore(stores[0]);
            return null;
        }
    }

    private class UpdateTask extends AsyncTask<Store, Void, Void> {

        @Override
        protected Void doInBackground(Store... stores) {
            db.storeDao().updateTimestamps(Calendar.getInstance().getTimeInMillis()/1000, stores[0].id);
            return null;
        }
    }

    Response.Listener<String> responseHandler = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            try {
                JSONArray listaStore = new JSONArray(response);
                for (int i = 0; i < listaStore.length(); i++) {
                    Log.d("Geofence", "SOno dentro OnRepsonse");
                    new InsertTask().execute(new Store(listaStore.getJSONObject(i)));

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    protected void updateGeofences() {
        db = AppDatabase.getDatabase(mContext);
        long curTime = (Calendar.getInstance().getTimeInMillis()) / 1000;
        List<Store> storeList = db.storeDao().loadAllStores(curTime - (10 * 60 ));
        if (!storeList.isEmpty()) {
            int i = 0;
            for (Store store : storeList) {
                new UpdateTask().execute(store);
                Log.d("Geofence", "" + store.id + "   " + i);
                mGeofenceList.add(new Geofence.Builder()
                        .setRequestId(Constants.PACKAGE_NAME + store.address)
                        .setCircularRegion(
                                store.latitude,
                                store.longitude,
                                store.radius
                        )
                        .setExpirationDuration(NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build());

            }
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent());
            } else
                Log.d("Geofence", "Permessi non trovati");

        } else if (db.storeDao().loadAllStores().isEmpty()) {
            HttpController.getStores(responseHandler, errorHandler, mContext);
        }
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if(mContext == null) mContext=getApplicationContext();
        mGeofencingClient = LocationServices.getGeofencingClient(mContext);
        GeofencingEvent geofencingEvent;
        updateGeofences();
        if (intent != null) {
            geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError()) {
                String errorMessage ;
                switch (geofencingEvent.getErrorCode()) {
                    case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                        errorMessage = "geofence service unavaible";
                    case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                        errorMessage = "too many geofences";
                    case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                        errorMessage = "too many pending intents";
                    default:
                        errorMessage = "unknown errors";
                }
                Log.d("Geofence", errorMessage);
                return;
            }
            Log.d("Geofence","I'm inside");
            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

                // Get the geofences that were triggered. A single event can trigger
                // multiple geofences.
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

                // Get the transition details as a String.
                String geofenceTransitionDetails = getGeofenceTransitionDetails(
                        this,
                        geofenceTransition,
                        triggeringGeofences
                );

                createNotificationChannel();
                // Send notification and log the transition details.
                sendNotification(geofenceTransitionDetails);

            } else {
                // Log the error.
                Log.e("Geofence", "Errore??"+geofenceTransition);
            }
        }
    }

    String getGeofenceTransitionDetails(Context c, int geofenceTransition, List<Geofence> triggeringGeofences) {
        List<String> toRemoveList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (Geofence g : triggeringGeofences) {
            Log.d("Geofence", g.getRequestId());
            sb.append(g.getRequestId());
            toRemoveList.add(g.getRequestId());
        }

        mGeofencingClient.removeGeofences(toRemoveList).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Geofence", "REMOVED");
            }
        });
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


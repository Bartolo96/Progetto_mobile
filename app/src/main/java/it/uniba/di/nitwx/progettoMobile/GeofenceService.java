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
import android.location.Location;
import android.net.Uri;
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
    private class getGeofenceDetails extends AsyncTask< List<Geofence>,Void,List<Store>>{
        @Override
        protected  List<Store> doInBackground(List<Geofence>... triggeredGeofences){
            List<Store> returnList = new ArrayList<>();
            List<String> toRemoveList = new ArrayList<>();
            for(Geofence g: triggeredGeofences[0]){
                Store temp = db.storeDao().loadStore(g.getRequestId());
                returnList.add(temp);
                toRemoveList.add(temp.id);
            }
            mGeofencingClient.removeGeofences(toRemoveList).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Geofence", "REMOVED");
                }
            });
            return returnList;
        }

        @Override
        protected void onPostExecute( List<Store> list) {
            super.onPostExecute(list);
            for(Store store: list){
                sendNotification(store);
            }
        }
    }

    Response.Listener<String> responseHandler = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            try {
                JSONArray listaStore = new JSONArray(response);
                for (int i = 0; i < listaStore.length(); i++) {
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
            for (Store store : storeList) {
                new UpdateTask().execute(store);
                mGeofenceList.add(new Geofence.Builder()
                        .setRequestId( store.id)
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
            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

                // Get the geofences that were triggered. A single event can trigger
                // multiple geofences.
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

                db = AppDatabase.getDatabase(this);

                // Get the transition details as a String.


                createNotificationChannel();
                // Send notification and log the transition details.
                new getGeofenceDetails().execute(triggeringGeofences);

            } else {
                // Log the error.
                Log.e("Geofence", "Errore??"+geofenceTransition);
            }
        }
    }

    public void sendNotification(Store store) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q="+store.latitude+","+store.longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this,0,mapIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, Constants.PACKAGE_NAME)
                .setContentIntent(notifyPendingIntent)
                .setSmallIcon(R.drawable.ice_marker)
                .setContentTitle("Myce Cream")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.notificationGeofence,store.address)))
                .setContentText(getString(R.string.notificationGeofence,store.address))
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


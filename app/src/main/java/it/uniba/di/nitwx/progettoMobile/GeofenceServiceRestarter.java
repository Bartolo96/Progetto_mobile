package it.uniba.di.nitwx.progettoMobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;
import com.fasterxml.jackson.databind.util.JSONPObject;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Bartolo on 05/07/2018.
 */


public class GeofenceServiceRestarter extends BroadcastReceiver {


    Response.Listener<String>  geofenceResponseHandler = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            try {
                JSONArray jsonResponse = new JSONArray(response);

            }catch(JSONException e){

            }
        }
    };
    public void onReceive(Context context, Intent intent) {
        Log.i(GeofenceServiceRestarter.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
        //HttpController.getGeofences(HttpController.authorizationHeader,responseHandler,errorHandler);
        context.startService(new Intent(context, GeofenceService.class));;
    }
}

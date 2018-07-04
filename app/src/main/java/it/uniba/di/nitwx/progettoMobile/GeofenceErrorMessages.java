package it.uniba.di.nitwx.progettoMobile;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.GeofenceStatusCodes;

/**
 * Created by Bartolo on 04/07/2018.
 */

public class GeofenceErrorMessages {
    /**
     * Prevents instantiation.
     */
    private GeofenceErrorMessages() {}

    /**
     * Returns the error string for a geofencing exception.
     */
    public  static String getErrorString(Context context, Exception e) {
        if (e instanceof ApiException) {
            return getErrorString(context, ((ApiException) e).getStatusCode());
        } else {
            return "unknown errors";
        }
    }

    /**
     * Returns the error string for a geofencing error code.
     */
    public  static String getErrorString(Context context, int errorCode) {
        Resources mResources = context.getResources();
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "geofence service unavaible";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "too many geofences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "too many pending intents";
            default:
                return "unknown errors";
        }
    }
}


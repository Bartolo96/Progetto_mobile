package it.uniba.di.nitwx.progettoMobile;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;


/**
 * Created by Bartolo on 05/07/2018.
 */

@Entity
public class Store {

    @PrimaryKey
    @android.support.annotation.NonNull
    public String id ;
    public String address;
    public double longitude;
    public double latitude;
    public int radius;
    public long timestamp;

    public Store(String id, String address, double longitude, double latitude, int radius, long timestamp) {
        this.id = id;
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.radius = radius;
        this.timestamp = 0;
    }

    public Store(JSONObject json) throws JSONException{
        this.id = json.getString("id");
        this.address = json.getString("address");
        this.longitude = json.getDouble("longitude");
        this.latitude = json.getDouble("latitude");
        this.radius = json.getInt("radius");
        this.timestamp = 0;
    }
}

package it.uniba.di.nitwx.progettoMobile.dummy;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nicol on 04/07/2018.
 */

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class OfferContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<OfferContent.Offer> ITEMS = new ArrayList<OfferContent.Offer>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Offer> ITEM_MAP = new HashMap<String, Offer>();

    public static List<Offer> populate(JSONArray response) throws JSONException {
        List<Offer> prova = new ArrayList<Offer>();
        for(int i=0; i<response.length();i++){
            ;
            addItem(createOfferItem(response.getJSONObject(i)));
        }
        return ITEMS;
    }
    public static void populate(List<Offer> list) throws JSONException {
        for(Offer p: list){
            addItem(p);
        }
    }
    private static void addItem(Offer item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static Offer createOfferItem(JSONObject offer) throws JSONException{
        return new Offer(offer.getString("productId"),offer.getDouble("productPrice"),offer.getString("id"),
                offer.getInt("quantity"), offer.getString("name"), offer.getString("description"),
                offer.getDouble("offerPrice"), offer.getString("code"),offer.getInt("point"));
    }


    /**
     * A dummy item representing a piece of content.
     */
    @Entity
    public static class Offer {

        @PrimaryKey
        @NonNull
        public  String productId;
        public double productPrice;
        public String id;
        public int quantity;
        public  String name;
        public  String description;
        public  double offerPrice;
        public  String code;
        public int point;

        public Offer(@NonNull String productId,double productPrice,String id,int quantity, String name, String description, double offerPrice, String code,int point) {
            this.productId=productId;
            this.productPrice=productPrice;
            this.id = id;
            this.quantity=quantity;
            this.name = name;
            this.description = description;
            this.offerPrice=offerPrice;
            this.code=code;
            this.point=point;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}



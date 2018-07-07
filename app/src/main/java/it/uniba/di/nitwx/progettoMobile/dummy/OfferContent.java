package it.uniba.di.nitwx.progettoMobile.dummy;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.uniba.di.nitwx.progettoMobile.OfferDao;


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
    public static final List<OfferContent.Offer> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Offer> ITEM_MAP = new HashMap<String, Offer>();

    public static List<Offer> populate(JSONArray response) throws JSONException {

        for(int i=0; i<response.length();i++){

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
        return new Offer(offer);
    }


    /**
     * A dummy item representing a piece of content.
     */
    @Entity
    public static class Offer {


        @NonNull
        @PrimaryKey
        public String id;
        public String name;
        public double offerPrice;
        public String code;
        public int points_cost;

        public List<OfferDao.ProductInOffer> product_list;

        public Offer(@NonNull List<OfferDao.ProductInOffer>product_list, String id, String name, double offerPrice, String code, int points_cost) {
            this.product_list = product_list;
            this.id = id;
            this.name = name;
            this.offerPrice = offerPrice;
            this.code = code;
            this.points_cost = points_cost;
        }
        public Offer(@NonNull JSONObject offer) {
            try {
                JSONArray productList = offer.getJSONArray("product_list");
                product_list = new ArrayList<>();
                for (int i = 0; i < productList.length(); i++) {
                    Log.d("Prova5",(productList.getJSONObject(i).toString()));
                    product_list.add(new OfferDao.ProductInOffer((productList.getJSONObject(i))));
                }


                this.id = offer.getString("id");
                this.name = offer.getString("name");
                this.offerPrice = offer.getDouble("price");
                this.points_cost = offer.getInt("points_cost");
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }

        @Override
        public String toString() {
            return name;
        }
    }
}



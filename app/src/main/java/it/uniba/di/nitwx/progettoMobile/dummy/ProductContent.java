package it.uniba.di.nitwx.progettoMobile.dummy;

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
public class ProductContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Product> ITEMS = new ArrayList<Product>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Product> ITEM_MAP = new HashMap<String, Product>();

    public static List<Product> populate(JSONArray response) throws JSONException {
        List<Product> prova = new ArrayList<Product>();
        for(int i=0; i<response.length();i++){
            ;
            addItem(createProductItem(response.getJSONObject(i)));
        }
        return ITEMS;
    }
    public static void populate(List<Product> list) throws JSONException {
        for(Product p: list){
            addItem(p);
        }
    }
    private static void addItem(Product item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static Product createProductItem(JSONObject product) throws JSONException{
        return new Product(product.getString("id"), product.getString("name"), product.getString("description"), product.getDouble("price"), product.getString("code"));
    }


    /**
     * A dummy item representing a piece of content.
     */
    @Entity
    public static class Product {

        @PrimaryKey
        @NonNull public String id;
        public  String name;
        public  String description;
        public  double price;
        public  String code;

        public Product(){}
        public Product(@NonNull String id, String name, String description, double price, String code) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price=price;
            this.code=code;
        }
        public  Product(JSONObject productInOffer){
            try {
                this.id = productInOffer.getString("id");
                this.name = productInOffer.getString("name");
                this.description = productInOffer.getString("description");
                this.price = productInOffer.getDouble("price");
                this.code = productInOffer.getString("code");
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

package it.uniba.di.nitwx.progettoMobile.dummy;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

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

    public static List<it.uniba.di.nitwx.progettoMobile.Product> populate(JSONArray response) throws JSONException {
        List<it.uniba.di.nitwx.progettoMobile.Product> prova = new ArrayList<it.uniba.di.nitwx.progettoMobile.Product>();
        for(int i=0; i<response.length();i++){
            Product temp = createProductItem(response.getJSONObject(i));
            prova.add(new it.uniba.di.nitwx.progettoMobile.Product(response.getJSONObject(i)));
            addItem(temp);
        }
        return prova;
    }
    private static void addItem(Product item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.getuId(), item);
    }

    private static Product createProductItem(JSONObject product) throws JSONException{
        return new Product(product.getString("id"), product.getString("name"), product.getString("description"), product.getDouble("price"), product.getString("code"));
    }


    /**
     * A dummy item representing a piece of content.
     */
    @Entity
    public static class Product {
        public int getuId() {
            return uId;
        }

        public void setuId(int uId) {
            this.uId = uId;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public Double getPrice() {
            return price;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Product(){};

        public Product(JSONObject json) throws JSONException{
            this.uId = json.getInt("id");
            this.name = json.getString("name");
            this.price = json.getDouble("price");
            this.code = json.getString("code");
        }

        @PrimaryKey
        int uId;

        private String name;
        private Double price;
        private String code;
        @Override
        public String toString() {
            return name;
        }
    }
}

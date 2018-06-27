package it.uniba.di.nitwx.progettoMobile.dummy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductContent {

    public static final List<ProductItem> ITEMS = new ArrayList<ProductItem>();
    public static final Map<String, ProductItem> ITEM_MAP = new HashMap<String, ProductItem>();

    public static void populate(JSONArray response) throws JSONException{
        for(int i=0; i<response.length();i++){
            createProductItem(response.getJSONObject(i));
        }
    }
    private static void addItem(ProductItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static ProductItem createProductItem(JSONObject product) throws JSONException {
        return new ProductItem(product.getString("id"),product.getString("name") , product.getString("description"), Float.parseFloat(product.getString("price")),product.getString("code"));
    }


    public static class ProductItem {
        public final String id;
        public final String name;
        public final String description;
        public final float price;
        public final String code;

        public ProductItem(String id, String name, String description, float price ,String code) {
            this.id = id;
            this.name=name;
            this.description=description;
            this.price=price;
            this.code=code;
        }

        @Override
        public String toString() {
            return "Name: " + name + "\nDescription: "+ description;
        }
    }
}

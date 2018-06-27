package it.uniba.di.nitwx.progettoMobile.dummy;

import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.uniba.di.nitwx.progettoMobile.R;

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
    public static final List<ProductItem> ITEMS = new ArrayList<ProductItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, ProductItem> ITEM_MAP = new HashMap<String, ProductItem>();

    private static final int COUNT = 25;

    public static void populate(JSONArray response) throws JSONException {
        for(int i=0; i<response.length();i++){
            ProductItem temp = createProductItem(response.getJSONObject(i));
            addItem(temp);
        }
    }
    private static void addItem(ProductItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static ProductItem createProductItem(JSONObject product) throws JSONException{
        return new ProductItem(product.getString("id"), product.getString("name"), product.getString("description"));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class ProductItem {
        public final String id;
        public final String content;
        public final String details;
        public final int imageId;

        public ProductItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
            this.imageId = R.drawable.cucciolone_algida;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}

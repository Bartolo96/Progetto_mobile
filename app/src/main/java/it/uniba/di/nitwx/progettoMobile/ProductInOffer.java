package it.uniba.di.nitwx.progettoMobile;

import org.json.JSONException;
import org.json.JSONObject;

import it.uniba.di.nitwx.progettoMobile.dummy.ProductContent;

/**
 * Created by nicol on 04/07/2018.
 */

public class ProductInOffer extends ProductContent.Product{
    int quantity;

    public ProductInOffer(JSONObject productInOffer){
        super(productInOffer);
        try {
            this.quantity = productInOffer.getInt("quantity");
        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }
}
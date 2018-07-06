package it.uniba.di.nitwx.progettoMobile;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import it.uniba.di.nitwx.progettoMobile.dummy.OfferContent;
import it.uniba.di.nitwx.progettoMobile.dummy.ProductContent;

@Dao
public interface OfferDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertProductsList (List<OfferContent.Offer> products);

    @Query("UPDATE offer SET product_list = :product_list WHERE id = :id")
    public void updateProductslist (List<OfferDao.ProductInOffer> product_list, int id);



    @Query("SELECT* FROM offer")
    List<OfferContent.Offer> loadAllOffers();



    public class ProductInOffer extends ProductContent.Product{
        int quantity;
        public ProductInOffer(){
            super();
        }
        public ProductInOffer(JSONObject json){
            super(json);
            try {
                this.quantity = json.getInt("quantity");
            }catch(JSONException e){
                e.printStackTrace();
            }
        }

    }

}

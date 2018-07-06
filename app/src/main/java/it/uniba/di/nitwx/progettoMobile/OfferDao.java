package it.uniba.di.nitwx.progettoMobile;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import it.uniba.di.nitwx.progettoMobile.dummy.OfferContent;

@Dao
public interface OfferDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertProductsList (List<OfferContent.Offer> products);

    @Update
    public void updateProductslist (OfferContent.Offer... product);

    @Query("SELECT* FROM offer ")
    List<OfferContent.Offer> loadAllOffers();

}

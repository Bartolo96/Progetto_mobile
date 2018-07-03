package it.uniba.di.nitwx.progettoMobile;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;


@Dao
public interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertProductsList (List<Product> products);

    @Update
    public void updateProductslist (Product... product);

    @Query("SELECT* FROM product")
    public Product[] loadAllProducts();


}

package it.uniba.di.nitwx.progettoMobile;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;


@Dao
public interface StoreDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void instertStore(Store... stores);

    @Query("SELECT * FROM store")
    List<Store> loadAllStores();

    @Query("SELECT * FROM store WHERE timestamp < :time ")
    List<Store> loadAllStores(long time);

    @Query("SELECT* FROM store WHERE id = :requestID")
    Store loadStore(String requestID);

    @Query("UPDATE store SET timestamp = :time WHERE id = :id")
    void updateTimestamps(long time,String id);
}

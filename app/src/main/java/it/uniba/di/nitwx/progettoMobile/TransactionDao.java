package it.uniba.di.nitwx.progettoMobile;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by nicol on 10/07/2018.
 */
@Dao
public interface TransactionDao {
    @Query("SELECT * FROM userTransaction WHERE offerId=:offerId AND userId=:userId")
    List<UserTransaction> loadAllTransaction(String offerId,String userId);
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void isertIntoTransaction(UserTransaction transaction);
}

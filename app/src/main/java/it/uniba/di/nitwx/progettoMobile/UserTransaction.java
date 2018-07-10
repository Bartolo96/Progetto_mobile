package it.uniba.di.nitwx.progettoMobile;

import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Entity;
import io.reactivex.annotations.NonNull;

/**
 * Created by nicol on 10/07/2018.
 */
@Entity
public class UserTransaction {
    @PrimaryKey
    @android.support.annotation.NonNull
    public String id="default";
    @android.support.annotation.NonNull
    public String token;
    @android.support.annotation.NonNull
    public String userId;
    public String offerId;
    public UserTransaction(String id, String token, String userId, String offerId) {
        this.id = id;
        this.token = token;
        this.userId = userId;
        this.offerId=offerId;
    }
}

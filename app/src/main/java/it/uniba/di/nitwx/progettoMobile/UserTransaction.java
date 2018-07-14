package it.uniba.di.nitwx.progettoMobile;

import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

/**
 * Created by nicol on 10/07/2018.
 */
@Entity
public class UserTransaction {
    @PrimaryKey
    @NonNull
    public String id="default";
    @NonNull
    public String token;
    @NonNull
    public String userId;
    @NonNull
    public String offerId;
    UserTransaction(@NonNull String id, @NonNull String token, @NonNull String userId, @NonNull String offerId) {
        this.id = id;
        this.token = token;
        this.userId = userId;
        this.offerId=offerId;
    }
}

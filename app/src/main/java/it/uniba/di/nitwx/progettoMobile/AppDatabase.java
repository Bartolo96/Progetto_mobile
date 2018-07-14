package it.uniba.di.nitwx.progettoMobile;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;


import it.uniba.di.nitwx.progettoMobile.dummy.OfferContent.Offer;
import it.uniba.di.nitwx.progettoMobile.dummy.ProductContent.Product;
import it.uniba.di.nitwx.progettoMobile.UserTransaction;

@Database(version = 10, entities = {Product.class,Store.class,Offer.class,UserTransaction.class})
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

        abstract public ProductDao productDao();
        abstract public OfferDao offerDao();
        abstract public StoreDao storeDao();
        abstract public TransactionDao transactionDao();

        private static AppDatabase INSTANCE;

        static AppDatabase getDatabase(final Context context){
                if(INSTANCE == null){
                        synchronized (AppDatabase.class){
                                if(INSTANCE == null){
                                        INSTANCE = Room.databaseBuilder(context,
                                                AppDatabase.class,"database-nitwx").fallbackToDestructiveMigration()
                                                .build();
                                }
                        }
                }
                return INSTANCE;
        }
}

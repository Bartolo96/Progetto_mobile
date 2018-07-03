package it.uniba.di.nitwx.progettoMobile;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;


@Database(version = 2, entities = {Product.class})
public abstract class AppDatabase extends RoomDatabase {
        abstract public ProductDao productDao();

}

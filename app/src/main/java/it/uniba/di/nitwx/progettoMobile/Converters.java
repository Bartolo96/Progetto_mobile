package it.uniba.di.nitwx.progettoMobile;

import android.arch.persistence.room.TypeConverter;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.nitwx.progettoMobile.dummy.ProductContent;

/**
 * Created by Bartolo on 06/07/2018.
 */

public class Converters {
    @TypeConverter
    public static List<OfferDao.ProductInOffer> fromString(String rawString) {
        List<OfferDao.ProductInOffer> temp = new ArrayList<>();
        try {
            JSONArray json = new JSONArray(rawString);
            for (int i = 0; i < json.length(); i++) {
                Log.d("Prova3",json.getJSONObject(i).toString());
                temp.add(new OfferDao.ProductInOffer(json.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return temp;
    }

    @TypeConverter
    public static String fromList(List<OfferDao.ProductInOffer> list) {
        String returnString;
        for(OfferDao.ProductInOffer a: list)
            Log.d("Prova1",a.toString());
        JSONArray json = new JSONArray(list);
        returnString = json.toString();
        Log.d("Prova2",json.toString());
        return returnString;
    }

}

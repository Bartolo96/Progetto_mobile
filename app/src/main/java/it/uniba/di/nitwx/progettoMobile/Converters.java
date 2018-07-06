package it.uniba.di.nitwx.progettoMobile;

import android.arch.persistence.room.TypeConverter;

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

        JSONArray json = new JSONArray(list);
        returnString = json.toString();

        return returnString;
    }

}

package it.uniba.di.nitwx.progettoMobile;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;

import org.json.JSONException;
import org.json.JSONObject;


@Entity
public class Product {
    public int getuId() {
        return uId;
    }

    public void setuId(int uId) {
        this.uId = uId;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Product(){};

    public Product(JSONObject json) throws JSONException{
        this.uId = json.getInt("id");
        this.name = json.getString("name");
        this.price = json.getDouble("price");
        this.code = json.getString("code");
    }



    @PrimaryKey
    protected int uId;

    private String name;
    private Double price;
    private String code;


}

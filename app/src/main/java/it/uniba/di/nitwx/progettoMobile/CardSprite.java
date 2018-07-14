package it.uniba.di.nitwx.progettoMobile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

public class CardSprite {
    private Bitmap backImage;
    private Bitmap frontImage;
    private Bitmap actualImage;

    public int id;
    public int card_start_X;
    public int card_start_Y;

    public CardSprite(Bitmap backBmp,Bitmap frontBmp,int id, int width, int height){
        this.id = id;
        backImage =  Bitmap.createScaledBitmap(backBmp,width,height,false);
        frontImage = Bitmap.createScaledBitmap(frontBmp,width,height,false);
        actualImage = backImage;

    }

    public void draw(Canvas canvas, int left, int top){
        canvas.drawBitmap(actualImage,card_start_X = left, card_start_Y = top,null);
    }
    public void update(){
        actualImage = actualImage == frontImage? backImage : frontImage;
    }

    public boolean isAlreadyTurned(){
        return actualImage == frontImage;
    }
}

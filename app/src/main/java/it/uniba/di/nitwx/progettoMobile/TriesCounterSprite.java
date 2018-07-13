package it.uniba.di.nitwx.progettoMobile;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class TriesCounterSprite {
    private int currentTries;
    private String displayTries;
    private Paint paint ;

    public TriesCounterSprite() {
        this.currentTries = 0;
        this.displayTries = "Tries: " + String.valueOf(currentTries);
        this.paint = new Paint();
        this.paint.setColor(Color.RED);
        this.paint.setTextSize(100);
    }

    public void update(){
        this.currentTries++;
        this.displayTries = "Tries: " + String.valueOf(currentTries);
    }
    public void draw(Canvas canvas, int left, int top){
        canvas.drawText(displayTries,left, top,paint);
    }

}

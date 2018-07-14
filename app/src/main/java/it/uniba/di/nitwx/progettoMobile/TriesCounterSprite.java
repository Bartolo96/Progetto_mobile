package it.uniba.di.nitwx.progettoMobile;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class TriesCounterSprite {
    private int currentTries;
    public int gameProgress;
    private String displayTries;
    private Paint paint ;

    public TriesCounterSprite(int height) {
        this.currentTries = 0;
        this.displayTries = "Tries: " + String.valueOf(currentTries);
        this.paint = new Paint();
        this.paint.setColor(Color.RED);
        this.paint.setTextSize(height);
    }

    public void update(boolean isProgressAdvancing){
        this.currentTries++;
        this.displayTries = "Tries: " + String.valueOf(currentTries);
        if(isProgressAdvancing){
            gameProgress++;
        }
    }
    public void draw(Canvas canvas, int left, int top){
        canvas.drawText(displayTries,left, top,paint);
    }

}

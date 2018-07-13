package it.uniba.di.nitwx.progettoMobile;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Timer;
import java.util.TimerTask;

public class TimerSprite {
    private Timer mTimer;
    private int currentTime;
    public String displayedTime;
    private Paint paint ;
    public float textWidth;

    public TimerSprite(){
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(renderTime,0,1 * 1000L);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(100);
        displayedTime = String.valueOf(currentTime / 60 )+ ":" + String.valueOf(currentTime%60);
        textWidth = paint.measureText(displayedTime);

    }

    private TimerTask renderTime = new TimerTask() {
        @Override
        public void run() {
            currentTime ++;
            displayedTime = String.valueOf(currentTime / 60 )+ ":" + String.valueOf(currentTime%60);
            textWidth = paint.measureText(displayedTime);
        }
    };
    public void draw(Canvas canvas, int left, int top){
        canvas.drawText(displayedTime,left, top,paint);
    }
    public void update(){
    }
}

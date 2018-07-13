package it.uniba.di.nitwx.progettoMobile;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Timer;
import java.util.TimerTask;

public class TimerSprite {
    private Timer mTimer;
    private int currentTime = 0;
    private String displayedTime;
    private Paint paint ;

    public TimerSprite(){
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(renderTime,0,1 * 1000L);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(100);
    }

    TimerTask renderTime = new TimerTask() {
        @Override
        public void run() {
            currentTime ++;
            displayedTime = String.valueOf(currentTime / 60 )+ ":" + String.valueOf(currentTime%60);
        }
    };
    public void draw(Canvas canvas, int left, int top){
        canvas.drawText(displayedTime,left, top,paint);
    }
    public void update(){
    }
}

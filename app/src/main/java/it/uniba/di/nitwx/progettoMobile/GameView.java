package it.uniba.di.nitwx.progettoMobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GameView extends SurfaceView implements  SurfaceHolder.Callback {
    private MainThread thread;
    private CardSprite cardSprite;
    private Point displaySize;
    private int cardWidth;
    private int cardHeight;
    private TimerSprite timerSprite;
    private TriesCounterSprite triesCounterSprite;
    private final int NUM_OF_CARDS = 16;
    private CardSprite lastCard = null;
    private CardSprite currentCard = null;
    private boolean isCardTouchEnabled = true;
    private Bitmap sfondo;
    private Paint deafultPaint;
    private List<CardSprite> playingCards = new ArrayList<>();

    public GameView(Context context){
        super(context);
        getHolder().addCallback(this);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        displaySize = new Point();
        wm.getDefaultDisplay().getRealSize(displaySize);
        cardWidth = (displaySize.x - 800) / 8;
        cardHeight = cardWidth *2;
        thread = new MainThread(getHolder(),this);

        setFocusable(true);

    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        for(int i = 0; i < NUM_OF_CARDS; i++){
            int imgId = getResources().getIdentifier("carta_gelato"+(i%8),"drawable",Constants.PACKAGE_NAME);
            playingCards.add(new CardSprite(BitmapFactory.decodeResource(getResources(),R.drawable.card_sprite),
                                    BitmapFactory.decodeResource(getResources(),imgId),i%8,cardWidth,cardHeight));
        }

        Collections.shuffle(playingCards);
        sfondo = BitmapFactory.decodeResource(getResources(),R.drawable.sfondo_gioco);
        sfondo = Bitmap.createScaledBitmap(sfondo,displaySize.x,displaySize.y,false);
        timerSprite = new TimerSprite();
        triesCounterSprite = new TriesCounterSprite();
        deafultPaint = new Paint();
        this.deafultPaint.setColor(Color.RED);
        this.deafultPaint.setTextSize(100);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (isCardTouchEnabled) {
            for (CardSprite card : playingCards) {
                if ((x > card.card_start_X && x < card.card_start_X + cardWidth) &&
                        (y > card.card_start_Y && y < card.card_start_Y + cardHeight)) {
                    if (!card.isAlreadyTurned() && this.lastCard == null) {
                        this.lastCard = card;
                        card.update();
                    } else if (!card.isAlreadyTurned() && card.id == lastCard.id) {
                        card.update();
                        this.lastCard = card;
                        this.lastCard = null;
                        triesCounterSprite.update(true);

                    } else if (!card.isAlreadyTurned() && card.id != lastCard.id) {
                        card.update();
                        currentCard = card;
                        isCardTouchEnabled = false;
                        triesCounterSprite.update(false);
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                currentCard.update();
                                lastCard.update();
                                currentCard = null;
                                lastCard = null;
                                isCardTouchEnabled = true;
                            }
                        }, 1000);
                    }
                    break;
                }
            }
        }

        return super.onTouchEvent(event);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry){
            try{
                thread.setRunning(false);
                thread.join();
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }
    }
    public void update(){

    }
    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);

        if(canvas != null){
            canvas.drawBitmap(sfondo,0,0,null);
            int positionLeft = 100;
            int positionTop = 100;
            int counter = 0;

            for(CardSprite card : playingCards){
                card.draw(canvas,positionLeft,positionTop);
                positionLeft += 200;
                counter++;

                if(counter == playingCards.size()/2){
                    positionTop += cardHeight + 100;
                    positionLeft = 100;
                }
            }
            if(triesCounterSprite.gameProgress != 8) {
                timerSprite.draw(canvas, 100, positionTop + cardHeight + 200);
                triesCounterSprite.draw(canvas, 100 + (int) timerSprite.textWidth + 100, positionTop + cardHeight + 200);
            }
            else {
                canvas.drawText("Game Completed!!", 100, positionTop + cardHeight + 200, deafultPaint);
                //Intent intent = new Intent().setClass(getContext(),HomeActivity.class);
                //((Activity)getContext()).startActivity(intent);
            }
        }
    }
}

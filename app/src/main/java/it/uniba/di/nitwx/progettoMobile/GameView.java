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
import android.view.Display;
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
    private int textHeight;
    private int cardSpacingX;
    private int cardSpacingY;
    private TimerSprite timerSprite;
    private TriesCounterSprite triesCounterSprite;
    private final int NUM_OF_CARDS = 16;
    private CardSprite lastCard = null;
    private CardSprite currentCard = null;
    private boolean isCardTouchEnabled = true;
    private Bitmap sfondo;
    private Paint deafultPaint;
    private List<CardSprite> playingCards = new ArrayList<>();
    private Context mContext;


    public static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public GameView(Context context){
        super(context);
        this.mContext = context;
        getHolder().addCallback(this);
        /*WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        wm.getDefaultDisplay().getRealSize(displaySize);*/
        this.displaySize = getAppUsableScreenSize(context);
        this.cardSpacingX = (displaySize.x) / 27;
        this.cardWidth = (displaySize.x - (cardSpacingX * 9)) / 8;
        this.cardHeight = cardWidth *2;
        this.textHeight = cardHeight /2;
        this.cardSpacingY = (displaySize.y - (cardHeight *2) - textHeight)/3 ;
        this.thread = new MainThread(getHolder(),this);

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
        timerSprite = new TimerSprite(textHeight);
        triesCounterSprite = new TriesCounterSprite(textHeight);
        deafultPaint = new Paint();
        this.deafultPaint.setColor(Color.RED);
        this.deafultPaint.setTextSize(textHeight);
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
            int positionLeft = cardSpacingX;
            int positionTop = cardSpacingY;
            int counter = 0;

            for(CardSprite card : playingCards){
                card.draw(canvas,positionLeft,positionTop);
                positionLeft += cardWidth + cardSpacingX;
                counter++;

                if(counter == playingCards.size()/2){
                    positionTop += cardHeight + cardSpacingY;
                    positionLeft = cardSpacingX;
                }
            }
            if(triesCounterSprite.gameProgress != 8) {
                timerSprite.draw(canvas, cardSpacingX, positionTop + textHeight/2 + cardHeight + cardSpacingY);
                triesCounterSprite.draw(canvas, cardSpacingX + (int) timerSprite.textWidth + cardSpacingX, positionTop + textHeight/2 + cardHeight + cardSpacingY);
            }
            else {
                canvas.drawText("Game Completed!!", cardSpacingX, positionTop + cardHeight + cardSpacingY, deafultPaint);
                this.thread.setRunning(false);
                Intent returnIntent = new Intent();//.setClass(this.mContext,HomeActivity.class);
                returnIntent.putExtra("completed",true);
                ((Activity)mContext).setResult(Activity.RESULT_OK,returnIntent);
                ((Activity)mContext).finish();
            }
        }
    }
}

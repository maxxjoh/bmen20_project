package com.example.snake_game.widget;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import com.example.snake_game.intface.OnSnakeDeadListener;
import com.example.snake_game.intface.OnSnakeEatFoodListener;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SnakeView extends View {

    private int mBlocksize = 20; // Length of each cell
    private int mWidth, mHeight; // The range of the game area, representing the number of cells
    private int mOffsetX, mOffsetY; // Offset of the active area
    private int mSnakeLen; // Length of the snake
    private int[] mSnakeX = new int[100]; // Coordinates of the snake's body
    private int[] mSnakeY = new int[100];
    private int mSnakeDir; // Direction of the snake's movement
    private int mFoodX, mFoodY; // Coordinates of the food
    private int mFoodCnt; // Number of food items eaten

    Paint ptBackground = new Paint();
    Paint ptHead = new Paint();
    Paint ptBody = new Paint();
    Paint ptFood = new Paint();
    Paint ptBorder = new Paint();

    //Snake's swimming direction
    public static final int DIR_UP = 0;//up
    public static final int DIR_RIGHT = 1;//right
    public static final int DIR_DOWN = 2;//down
    public static final int DIR_LEFT = 3;//left


    //Timer related settings
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private Handler mHandler = null;
    private final int SNAKE_MOVE = 1;

    private int mGameStatus;
    private final int STATUS_RUN = 1;
    private final int STATUS_DEAD = 2;
    private final int STATUS_PAUSE = 3;
    private final int STATUS_START = 0;

    private OnSnakeEatFoodListener mOnSnakeEatListener;
    private OnSnakeDeadListener mOnSnakeDeadListener;

    public void setmOnSnakeEatListener(OnSnakeEatFoodListener mOnSnakeEatListener){
        this.mOnSnakeEatListener = mOnSnakeEatListener;
    }
    public void setmOnSnakeDeadListener(OnSnakeDeadListener mOnSnakeDeadListener){
        this.mOnSnakeDeadListener = mOnSnakeDeadListener;
    }

    //The code is called when creating the control
    public SnakeView(Context context) {
        super(context);
        InitGame();
    }
    //This constructor is called when creating a control in an XML file
    public SnakeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        InitGame();
    }

    public void InitGame(){
        ptBackground.setColor(Color.argb(255,0,0,0));
        ptHead.setColor(Color.argb(255,255,0,0));
        ptBody.setColor(Color.argb(255,255,211,55));
        ptBorder.setColor(Color.argb(255,255,255,255));
        ptFood.setColor(Color.argb(255,0,11,255));
        InitSnake();

        //Snake timer
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case SNAKE_MOVE:
                        SnakeMove();
                        break;
                    default:
                        break;
                }

            }
        };


        if(mTimer == null){
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask(){
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = SNAKE_MOVE;
                    mHandler.sendMessage(message);
                }
            };
        }

        if(mTimer != null && mTimerTask != null){
            mTimer.schedule(mTimerTask,300,300);
        }
    }

    public void SnakeMove(){
        //If the game is not in motion, the snake will not swim
        if(mGameStatus != STATUS_RUN){
            return;
        }
        int newheadx = 0, newheady = 0;
        //Calculate the position of the snake head
        switch (mSnakeDir){
            case 0:
                newheadx = mSnakeX[0];
                newheady = mSnakeY[0] - 1;
                break;
            case 1:
                newheadx = mSnakeX[0] + 1;
                newheady = mSnakeY[0];
                break;
            case 2:
                newheadx = mSnakeX[0];
                newheady = mSnakeY[0] + 1;
                break;
            case 3:
                newheadx = mSnakeX[0] - 1;
                newheady = mSnakeY[0];
                break;
        }
        //Determine whether the snake head exceeds the game area. If it exceeds the game area, change the game state.
        if(newheadx < 0 || newheadx >= mWidth || newheady < 0 || newheady >= mHeight){
            mGameStatus = STATUS_DEAD;
            if(mOnSnakeDeadListener != null){
                mOnSnakeDeadListener.OnSnakeDead(mSnakeLen);
            }
            return;
        }
        //Determine whether the snake has eaten food. If it eats food, it will increase its body and generate the next food immediately.
        if(newheadx == mFoodX && newheady == mFoodY){
            Random random = new Random();
            mFoodX = random.nextInt(mWidth - 1);
            mFoodY = random.nextInt(mHeight - 1);
            mSnakeLen++;
            mFoodCnt++;

            if(mOnSnakeEatListener != null){
                mOnSnakeEatListener.OnSnakeEatFood(mFoodCnt);
            }
        }
        //Move the snake's position
        for(int i = mSnakeLen - 1; i > 0; i--){
            mSnakeX[i] = mSnakeX[i - 1];
            mSnakeY[i] = mSnakeY[i - 1];
        }

        //Set the position of the snake head
        mSnakeX[0] = newheadx;
        mSnakeY[0] = newheady;
        //Trigger onDraw to redraw
        invalidate();
    }

    public void StartGame(){
        switch (mGameStatus){
            case STATUS_DEAD:
                InitSnake();
                mGameStatus = STATUS_RUN;
                if(mOnSnakeEatListener != null){
                    mOnSnakeEatListener.OnSnakeEatFood(mFoodCnt);
                }
                break;
            case STATUS_PAUSE:
                mGameStatus = STATUS_RUN;
                break;
            case STATUS_START:
                mGameStatus = STATUS_RUN;
                break;
            default:
                break;
        }
    }

    public void PauseGame(){
        if(mGameStatus == STATUS_RUN){
            mGameStatus = STATUS_PAUSE;
        }
    }

    public void ControlGame(int dir){
        if(mGameStatus != STATUS_RUN){
            return;
        }
        switch (dir){
            case DIR_UP:
                mSnakeDir = dir;
                break;
            case DIR_RIGHT:
            case DIR_DOWN:
            case DIR_LEFT:
                mSnakeDir = dir;
                break;
            default:
                break;
        }
    }

    //Snake initialization state
    public void InitSnake(){
        mSnakeLen = 4;
        mSnakeX[0] = 3;
        mSnakeY[0] = 0;
        mSnakeX[1] = 2;
        mSnakeY[1] = 0;
        mSnakeX[2] = 1;
        mSnakeY[2] = 0;
        mSnakeX[3] = 0;
        mSnakeY[3] = 0;
        mFoodX = 4;
        mFoodY = 4;
        mFoodCnt = 0;
        mSnakeDir = DIR_RIGHT;
    }
    //Methods when View size changes
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w/mBlocksize - 1;
        mHeight = h/mBlocksize - 1;
        mOffsetX = (w - mWidth * mBlocksize)/2;
        mOffsetY = (h - mHeight * mBlocksize)/2;
    }

    //The Paint class, which we call a brush, defines various parameters for the drawing process, such as color, line style, pattern style, etc.
    //We define the Canvas class as a canvas, which mainly provides several methods for drawing various color patterns: points, lines, paths, etc.
    //Methods for drawing layout content
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Paint the game area background
        canvas.drawRect(mOffsetX,mOffsetY,(mWidth) * mBlocksize + mOffsetX,
                (mHeight) * mBlocksize + mOffsetY,ptBackground);
        //Draw the game area border
        canvas.drawLine(mOffsetX - 1,mOffsetY - 1,mWidth * mBlocksize +mOffsetX,
                mOffsetY - 1,ptBorder);
        canvas.drawLine(mOffsetX - 1,mOffsetY - 1,mOffsetX - 1,
                mHeight * mBlocksize + mOffsetY,ptBorder);
        canvas.drawLine(mWidth * mBlocksize + mOffsetX,mOffsetY - 1,
                mWidth * mBlocksize + mOffsetX,mHeight * mBlocksize + mOffsetY,ptBorder);
        canvas.drawLine(mOffsetX - 1,mHeight * mBlocksize + mOffsetY,
                mWidth * mBlocksize + mOffsetX,mHeight * mBlocksize + mOffsetY,ptBorder);
        //Draw food
        canvas.drawRect(mFoodX * mBlocksize + mOffsetX,mFoodY * mBlocksize + mOffsetY,
                (mFoodX + 1) * mBlocksize + mOffsetX,
                (mFoodY + 1) * mBlocksize + mOffsetY,ptFood);
        //Draw snake
        for(int i = 0; i < mSnakeLen; i++){
            if(i == 0){
                //Draw a snake head
                canvas.drawRect(mSnakeX[i] * mBlocksize + mOffsetX,mSnakeY[i] * mBlocksize + mOffsetY,
                        (mSnakeX[i] + 1) * mBlocksize + mOffsetX,
                        (mSnakeY[i] + 1) * mBlocksize + mOffsetY,ptHead);
            }else{
                //Draw a snake body
                canvas.drawRect(mSnakeX[i] * mBlocksize + mOffsetX,mSnakeY[i] * mBlocksize + mOffsetY,
                        (mSnakeX[i] + 1) * mBlocksize + mOffsetX,
                        (mSnakeY[i] + 1) * mBlocksize + mOffsetY,ptBody);
            }
        }
    }
}



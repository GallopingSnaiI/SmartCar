package com.example.kai.mulitactivityapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;


/**
 * TODO: document your custom view class.
 */
public class ManualControlView extends View {
    Bitmap JoyStickStatic, JoyStickUp,JoyStickLeft,JoyStickRight,JoyStickDown;
    StickDirection direction = StickDirection.Stop;
    int imgRadius = 100;
    String setupString;
    String commandString;

    public ManualControlView(Context context) {
        super(context);
        init(null, 0);
    }

    public ManualControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ManualControlView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        JoyStickStatic = BitmapFactory.decodeResource(getResources(),R.drawable.thumb_pad);
        JoyStickUp = BitmapFactory.decodeResource(getResources(),R.drawable.thumb_pad_up);
        JoyStickLeft = BitmapFactory.decodeResource(getResources(),R.drawable.thumb_pad_left);
        JoyStickRight = BitmapFactory.decodeResource(getResources(),R.drawable.thumb_pad_right);
        JoyStickDown = BitmapFactory.decodeResource(getResources(),R.drawable.thumb_pad_down);
        imgRadius = JoyStickStatic.getWidth();
    }

    public boolean onTouchEvent(MotionEvent e){
        if(e.getActionMasked()==MotionEvent.ACTION_MOVE||e.getActionMasked()==MotionEvent.ACTION_DOWN){
            float x = e.getX();
            float y = e.getY();
            float w = this.getWidth();
            float h = this.getHeight();
           // System.out.println(x+","+y+" vs "+this.getWidth()+"x"+this.getHeight());
            if(x>(w-imgRadius)/2 && x < (w+imgRadius)/2 && y>0 && y<(h-imgRadius)/2){
                setDirection(StickDirection.MoveForward);
            }else if(x>0 && x < (w-imgRadius)/2 && y>(h-imgRadius)/2 && y<(h+imgRadius)/2){
                setDirection(StickDirection.TurnLeft);
            }else if(x>(w+imgRadius)/2 && x < w && y>(h-imgRadius)/2 && y<(h+imgRadius)/2) {
                setDirection(StickDirection.TurnRight);
            }else if(x>(w-imgRadius)/2 && x < (w+imgRadius)/2 && y>(h+imgRadius)/2 && y < h){
                setDirection(StickDirection.MoveBack);
            }else{
                setDirection(StickDirection.Stop);
            }
        }else if (e.getActionMasked()==MotionEvent.ACTION_UP){
            setDirection(StickDirection.Stop);
        }
        return true;
    }

    public void setDirection(StickDirection direction) {
        if(direction != this.direction) {
            this.direction = direction;
            postInvalidate();
            sendBluetoothInstruction(direction);
        }
    }

    private void sendBluetoothInstruction(StickDirection direction) {
        setupString = "1!";

        switch (direction){
            case MoveForward:
                commandString = "$goForward*";
                break;
            case MoveBack:
                commandString = "$goBackward*";
                break;
            case TurnLeft:
                commandString = "$rotateCounterClockwise*";
                break;
            case TurnRight:
                commandString = "$rotateClockwise*";
                break;
            case Stop:
                commandString = "$stop*";
                break;
        }
        if(MainActivity.out != null) {
            try {
                // MainActivity.out.write(setupString.getBytes());
                MainActivity.out.write(commandString.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("Output is null");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Bitmap img;
        switch (direction) {
            case MoveForward:
                img = JoyStickUp;
                break;
            case TurnLeft:
                img = JoyStickLeft;
                break;
            case TurnRight:
                img = JoyStickRight;
                break;
            case MoveBack:
                img = JoyStickDown;
                break;
            default:
                img = JoyStickStatic;
        }
        Paint p = new Paint();
        //p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        img = img.copy(Bitmap.Config.ARGB_8888,false);
        canvas.drawBitmap(img, canvas.getWidth() / 2 - JoyStickStatic.getWidth() / 2, canvas.getHeight() / 2 - JoyStickStatic.getHeight() / 2, p);
    }


    public static enum StickDirection {
        MoveForward,MoveBack,TurnLeft,TurnRight,Stop
    }
}

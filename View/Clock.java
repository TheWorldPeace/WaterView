package com.worldpeace.waterview.View;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;

import com.worldpeace.waterview.R;

import java.util.TimeZone;


public class Clock extends View {
    private Time mCalendar;    //用来记录当前时间

    //存放3张图片资源
    private Drawable mHourHand;//小时的针头
    private Drawable mMinuteHand;//分针的针头
    private Drawable mSecondHand;//秒针的针头

    //表盘的宽高
    private int mDialWidth;
    private int mDialHeight;

    //记录View是否添加到windows中
    private boolean mAttached;
    //时和分
    private float mMinutes;
    private float mHour;

    private Boolean mChange;//判断View是否变化，View的尺寸变化，要做对应的缩放

    public Clock(Context context) {
        //super(context);
        this(context,null);
    }

    public Clock(Context context,  AttributeSet attrs) {
        //super(context, attrs);
        this(context,attrs,0);
    }

    public Clock(Context context,  AttributeSet attrs, int defStyleAttr) {
        //super(context, attrs, defStyleAttr);
        this(context,attrs,defStyleAttr,0);
    }

    public Clock(Context context,  AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        //取资源；
        final Resources res=context.getResources();
        if(mSecondHand==null){
            mSecondHand=context.getDrawable(R.drawable.clock_dial);
        }
        if(mMinuteHand==null){
            mMinuteHand=context.getDrawable(R.drawable.clock_hand_minute);
        }
        if(mHourHand==null){
            mHourHand=context.getDrawable(R.drawable.clock_hand_hour);
        }
        mCalendar = new Time();
        mDialHeight=mSecondHand.getIntrinsicHeight();
        mDialWidth=mSecondHand.getIntrinsicWidth();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
       // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode=MeasureSpec.getMode(widthMeasureSpec);
        int widthSize=MeasureSpec.getSize(widthMeasureSpec);

        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        int heightSize=MeasureSpec.getSize(heightMeasureSpec);

        float hScale=1.0f;
        float wScale=1.0f;

        //如果实际图大于父类图，需要进行缩放

        if(widthMode!=MeasureSpec.UNSPECIFIED&&widthSize<mDialWidth){
            wScale=(float)mDialWidth/(float)widthSize;
        }
        if(heightMode!=MeasureSpec.UNSPECIFIED&&heightSize<mDialHeight){
            hScale=(float)mDialHeight/(float)heightSize;
        }
        //两者取最小
        float scale=Math.min(wScale,hScale);
        //设置宽高
        setMeasuredDimension(resolveSizeAndState((int)(mDialWidth*scale),widthMeasureSpec,0),resolveSizeAndState((int)(mDialHeight*scale),heightMeasureSpec,0));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mChange=true;
    }
    //为了让View监听事件的变化
    protected void onTimeChanged(){
        mCalendar.setToNow();
        int hour=mCalendar.hour;
        int minute=mCalendar.minute;
        int second=mCalendar.second;

        mMinutes = minute + second / 60.0f;
        mHour = hour + mMinutes / 60.0f;
        mChange = true;
    }

    //做一个广播，每次接受到广博用于改变；
    private  final BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //这个if判断主要是用来在时区发生变化时，更新mCalendar的时区的，这
            //样，我们的自定义View在全球都可以使用了。
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
            }
            //如果接收到了XX广播，那就需要更新时间
            onTimeChanged();
            //引发重绘
            invalidate();
        }
    };
    //动态注册广播接收器
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(!mAttached){
            IntentFilter intentFilter=new IntentFilter();
            intentFilter.addAction(Intent.ACTION_TIME_TICK);
            intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
            intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            getContext().registerReceiver(broadcastReceiver,intentFilter);
        }
        mCalendar=new Time();
        onTimeChanged();

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mAttached){
            getContext().unregisterReceiver(broadcastReceiver);
            mAttached=false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        boolean changed = mChange;
        if (changed) {
            mChange = false;
        }
        int availableWidth=super.getRight()-super.getLeft();
        int availableHeight=super.getBottom()-super.getTop();

        int x=availableWidth/2;
        int y=availableHeight/2;
        //秒针---------------------------------------------------------------
        final Drawable dial=mSecondHand;
        int w=dial.getIntrinsicWidth();
        int h=dial.getIntrinsicHeight();

        boolean isscale=false;
        //判断是否需要进行缩放
        if(availableHeight>h||availableWidth>w){
            isscale=true;
            float scale=Math.min((float)availableHeight/(float)h,(float)availableWidth/(float)w);
            canvas.save();
            canvas.scale(scale,scale,x,y);
        }
        if(changed){
            dial.setBounds(x-(w/2),y-(h/2),x+(w/2),y+(h/2));

        }
        dial.draw(canvas);
        canvas.save();
        canvas.rotate(mHour/12.0f*360.0f,x,y);
        //时针---------------------------------------------------------------
        final Drawable hour=mHourHand;
        if(changed){
            w=hour.getIntrinsicWidth();
            h=hour.getIntrinsicHeight();
            hour.setBounds(x-(w/2),y-(h/2),x+(w/2),y+(h/2));
        }
        hour.draw(canvas);
        canvas.restore();
        canvas.save();
        canvas.rotate(mMinutes / 60.0f * 360.0f, x, y);
        //分针---------------------------------------------------------------
        final Drawable minute=mMinuteHand;

        if(changed){
            w=minute.getIntrinsicWidth();
            h=minute.getIntrinsicHeight();
            minute.setBounds(x-(w/2),y-(h/2),x+(w/2),y+(h/2));
        }
        minute.draw(canvas);
        if (isscale) {
            canvas.restore();
        }

    }
}

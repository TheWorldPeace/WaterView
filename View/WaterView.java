package com.worldpeace.waterview.View;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.jar.Attributes;

public class WaterView extends View {
    //构造一个水波纹的自定义view
    private int len;
    public WaterView(Context context){
        super(context);

    }
    //重写测量方法

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width=MeasureSpec.getSize(widthMeasureSpec);
        int height=MeasureSpec.getMode(heightMeasureSpec);
        //最短长为正方形的边
        len=Math.min(width,height);
        setMeasuredDimension(len,len);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}

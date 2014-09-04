package com.wowfly.android.core;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Created by user on 8/15/14.
 */
public class SquareLayout extends RelativeLayout{
    public SquareLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SquareLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public  SquareLayout(Context context) {
        super(context);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));

        int childWidthSize = getMeasuredWidth();
        int childHeightSize = getMeasuredHeight();
        heightMeasureSpec = widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
     }
}

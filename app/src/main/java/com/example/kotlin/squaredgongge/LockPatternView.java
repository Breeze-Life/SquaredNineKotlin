package com.example.kotlin.squaredgongge;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class LockPatternView extends View {

    // 是否初始化，确保只初始化一次
    private boolean mIsInit = false;

    // 画笔
    private Paint mLinePaint = null;
    private Paint mPressedPaint = null;
    private Paint mErrorPaint = null;
    private Paint mNormalPaint = null;
    private Paint mArrowPaint = null;

    // 颜色
    private int mOuterPressedColor = 0xff8cbad8;
    public LockPatternView(Context context) {
        super(context);
    }

    public LockPatternView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LockPatternView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onDraw(Canvas canvas) {

        if (!mIsInit) {

        }
    }
}

package com.example.customdividingviewdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by dongrong.fu on 2019/7/16
 * 自定义创作计划中的刻度尺，所有属性基本都支持灵活设置
 * 可以滑动选择不同的数值，并且对外提供了获取值的Public接口
 */
public class DividingView extends View {

    private Context mContext;
    private float mStartNum;    //刻度起点值
    private float mEndNum;  //刻度终点值
    private int mSize;  //大间距个数
    private int mContainsSize;  //一个大间距中包含小间距的个数
    private float mDividingSpace;   //刻度线间距大小
    private float mDividingHeightSmall; //小刻度线高度
    private float mDividingSizeSmall;   //小刻度线大小
    private int mDividingColorSmall;    //小刻度线颜色
    private float mDividingHeightBig;   //大刻度线高度
    private int mDividingColorBig;  //大刻度线颜色
    private float mDividingSizeBig; //大刻度线大小
    private float mDividingHeightSelect;    //选择线高度
    private float mDividingSizeSelect;  //选择刻度线大小
    private int mDividingColorSelect;   //选择刻度线颜色
    private float mDividingTextSize;    //刻度标识字体大小
    private int mDividingTextColor; //刻度标识字体颜色
    private float mDividingSelectDefault; //选择线默认的位置
    private int mDividingBackgroundColor;   //刻度尺背景颜色
    private boolean mIsAlignCenter;  //左右是否居中
    private float mDividingWidth;   //控件的实际宽度，不包含margin
    private float mDividingOvalWidth;   //两边圆弧的宽度
    private float mDividingHeight;  //控件的实际高度
    private Paint mPaint;
    private Path mPath;
    private float mScrollX;  //滑动的总距离
    private float mLastMoveX;   //上次滑动时的坐标
    private float mMoveBufferX; //移动时的距离缓存，为了吸附效果
    private DividingResultListener mDividingListener;
    private int mScrollSize;    //当前移动的刻度个数
    private float mOriginOffset;    //默认刻度;
    private float mSmallDividingAmplitude = 100; //小刻度之间差值
    private float mCurrentDividing; //当前刻度
    private int mSmallSpaceSize;    //小间距个数
    public DividingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DividingView);
        mStartNum = typedArray.getFloat(R.styleable.DividingView_start_num,100f);
        mEndNum = typedArray.getFloat(R.styleable.DividingView_end_num,50000f);
        mSize = typedArray.getInt(R.styleable.DividingView_size,50);
        mContainsSize = typedArray.getInt(R.styleable.DividingView_size_contains,10);
        mDividingSpace = typedArray.getDimension(R.styleable.DividingView_dividing_space, ScreenUtils.dp2px(mContext,4));
        mDividingHeightSmall = typedArray.getDimension(R.styleable.DividingView_dividing_height_small,ScreenUtils.dp2px(mContext,8));
        mDividingSizeSmall = typedArray.getDimension(R.styleable.DividingView_dividing_size_small,ScreenUtils.dp2px(mContext,1));
        mDividingColorSmall = typedArray.getColor(R.styleable.DividingView_dividing_color_small, Color.BLUE);
        mDividingHeightBig = typedArray.getDimension(R.styleable.DividingView_dividing_height_big,ScreenUtils.dp2px(mContext,12));
        mDividingColorBig = typedArray.getColor(R.styleable.DividingView_dividing_color_big, Color.GREEN);
        mDividingSizeBig = typedArray.getDimension(R.styleable.DividingView_dividing_size_big,ScreenUtils.dp2px(mContext,1));
        mDividingHeightSelect = typedArray.getDimension(R.styleable.DividingView_dividing_height_select,ScreenUtils.dp2px(mContext,48));
        mDividingSizeSelect = typedArray.getDimension(R.styleable.DividingView_dividing_size_select,ScreenUtils.dp2px(mContext,2));
        mDividingColorSelect = typedArray.getColor(R.styleable.DividingView_dividing_color_select, Color.RED);
        mDividingTextSize = typedArray.getDimension(R.styleable.DividingView_dividing_text_size,ScreenUtils.dp2px(mContext,12));
        mDividingTextColor = typedArray.getInt(R.styleable.DividingView_dividing_text_color, Color.BLACK);
        mDividingSelectDefault = typedArray.getFloat(R.styleable.DividingView_dividing_select_default,0f);
        mDividingBackgroundColor = typedArray.getColor(R.styleable.DividingView_dividing_background_color, Color.GRAY);
        mIsAlignCenter = typedArray.getBoolean(R.styleable.DividingView_dividing_align_center,true);
        mDividingWidth = typedArray.getDimension(R.styleable.DividingView_dividing_width,ScreenUtils.dp2px(mContext,280));
        mDividingOvalWidth = typedArray.getDimension(R.styleable.DividingView_dividing_oval_width,ScreenUtils.dp2px(mContext,20));
        mDividingHeight = typedArray.getDimension(R.styleable.DividingView_dividing_height,ScreenUtils.dp2px(mContext,48));
        typedArray.recycle();
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        clipCanvas(canvas);
        drawSmallLine(canvas);
        drawBigLine(canvas);
        drawDividingText(canvas);
        drawSelectLine(canvas);
    }

    private void init(){
        mPaint = new Paint();
        mPath = new Path();
        float defaultPositon = mDividingSelectDefault / mSmallDividingAmplitude;
        mOriginOffset = defaultPositon * mDividingSpace - ScreenUtils.getScreenWidth(mContext) / 2;
        mSmallSpaceSize = mSize * mContainsSize;
    }

    private void clipCanvas(Canvas canvas) {
        mPaint.setAntiAlias(true);
        mPaint.setColor(mDividingBackgroundColor);
        mPaint.setTextSize(6);
        mPaint.setStyle(Paint.Style.FILL);
        float leftMargin = 0;
        if(mIsAlignCenter){
            leftMargin = (ScreenUtils.getScreenWidth(mContext) - mDividingWidth)/2f;
        }
        mPath.reset();
        mPath.arcTo(new RectF(leftMargin,getMarginHeight(),mDividingOvalWidth * 2 + leftMargin,mDividingHeight + getMarginHeight()),90,180,true);
        mPath.lineTo(mDividingWidth - mDividingOvalWidth + leftMargin,getMarginHeight());
        mPath.arcTo(new RectF(mDividingWidth - mDividingOvalWidth * 2 +leftMargin,getMarginHeight(),mDividingWidth + leftMargin,mDividingHeight + getMarginHeight()),-90,180);
        mPath.lineTo(mDividingOvalWidth + leftMargin,mDividingHeight + getMarginHeight());
        canvas.drawPath(mPath,mPaint);
        canvas.clipPath(mPath);
        Bitmap bmp = ((BitmapDrawable)getResources().getDrawable(R.drawable.dividing_background)).getBitmap();
        canvas.drawBitmap(Bitmap.createScaledBitmap(bmp,(int)mDividingWidth , (int)mDividingHeight, true),leftMargin,0,mPaint);
    }

    private void drawSmallLine(Canvas canvas){
        mPaint.setAntiAlias(true);
        mPaint.setColor(mDividingColorSmall);
        mPaint.setStrokeWidth(mDividingSizeSmall);
        int lineSize = (int)(mEndNum - mStartNum) / 100;
        float startX = -mScrollX - mOriginOffset + mStartNum / 100 * mDividingSpace;
        float startY = mDividingHeightSelect - mDividingHeightSmall;
        for(int i = 0; i <= lineSize;i++){
            float endY = startY + mDividingHeightSmall;
            canvas.drawLine(startX,startY + getMarginHeight(),startX,endY + getMarginHeight(),mPaint);
            startX += mDividingSpace;
        }
    }

    private void drawBigLine(Canvas canvas){
        mPaint.setAntiAlias(true);
        mPaint.setColor(mDividingColorBig);
        mPaint.setStrokeWidth(mDividingSizeBig);
        float startX = -mScrollX - mOriginOffset;
        float startY = mDividingHeightSelect - mDividingHeightBig;
        float endY = startY + mDividingHeightBig;
        for(int i = 0;i < mSize;i++){
            startX += mDividingSpace * mContainsSize;
            canvas.drawLine(startX,startY + getMarginHeight(),startX,endY + getMarginHeight(),mPaint);
        }
    }

    private void drawSelectLine(Canvas canvas) {
        mPaint.setAntiAlias(true);
        mPaint.setColor(mDividingColorSelect);
        mPaint.setStrokeWidth(mDividingSizeSelect);
        float screenWidthHalf = ScreenUtils.getScreenWidth(mContext) / 2f;
        canvas.drawLine(screenWidthHalf,getMarginHeight(),screenWidthHalf,mDividingHeightSelect + getMarginHeight(),mPaint);
    }

    private void drawDividingText(Canvas canvas){
        mPaint.setAntiAlias(true);
        mPaint.setColor(mDividingTextColor);
        mPaint.setTextSize(mDividingTextSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
        float startX = -mScrollX - mOriginOffset;
        float startY = mDividingHeightSelect / 2f;
        for(int i = 0;i <= mSize;i++){
            StringBuilder text = new StringBuilder();
            if(i == 0){
                text.append(mStartNum / 1000).append("k");
                canvas.drawText(text.toString(),startX + mStartNum / 100 * mDividingSpace,startY + getMarginHeight(),mPaint);
            }else {
                text.append((int)mEndNum / mSize * i / 1000).append("k");
                canvas.drawText(text.toString(),startX,startY + getMarginHeight(),mPaint);
            }
            startX += mDividingSpace * mContainsSize;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastMoveX = event.getX();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (mScrollX + mOriginOffset + mLastMoveX - event.getX() < -ScreenUtils.getScreenWidth(mContext) / 2f + mDividingSpace * mStartNum / 100) {   //预计滑动将超出左边界
                    float temp = -ScreenUtils.getScreenWidth(mContext) / 2f + mDividingSpace * mStartNum / 100 - mOriginOffset;   //将左边界的移动总数置为最大值
                    mMoveBufferX += temp - mScrollX;   //计算新的缓存
                    float currentMoveX = getMinSpaceDividing();
                    mMoveBufferX -= currentMoveX;
                    mScrollX = temp;
                } else if (mScrollX + mOriginOffset + mLastMoveX - event.getX() > mDividingSpace * mSmallSpaceSize - ScreenUtils.getScreenWidth(mContext) / 2f) { //预计滑动将超出右边界
                    float temp = mDividingSpace * mSmallSpaceSize - ScreenUtils.getScreenWidth(mContext) / 2f - mOriginOffset;
                    mMoveBufferX += temp - mScrollX;
                    float currentMoveX = getMinSpaceDividing();
                    mMoveBufferX -= currentMoveX;
                    mScrollX = temp;
                }else{
                    mMoveBufferX += mLastMoveX - event.getX();
                    float currentMoveX = getMinSpaceDividing();
                    mScrollX += currentMoveX;
                    mMoveBufferX -= currentMoveX;
                    mLastMoveX = event.getX();
                }
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }


    private float getMinSpaceDividing(){
        //为了支持吸附效果，计算这次移动需要跨几个格子
        int size = Math.round(mMoveBufferX / mDividingSpace);
        if(size != 0){
            mScrollSize += size;
            if(mDividingListener != null){
                float result = mDividingSelectDefault + mScrollSize * mSmallDividingAmplitude;
                mDividingListener.onResultChange(result);
                mCurrentDividing = result;
            }
        }
        return mDividingSpace * size;
    }

    public void setStartNum(float startNum){
        mStartNum = startNum;
    }

    public void setEndNum(float endNum){
        mEndNum = endNum;
        mSize = (int)endNum / 1000;
        mSmallSpaceSize = (int)endNum / 100;
    }

    public void setDefaultDividing(float defaultDividing){
        mDividingSelectDefault = defaultDividing;
        float defaultPositon = mDividingSelectDefault / mSmallDividingAmplitude;
        mOriginOffset = defaultPositon * mDividingSpace - ScreenUtils.getScreenWidth(mContext) / 2;
        invalidate();
    }

    private float getMarginHeight(){    //让view上下居中
        return (getHeight() - mDividingHeightSelect) / 2f;
    }

    public float getPlayDayWords(){
        return mCurrentDividing;
    }

    public void setOnResultListener(DividingResultListener dividingResultListener){
        this.mDividingListener = dividingResultListener;
        if(mDividingListener != null){
            mCurrentDividing = mDividingSelectDefault;
            mDividingListener.onResultChange(mDividingSelectDefault);
        }
    }

    public interface DividingResultListener {
        void onResultChange(float result);
    }

}

package com.devilist.spv;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.Region;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;

import com.devilist.spv.fillpath.FillingOrientation;
import com.devilist.spv.fillpath.FillingTransform;
import com.devilist.spv.fillpath.WavesFillingTransform;
import com.devilist.spv.svgpathpaser.ScaleSvgPathParser;

import java.text.ParseException;


/**
 * 一个显示路径动画和填充动画的view
 * Created by zengpu on 2017/1/13.
 */

public class SvgPathView extends View {

    private static final String TAG = SvgPathView.class.getSimpleName();

    private Context mContext;
    private int mScreenWidth;      // 屏幕宽度
    private int mScreenHeight;      // 屏幕高度

    private Paint mStrokePaint;
    private int mStrokeColor = 0xff000000;
    private float mStrokeWidth = 1.0f;
    private long mStrokeAnimDuration = 2000; // 路径动画时间

    private Paint mFillPaint;
    private int mFillColor = 0xffffffff;
    private long mFillAnimDuration = 2000; // 填充动画时间
    private boolean isNeedFill = false; // 是否需要填充
    private float mFillAnimPertange = 0; // 填充百分比
    private FillingTransform mFillingTransform; // 填充变换
    private int mFillingOrientation = FillingOrientation.BOTTOM_TO_TOP;// 填充方向

    private long mAnimDelay = 0; // 动画延迟播放时间
    private ValueAnimator mStrokeAnimator, mFillAnimator;

    private SvgPath mSvgPath; // 路径封装类
    private Path mStrokeAnimPath; // 动画路径
    private String mSvgPathString; // 路径字符串
    private float mPathOriWidth, mPathOriHeight; // 路径原始宽高

    private int mAnimState = -1; // 动画状态

    private OnAnimStateChangeListener mOnAnimStateChangeListener;

    public SvgPathView(Context context) {
        this(context, null);
    }

    public SvgPathView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SvgPathView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.SvgPathView);

        mSvgPathString = mTypedArray.getString(R.styleable.SvgPathView_svgPathString);
        mStrokeColor = mTypedArray.getColor(R.styleable.SvgPathView_strokeColor, 0x99000000);
        mStrokeWidth = mTypedArray.getFloat(R.styleable.SvgPathView_strokeWidth, 1.0f);
        mStrokeAnimDuration = mTypedArray.getInt(R.styleable.SvgPathView_strokeAnimDuration, 2000);
        isNeedFill = mTypedArray.getBoolean(R.styleable.SvgPathView_needFill, false);
        mFillColor = mTypedArray.getColor(R.styleable.SvgPathView_fillColor, 0x99000000);
        mFillAnimDuration = mTypedArray.getInt(R.styleable.SvgPathView_fillAnimDuration, 2000);
        mAnimDelay = mTypedArray.getInt(R.styleable.SvgPathView_animDelay, 0);

        if (TextUtils.isEmpty(mSvgPathString))
            mSvgPathString = context.getResources().getString(R.string.path_svgpathview);

        mTypedArray.recycle();

        this.mContext = context;
        init();
    }

    private void init() {

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mScreenWidth = wm.getDefaultDisplay().getWidth();
        mScreenHeight = wm.getDefaultDisplay().getHeight();

        initPaint();

        mStrokeAnimPath = new Path();

        changeAnimState(AnimState.IDLE);
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        // strokePaint
        mStrokePaint = new Paint();
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setStrokeWidth(mStrokeWidth);
        mStrokePaint.setColor(mStrokeColor);
        // fillPaint
        mFillPaint = new Paint();
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setAntiAlias(true);
        mFillPaint.setColor(mFillColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // view测量的的宽高(包含padding)
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        // 测量path的原始宽高(不包含padding)
        measurePathOriSize(widthSize - getPaddingLeft() - getPaddingRight(),
                heightSize - getPaddingTop() - getPaddingBottom());

        // 宽高缩放比例(不包含padding)
        float scale = Math.min((widthSize - getPaddingLeft() - getPaddingRight()) / mPathOriWidth,
                (heightSize - getPaddingTop() - getPaddingBottom()) / mPathOriHeight);

        // 缩放后的宽高(不包含padding)
        int scaledWidth = (int) (mPathOriWidth * scale);
        int scaledHeight = (int) (mPathOriHeight * scale);

        // 缩放后的宽高加上padding和view测量的的宽高比较，取最小值,作为最后的测量宽高
        widthSize = Math.min(scaledWidth + getPaddingLeft() + getPaddingRight(), widthSize);
        heightSize = Math.min(scaledHeight + getPaddingTop() + getPaddingBottom(), heightSize);
        // 不能超出屏幕
        widthSize = Math.min(widthSize, mScreenWidth);
        heightSize = Math.min(heightSize, mScreenHeight);

        // path的原始宽高加上padding，作为path的原始宽高 (wrap_content)
        int oriWidth = (int) (mPathOriWidth + getPaddingLeft() + getPaddingRight());
        int oriHeight = (int) (mPathOriHeight + getPaddingTop() + getPaddingBottom());
        // 不能超出屏幕
        oriWidth = Math.min(oriWidth, mScreenWidth);
        oriHeight = Math.min(oriHeight, mScreenHeight);

        // 宽高模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(widthMeasureSpec);
        int measuredWidth = widthMode == MeasureSpec.AT_MOST ? oriWidth : widthSize;
        int measuredHeight = heightMode == MeasureSpec.AT_MOST ? oriHeight : heightSize;
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    /**
     * 测量path的边界
     *
     * @param contentWidthSize
     * @param contentHeightSize
     */
    private void measurePathOriSize(float contentWidthSize, float contentHeightSize) {

        final float strokeWidth = mStrokePaint.getStrokeWidth() / 2;

        ScaleSvgPathParser oriPathParser = new ScaleSvgPathParser();
        Path oriPath;
        try {
            oriPath = oriPathParser.parsePath(mSvgPathString);
        } catch (ParseException e) {
            Log.e(TAG, "Path parsing error. PathString is not validate ! --->" + e.toString());
            oriPath = new Path();
        }
        // 获得边界
        Region region = new Region();
        region.setPath(oriPath, new Region(Integer.MIN_VALUE, Integer.MIN_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE));
        Rect bounds = region.getBounds();
        mPathOriWidth = bounds.left + bounds.width() + strokeWidth;
        mPathOriHeight = bounds.top + bounds.height() + strokeWidth;
        if (mPathOriWidth == 0) {
            Log.w(TAG, "Warning! the parsed path may be not validate !");
            mPathOriWidth = contentWidthSize;
        }
        if (mPathOriHeight == 0) {
            Log.w(TAG, "Warning! the parsed path may be not validate !");
            mPathOriHeight = contentHeightSize;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        parsePath(w - getPaddingLeft() - getPaddingRight(), h - getPaddingTop() - getPaddingBottom());
    }

    /**
     * 解析path路径
     * 根据path的原始宽高和view的测量宽高计算出缩放比例，对path进行缩放
     *
     * @param desiredPathWidth
     * @param desiredPathHeight
     */
    private void parsePath(float desiredPathWidth, float desiredPathHeight) {

        float scale = Math.min(desiredPathWidth / mPathOriWidth, desiredPathHeight / mPathOriHeight);
        ScaleSvgPathParser desirePathParser = new ScaleSvgPathParser(scale, scale);
        Path desirePath;
        try {
            desirePath = desirePathParser.parsePath(mSvgPathString);
        } catch (ParseException e) {
            Log.e(TAG, "Path parsing error. PathString is not validate ! --->" + e.toString());
            desirePath = new Path();
        }
        mSvgPath = new SvgPath(desirePath);
        Log.e(TAG, " Path parsing finished!");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(getPaddingLeft(), getPaddingTop());
        // 路径
        canvas.drawPath(mStrokeAnimPath, mStrokePaint);
        // 填充
        if (mAnimState == AnimState.FILL) {
            if (null == mFillingTransform)
                mFillingTransform = new WavesFillingTransform(mFillingOrientation);
            mFillingTransform.update(this, canvas, mFillAnimPertange);
            canvas.drawPath(mSvgPath.getPath(), mFillPaint);
        }
        // 完成后的补刀
        if (mAnimState == AnimState.FINISH && isNeedFill)
            canvas.drawPath(mSvgPath.getPath(), mFillPaint);
    }

    /**
     * 播放动画，延迟一段时间，防止view还没有测量完
     */
    public void startAnim() {
        stopAnim();
        if (null == mSvgPath) {
            Log.e(TAG, "Waiting for the path parsing finished before anim started...");
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    startAnim();
                }
            }, 150);
        } else {
            Log.e(TAG, "Anim started!");
            startStrokeAnim();
        }
    }

    private void stopAnim() {
        if (null != mStrokeAnimator && mStrokeAnimator.isRunning()) {
            mStrokeAnimator.end();
            invalidate();
        }
        if (null != mFillAnimator && mFillAnimator.isRunning()) {
            mFillAnimator.end();
            invalidate();
        }
    }

    /**
     * 开始路径动画
     * <p>当一个路径文件里包含有多条路径时，需要为每条路径单独设置动画，然后依次播放。
     * <p>PathMeasure.getLength()获得的是路径中某条子路径的长度；
     * <p>PathMeasure.getSegment()获得的是路径中某条子路径的片段；
     * <p>利用PathMeasure.nextContour()方法可以一次获得每条路径，从而为每条路径设置动画。
     */
    private void startStrokeAnim() {
        changeAnimState(AnimState.IDLE);
        if (mSvgPath.getCount() == 0) {
            Log.e(TAG, "Path count must be more than zero! If the count is zero, " +
                    "there may be some errors occurred in the process of path parsing. " +
                    "please check the path String whether is validate or not.");
            return;
        }
        // 获取路径的PathMeasure
        final PathMeasure strokePathMeasure = mSvgPath.getPathMeasure();
        // 子路径持续时间
        long subPathDuration = mStrokeAnimDuration / mSvgPath.getCount();
        mStrokeAnimator = ValueAnimator.ofFloat(0, 1f);
        mStrokeAnimator.setDuration(subPathDuration);
        mStrokeAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mStrokeAnimator.setInterpolator(new LinearInterpolator());

        mStrokeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                //更新动画路径
                strokePathMeasure.getSegment(0, strokePathMeasure.getLength() * value, mStrokeAnimPath, true);
                SvgPathView.this.invalidate();
            }
        });

        mStrokeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                strokePathMeasure.getSegment(0, strokePathMeasure.getLength(), mStrokeAnimPath, true);
                // 移动到下一条路径
                strokePathMeasure.nextContour();
                if (strokePathMeasure.getLength() == 0) {
                    mStrokeAnimator.end();
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                changeAnimState(AnimState.STROKE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // stoke播放完之后
                if (isNeedFill) {
                    startFillAnim();
                } else
                    changeAnimState(AnimState.FINISH);
            }
        });
        // 初始化
        mStrokeAnimPath.reset();
        mStrokeAnimPath.lineTo(0, 0);
        mStrokeAnimator.setStartDelay(mAnimDelay);
        mStrokeAnimator.start();
    }

    /**
     * 开始填充动画
     */
    private void startFillAnim() {

        mFillAnimator = ValueAnimator.ofFloat(0, 1f);
        mFillAnimator.setDuration(mFillAnimDuration);
        mFillAnimator.setInterpolator(new LinearInterpolator());
        mFillAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mFillAnimPertange = (float) animation.getAnimatedValue();
                SvgPathView.this.invalidate();
            }
        });

        mFillAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                changeAnimState(AnimState.FILL);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                changeAnimState(AnimState.FINISH);
            }
        });
        mFillAnimator.start();
    }

    private void changeAnimState(int state) {
        if (mAnimState == state)
            return;
        mAnimState = state;
        if (null != mOnAnimStateChangeListener) {
            mOnAnimStateChangeListener.onAnimStateChange(mAnimState);
        }
    }

    /*----------------------------------------------------------------------------*/
    // 参数配置

    public SvgPathView setSvgPathString(String svgPathString) {
        this.mSvgPathString = svgPathString;
        return this;
    }

    public SvgPathView setStrokeAnimDuration(long strokeAnimDuration) {
        this.mStrokeAnimDuration = strokeAnimDuration;
        return this;
    }

    public SvgPathView setStrokeColor(int strokeColor) {
        this.mStrokeColor = strokeColor;
        mStrokePaint.setColor(mStrokeColor);
        return this;
    }

    public SvgPathView setStrokeWidth(float strokeWidth) {
        this.mStrokeWidth = strokeWidth;
        mStrokePaint.setStrokeWidth(mStrokeWidth);
        return this;
    }

    public SvgPathView setAnimDelay(long animDelay) {
        this.mAnimDelay = animDelay;
        return this;
    }

    public SvgPathView setNeedFill(boolean needFill) {
        isNeedFill = needFill;
        return this;
    }

    public SvgPathView setFillAnimDuration(long fillAnimDuration) {
        this.mFillAnimDuration = fillAnimDuration;
        return this;
    }

    public SvgPathView setFillColor(int fillColor) {
        this.mFillColor = fillColor;
        mFillPaint.setColor(mFillColor);
        return this;
    }

    public SvgPathView setFillingOrientation(int orientation) {
        this.mFillingOrientation = orientation;
        return this;
    }

    public SvgPathView setFillingTransform(FillingTransform transform) {
        this.mFillingTransform = transform;
        return this;
    }

    public SvgPathView setOnAnimStateChangeLisenter(OnAnimStateChangeListener listener) {
        this.mOnAnimStateChangeListener = listener;
        return this;
    }

    /**
     * 状态变化监听
     */
    public interface OnAnimStateChangeListener {

        /**
         * 动画状态
         *
         * @param animState
         */
        void onAnimStateChange(int animState);
    }
}

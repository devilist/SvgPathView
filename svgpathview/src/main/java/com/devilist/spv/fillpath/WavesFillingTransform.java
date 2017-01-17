package com.devilist.spv.fillpath;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Region;
import android.view.View;

import java.util.Random;

/**
 * 波纹填充效果
 * Created by zengpu on 2017/1/16.
 */

public class WavesFillingTransform implements FillingTransform {

    private static final String TAG = WavesFillingTransform.class.getSimpleName();

    private int mFillingOrientation = 0;
    private int mViewWidth, mViewHeight;
    private float mClippingWidth, mClippingHeight;
    private Path mWavesPath;
    private int mCurrentWave = 0;
    private int mWaveHeight = 40;

    public WavesFillingTransform() {
        this(FillingOrientation.BOTTOM_TO_TOP);
    }

    public WavesFillingTransform(int orientation) {
        this.mFillingOrientation = orientation;
    }

    @Override
    public void update(View view, Canvas canvas, float percentage) {
        updateDimensions(view.getWidth(), view.getHeight());
        updateFillingPath(percentage);
        rotateAndTransClippingArea();
        updateClipping(canvas, percentage);
    }

    /**
     * 缓存数据
     *
     * @param width
     * @param height
     */
    private void updateDimensions(int width, int height) {
        if (this.mViewWidth == 0 || this.mViewHeight == 0) {
            this.mViewWidth = width;
            this.mViewHeight = height;
            updateFillingDimensions();
        }
    }

    /**
     * 根据填充的方向计算用于剪切的填充区域的尺寸
     */
    private void updateFillingDimensions() {
        if (mFillingOrientation == FillingOrientation.BOTTOM_TO_TOP
                || mFillingOrientation == FillingOrientation.TOP_TO_BOTTOM) {
            mClippingWidth = mViewWidth;
            mClippingHeight = mViewHeight;

        } else if (mFillingOrientation == FillingOrientation.LEFT_TO_RIGHT
                || mFillingOrientation == FillingOrientation.RIGHT_TO_LEFT) {
            mClippingWidth = mClippingHeight = Math.max(mViewWidth, mViewHeight);

        } else if (mFillingOrientation == FillingOrientation.LEFT_TOP_TO_RIGHT_BOTTOM
                || mFillingOrientation == FillingOrientation.RIGHT_BOTTOM_TO_LEFT_TOP
                || mFillingOrientation == FillingOrientation.LEFT_BOTTOM_TO_RIGHT_TOP
                || mFillingOrientation == FillingOrientation.RIGHT_TOP_TO_LEFT_BOTTOM) {
            // View对角线与宽的夹角，弧度
            float alpha = (float) Math.asin(mViewHeight * 1f
                    / Math.sqrt(mViewWidth * mViewWidth + mViewHeight * mViewHeight));
            // mClippingWidth与View对角线夹角 弧度
            float beta = (float) Math.abs(alpha - Math.PI / 4);
            mClippingWidth = (float) (Math.sqrt(mViewWidth * mViewWidth + mViewHeight
                    * mViewHeight) * Math.cos(beta));
            mClippingHeight = mClippingWidth;

        } else if (mFillingOrientation == FillingOrientation.INNER_TO_OUT
                || mFillingOrientation == FillingOrientation.OUT_TO_INNER) {
            mClippingWidth = (float) (0.5 * Math.sqrt(mViewWidth * mViewWidth +
                    mViewHeight * mViewHeight));
            mClippingHeight = mClippingWidth;

        } else {
            mClippingWidth = mViewWidth;
            mClippingHeight = mViewHeight;
        }
    }

    private void updateFillingPath(float percentage) {
        mWavesPath = new Path();
        if (mFillingOrientation == FillingOrientation.INNER_TO_OUT) {
            mWavesPath.addCircle(mViewWidth / 2, mViewHeight / 2, percentage * mClippingWidth, Path.Direction.CCW);
            mWavesPath.close();
        } else if (mFillingOrientation == FillingOrientation.OUT_TO_INNER) {
            mWavesPath.addCircle(mViewWidth / 2, mViewHeight / 2, (1 - percentage) * mClippingWidth, Path.Direction.CCW);
            mWavesPath.close();
        } else {
            buildWaveAtIndex(mCurrentWave++ % 128, 128);
        }
    }

    private void buildWaveAtIndex(int index, int waveCount) {

        float startingHeight = mClippingHeight - mWaveHeight;
        boolean initialOrLast = (index == 1 || index == waveCount);

        float xMovement = (mClippingWidth * 1f / waveCount) * index;
        float divisions = 8;
        float variation = mWaveHeight / 2; // 振幅

        mWavesPath.moveTo(-mClippingWidth, startingHeight);

        // First wave
        if (!initialOrLast) {
            variation = randomFloat();
        }

        mWavesPath.quadTo(-mClippingWidth + mClippingWidth * 1f / divisions + xMovement, startingHeight + variation,
                -mClippingWidth + mClippingWidth * 1f / 4 + xMovement, startingHeight);

        if (!initialOrLast) {
            variation = randomFloat();
        }

        mWavesPath.quadTo(-mClippingWidth + mClippingWidth * 1f / divisions * 3 + xMovement, startingHeight - variation,
                -mClippingWidth + mClippingWidth * 1f / 2 + xMovement, startingHeight);

        // Second wave
        if (!initialOrLast) {
            variation = randomFloat();
        }

        mWavesPath.quadTo(-mClippingWidth + mClippingWidth * 1f / divisions * 5 + xMovement, startingHeight + variation,
                -mClippingWidth + mClippingWidth * 1f / 4 * 3 + xMovement, startingHeight);

        if (!initialOrLast) {
            variation = randomFloat();
        }

        mWavesPath.quadTo(-mClippingWidth + mClippingWidth * 1f / divisions * 7 + xMovement, startingHeight - variation,
                -mClippingWidth + mClippingWidth + xMovement, startingHeight);

        // Third wave
        if (!initialOrLast) {
            variation = randomFloat();
        }

        mWavesPath.quadTo(mClippingWidth * 1f / divisions + xMovement, startingHeight + variation,
                mClippingWidth * 1f / 4 + xMovement, startingHeight);

        if (!initialOrLast) {
            variation = randomFloat();
        }

        mWavesPath.quadTo(mClippingWidth * 1f / divisions * 3 + xMovement, startingHeight - variation,
                mClippingWidth * 1f / 2 + xMovement, startingHeight);

        // Forth wave
        if (!initialOrLast) {
            variation = randomFloat();
        }

        mWavesPath.quadTo(mClippingWidth * 1f / divisions * 5 + xMovement, startingHeight + variation,
                mClippingWidth * 1f / 4 * 3 + xMovement, startingHeight);

        if (!initialOrLast) {
            variation = randomFloat();
        }

        mWavesPath.quadTo(mClippingWidth * 1f / divisions * 7 + xMovement, startingHeight - variation,
                mClippingWidth + xMovement, startingHeight);

        // Closing path
        mWavesPath.lineTo(mClippingWidth + 100, startingHeight);
        mWavesPath.lineTo(mClippingWidth + 100, 0);
        mWavesPath.lineTo(0, 0);
        mWavesPath.close();
    }

    private float randomFloat() {
        return nextFloat(10) + mClippingHeight * 1f / 25;
    }

    private float nextFloat(float upperBound) {
        Random random = new Random();
        return (Math.abs(random.nextFloat()) % (upperBound + 1));
    }

    /**
     * 旋转平移填充剪切区域（十种情况）
     * <p>
     * <p>LEFT_TOP_TO_RIGHT_BOTTOM, RIGHT_BOTTOM_TO_LEFT_TOP : 逆时针旋转45度，然后平移
     * <p>LEFT_BOTTOM_TO_RIGHT_TOP，RIGHT_TOP_TO_LEFT_BOTTOM : 顺时针旋转45度，然后平移
     * <p>TOP_TO_BOTTOM：平移
     * <p>LEFT_TO_RIGHT，RIGHT_TO_LEFT：逆时针旋转90度，然后平移
     * <p>BOTTOM_TO_TOP, INNER_TO_OUT, OUT_TO_INNER : 不做变换
     */
    private void rotateAndTransClippingArea() {
        Matrix matrix = new Matrix();
        if (mFillingOrientation == FillingOrientation.LEFT_TOP_TO_RIGHT_BOTTOM) {
            matrix.preRotate(-45f);
            matrix.postTranslate(-0.5f * mViewHeight - 1.414f / 2 * (mClippingWidth - mViewHeight),
                    0.5f * mViewHeight - 1.414f / 2 * (mClippingWidth - mViewHeight));

        } else if (mFillingOrientation == FillingOrientation.RIGHT_BOTTOM_TO_LEFT_TOP) {
            matrix.preRotate(-45f);
            matrix.postTranslate(-0.5f * mViewHeight, 0.5f * mViewHeight);

        } else if (mFillingOrientation == FillingOrientation.LEFT_BOTTOM_TO_RIGHT_TOP) {
            matrix.preRotate(45f);
            matrix.postTranslate(0.5f * mViewWidth, -0.5f * mViewWidth);

        } else if (mFillingOrientation == FillingOrientation.RIGHT_TOP_TO_LEFT_BOTTOM) {
            matrix.preRotate(45f);
            matrix.postTranslate(0.5f * mViewWidth + 1.414f / 2 * (mClippingWidth - mViewHeight),
                    -0.5f * mViewWidth + 1.414f / 2 * (mClippingWidth - mViewHeight));

        } else if (mFillingOrientation == FillingOrientation.TOP_TO_BOTTOM) {
            matrix.preTranslate(0, -mClippingHeight + mViewHeight);

        } else if (mFillingOrientation == FillingOrientation.LEFT_TO_RIGHT) {
            matrix.preRotate(-90f);
            matrix.postTranslate(-mClippingWidth + mViewHeight, mClippingHeight);

        } else if (mFillingOrientation == FillingOrientation.RIGHT_TO_LEFT) {
            matrix.preRotate(-90f);
            matrix.postTranslate(mViewWidth - mClippingWidth, mClippingHeight);
        }
        mWavesPath.transform(matrix);
    }

    /**
     * 根据不同的填充方向对wavepath剪切
     * <p> mWavesPath.offset(offsetX, offsetY) 将path偏移
     * <p> canvas.clipPath(mWavesPath, op) 剪切path
     */
    private void updateClipping(Canvas canvas, float percentage) {
        float offsetX = 0;
        float offsetY = 0;
        Region.Op op = Region.Op.DIFFERENCE;
        if (mFillingOrientation == FillingOrientation.BOTTOM_TO_TOP) {
            offsetX = 0;
            offsetY = mClippingHeight * -percentage;
            op = Region.Op.DIFFERENCE;

        } else if (mFillingOrientation == FillingOrientation.TOP_TO_BOTTOM) {
            offsetX = 0;
            offsetY = (-mClippingHeight + mWaveHeight) * (1 - percentage);
            op = Region.Op.INTERSECT;

        } else if (mFillingOrientation == FillingOrientation.RIGHT_TO_LEFT) {
            offsetX = mViewWidth * -percentage;
            offsetY = 0;
            op = Region.Op.DIFFERENCE;

        } else if (mFillingOrientation == FillingOrientation.LEFT_TO_RIGHT) {
            if (mViewWidth < mViewHeight) {
                offsetX = (mViewWidth) * percentage - mClippingWidth;
            } else {
                offsetX = (mViewWidth - mViewHeight + mClippingWidth) * percentage - mClippingWidth;
            }
            offsetY = 0;
            op = Region.Op.INTERSECT;

        } else if (mFillingOrientation == FillingOrientation.RIGHT_BOTTOM_TO_LEFT_TOP) {
            offsetX = 0.707f * mClippingWidth * -percentage;
            offsetY = 0.707f * mClippingHeight * -percentage;
            op = Region.Op.DIFFERENCE;

        } else if (mFillingOrientation == FillingOrientation.LEFT_TOP_TO_RIGHT_BOTTOM) {
            if (mViewWidth > mViewHeight) {
                offsetX = 0.707f * ((mViewWidth - mViewHeight + mClippingWidth) * percentage - mClippingWidth);
                offsetY = offsetX;
            } else {
                offsetX = -0.707f * mClippingWidth * (1 - percentage);
                offsetY = -0.707f * mClippingHeight * (1 - percentage);
            }
            op = Region.Op.INTERSECT;

        } else if (mFillingOrientation == FillingOrientation.LEFT_BOTTOM_TO_RIGHT_TOP) {
            offsetX = 0.707f * mClippingWidth * percentage;
            offsetY = 0.707f * mClippingHeight * -percentage;
            op = Region.Op.DIFFERENCE;

        } else if (mFillingOrientation == FillingOrientation.RIGHT_TOP_TO_LEFT_BOTTOM) {
            offsetX = 0.707f * mClippingWidth * (1 - percentage);
            offsetY = -0.707f * mClippingHeight * (1 - percentage);
            op = Region.Op.INTERSECT;

        } else if (mFillingOrientation == FillingOrientation.INNER_TO_OUT) {
            offsetX = offsetY = 0;
            op = Region.Op.INTERSECT;
        } else if (mFillingOrientation == FillingOrientation.OUT_TO_INNER) {
            offsetX = offsetY = 0;
            op = Region.Op.DIFFERENCE;
        }

        mWavesPath.offset(offsetX, offsetY);
        canvas.clipPath(mWavesPath, op);
    }
}

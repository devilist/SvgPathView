package com.devilist.spv.fillpath;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Region;
import android.view.View;

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
    private int mCurrentIndex = 0;
    private int mWaveHeight = 40;

    public WavesFillingTransform() {
        this(FillingOrientation.BOTTOM_TO_TOP);
    }

    public WavesFillingTransform(int orientation) {
        this.mFillingOrientation = orientation;
    }

    @Override
    public void update(View view, Canvas canvas, float percentage) {
        // 1.根据填充方向计算相关尺寸
        updateDimensions(view.getWidth(), view.getHeight());
        // 2.生成闭合波纹路径
        updateFillingPath(percentage);
        // 3.根据填充方向对波纹路径旋转和平移
        rotateAndTransClippingArea();
        // 4.用波纹路径在canvas上进行剪切操作，布尔运算
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

    /**
     * 生成波纹路径
     * @param percentage
     */
    private void updateFillingPath(float percentage) {
        mWavesPath = new Path();
        if (mFillingOrientation == FillingOrientation.INNER_TO_OUT) {
            mWavesPath.addCircle(mViewWidth / 2, mViewHeight / 2, percentage * mClippingWidth, Path.Direction.CCW);
            mWavesPath.close();
        } else if (mFillingOrientation == FillingOrientation.OUT_TO_INNER) {
            mWavesPath.addCircle(mViewWidth / 2, mViewHeight / 2, (1 - percentage) * mClippingWidth, Path.Direction.CCW);
            mWavesPath.close();
        } else {
            createWaveAtIndex(mCurrentIndex++ % 128, 128);
        }
    }

    /**
     * 在 2 * mClippingWidth 的宽度上产生mWaveNumber个波纹
     *
     * @param index
     * @param maxIndex
     */
    private void createWaveAtIndex(int index, int maxIndex) {
        int mWaveNumber = 6; // 波纹个数
        float waveWidth = 2 * mClippingWidth / mWaveNumber; // 一个波纹(2pi)的宽度
        float variationY = mWaveHeight / 2; // 波纹的振幅一半

        float startingHeight = mClippingHeight - mWaveHeight / 2; // 波纹开始坐标Y
        float xOffset = mClippingWidth * 1f * index / maxIndex; // 波纹X方向的章动

        // 移动到第一个点
        mWavesPath.moveTo(-mClippingWidth + xOffset - 50, startingHeight);
        // 产生16个波纹
        for (int i = 0; i < mWaveNumber; i++) {
            // 前半个波
            float x1 = -mClippingWidth + waveWidth * (i + 0.25f) + xOffset;
            float y1 = startingHeight - createRandomVariation(variationY);
            float x2 = -mClippingWidth + waveWidth * (i + 0.5f) + xOffset;
            float y2 = startingHeight;
            mWavesPath.quadTo(x1, y1, x2, y2);
            // 后半个波
            float x3 = -mClippingWidth + waveWidth * (i + 0.75f) + xOffset;
            float y3 = startingHeight + createRandomVariation(variationY);
            float x4 = -mClippingWidth + waveWidth * (i + 1f) + xOffset;
            float y4 = startingHeight;
            mWavesPath.quadTo(x3, y3, x4, y4);
        }
        // 封闭path
        mWavesPath.lineTo(mClippingWidth + 100, startingHeight);
        mWavesPath.lineTo(mClippingWidth + 100, 0);
        mWavesPath.lineTo(0, 0);
        mWavesPath.close();
    }

    private float createRandomVariation(float base) {
        return (float) (base + Math.abs(Math.random()) * base / 2);
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
            matrix.postTranslate(0.5f * mViewWidth + 1.414f / 2 * (mClippingWidth - mWaveHeight),
                    -0.5f * mViewWidth - 1.414f / 2 * (mClippingWidth - mWaveHeight));

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
            offsetX = -0.707f * (mClippingWidth - mWaveHeight) * percentage;
            offsetY = 0.707f * (mClippingHeight - mWaveHeight) * percentage;
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

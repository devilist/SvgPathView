package com.svg.sample.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by tao on 2017/1/16.
 */

public class PathView extends View {

    private Paint oriPaint, clipPaint;
    private Path oriPath1, oriPath2, oriPath3, oriPath4, oriPath5;
    private Path clipPath1, clipPath2, clipPath3, clipPath4, clipPath5;

    public PathView(Context context) {
        this(context, null);
    }

    public PathView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PathView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPath();
    }

    private void initPath() {


        oriPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        oriPaint.setColor(0xff000000);
        oriPaint.setStyle(Paint.Style.STROKE);
        clipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        clipPaint.setColor(Color.RED);
        clipPaint.setStyle(Paint.Style.FILL);


        oriPath1 = new Path();
        oriPath1.moveTo(100, 100);
        oriPath1.lineTo(600, 100);
        oriPath1.lineTo(600, 600);
        oriPath1.lineTo(100, 600);
        oriPath1.lineTo(100, 100);
        oriPath1.close();

        clipPath1 = new Path();
        clipPath1.moveTo(0, 500);
        clipPath1.lineTo(600, 500);
        clipPath1.lineTo(600, 0);
        clipPath1.lineTo(0, 0);
        clipPath1.close();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(oriPath1, oriPaint);
        Matrix matrix = new Matrix();
        matrix.preTranslate(100, 10);
        matrix.preRotate(-45f);
        clipPath1.transform(matrix);
        canvas.clipPath(clipPath1, Region.Op.DIFFERENCE);
//        canvas.drawPath(oriPath1, clipPaint);

        Path textPath = new Path();
        textPath.moveTo(300, 400);
        textPath.lineTo(300, 600);
        canvas.drawTextOnPath("晴川历历汉阳树", textPath, 0, 0, clipPaint);
        canvas.drawText("晴川历历汉阳树", 300, 400, clipPaint);
    }
}

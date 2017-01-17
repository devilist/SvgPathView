package com.devilist.spv.fillpath;

import android.graphics.Canvas;
import android.view.View;

/**
 * 填充效果
 * Created by zengpu on 2017/1/16.
 */

public interface FillingTransform {
    void update(View view, Canvas canvas, float percentage);
}

package com.devilist.spv;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.Region;

/**
 * Created by zengpu on 2017/1/13.
 */

public class SvgPath {

    private final Region pathRegion; // 路径边界
    private final Path path; // 矢量路径
    private float length; // 路径长度
    private final Rect pathBounds; // 路径的边界
    private final PathMeasure pathMeasure;
    private final int count; // 路径总数

    public SvgPath(Path path) {
        this.path = path;

        // 计算长度
        pathMeasure = new PathMeasure(path, false);
        length = getPathLength(pathMeasure);
        count = getPathCount(pathMeasure);

        // 获得边界
        pathRegion = new Region();
        pathRegion.setPath(path, new Region(Integer.MIN_VALUE, Integer.MIN_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE));
        pathBounds = pathRegion.getBounds();
    }

    public float getLength() {
        return length;
    }

    public int getCount() {
        return count;
    }

    /**
     * 计算路径里每一段的长度
     *
     * @param measure
     * @return
     */
    private float getPathLength(PathMeasure measure) {
        float length = 0;
        while (measure.getLength() != 0) {
            length += measure.getLength();
            measure.nextContour();
        }
        return length;
    }

    private int getPathCount(PathMeasure measure) {
        measure.setPath(path, false);
        int count = 0;
        while (measure.getLength() != 0) {
            measure.nextContour();
            count++;
        }
        return count;
    }

    public Path getPath() {
        return path;
    }

    public Rect getPathBounds() {
        return pathBounds;
    }

    public PathMeasure getPathMeasure() {
        // pathMeasure初始化，移动到第一段路径上
        pathMeasure.setPath(path, false);
        return pathMeasure;
    }
}

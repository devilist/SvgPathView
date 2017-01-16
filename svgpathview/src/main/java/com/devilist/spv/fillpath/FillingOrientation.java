package com.devilist.spv.fillpath;

/**
 * 填充动画填充方向
 * Created by zengpu on 2017/1/16.
 */

public class FillingOrientation {
    /**
     * 从下向上填充
     */
    public static final int BOTTOM_TO_TOP = 0;
    /**
     * 从上向下填充
     */
    public static final int TOP_TO_BOTTOM = 1;
    /**
     * 从左向右填充
     */
    public static final int LEFT_TO_RIGHT = 2;
    /**
     * 从右向左填充
     */
    public static final int RIGHT_TO_LEFT = 3;
    /**
     * 从左上向右下45度方向填充
     */
    public static final int LEFT_TOP_TO_RIGHT_BOTTOM = 4;
    /**
     * 从右下向左上45度方向填充
     */
    public static final int RIGHT_BOTTOM_TO_LEFT_TOP = 5;
    /**
     * 从左下向右上45度方向填充
     */
    public static final int LEFT_BOTTOM_TO_RIGHT_TOP = 6;
    /**
     * 从右上向左下45度方向填充
     */
    public static final int RIGHT_TOP_TO_LEFT_BOTTOM = 7;
    /**
     * 从中心向外围填充
     */
    public static final int INNER_TO_OUT = 8;
    /**
     * 从外围向中心填充
     */
    public static final int OUT_TO_INNER = 9;
}

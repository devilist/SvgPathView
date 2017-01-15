package com.devilist.spv;

/**
 * 动画所处的状态
 * Created by zengpu on 2017/1/14.
 */

public class AnimState {
    public static final int IDLE = 0; // 未开始
    public static final int STROKE = 1; // 正在路径动画
    public static final int FILL = 2; // 正在填充动画
    public static final int FINISH = 3; // 完成
}

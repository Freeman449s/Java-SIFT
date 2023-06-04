package main;

/**
 * 全局参数
 */
public class GlobalParam {
    static final float SIGMA = 1.6f;                        // octave中栈底图像的sigma
    static final int S = 3;                                 // octave中高斯图像的interval数量
    static final float K = (float) Math.pow(2, 1.0 / S);    // 相邻高斯图像sigma的比例
    static final float MAX_LOCAL_SCALE = 2 * SIGMA * K * K; // octave内高斯图像的最大尺度
}

package flib;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class MathX {

    // ============================== 常用数学函数 ==============================

    /**
     * 高斯函数
     *
     * @param x     自变量
     * @param sigma 标准差
     * @param mu    数学期望
     * @return 高斯函数值
     */
    public static double gauss(double x, double sigma, double mu) {
        double coefficient = 1 / (sigma * Math.sqrt(2 * Math.PI));
        double exp = -Math.pow(x - mu, 2) / (2 * Math.pow(sigma, 2));
        return coefficient * Math.exp(exp);
    }

    /**
     * 默认数学期望为0的高斯函数。
     *
     * @param x     自变量
     * @param sigma 标准差
     * @return 高斯函数值
     */
    public static double gauss(double x, double sigma) {
        return gauss(x, sigma, 0);
    }

    /**
     * 默认标准差为1，数学期望为0的高斯函数（标准正态分布）。
     *
     * @param x 自变量
     * @return 高斯函数值
     */
    public static double gauss(double x) {
        return gauss(x, 1, 0);
    }

    /**
     * 返回以2为底，x的对数
     *
     * @param x 自变量
     * @return 以2为底，x的对数
     */
    public static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    /**
     * 角度值转弧度值
     *
     * @param deg 角度值
     * @return deg对应的弧度值
     */
    public static double deg2Rad(double deg) {
        return deg / 180 * Math.PI;
    }

    /**
     * 弧度值转角度值
     *
     * @param rad 弧度值
     * @return rad对应的角度值
     */
    public static double rad2Deg(double rad) {
        return rad / Math.PI * 180;
    }
}

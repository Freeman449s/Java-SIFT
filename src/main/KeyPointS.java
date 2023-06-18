package main;

import org.opencv.core.KeyPoint;

import java.io.Serializable;

/**
 * 可序列化KeyPoint。除了该类可序列化外，用法与org.opencv.core.KeyPoint相同。
 */
public class KeyPointS implements Serializable {
    public double x, y;     // 关键点的坐标。 FIXME CV的KeyPoint内有一个Point类型字段
    public float size;      // 关键点有效邻域的直径。
    public float angle;     // 关键点的朝向（如不适用，则为-1）。
    public float response;  // 关键点的响应。基于响应，可以选择最突出的关键点。可以利用响应做进一步的排序或降采样。
    public int octave;      // 提取关键点的octave（金字塔层级）。
    public int class_id;    // 对象ID，可以据此将属于同一对象的关键点聚为一类。

    public KeyPointS(KeyPoint cvKeyPoint) {
        this.x = cvKeyPoint.pt.x;
    }

}

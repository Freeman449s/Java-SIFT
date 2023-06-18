package main;

import org.jblas.FloatMatrix;
import org.opencv.core.KeyPoint;

import java.io.PushbackInputStream;
import java.io.Serializable;

/**
 * 可序列化KeyPointX，存储与KeyPointX内容相同的数据，但数据类型有所不同。该类不存储不可序列化的org.opencv.core.KeyPoint，
 * 而是将OpenCV KeyPoint的数据转为基本类型存储。因此，该类只应作为序列化时存储数据的容器使用。
 */
public class SerializableKeyPointX implements Serializable {
    public double x, y;             // 关键点的坐标。
    public float size;              // 关键点有效邻域的直径。
    public float angle;             // 关键点的朝向（如不适用，则为-1）。
    public float response;          // 关键点的响应。基于响应，可以选择最突出的关键点。可以利用响应做进一步的排序或降采样。
    public int octave;              // 提取关键点的octave（金字塔层级）。
    public int class_id;            // 对象ID，可以据此将属于同一对象的关键点聚为一类。
    public FloatMatrix descriptor;  // 关键点的描述子。

    public SerializableKeyPointX(KeyPointX keyPointX) {
        this(keyPointX.keyPoint, keyPointX.descriptor);
    }

    public SerializableKeyPointX(KeyPoint cvKeyPoint, FloatMatrix descriptor) {
        this.x = cvKeyPoint.pt.x;
        this.y = cvKeyPoint.pt.y;
        this.size = cvKeyPoint.size;
        this.angle = cvKeyPoint.angle;
        this.response = cvKeyPoint.response;
        this.octave = cvKeyPoint.octave;
        this.class_id = cvKeyPoint.class_id;
        this.descriptor = descriptor.dup();
    }

    public KeyPointX toKeyPointX() {
        return toKeyPointX(this);
    }

    public static KeyPointX toKeyPointX(SerializableKeyPointX serializable) {
        KeyPoint cvKeyPoint = new KeyPoint((float) serializable.x, (float) serializable.y, serializable.size, serializable.angle,
                serializable.response, serializable.octave, serializable.class_id);
        return new KeyPointX(cvKeyPoint, serializable.descriptor);
    }
}

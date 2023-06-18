package main;

import flib.MathX;
import org.jblas.FloatMatrix;
import org.opencv.core.KeyPoint;
import org.opencv.core.Point;

import java.io.Serializable;

public class KeyPointX implements Comparable<KeyPointX>, Serializable {
    public final transient KeyPoint keyPoint;
    public final FloatMatrix descriptor;

    // ============================== 为实现序列化而存储的KeyPoint的数据 ==============================
    private double x, y; // FIXME 更新keyPoint时，需要同步更新这些字段
    private float size;
    private float angle;
    private float response;
    private int octave;
    private int class_id;

    public KeyPointX(KeyPoint keyPoint, FloatMatrix descriptor) {
        this.keyPoint = keyPoint;
        this.descriptor = descriptor;

        x = this.keyPoint.pt.x;
        y = this.keyPoint.pt.y;
        size=this.keyPoint.size;
        angle=this.keyPoint.angle;
        response=this.keyPoint.response;
        octave=this.keyPoint
    }

    @Override
    public String toString() {
        return "{" + keyPoint.toString() + "; Descriptor " + descriptor.toString() + "}";
    }

    /**
     * This method is not well-defined, hence it's only for test purpose.
     *
     * @param o the object to be compared.
     * @return 当该对象的(x, y)坐标（先比较x坐标，再比较y坐标）小于传入对象的(x,y)坐标时，返回-1；大于传入对象的(x,y)坐标时，返回1；
     * 如果该对象(x,y)坐标与传入对象的(x,y)坐标相等，返回0.
     */
    @Override
    @Deprecated
    public int compareTo(KeyPointX o) {
        if (keyPoint.pt.x < o.keyPoint.pt.x) return -1;
        else if (keyPoint.pt.x > o.keyPoint.pt.x) return 1;
        else { // x相等
            return Double.compare(keyPoint.pt.y, o.keyPoint.pt.y);
        }
    }
}

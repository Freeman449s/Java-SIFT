package core;

import org.jblas.FloatMatrix;
import org.opencv.core.KeyPoint;

public class KeyPointX implements Comparable<KeyPointX> {
    public final KeyPoint keyPoint;
    public final FloatMatrix descriptor;

    public KeyPointX(KeyPoint keyPoint, FloatMatrix descriptor) {
        this.keyPoint = Util.keyPointDeepCopy(keyPoint);
        this.descriptor = descriptor.dup();
    }

    @Override
    public String toString() {
        return "{" + keyPoint.toString() + "; Descriptor " + descriptor.toString() + "}";
    }

    /**
     * 返回两个关键点的几何距离（使用keyPoint成员的位置计算的距离）
     *
     * @param pt1 关键点1
     * @param pt2 关键点2
     * @return 关键点pt1和pt2的几何距离
     */
    public static double geoDist(KeyPointX pt1, KeyPointX pt2) {
        double deltaX = pt1.keyPoint.pt.x - pt2.keyPoint.pt.x;
        double deltaY = pt1.keyPoint.pt.y - pt2.keyPoint.pt.y;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    /**
     * 返回该点与other的几何距离（使用keyPoint成员的位置计算的距离）
     *
     * @param other 另一个关键点
     * @return 该关键点和other的几何距离
     */
    public double geoDist(KeyPointX other) {
        return geoDist(this, other);
    }

    /**
     * 计算两个关键点描述子的欧氏距离
     *
     * @param pt1 关键点1
     * @param pt2 关键点2
     * @return 关键点pt1和pt2的描述子的欧式距离
     */
    public static double featureEuclidDist(KeyPointX pt1, KeyPointX pt2) {
        FloatMatrix delta = pt1.descriptor.sub(pt2.descriptor);
        return delta.norm2();
    }

    /**
     * 计算该点与other的描述子的欧氏距离
     *
     * @param other 另一个关键点
     * @return 该关键点和other的描述子的欧氏距离
     */
    public double featureEuclidDist(KeyPointX other) {
        return featureEuclidDist(this, other);
    }

    public static KeyPointX deepCopy(KeyPointX keyPointX) {
        return new KeyPointX(keyPointX.keyPoint, keyPointX.descriptor);
    }

    public KeyPointX deepCopy() {
        return deepCopy(this);
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

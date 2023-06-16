package main;

import org.jblas.FloatMatrix;
import org.opencv.core.KeyPoint;

public class KeyPointX {
    public final KeyPoint keyPoint;
    public final FloatMatrix descriptor;

    public KeyPointX(KeyPoint keyPoint, FloatMatrix descriptor) {
        this.keyPoint = keyPoint;
        this.descriptor = descriptor;
    }
}

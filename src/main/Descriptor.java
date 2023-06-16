package main;

import org.jblas.FloatMatrix;
import org.opencv.core.KeyPoint;

public class Descriptor {
    public final KeyPoint keyPoint;
    public final FloatMatrix descriptor;

    public Descriptor(KeyPoint keyPoint, FloatMatrix descriptor) {
        this.keyPoint = keyPoint;
        this.descriptor = descriptor;
    }
}

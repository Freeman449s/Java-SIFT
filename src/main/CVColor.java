package main;

import org.opencv.core.Scalar;

public enum CVColor {
    white(255, 255, 255),
    black(0, 0, 0),
    red(0, 0, 255),
    green(0, 255, 0),
    blue(255, 0, 0),
    yellow(0, 255, 255),
    cyan(255, 255, 0),
    magenta(255, 0, 255);


    private final Scalar state;

    CVColor(double B, double G, double R) {
        state = new Scalar(B, G, R);
    }

    public Scalar getState() {
        return state;
    }
}

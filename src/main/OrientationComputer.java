package main;

import org.jblas.FloatMatrix;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;

import java.util.ArrayList;

/**
 * 计算关键点朝向（论文第5章）
 */
public class OrientationComputer {
    public ArrayList<KeyPoint> run(ArrayList<KeyPoint> keyPoints, ArrayList<Octave> octaves) {

    }

    private static FloatMatrix computeOrientationHist(KeyPoint keyPoint, ArrayList<Octave> octaves) {
        GaussianImage gaussianImage = octaves.get(keyPoint.octave).gaussianImages.get(Util.getLocalGaussianImageId(keyPoint));

    }

    private static ArrayList<Float> computeOrientations(FloatMatrix hist) {

    }
}

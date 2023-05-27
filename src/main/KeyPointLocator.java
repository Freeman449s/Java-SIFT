package main;

import org.opencv.core.*;

import java.util.ArrayList;

/**
 * 精确关键点定位（论文第4章）
 */
public class KeyPointLocator {
    public ArrayList<KeyPoint> run(ArrayList<KeyPoint> coarseKeyPoints) {
        for (int ptIdx = 0; ptIdx < coarseKeyPoints.size(); ptIdx++) {

        }
    }

    private KeyPoint accurateLocalize(KeyPoint coarseKeyPoint, Mat dog) {

    }

    private boolean needCulling(KeyPoint keyPoint, Mat dog) {

    }
}

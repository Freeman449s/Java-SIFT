package main;

import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import static org.opencv.core.CvType.*;

public class Visualization {
    /**
     * 在image上标注关键点。该方法默认image的octave序号为1，即image的尺寸与从文件读取的图像尺寸相同。该方法不会更改传入的image。
     *
     * @param image     用于标注关键点的图像。图像应为以下类型之一：CV_8U, CV_8UC1, CU_8UC3。
     * @param keyPoints 关键点列表
     * @return 标注了关键点的图像
     * @throws IllegalArgumentException 如果图像的类型不属于CV_8U, CV_8UC1, CU_8UC3中的一者，将抛出此异常。
     */
    public static Mat visualize(Mat image, ArrayList<KeyPoint> keyPoints) throws IllegalArgumentException {
        return visualize(image, keyPoints, 1);
    }

    /**
     * 在image上标注关键点。该方法不会更改传入的image。
     *
     * @param image       用于标注关键点的图像。图像应为以下类型之一：CV_8U, CV_8UC1, CU_8UC3。
     * @param keyPoints   关键点列表
     * @param imageOctave image对应的octave序号
     * @return 标注了关键点的图像
     * @throws IllegalArgumentException 如果图像的类型不属于CV_8U, CV_8UC1, CU_8UC3中的一者，将抛出此异常。
     */
    public static Mat visualize(Mat image, ArrayList<KeyPoint> keyPoints, int imageOctave) throws IllegalArgumentException {
        if (image.type() != CV_8U && image.type() != CV_8UC1 && image.type() != CV_8UC3)
            throw new IllegalArgumentException("Image type is not one of the following: CV_8U, CV_8UC1, CU_8UC3.");
        int numOfOctaves = ExtremaDetector.computeNumOfOctaves(image);
        if (imageOctave < 0 || imageOctave > numOfOctaves - 1)
            throw new IllegalArgumentException("Image octave " + imageOctave + " exceeds bounds.");

        Mat imageWithPoints = image.clone();
        for (KeyPoint keyPoint : keyPoints) {
            float[] pos = Util.relocate(keyPoint, imageOctave);
            Point point = new Point(new double[]{pos[0], pos[1]});
            Imgproc.circle(imageWithPoints, point, 1, CVColor.cyan.getState());
        }

        return imageWithPoints;
    }
}

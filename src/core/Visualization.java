package core;

import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import static org.opencv.core.CvType.*;

public class Visualization {
    /**
     * 在image上标注关键点，同时标注朝向及尺度。该方法默认image的octave序号为1，即image的尺寸与从文件读取的图像尺寸相同。
     * 该方法不会更改传入的image。
     *
     * @param image     用于标注关键点的图像。图像应为以下类型之一：CV_8U, CV_8UC1, CU_8UC3。
     * @param keyPoints 关键点列表
     * @return 标注了关键点的图像
     * @throws IllegalArgumentException 如果图像的类型不属于CV_8U, CV_8UC1, CU_8UC3中的一者，将抛出此异常。
     */
    public static Mat visualize(Mat image, ArrayList<KeyPoint> keyPoints) throws IllegalArgumentException {
        return visualize(image, keyPoints, true, true, 1);
    }

    /**
     * 在image上标注关键点。该方法默认image的octave序号为1，即image的尺寸与从文件读取的图像尺寸相同。该方法不会更改传入的image。
     *
     * @param image           用于标注关键点的图像。图像应为以下类型之一：CV_8U, CV_8UC1, CU_8UC3。
     * @param keyPoints       关键点列表
     * @param withSize        是否标注尺寸
     * @param withOrientation 是否标注朝向
     * @return 标注了关键点的图像
     * @throws IllegalArgumentException 如果图像的类型不属于CV_8U, CV_8UC1, CU_8UC3中的一者，将抛出此异常。
     */
    public static Mat visualize(Mat image, ArrayList<KeyPoint> keyPoints, boolean withSize, boolean withOrientation) throws IllegalArgumentException {
        return visualize(image, keyPoints, withSize, withOrientation, 1);
    }

    /**
     * 在image上标注关键点。该该方法不会更改传入的image。
     *
     * @param image           用于标注关键点的图像。图像应为以下类型之一：CV_8U, CV_8UC1, CU_8UC3。
     * @param keyPoints       关键点列表
     * @param withSize        是否标注尺寸
     * @param withOrientation 是否标注朝向
     * @param imageOctave     参数image对应的octave序号；设置此参数将改变标注的关键点的位置
     * @return 标注了关键点的图像
     * @throws IllegalArgumentException 如果图像的类型不属于CV_8U, CV_8UC1, CU_8UC3中的一者，将抛出此异常。
     */
    public static Mat visualize(Mat image, ArrayList<KeyPoint> keyPoints, boolean withSize, boolean withOrientation, int imageOctave) throws IllegalArgumentException {
        if (image.type() != CV_8U && image.type() != CV_8UC1 && image.type() != CV_8UC3)
            throw new IllegalArgumentException("Image type is not one of the following: CV_8U, CV_8UC1, CU_8UC3.");
        int numOfOctaves = ExtremaDetector.computeNumOfOctaves(image);
        if (imageOctave < 0 || imageOctave > numOfOctaves - 1)
            throw new IllegalArgumentException("Image octave " + imageOctave + " exceeds bounds.");

        Mat imageWithPoints = image.clone();
        Scalar color = CVColor.green.getState();
        for (KeyPoint keyPoint : keyPoints) {
            float[] pos = Util.relocate(keyPoint, imageOctave); // (x,y)
            Point point = new Point(new double[]{pos[0], pos[1]});
            int radius = Math.max( // 半径至少为1
                    Math.round(withSize ? keyPoint.size : 1), 1);
            Imgproc.circle(imageWithPoints, point, radius, color);
            if (withOrientation) {
                float[] delta = new float[]{(float) (radius * Math.cos(keyPoint.angle)), (float) (radius * Math.sin(keyPoint.angle))};
                float[] lineEndPos = new float[]{pos[0] + delta[0], pos[1] + delta[1]};
                Point lineEndPoint = new Point(new double[]{lineEndPos[0], lineEndPos[1]});
                Imgproc.arrowedLine(imageWithPoints, point, lineEndPoint, color);
            }
        }

        return imageWithPoints;
    }
}

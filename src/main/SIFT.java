package main;

import org.jblas.FloatMatrix;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import flib.Log;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static org.opencv.core.CvType.*;

public class SIFT {

    public final Mat grayFloat;                             // 浮点类型的灰度图像
    private ArrayList<Octave> octaves;
    private ArrayList<KeyPoint> keyPoints;                  // 包含完整信息（位置、尺度、朝向、响应、octave序号）的关键点
    private ArrayList<FloatMatrix> descriptors;             // 描述子列表
    private ArrayList<KeyPointX> keyPointsWithDescriptor;   // 带描述子的关键点

    public SIFT(Mat grayFloat) {
        if (grayFloat.type() != CV_32F) {
            throw new IllegalArgumentException("grayFloat must be a floating-point gray image.");
        }
        this.grayFloat = grayFloat;
    }

    public ArrayList<KeyPointX> run() {
        try {
            ExtremaDetector extremaDetector = new ExtremaDetector();
            ArrayList<KeyPoint> coarseKeyPoints = extremaDetector.run(grayFloat);
            octaves = extremaDetector.octaves;

            KeyPointLocator locator = new KeyPointLocator();
            ArrayList<KeyPoint> keyPoints = locator.run(coarseKeyPoints, octaves);

            OrientationComputer orientationComputer = new OrientationComputer();
            ArrayList<KeyPoint> keyPointsWithOrientation = orientationComputer.run(keyPoints, octaves);
            this.keyPoints = keyPointsWithOrientation;

            DescriptorGenerator descriptorGenerator = new DescriptorGenerator();
            descriptors = descriptorGenerator.run(keyPointsWithOrientation, octaves);
            keyPointsWithDescriptor = descriptorGenerator.keyPointsWithDescriptor;

            return keyPointsWithDescriptor;
        } catch (InterruptedException ex) {
            Log.error("Internal error raised when detecting scale-space extrema.");
            ex.printStackTrace();
        } catch (TimeoutException ex) {
            Log.error("Time limit was exceeded (currently 1h) in the process of scale-space extrema detection.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * 返回在输入图像上建立的octaves。
     *
     * @return 在输入图像上建立的octaves。
     * @throws EarlyCallException 应先调用run()以准备数据。如果在调用run()之前调用此方法，将抛出此异常。
     */
    public ArrayList<Octave> getOctaves() {
        if (octaves == null)
            throw new EarlyCallException("Getter called before data have been prepared. Call run() first.");
        return octaves;
    }

    /**
     * 返回带有完整信息（位置、尺度、朝向、响应、octave序号），但是不带有描述子的关键点的列表。
     *
     * @return 带有完整信息（位置、尺度、朝向、响应、octave序号），但是不带有描述子的关键点的列表。
     * @throws EarlyCallException 应先调用run()以准备数据。如果在调用run()之前调用此方法，将抛出此异常。
     */
    public ArrayList<KeyPoint> getKeyPoints() {
        if (keyPoints == null)
            throw new EarlyCallException("Getter called before data have been prepared. Call run() first.");
        return keyPoints;
    }

    /**
     * 返回描述子列表。描述子的顺序与关键点的顺序相同。
     *
     * @return 描述子列表。
     * @throws EarlyCallException 应先调用run()以准备数据。如果在调用run()之前调用此方法，将抛出此异常。
     */
    public ArrayList<FloatMatrix> getDescriptors() {
        if (descriptors == null)
            throw new EarlyCallException("Getter called before data have been prepared. Call run() first.");
        return descriptors;
    }

    /**
     * 返回带有完整信息（位置、尺度、朝向、响应、octave序号）以及描述子的关键点的列表。
     *
     * @return 带有完整信息（位置、尺度、朝向、响应、octave序号）以及描述子的关键点的列表。
     * @throws EarlyCallException 应先调用run()以准备数据。如果在调用run()之前调用此方法，将抛出此异常。
     */
    public ArrayList<KeyPointX> getKeyPointsWithDescriptor() {
        if (keyPointsWithDescriptor == null)
            throw new EarlyCallException("Getter called before data have been prepared. Call run() first.");
        return keyPointsWithDescriptor;
    }

}

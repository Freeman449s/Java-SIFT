package test;

import org.jblas.FloatMatrix;
import org.opencv.core.*;

import static org.opencv.core.Core.*;
import static org.opencv.core.CvType.*;
import static org.opencv.highgui.HighGui.imshow;
import static org.opencv.highgui.HighGui.waitKey;
import static org.opencv.imgcodecs.Imgcodecs.*;

import main.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

@SuppressWarnings("DuplicatedCode")
public class Test {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        String imagePath = "image/box.png";
        Mat gray = imread(imagePath, IMREAD_GRAYSCALE);
        //System.out.printf("Min value = %.3f, max value = %.3f\n", Util.min(gray), Util.max(gray));
        normalize(gray, gray, 0, 1, NORM_MINMAX, CV_32F);
        try {
            siftTest(imagePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void foreachPixelDoTest(Mat gray) {
        // 简单实现
        Mat grayCopy1 = Mat.zeros(gray.size(), gray.type());
        long startTime = System.currentTimeMillis();
        for (int x = 0; x < gray.width(); x++) {
            for (int y = 0; y < gray.height(); y++) {
                for (int i = 0; i < 100; i++) grayCopy1.put(y, x, gray.get(y, x)[0]);
            }
        }
        long endTime = System.currentTimeMillis();
        BigDecimal duration = new BigDecimal(endTime - startTime)
                .divide(new BigDecimal(1000));
        System.out.printf("Plain implementation: %.3f s.\n", duration);
        System.out.printf("grayCopy[100,100] = %.3f\n", grayCopy1.get(100, 100)[0]);

        // foreachPixelDo
        Mat grayCopy2 = Mat.zeros(gray.size(), gray.type());
        startTime = System.currentTimeMillis();
        Util.foreachPixelDo(gray.width(), gray.height(), (x, y) -> {
            for (int i = 0; i < 100; i++) grayCopy2.put(y, x, gray.get(y, x)[0]);
        });
        endTime = System.currentTimeMillis();
        duration = new BigDecimal(endTime - startTime)
                .divide(new BigDecimal(1000));
        System.out.printf("foreachPixelDo: %.3f s.\n", duration);
        System.out.printf("grayCopy[100,100] = %.3f\n", grayCopy2.get(100, 100)[0]);

        // foreachPixelParallelDo
        Mat grayCopy3 = Mat.zeros(gray.size(), gray.type());
        startTime = System.currentTimeMillis();
        try {
            Util.foreachPixelParallelDo(gray.width(), gray.height(), (x, y) -> {
                for (int i = 0; i < 100; i++) grayCopy3.put(y, x, gray.get(y, x)[0]);
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        endTime = System.currentTimeMillis();
        duration = new BigDecimal(endTime - startTime)
                .divide(new BigDecimal(1000));
        System.out.printf("foreachPixelParallelDo: %.3f s.\n", duration);
        System.out.printf("grayCopy[100,100] = %.3f\n", grayCopy3.get(100, 100)[0]);
    }

    private static void detectorTest(Mat gray) throws InterruptedException, TimeoutException {
        ExtremaDetector extremaDetector = new ExtremaDetector();
        ArrayList<KeyPoint> keyPoints = extremaDetector.run(gray);
        System.out.println();
    }

    private static void locatorTest(Mat gray) throws InterruptedException, TimeoutException {
        ExtremaDetector extremaDetector = new ExtremaDetector();
        ArrayList<KeyPoint> coarseKeyPoints = extremaDetector.run(gray);
        ArrayList<Octave> octaves = extremaDetector.octaves;

        KeyPointLocator locator = new KeyPointLocator();
        ArrayList<KeyPoint> keyPoints = locator.run(coarseKeyPoints, octaves);
        System.out.println();
    }

    private static void descriptorTest(Mat gray) throws InterruptedException, TimeoutException {
        ExtremaDetector extremaDetector = new ExtremaDetector();
        ArrayList<KeyPoint> coarseKeyPoints = extremaDetector.run(gray);
        ArrayList<Octave> octaves = extremaDetector.octaves;

        KeyPointLocator locator = new KeyPointLocator();
        ArrayList<KeyPoint> keyPoints = locator.run(coarseKeyPoints, octaves);

        OrientationComputer orientationComputer = new OrientationComputer();
        ArrayList<KeyPoint> keyPointsWithOrientation = orientationComputer.run(keyPoints, octaves);

        DescriptorGenerator descriptorGenerator = new DescriptorGenerator();
        ArrayList<KeyPointX> keyPointsWithDescriptor = new ArrayList<>();
        for (KeyPoint keyPoint : keyPointsWithOrientation) {
            keyPointsWithDescriptor.add(new KeyPointX(keyPoint, descriptorGenerator.generate(keyPoint, octaves)));
        }
    }

    private static void siftTest(String path) {
        Mat image = Imgcodecs.imread(path);
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        Mat grayFloat = new Mat();
        normalize(gray, grayFloat, 0, 1, NORM_MINMAX, CV_32F);

        SIFT sift = new SIFT(grayFloat);
        ArrayList<KeyPointX> keyPointsWithDescriptor = sift.run();
        ArrayList<KeyPoint> keyPoints = sift.getKeyPoints();
        Mat markedImage = Visualization.visualize(image, keyPoints, false, false);
        normalize(markedImage, markedImage, 0, 255, NORM_MINMAX, CV_8UC1);
        imshow("Marked Image", markedImage);
        waitKey();
    }

    private static void parallelDescriptorComputationTest(String path) throws IOException {
        Mat image = Imgcodecs.imread(path);
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        Mat grayFloat = new Mat();
        normalize(gray, grayFloat, 0, 1, NORM_MINMAX, CV_32F);

        SIFT sift = new SIFT(grayFloat);
        ArrayList<KeyPointX> keyPointsWithDescriptor = sift.run();
        Collections.sort(keyPointsWithDescriptor);
        ArrayList<FloatMatrix> descriptors = sift.getDescriptors();
        String outputFilePath = "parallel 3.txt";
        File outputFile = new File(outputFilePath);
        if (outputFile.exists()) {
            throw new IOException("Output file \"" + outputFilePath + "\" already exists.");
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            for (KeyPointX keyPoint : keyPointsWithDescriptor) {
                writer.write(keyPoint.toString());
                writer.newLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

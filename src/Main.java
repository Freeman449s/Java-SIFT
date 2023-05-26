import org.opencv.core.*;

import java.io.*;

import static org.opencv.core.Core.*;
import static org.opencv.core.CvType.*;
import static org.opencv.imgcodecs.Imgcodecs.*; // 导入静态方法
import static org.opencv.imgproc.Imgproc.*;

public class Main {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static final String IMAGE_PATH = "image/beach.jpg";

    public static void main(String[] args) {
        File imageFile = new File(IMAGE_PATH);
        if (!(imageFile.exists() && imageFile.isFile())) {
            System.out.printf("File \"%s\" does not exist or is not a valid file.", IMAGE_PATH);
        }

        Mat image = imread(IMAGE_PATH);
        Mat gray = new Mat();
        cvtColor(image, gray, COLOR_BGR2GRAY);
        Mat grayFloat = new Mat();
        normalize(gray, grayFloat, 0, 1, NORM_MINMAX, CV_32F);
    }
}

package core;

import io.IOUtil;
import org.opencv.core.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import static org.opencv.core.Core.*;
import static org.opencv.core.CvType.*;
import static org.opencv.highgui.HighGui.imshow;
import static org.opencv.highgui.HighGui.waitKey;
import static org.opencv.imgcodecs.Imgcodecs.*; // 导入静态方法
import static org.opencv.imgproc.Imgproc.*;

public class Main {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static final String IMAGE_PATH = "image/box_in_scene.png";
    private static final String STORAGE_FILE_PATH = "data/box_in_scene.dat";

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.print("图像路径：");
            String imagePath = in.nextLine();
            File imageFile = new File(imagePath);
            String imageName = imageFile.getName().split("\\.")[0], postfix = imageFile.getName().split("\\.")[1];
            if (!(imageFile.exists() && imageFile.isFile())) {
                System.out.printf("File \"%s\" does not exist or is not a valid file.", imageFile);
            }
            String datPath = "data/" + imageFile.getName().split("\\.")[0] + ".dat";
            String siftPath = imageFile.getParent() + "/" + imageName + " sift." + postfix;

            Mat image = imread(imagePath);
            Mat gray = new Mat();
            cvtColor(image, gray, COLOR_BGR2GRAY);
            Mat grayFloat = new Mat();
            normalize(gray, grayFloat, 0, 1, NORM_MINMAX, CV_32F);

            SIFT sift = new SIFT(grayFloat);
            ArrayList<KeyPointX> keyPointsWithDescriptor = sift.run();
            ArrayList<KeyPoint> keyPoints = new ArrayList<>(keyPointsWithDescriptor.size());
            for (KeyPointX keyPointX : keyPointsWithDescriptor)
                keyPoints.add(keyPointX.keyPoint);
            Mat imageWithMark = Visualization.visualize(image, keyPoints, true, true);
            imwrite(siftPath, imageWithMark);
            try {
                IOUtil.writeKeyPointXes(keyPointsWithDescriptor, datPath, false);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}

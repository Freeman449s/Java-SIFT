package test;

import org.opencv.core.*;

import static org.opencv.imgcodecs.Imgcodecs.*;

import main.Util;

import java.math.*;

public class Test {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        foreachPixelDoTest();
    }

    private static void foreachPixelDoTest() {
        Mat gray = imread("image/beach.jpg", IMREAD_GRAYSCALE);

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
        System.out.printf("grayCopy[100,100] = %.0f\n", grayCopy1.get(100, 100)[0]);

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
        System.out.printf("grayCopy[100,100] = %.0f\n", grayCopy2.get(100, 100)[0]);

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
        System.out.printf("grayCopy[100,100] = %.0f\n", grayCopy3.get(100, 100)[0]);
    }
}

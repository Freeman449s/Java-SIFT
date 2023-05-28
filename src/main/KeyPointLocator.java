package main;

import flib.MathX;
import org.jblas.*;
import org.opencv.core.*;

import java.util.ArrayList;
import java.util.Objects;

import static org.opencv.core.CvType.*;

/**
 * 精确关键点定位（论文第4章）
 */
public class KeyPointLocator {
    public ArrayList<KeyPoint> run(ArrayList<KeyPoint> coarseKeyPoints) {
        for (int ptIdx = 0; ptIdx < coarseKeyPoints.size(); ptIdx++) {

        }
    }

    private KeyPoint accurateLocalize(KeyPoint coarseKeyPoint, ArrayList<Mat> dogImages) {
        final int ITERATION_LIMIT = 5;

        KeyPoint keyPoint = Util.keyPointDeepCopy(coarseKeyPoint);
        for (int i = 1; i <= ITERATION_LIMIT; i++) {

        }
    }

    /**
     * TODO need comments
     *
     * @param keyPoint
     * @param dogImages
     * @return
     * @throws IndexOutOfBoundsException
     */
    private Mat constructPixelCube(KeyPoint keyPoint, ArrayList<Mat> dogImages) throws IndexOutOfBoundsException {
        int intX = (int) Math.round(keyPoint.pt.x), intY = (int) Math.round(keyPoint.pt.y);
        int imageId = (int) Math.round(MathX.log2(
                keyPoint.size / GlobalParam.SIGMA / Math.pow(2, keyPoint.octave))
                * GlobalParam.S); // 本octave中的图像Id
        Mat pixelCube = new Mat(3, 3, CV_32FC3);
        Mat prev = dogImages.get(imageId - 1), curr = dogImages.get(imageId), next = dogImages.get(imageId + 1);
        for (int i = -1; i <= 1; i++) {
            if (intX + i < 0 || intX + i >= curr.width())
                throw new IndexOutOfBoundsException("X index " + intX + " is out of bound.");
            for (int j = -1; j <= 1; j++) {
                if (intY + j < 0 || intY + j >= curr.height())
                    throw new IndexOutOfBoundsException("Y index " + intY + " is out of bound.");
                pixelCube.put(j + 1, i + 1, new float[]{
                        (float) prev.get(intY + j, intX + i)[0],
                        (float) curr.get(intY + j, intX + i)[0],
                        (float) next.get(intY + j, intX + i)[0]});
            }
        }
        return pixelCube;
    }

    /**
     * 计算像素立方体中中心像素处的梯度。
     *
     * @param pixelCube 3*3*3的像素立方体，要计算梯度的像素位于中间。
     * @return 3*1的梯度向量，顺序为[dx,dy,ds]，其中dx表示沿列方向的导数，dy表示沿行方向的导数，ds表示沿尺度方向的导数。
     */
    private FloatMatrix computeCenterPixelGradient(Mat pixelCube) {
        // 导数f'(x)的O(h^2)阶近似值为(f(x + h) - f(x - h)) / (2 * h)
        // 当h = 1时，上式简化为f'(x) = (f(x + 1) - f(x - 1)) / 2
        float dx = (float) (pixelCube.get(1, 2)[1] - pixelCube.get(1, 0)[1]) / 2;
        float dy = (float) (pixelCube.get(2, 1)[1] - pixelCube.get(0, 1)[1]) / 2;
        float ds = (float) (pixelCube.get(1, 1)[2] - pixelCube.get(1, 1)[0]) / 2;
        FloatMatrix gradient = new FloatMatrix(3, 1);
        gradient.put(0, dx);
        gradient.put(1, dy);
        gradient.put(2, ds);
        return gradient;
    }

    private FloatMatrix computeCenterPixelHessian(Mat pixelCube) {
        // 二阶导数f''(x)的O(h^2)阶近似值为(f(x + h) - 2 * f(x) + f(x - h)) / (h ^ 2)
        // 当h = 1时，上式简化为f''(x) = (f(x + 1) - 2 * f(x) + f(x - 1))
        // 混合偏导数(d^2) f / (dx dy)的O(h^2)阶近似值为(f(x + h, y + h) - f(x + h, y - h) - f(x - h, y + h) + f(x - h, y - h)) / (4 * h ^ 2)
        // 当h = 1时，上式简化为(d^2) f / (dx dy) = (f(x + 1, y + 1) - f(x + 1, y - 1) - f(x - 1, y + 1) + f(x - 1, y - 1)) / 4
        float centerVal = (float) pixelCube.get(1, 1)[1];
        float dxx = (float) (pixelCube.get(1, 2)[1] - 2 * centerVal + pixelCube.get(1, 0)[1]);
        float dyy = (float) (pixelCube.get(2, 1)[1] - 2 * centerVal + pixelCube.get(0, 1)[1]);
        float dss = (float) (pixelCube.get(1, 1)[2] - 2 * centerVal + pixelCube.get(1, 1)[0]);
        float dxy = (float) (pixelCube.get(2, 2)[1] - pixelCube.get(0, 2)[1]
                - pixelCube.get(2, 0)[1] + pixelCube.get(0, 0)[1]) / 4;
        float dxs = (float) (pixelCube.get(1, 2)[2] - pixelCube.get(1, 2)[0]
                - pixelCube.get(1, 0)[2] + pixelCube.get(1, 0)[0]) / 4;
        float dys = (float) (pixelCube.get(2, 1)[2] - pixelCube.get(2, 1)[0]
                - pixelCube.get(0, 1)[2] + pixelCube.get(0, 1)[0]) / 4;
        return new FloatMatrix(new float[][]{
                {dxx, dxy, dxs},
                {dxy, dyy, dys},
                {dxs, dys, dss}});
    }

    private boolean needCulling(KeyPoint keyPoint, Mat dog) {

    }
}

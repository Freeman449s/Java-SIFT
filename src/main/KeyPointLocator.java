package main;

import flib.MathX;
import org.jblas.*;
import org.opencv.core.*;

import java.util.ArrayList;

import static org.opencv.core.CvType.*;

/**
 * 精确关键点定位（论文第4章）
 */
public class KeyPointLocator {
    static final float CONTRAST_CULLING_THRESHOLD = 0.03f;  // DoG响应低于此阈值的关键点将被剔除
    static final float EDGE_CULLING_THRESHOLD =
            (float) Math.pow(10 + 1, 2) / 10;               // 边缘剔除阈值

    ArrayList<KeyPoint> keyPoints = null;

    public ArrayList<KeyPoint> run(ArrayList<KeyPoint> coarseKeyPoints, ArrayList<Octave> octaves) {
        System.out.print("Localizing key points...");
        keyPoints = new ArrayList<>();
        for (KeyPoint coarseKeyPoint : coarseKeyPoints) {
            KeyPoint keyPoint = accurateLocalize(coarseKeyPoint, octaves);
            if (keyPoint != null) keyPoints.add(keyPoint);
        }
        System.out.println("DONE");
        return keyPoints;
    }

    /**
     * 精确定位关键点
     *
     * @param coarseKeyPoint 局部极值检测得到的粗糙关键点
     * @param octaves        octaves
     * @return 精确定位的关键点。如果在定位过程中发生越界，或达到最大迭代次数未收敛，或未能通过弱对比和边缘剔除，将返回null。
     */
    private static KeyPoint accurateLocalize(KeyPoint coarseKeyPoint, ArrayList<Octave> octaves) {
        final int ITERATION_LIMIT = 5;
        ArrayList<Mat> dogImages = octaves.get(coarseKeyPoint.octave).dogImages;
        KeyPoint keyPoint = Util.keyPointDeepCopy(coarseKeyPoint);
        Mat pixelCube = null;
        FloatMatrix gradient = null, hessian = null, hessianInv, displace = null; // 预先声明在迭代结束后需要保存的变量
        int width = dogImages.get(0).width(), height = dogImages.get(0).height();
        int iteration = 1;

        for (; iteration <= ITERATION_LIMIT; iteration++) {
            // 计算位移
            try {
                pixelCube = constructPixelCube(keyPoint, dogImages);
            } catch (IndexOutOfBoundsExceptionC ex) { // 构建pixelCube时越界
                return null;
            }
            gradient = computeCenterPixelGradient(pixelCube);
            hessian = computeCenterPixelHessian(pixelCube);
            hessianInv = Solve.pinv(hessian);
            displace = hessianInv.mmul(gradient).mul(-1); // 3x1

            // 更新位置
            // TODO 需要考虑梯度是对局部坐标求导的，还是对全局坐标求导的，两者有什么差异；进而确定如何更新位置
            // 这里全部使用局部坐标
            float newX = (float) keyPoint.pt.x + displace.get(0), newY = (float) keyPoint.pt.y + displace.get(1),
                    newLocalScale = Util.global2LocalScale(keyPoint.size, keyPoint.octave) + displace.get(2);
            if (Math.round(newX) < 0 || Math.round(newX) >= width || Math.round(newY) < 0 || Math.round(newY) > height ||
                    newLocalScale < GlobalParam.SIGMA || newLocalScale > GlobalParam.MAX_LOCAL_SCALE)
                return null; // 迭代过程中越界，舍弃此关键点
            keyPoint = new KeyPoint(newX, newY, Util.local2GlobalScale(newLocalScale, keyPoint.octave), -1, 0, keyPoint.octave);
            if (displace.get(0) < 0.5 && displace.get(1) < 0.5 && displace.get(2) < 0.5) {
                break;
            }
        }
        if (iteration > 5) return null; // 迭代5次仍未收敛，舍弃此关键点

        // 弱对比剔除
        float response = (float) pixelCube.get(1, 1)[1] + 0.5f * gradient.transpose().mmul(displace).get(0);
        if (Math.abs(response) < CONTRAST_CULLING_THRESHOLD) return null;
        // 边缘剔除
        float trace = hessian.get(0, 0) + hessian.get(1, 1);
        float det = hessian.get(0, 0) * hessian.get(1, 1) -
                hessian.get(0, 1) * hessian.get(1, 0);
        if (det < 0 || Math.pow(trace, 2) / det >= EDGE_CULLING_THRESHOLD) return null;

        return keyPoint; // 通过所有测试，返回精确定位的关键点
    }

    /**
     * 以keyPoint为中心，构建3×3×3的像素立方
     *
     * @param keyPoint 像素立方的中心
     * @param octaves  在图像上建立的所有octave
     * @return 以keyPoint为中心，3×3×3的像素立方
     * @throws IndexOutOfBoundsExceptionC 如果关键点位于图像边缘，导致采集周围像素时发生越界
     */
    private static Mat _constructPixelCube(KeyPoint keyPoint, ArrayList<Octave> octaves) throws IndexOutOfBoundsExceptionC {
        return constructPixelCube(keyPoint, octaves.get(keyPoint.octave).dogImages);
    }

    /**
     * 以keyPoint为中心，构建3×3×3的像素立方
     *
     * @param keyPoint  像素立方的中心
     * @param dogImages DoG图像栈
     * @return 以keyPoint为中心，3×3×3的像素立方
     * @throws IndexOutOfBoundsExceptionC 如果关键点位于图像边缘，导致采集周围像素时发生越界
     */
    private static Mat constructPixelCube(KeyPoint keyPoint, ArrayList<Mat> dogImages) throws IndexOutOfBoundsExceptionC {
        int intX = (int) Math.round(keyPoint.pt.x), intY = (int) Math.round(keyPoint.pt.y);
        int imageId = (int) Math.round(MathX.log2(
                keyPoint.size / GlobalParam.SIGMA / Math.pow(2, keyPoint.octave))
                * GlobalParam.S); // 本octave中的图像Id
        if (imageId < 1 || imageId > dogImages.size() - 2)
            throw new IndexOutOfBoundsExceptionC("Image index " + imageId + " is out of bound.");
        Mat pixelCube = new Mat(3, 3, CV_32FC3);
        Mat prev = dogImages.get(imageId - 1), curr = dogImages.get(imageId), next = dogImages.get(imageId + 1);
        for (int i = -1; i <= 1; i++) {
            if (intX + i < 0 || intX + i >= curr.width())
                throw new IndexOutOfBoundsExceptionC("X index " + intX + " is out of bound.");
            for (int j = -1; j <= 1; j++) {
                if (intY + j < 0 || intY + j >= curr.height())
                    throw new IndexOutOfBoundsExceptionC("Y index " + intY + " is out of bound.");
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
    private static FloatMatrix computeCenterPixelGradient(Mat pixelCube) {
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

    private static FloatMatrix computeCenterPixelHessian(Mat pixelCube) {
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
}

package align;

import core.KeyPointX;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Collections;

public class AlignUtil {
    private static final int GOOD_LOWER_BOUND = 10; // 有效配对的下界；寻找到的有效配对低于此值时，可能无法有效地拼接
    private static final int GOOD_UPPER_BOUND = 20; // 有效配对的上界；寻找到的有效配对高于此值时，可能无法有效地拼接（简单的单应矩阵求解对外点不鲁棒）
    public static final boolean USE_CV_HOMOGRAPHY = true;

    /**
     * 为queryPoints中的每个关键点，在trainPoints中找到描述子的欧式距离最接近的2个关键点
     *
     * @param queryPoints 查询点集合
     * @param trainPoints 训练点集合
     * @return 一个列表，包含了与queryPoints中的每个关键点的描述子最近的2个来自trainPoints的关键点
     */
    public static ArrayList<Match> findMatches(ArrayList<KeyPointX> queryPoints, ArrayList<KeyPointX> trainPoints) {
        if (trainPoints.size() < 2)
            throw new TooFewElementsException("trainPoints has to contain at least 2 elements.");

        ArrayList<Match> matches = new ArrayList<>(queryPoints.size());
        for (int i = 0; i < queryPoints.size(); i++) {
            KeyPointX queryPt = queryPoints.get(i);

            // 初始化
            double dist1 = queryPt.featureEuclidDist(trainPoints.get(0)), dist2 = queryPt.featureEuclidDist(trainPoints.get(1)); // 与pt2中前2个点的距离
            boolean reverseFlag = dist1 > dist2;
            double nearestDist = Math.min(dist1, dist2), secNearestDist = Math.max(dist1, dist2);
            Match match = new Match(queryPt, reverseFlag ? trainPoints.get(1) : trainPoints.get(0), reverseFlag ? trainPoints.get(0) : trainPoints.get(1),
                    i, reverseFlag ? 1 : 0, reverseFlag ? 0 : 1);

            for (int j = 2; j < trainPoints.size(); j++) {
                KeyPointX trainPt = trainPoints.get(j);
                double dist = queryPt.featureEuclidDist(trainPt);
                if (dist < nearestDist) {
                    secNearestDist = nearestDist;
                    nearestDist = dist;
                    match.trainPt2 = match.trainPt1;
                    match.trainPt1 = trainPt.deepCopy();
                    match.trainIdx2 = match.trainIdx1;
                    match.trainIdx1 = j;
                } else if (dist < secNearestDist) { // dist >= nearestDist
                    secNearestDist = dist;
                    match.trainPt2 = trainPt.deepCopy();
                    match.trainIdx2 = j;
                } // dist >= secNearestDist时，什么也不做
            }

            matches.add(match);
        }

        return matches;
    }

    /**
     * 筛选有效的配对
     *
     * @param matches 待筛选的配对列表
     * @return 经过筛选后留下的配对
     */
    public static ArrayList<Match> filterMatches(ArrayList<Match> matches) {
        ArrayList<Match> tmp = new ArrayList<>(), filteredMatches = new ArrayList<>();
        for (Match match : matches) {
            if (match.isGood()) tmp.add(match);
        }

        if (tmp.size() < GOOD_LOWER_BOUND) return null; // 如果找不到足够的配对，则返回空引用

        if (tmp.size() > GOOD_UPPER_BOUND) Collections.sort(tmp); // 如果配对数过多，则保留最有效的若干配对
        for (int i = 0; i < GOOD_UPPER_BOUND; i++) filteredMatches.add(tmp.get(i));

        return filteredMatches;
    }

    /**
     * 在控制台打印单应矩阵
     *
     * @param H 单应矩阵
     */
    public static void printH(Mat H) {
        System.out.print("[");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print(H.get(i, j)[0]);
                if (j < 2) System.out.print(", ");
            }
            if (i < 2) System.out.println();
            else System.out.println("]");
        }
    }
}

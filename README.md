## 介绍

Java实现的SIFT算法（[Lowe 2004] Distinctive Image Features from Scale-Invariant Keypoints）。

## 配置环境

### 运行环境

经测试，本仓库可以在下述环境运行：

- Windows 10 22H2
- IntelliJ IDEA 2023.1.2
- JDK 14.0.2

### 依赖项

- jblas 1.2.4 [mikiobraun/jblas @ GitHub](https://jblas.org/)
- OpenCV 4.7.0 [OpenCV - Open Computer Vision Library](https://opencv.org/)

## 使用方法

1. 下载依赖项，并包含到项目中。你可能还需要将OpenCV/build/java下的动态链接库添加到Java库路径。

2. 将本仓库中的代码包含到你的项目中。

3. 如要检测关键点和计算描述子，需要先将图像转为[0,1]范围内的灰度图，然后运行以下代码：

   ```java
   SIFT sift = new SIFT(grayFloat);
   ArrayList<KeyPointX> keyPointsWithDescriptor = sift.run();
   ```

   返回的`KeyPointX`对象具有CV`KeyPoint`类型的关键点，以及jblas`FloatMatrix`类型的描述子。

4. 要将关键点可视化，需要从`KeyPointX`中取出`KeyPoint`对象，组织成`ArrayList`，然后调用`Visualization.visualize()`：

   ```java
   ArrayList<KeyPoint> keyPoints = new ArrayList<>(keyPointsWithDescriptor.size());
   for (KeyPointX keyPointX : keyPointsWithDescriptor)
   	keyPoints.add(keyPointX.keyPoint);
   Mat imageWithMark = Visualization.visualize(image, keyPoints, true, true);
   imshow("Image with Mark", imageWithMark);
   waitKey();
   ```

5. 要向文件写入关键点和描述子，可以调用`IOUtil`的`writeKeyPointXes()`和`readKeyPointXes()`方法：

   ```java
   String filePath = "KeyPointList.dat";
   IOUtil.writeKeyPointXes(keyPointsWithDescriptor, filePath, false);
   ArrayList<KeyPointX> recoveredList = IOUtil.readKeyPointXes(filePath);
   ```

### 测试样例

example文件夹下有“book1.jpg”和“book2.jpg”两张测试图片：

![book1](example/book1.jpg) ![book2](example/book2.jpg)

在测试图片上检测关键点并可视化，可以得到如下结果：

![book1 sift](example/book1 sift.jpg) ![book2 sift](example/book2 sift.jpg)

检测到的关键点和描述子可以用于下游操作。如使用OpenCV计算单应矩阵，并进行图像拼接，可以得到类似下图的结果：

![stitch](example/stitch.jpg)

## 目前已知的问题

- 在“精确关键点定位”一步中，被剔除的关键点较多，致使剩余的关键点较少。可能是在将图像归一化到[0,1]范围时操作不当导致的。
- 该实现的鲁棒性不是很高，当图像发生角度较大的透视变换时，一些关键点可能无法被检测到，并且在一些关键点上发现了方向反转的问题。

## 备注

### 关于`KeyPoint`各个字段的意义

本实现中，`KeyPoint`各个字段的意义和OpenCV惯例略有不同，现列出本实现中各个字段的意义：

- `pt` - 关键点在图像中的局部坐标，没有与输入图像或“base image”对齐。例如，假设输入图像的尺寸为`w*h`，则下一octave中的关键点的坐标在`[0,w/2]`以及`[0,h/2]`范围内。
- `size` - 关键点在整个尺度空间中的全局尺度。
- `angle` - 关键点的梯度方向；以角度制表示，取值范围(0,360°)。一个关键点可能对应了多个方向，与关键点周围像素的梯度方向有关。
- `response` - 关键点的DoG响应。
- `octave` - 关键点所属octave的序号，从0开始。
- `class_id` - 未使用的字段，为默认值-1。
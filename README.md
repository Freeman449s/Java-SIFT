## 介绍

Java实现的SIFT算法（[Lowe 2004] Distinctive Image Features from Scale-Invariant Keypoints）。

## 目前已知的问题

在“精确关键点定位”一步中，被剔除的关键点较多，致使剩余的关键点较少。

## 备注

### 关于`KeyPoint`各个字段的意义

本实现中，`KeyPoint`各个字段的意义和OpenCV惯例略有不同：

- `pt` - 关键点在图像中的局部坐标
- `size` - 关键点在整个尺度空间中的全局尺度
- `octave` - 关键所属octave的序号，从0开始
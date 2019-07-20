# video

#### 介绍
这是关于video学习的，之后可以以这个为素材做一门课


# 第一章 - 基本的video api接口

### 1.1 MediaMetadataRetriever

### 1.2 MediaExtractor




# 第二章 - Video播放

### 1.1 普通播放
testVideoPlay
用decoder给video extract出来，然后输出到surfaceView的surface上，要注意控制食品的播放速度，里面有个SpeedController
另外，不支持视频缩放，所有的视频都是竖屏播放，对于横屏的视频会拉伸。


### 1.2 视频横竖屏自适应
testVideoPlay2
绘制到pbuffer上，但是主屏幕上没显示，另外支持保存为图片


### 1.2 循环播放

### 1.3 加速播放


### 1.4 视频处理播放
视频加一些滤镜播放


### 1.5 断点播放
支持暂停和继续



### 1.6 支持time ranged操作
比如某一段循环


### 1.7 加速播放
支持某一段加速


### 1.8 overlay
视频上加一些东西，比如加水印logo


### 1.9 关于声音合成的
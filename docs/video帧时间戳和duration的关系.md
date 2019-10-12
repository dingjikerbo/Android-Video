
我们用MediaExtractor去getSampleTime，这个时间戳和video的duration相比，可能大于，也可能小于等于，所以我们不能给这个当做是判断video最后一帧的工具。

比如

[platform-based-generated.mp4](../resources/platform-based-generated.mp4)，最后一帧的时间戳就比duration小，duration是1400ms，但是最后一帧是1350ms。

[ffmpeg-muxer-generated.mp4](../resources/ffmpeg-muxer-generated.mp4)，最后一帧的时间戳就比duration大，duration是1300ms，但是最后一帧是1350ms。

这两个video是怎么生成的呢，他们都是来自于同一个video，只不过上面那个video是由Android原生muxer来合成的，下面那个video是由FFMpeg来合成的。所以我们看到，不同的Muxer可能合成的video的duration会不一样。

另外，我们上面提到的video最后一帧指的是数据帧，而不包括EOF帧。我们怎么判断是最后一个数据帧呢，不能用这一帧的sampleTime和video的duration对比，我暂时没找到好的办法，我们倒是能判断是不是EOF帧，因为sampleSize和sampleFlags还有sampleTime都是-1.
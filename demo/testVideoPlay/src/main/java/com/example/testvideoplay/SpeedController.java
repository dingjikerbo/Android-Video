package com.example.testvideoplay;

public class SpeedController {

    private static final long ONE_MILLION = 1000000;

    private long mPrevPresentUsec;
    private long mPrevMonoUsec;
    private long mFixedFrameDurationUsec;

    public void setFixedPlaybackRate(int fps) {
        mFixedFrameDurationUsec = ONE_MILLION / fps;
    }

    public void waiting(long presentationTimeUsec) {
        if (mPrevMonoUsec == 0) {
            mPrevMonoUsec = System.nanoTime() / 1000;
            mPrevPresentUsec = presentationTimeUsec;
        } else {
            long frameDelta;

            // 有两种模式，
            // 第一种按固定的速度，忽略视频帧自带的presentationTime
            // 第二种按视频帧的presentationTime
            if (mFixedFrameDurationUsec != 0) {
                frameDelta = mFixedFrameDurationUsec;
            } else {
                frameDelta = presentationTimeUsec - mPrevPresentUsec;
            }

            if (frameDelta < 0) {
                frameDelta = 0;
            } else if (frameDelta == 0) {

            } else if (frameDelta > 10 * ONE_MILLION) {
                // 如果这个值大于10s那就太长了，缩短到5s
                frameDelta = 5 * ONE_MILLION;
            }

            long desiredUsec = mPrevMonoUsec + frameDelta;
            long nowUsec = System.nanoTime() / 1000;

            while (nowUsec < desiredUsec - 100) {
                long sleepTimeUsec = desiredUsec - nowUsec;

                // 为了保证能及时响应暂停播放，这里每隔0.5s就唤醒一下
                if (sleepTimeUsec > 500000) {
                    sleepTimeUsec = 500000;
                }

                try {
                    Thread.sleep(sleepTimeUsec / 1000, (int) (sleepTimeUsec % 1000) * 1000);
                } catch (InterruptedException e) {}

                nowUsec = System.nanoTime() / 1000;
            }

            mPrevMonoUsec += frameDelta;
            mPrevPresentUsec += frameDelta;
        }
    }
}

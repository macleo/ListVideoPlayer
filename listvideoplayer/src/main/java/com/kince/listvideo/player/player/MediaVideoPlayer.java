package com.kince.listvideo.player.player;

import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import android.view.TextureView;

import com.kince.listvideo.player.state.VideoPlayerState;

/**
 * Created by Kince
 *
 * 继承此类时，如果想要获取当前正在播放的视频url，请用如下方式获取：
 * {@link VideoPlayerManager#getVideoUrl()}
 * 因为此处的{@link #mUrl} 在开启缓存时是视频的缓存代理地址，不是用户播放视频时所传入的地址
 *
 */
public class MediaVideoPlayer extends AbsBaseVideoPlayer implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener {

    private static final String TAG = "VideoMediaPlayer";

    private static final int MSG_PREPARE = 1;
    private static final int MSG_RELEASE = 2;

    private MediaPlayer mMediaPlayer;
    private HandlerThread mMediaHandlerThread;
    private MediaHandler mMediaHandler;

    public MediaVideoPlayer() {
        this(false);
    }

    public MediaVideoPlayer(boolean enableLog) {
        mMediaPlayer = new MediaPlayer();
        mMediaHandlerThread = new HandlerThread(TAG);
        mMediaHandlerThread.start();
        mMediaHandler = new MediaHandler(mMediaHandlerThread.getLooper());
        mEnableLog = enableLog;
    }

    @Override
    public void setTextureView(TextureView textureView) {
        if (textureView == null && mSurfaceTexture != null) {
            mSurfaceTexture.release();
        }
        super.setTextureView(textureView);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        try {
            mMediaPlayer.setSurface(new Surface(surface));
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onSurfaceTextureAvailable(surface, width, height);
    }

    class MediaHandler extends Handler {

        public MediaHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_RELEASE:
                    mMediaPlayer.release();
                    break;
                case MSG_PREPARE:
                    try {
                        mMediaPlayer.release();
                        mMediaPlayer = new MediaPlayer();
                        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mMediaPlayer.setOnPreparedListener(MediaVideoPlayer.this);
                        mMediaPlayer.setOnCompletionListener(MediaVideoPlayer.this);
                        mMediaPlayer.setOnBufferingUpdateListener(MediaVideoPlayer.this);
                        mMediaPlayer.setScreenOnWhilePlaying(true);
                        mMediaPlayer.setOnSeekCompleteListener(MediaVideoPlayer.this);
                        mMediaPlayer.setOnErrorListener(MediaVideoPlayer.this);
                        mMediaPlayer.setOnInfoListener(MediaVideoPlayer.this);
                        mMediaPlayer.setDataSource(mUrl);
                        mMediaPlayer.prepareAsync();
                        mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    @Override
    protected void prepare() {
        mMediaHandler.obtainMessage(MSG_PREPARE).sendToTarget();
    }

    @Override
    public void start(String url) {
        mUrl = url;
    }

    @Override
    public void play() {
        mMediaPlayer.start();
    }

    @Override
    public void pause() {
        if (getPlayerState() == VideoPlayerState.STATE_PLAYING) {
            mMediaPlayer.pause();
        }
    }

    /**
     * 恢复播放
     */
    @Override
    public void resume() {
        mMediaPlayer.start();
    }

    @Override
    public void stop() {
        mMediaHandler.obtainMessage(MSG_RELEASE).sendToTarget();
    }


    /**
     * 重置播放器
     */
    @Override
    public void reset() {

    }

    @Override
    public void release() {
        mMediaHandler.obtainMessage(MSG_RELEASE).sendToTarget();
    }

    @Override
    public void setPlayerState(int state) {
        mState = state;
    }

    @Override
    public int getPlayerState() {
        return mState;
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    @Override
    public void seekTo(int position) {
        mMediaPlayer.seekTo(position);
    }

    /**
     * 设置音量
     *
     * @param volume
     */
    @Override
    public void setVolume(int volume) {

    }

    /**
     * 获取当前音量
     *
     * @return
     */
    @Override
    public int getVolume() {
        return 0;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if (mPlayCallback != null && isPlaying()) {
            mPlayCallback.onPlayStateChanged(VideoPlayerState.STATE_PLAYING);
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mPlayCallback != null) {
            mPlayCallback.onComplete();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (mPlayCallback != null) {
            mPlayCallback.onError("Play error, what=" + what + ", extra=" + extra);
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mPlayCallback != null) {
            mPlayCallback.onDurationChanged(mp.getDuration());
            mPlayCallback.onPlayStateChanged(VideoPlayerState.STATE_PLAYING);
        }
        play();
    }

}

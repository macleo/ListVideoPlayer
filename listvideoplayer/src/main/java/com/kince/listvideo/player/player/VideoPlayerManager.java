package com.kince.listvideo.player.player;

import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.ViewGroup;

import com.kince.listvideo.player.config.VideoPlayerConfig;
import com.kince.listvideo.player.message.BackPressedMessage;
import com.kince.listvideo.player.message.DurationMessage;
import com.kince.listvideo.player.message.Message;
import com.kince.listvideo.player.message.UIStateMessage;
import com.kince.listvideo.player.state.VideoPlayerState;
import com.kince.listvideo.player.state.ScreenViewState;
import com.kince.listvideo.player.utils.Utils;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Kince
 *
 * 视频播放管理类，主要与视频展示展示UI进行交互，视频播放的具体操作交
 * 由播放器抽象类{@link AbsBaseVideoPlayer}实现
 * 通过此管理类达到视频播放控制与UI层的解耦，同时便于自定义播放器
 *
 */
public final class VideoPlayerManager implements IVideoPlayer.PlayCallback {
    private static final String TAG = "PlayerManager";

    private static volatile VideoPlayerManager sVideoPlayerManager;

    // 播放器实例
    private AbsBaseVideoPlayer mPlayer;
    // 播放状态观察者
    private PlayStateObservable mPlayStateObservable;
    // 当前播放地址
    private String mVideoUrl;
    private int mObserverHash = -1;
    // 当前模仿窗口模式
    private int mScreenState = ScreenViewState.SCREEN_STATE_NORMAL;
    // 播放相关配置
    private VideoPlayerConfig mVideoPlayerConfig;

    /**
     * 传入播放器配置
     *
     * @param videoPlayerConfig
     */
    private VideoPlayerManager(VideoPlayerConfig videoPlayerConfig) {
        mVideoPlayerConfig = videoPlayerConfig;
        createPlayer();
        mPlayStateObservable = new PlayStateObservable();
    }

    /**
     * 获取实例，全局有且只有一个
     *
     * @return
     */
    public static VideoPlayerManager getInstance() {
        if (sVideoPlayerManager == null) {
            synchronized (VideoPlayerManager.class) {
                if (sVideoPlayerManager == null) {
                    //加载默认配置
                    loadConfig(new VideoPlayerConfig.Builder().build());
                }
            }
        }
        if (sVideoPlayerManager.mPlayer == null) {
            synchronized (VideoPlayerManager.class) {
                if (sVideoPlayerManager.mPlayer == null) {
                    sVideoPlayerManager.createPlayer();
                }
            }
        }
        return sVideoPlayerManager;
    }

    /**
     * 创建播放器实例
     */
    private void createPlayer() {
        mPlayer = mVideoPlayerConfig.getPlayerFactory().create();
        mPlayer.setPlayCallback(this);
    }

    public VideoPlayerConfig getConfig() {
        return mVideoPlayerConfig;
    }

    /**
     * 加载配置
     *
     * @param videoPlayerConfig
     */
    public static void loadConfig(VideoPlayerConfig videoPlayerConfig) {
        if (sVideoPlayerManager == null) {
            sVideoPlayerManager = new VideoPlayerManager(videoPlayerConfig);
        }
    }

    public void removeTextureView() {
        if (mPlayer.mTextureView != null &&
                mPlayer.mTextureView.getParent() != null) {
            ((ViewGroup) mPlayer.mTextureView.getParent()).removeView(mPlayer.mTextureView);
            setTextureView(null);
            if (mPlayer.mTextureView != null) {
                Utils.log("remove TextureView:" + mPlayer.mTextureView.toString());
            }
        }
    }

    public void setTextureView(TextureView textureView) {
        if (textureView != null) {
            Utils.log("set TextureView:" + textureView.toString());
        }
        mPlayer.setTextureView(textureView);
    }

    /**
     * 待播放视频是否已经缓存
     *
     * @param videoUrl
     * @return
     */
    public boolean isCached(String videoUrl) {
        if (mVideoPlayerConfig.isCacheEnable() && mVideoPlayerConfig.getCacheProxy().isCached(videoUrl)) {
            return true;
        }
        return false;
    }

    /**
     * 获取正在播放的视频地址，必须在stop或release方法调用之前获取
     *
     * @return
     */
    public String getVideoUrl() {
        return mVideoUrl;
    }

    public void start(String url, int observerHash) {
        bindPlayerView(url, observerHash);

        onPlayStateChanged(VideoPlayerState.STATE_LOADING);
        Utils.log(String.format("start loading video, hash=%d, url=%s", mObserverHash, mVideoUrl));
        String wrapperUrl = url;
        if (mVideoPlayerConfig.isCacheEnable()) {
            wrapperUrl = mVideoPlayerConfig.getCacheProxy().getProxyUrl(url);
        }
        mPlayer.start(wrapperUrl);
    }

    void bindPlayerView(String url, int observerHash) {
        this.mVideoUrl = url;
        this.mObserverHash = observerHash;
    }

    public void play() {
        Utils.log(String.format("play video, hash=%d, url=%s", mObserverHash, mVideoUrl));
        mPlayer.play();
        onPlayStateChanged(VideoPlayerState.STATE_PLAYING);
    }

    public void resume() {
        if (getState() == VideoPlayerState.STATE_PAUSE) {
            Utils.log(String.format("resume video, hash=%d, url=%s", mObserverHash, mVideoUrl));
            play();
        }
    }

    public void pause() {
        if (getState() == VideoPlayerState.STATE_PLAYING) {
            Utils.log(String.format("pause video, hash=%d, url=%s", mObserverHash, mVideoUrl));
            mPlayer.pause();
            onPlayStateChanged(VideoPlayerState.STATE_PAUSE);
        } else {
            Utils.log(String.format("pause video for state: %d, hash=%d, url=%s", getState(), mObserverHash, mVideoUrl));
        }
    }

    public void stop() {
        Utils.log(String.format("stop video, hash=%d, url=%s", mObserverHash, mVideoUrl));
        onPlayStateChanged(VideoPlayerState.STATE_NORMAL);
        mPlayer.stop();
        removeTextureView();
        mObserverHash = -1;
        mVideoUrl = null;
        mScreenState = ScreenViewState.SCREEN_STATE_NORMAL;
    }

    public void release() {
        Utils.log("release player");
        mPlayer.setPlayerState(VideoPlayerState.STATE_NORMAL);
        removeTextureView();
        mPlayer.release();
        mPlayer = null;
        mObserverHash = -1;
        mVideoUrl = null;
        mScreenState = ScreenViewState.SCREEN_STATE_NORMAL;
    }

    /**
     * 设置是否自动播放
     * @param auto true则自动播放。
     */
    public void setAutoPlay(boolean auto){

    }

    /**
     *
     * @param auto
     */
    public void setRepeat(boolean auto){

    }

    /**
     * 设置倍速播放速度
     * @param speed 倍速值。范围0.5~2。
     */
    public void setPlaySpeed(float speed){

    }

    /**
     * 设置边播边缓存的配置
     * @param enable 是否可以边播边存。如果为true，则根据后面的几个参数决定是否能够缓存。
     * @param saveDir 缓存的目录（绝对路径）
     * @param maxDuration 能缓存的单个视频最大长度（单位：秒）。如果单个视频超过这个值，就不缓存。
     * @param maxSize 缓存目录的所有缓存文件的总的最大大小（单位：MB）。如果超过则删除最旧文件，如果还是不够，则不缓存。
     */
    public void setPlayingCache(boolean enable, String saveDir, int maxDuration, long maxSize){

    }

    /**
     * 设置网络超时时间
     * @param mstimeout 超时时间，单位：ms
     */
    public void setNetworkTimeout(int mstimeout){

    }

    /**
     * 界面上是否存在视频播放
     *
     * @return
     */
    public boolean hasViewPlaying() {
        return mObserverHash != -1;
    }

    public boolean onBackPressed() {
        boolean consume = ScreenViewState.isNormal(mScreenState);
        if (consume == false) {
            mPlayStateObservable.notify(new BackPressedMessage(mScreenState, mObserverHash, mVideoUrl));
            return true;
        }
        return false;
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    /**
     * 指定View是否在播放视频
     *
     * @param viewHash
     * @return
     */
    public boolean isViewPlaying(int viewHash) {
        return mObserverHash == viewHash;
    }

    public int getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    public void seekTo(int position) {
        if (isPlaying()) {
            onPlayStateChanged(VideoPlayerState.STATE_PLAYING_BUFFERING_START);
        }
        mPlayer.seekTo(position);
    }

    public int getState() {
        return sVideoPlayerManager.mPlayer.getPlayerState();
    }

    @Override
    public void onError(String error) {
        Utils.log("error video, error= " + error == null ? "null" : error + ", url=" + mVideoUrl);
        if (!TextUtils.isEmpty(error)) {
            Log.d(TAG, error);
        }
        mPlayer.stop();
        changeUIState(VideoPlayerState.STATE_ERROR);
    }

    @Override
    public void onComplete() {
        changeUIState(VideoPlayerState.STATE_AUTO_COMPLETE);
    }

    @Override
    public void onPlayStateChanged(int state) {
        changeUIState(state);
    }

    @Override
    public void onDurationChanged(int duration) {
        mPlayStateObservable.notify(new DurationMessage(mObserverHash, mVideoUrl, duration));
    }

    public void addObserver(Observer observer) {
        mPlayStateObservable.addObserver(observer);
    }

    public void removeObserver(Observer observer) {
        mPlayStateObservable.deleteObserver(observer);
    }

    private void changeUIState(int state) {
        mPlayer.setPlayerState(state);
        mPlayStateObservable.notify(new UIStateMessage(mObserverHash, mVideoUrl, state));
    }

    public void setScreenState(int screenState) {
        mScreenState = screenState;
    }

    class PlayStateObservable extends Observable {

        private void setObservableChanged() {
            this.setChanged();
        }

        public void notify(Message message) {
            setObservableChanged();
            notifyObservers(message);
        }
    }

}

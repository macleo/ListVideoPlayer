package com.kince.listvideo.player.player;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;

import com.kince.listvideo.player.state.VideoPlayerState;

/**
 * Created by Kince
 * 基类播放器
 */
public abstract class AbsBaseVideoPlayer implements IVideoPlayer, TextureView.SurfaceTextureListener {

    /**
     * 准备播放
     */
    protected abstract void prepare();
    /**
     * 播放器状态回调
     */
    protected int mState = VideoPlayerState.STATE_NORMAL;
    protected String mUrl;
    protected PlayCallback mPlayCallback;
    /**
     * 配合播放器使用
     */
    protected TextureView mTextureView;
    protected SurfaceTexture mSurfaceTexture;
    // 是否开启日志
    protected boolean mEnableLog;

    /**
     * Invoked when a {@link TextureView}'s SurfaceTexture is ready for use.
     *
     * @param surface The surface returned by
     *                {@link TextureView#getSurfaceTexture()}
     * @param width   The width of the surface
     * @param height  The height of the surface
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mEnableLog) {
            Log.i("ListVideoPlayer", "AbsBaseVideoPlayer onSurfaceTextureAvailable");
        }
        if (mSurfaceTexture == null && (getState() == VideoPlayerState.STATE_NORMAL || getState() == VideoPlayerState.STATE_LOADING)) {
            prepare();
        }
        mSurfaceTexture = surface;
    }

    /**
     * Invoked when the {@link SurfaceTexture}'s buffers size changed.
     *
     * @param surface The surface returned by
     *                {@link TextureView#getSurfaceTexture()}
     * @param width   The new width of the surface
     * @param height  The new height of the surface
     */
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (mEnableLog) {
            Log.i("ListVideoPlayer", "AbsBaseVideoPlayer onSurfaceTextureSizeChanged");
        }
    }

    /**
     * Invoked when the specified {@link SurfaceTexture} is about to be destroyed.
     * If returns true, no rendering should happen inside the surface texture after this method
     * is invoked. If returns false, the client needs to call {@link SurfaceTexture#release()}.
     * Most applications should return true.
     *
     * @param surface The surface about to be destroyed
     */
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mEnableLog) {
            Log.i("ListVideoPlayer", "AbsBaseVideoPlayer onSurfaceTextureDestroyed");
        }
        return false;
    }

    /**
     * Invoked when the specified {@link SurfaceTexture} is updated through
     * {@link SurfaceTexture#updateTexImage()}.
     *
     * @param surface The surface just updated
     */
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        if (mEnableLog) {
            Log.i("ListVideoPlayer", "AbsBaseVideoPlayer onSurfaceTextureUpdated");
        }
    }

    /**
     * 当前是否正在播放
     *
     * @return boolean
     */
    @Override
    public boolean isPlaying() {
        if (mEnableLog) {
            Log.i("ListVideoPlayer", "AbsBaseVideoPlayer isPlaying");
        }
        return (getState() == VideoPlayerState.STATE_PLAYING ||
                getState() == VideoPlayerState.STATE_PLAYING_BUFFERING_START) &&
                getCurrentPosition() < getDuration();
    }

    /**
     * 设置播放回调函数
     *
     * @param playCallback
     */
    @Override
    public void setPlayCallback(PlayCallback playCallback) {
        if (mEnableLog) {
            Log.i("ListVideoPlayer", "AbsBaseVideoPlayer setPlayCallback");
        }
        mPlayCallback = playCallback;
    }

    public void setEnableLog(boolean mEnableLog) {
        this.mEnableLog = mEnableLog;
    }

    /**
     * @param textureView
     */
    @Override
    public void setTextureView(TextureView textureView) {
        if (mEnableLog) {
            Log.i("ListVideoPlayer", "AbsBaseVideoPlayer setTextureView");
        }
        if (mTextureView != null) {
            mTextureView.setSurfaceTextureListener(null);
        }
        mSurfaceTexture = null;
        mTextureView = textureView;
        if (textureView != null) {
            mTextureView.setSurfaceTextureListener(this);
        }
    }

}
package com.minigame.androiddemo;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.minigame.host.HostHandle;
import com.minigame.host.InitOption;
import com.minigame.host.MiniGameSDK;

public final class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private HostHandle host;
    private final Activity activity;

    public GameSurfaceView(Activity activity) {
        super(activity);
        this.activity = activity;
        getHolder().addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        InitOption option = new InitOption.Builder(getContext()).build();

        host = MiniGameSDK.getInstance().initialize(holder.getSurface(), activity, option);

        if (host != null) {
            String codeDir = getContext().getFilesDir().getAbsolutePath() + "/minigame";
            host.startGame(codeDir, "game.js");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (host != null) {
            host.updateSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (host != null) {
            host.destroy();
            host = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (host != null) {
            host.onTouch(event);
            return true;
        }
        return false;
    }

    public void onHostShow() {
        if (host != null) {
            host.onShow();
        }
    }

    public void onHostHide() {
        if (host != null) {
            host.onHide();
        }
    }

    public void release() {
        if (host != null) {
            host.destroy();
            host = null;
        }
    }
}

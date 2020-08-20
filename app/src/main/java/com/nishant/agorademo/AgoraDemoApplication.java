package com.nishant.agorademo;

import android.app.Application;
import android.util.Log;

import com.nishant.agorademo.handler.EventHandler;
import com.nishant.agorademo.handler.RTCEventHandler;

import io.agora.AgoraAPIOnlySignal;
import io.agora.rtc.RtcEngine;

public class AgoraDemoApplication extends Application {

    private final String TAG = AgoraDemoApplication.class.getSimpleName();
    private RTCEventHandler mHandler = new RTCEventHandler();

    private static AgoraDemoApplication mInstance;
    private AgoraAPIOnlySignal mAgoraAPI;
    private RtcEngine mRtcEngine;

    public static AgoraDemoApplication getInstance() {
        return mInstance;
    }

    public AgoraDemoApplication() {
        mInstance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setupAgoraEngine();
    }

    public AgoraAPIOnlySignal getAgoraAPI() {
        return mAgoraAPI;
    }

    public RtcEngine rtcEngine() {
        return mRtcEngine;
    }

    public void registerEventHandler(EventHandler handler) {
        mHandler.initHandler(handler);
    }

    private void setupAgoraEngine() {
        String appID = getString(R.string.agora_app_id);

        try {
            mAgoraAPI = AgoraAPIOnlySignal.getInstance(this, appID);
            mRtcEngine = RtcEngine.create(getBaseContext(), appID, mHandler);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));

            throw new RuntimeException("Check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }
}

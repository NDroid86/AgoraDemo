package com.nishant.agorademo.handler;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcChannel;
import io.agora.rtc.models.UserInfo;

public class RTCEventHandler extends IRtcEngineEventHandler {

    private EventHandler handler = null;

    public void initHandler(EventHandler mHandler) {
        this.handler = mHandler;
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        handler.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        handler.onJoinChannelSuccess(channel, uid, elapsed);
    }

    @Override
    public void onLeaveChannel(RtcStats rtcStats) {
        handler.onLeaveChannel(rtcStats);
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        handler.onUserOffline(uid, reason);
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        handler.onUserJoined(uid, elapsed);
    }

    @Override
    public void onStreamPublished(String url, int error) {
        handler.onStreamPublished(url, error);
    }

    @Override
    public void onUserInfoUpdated(int i, UserInfo userInfo) {
        handler.onUserInfoUpdated(i, userInfo);
    }

    @Override
    public void onUserMuteVideo(int uid, boolean muted) {
        handler.onUserMuteVideo(uid, muted);
    }

    @Override
    public void onRemoteVideoStats(RemoteVideoStats remoteVideoStats) {
        handler.onRemoteVideoStats(remoteVideoStats);
    }

    @Override
    public void onRtmpStreamingStateChanged(String url, int state, int errorCode) {
        handler.onRtmpStreamingStateChanged(url, state, errorCode);
    }
}

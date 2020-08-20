package com.nishant.agorademo.handler;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcChannel;
import io.agora.rtc.models.UserInfo;

public interface EventHandler {
    void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed);

    void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats);

    void onJoinChannelSuccess(String channel, int uid, int elapsed);

    void onUserOffline(int uid, int reason);

    void onUserJoined(int uid, int elapsed);

    void onStreamPublished(String s, int i);

    void onUserInfoUpdated(int i, UserInfo userInfo);

    void onUserMuteVideo(final int uid, final boolean muted);

    void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats);

    void onRtmpStreamingStateChanged(String url, int state, int errCode);
}

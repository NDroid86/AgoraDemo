package com.nishant.agorademo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.lifecycle.Observer;

import com.nishant.agorademo.handler.EventHandler;
import com.nishant.agorademo.model.AgoraUserInfo;
import com.nishant.agorademo.utils.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcChannel;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.live.LiveTranscoding;
import io.agora.rtc.models.UserInfo;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;


public class VideoActivity extends BaseVideoActivity implements EventHandler {
    private int clientRole = Constants.CLIENT_ROLE_BROADCASTER;
    private int channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
    private static final int PERMISSION_REQ_ID = 1111;
    private TextView txt_viewers_count = null;
    private SurfaceView selfView;
    private int selfuid;
    private Map<Integer, AgoraUserInfo> agoraUsers = new HashMap<>();
    private LiveTranscoding mLiveTranscoding;

    private void updateViews() {
        viewersListener.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer value) {
                txt_viewers_count.setText(String.format("%s", value));
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        txt_viewers_count = (TextView) findViewById(R.id.txt_viewers_count);
        registerRtcEventHandler(this);
        updateViews();

        mPublishUrl = getString(R.string.stream_url);

        if (checkSelfPermissions()) {
            initAgoraEngineAndJoinChannel();
        }
    }

    private void initAgoraEngineAndJoinChannel() {
        rtcEngine().setChannelProfile(channelProfile);
        rtcEngine().setClientRole(clientRole);
        setupVideoProfile();
        setupLocalVideo();
        joinChannel();
        //initTranscoding(480, 640, 1800);
        //setTranscoding();
    }


    public void onLocalAudioMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        rtcEngine().muteLocalAudioStream(iv.isSelected());
    }

    private void onRemoteUserVideoMuted(int uid, boolean muted) {
        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);
        SurfaceView surfaceView = (SurfaceView) container.getChildAt(0);
        Object tag = surfaceView.getTag();
        if (tag != null && (Integer) tag == uid) {
            surfaceView.setVisibility(muted ? View.GONE : View.VISIBLE);
        }
    }


    public void onLocalVideoMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        rtcEngine().muteLocalVideoStream(iv.isSelected());

        FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);
        SurfaceView surfaceView = (SurfaceView) container.getChildAt(0);
        surfaceView.setZOrderMediaOverlay(!iv.isSelected());
        surfaceView.setVisibility(iv.isSelected() ? View.GONE : View.VISIBLE);
    }

    private void setupRemoteVideo(int uid) {
        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        container.addView(surfaceView);
        rtcEngine().setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid));
    }

    protected void setupLocalVideo() {
        FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        surfaceView.setZOrderMediaOverlay(true);
        container.addView(surfaceView);
        rtcEngine().setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0));
    }

    protected void setupVideoProfile() {
        rtcEngine().enableVideo();
        rtcEngine().setVideoEncoderConfiguration(new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_1280x720, VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    public void onSwitchCameraClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        rtcEngine().switchCamera();
    }

    public void onShareURLClicked(View view) {
        AlertDialog streamingURL = new AlertDialog.Builder(VideoActivity.this).create();
        streamingURL.setTitle("Waitroom URL:");
        streamingURL.setMessage(getString(R.string.stream_sharing_url));
        streamingURL.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        streamingURL.show();
    }

    public void onBeautifyClicked(View view) {

        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        rtcEngine().setBeautyEffectOptions(view.isActivated(), Constant.DEFAULT_BEAUTY_OPTIONS);
    }

    protected void onEndCallClicked(View view) {
        finish();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void askPermission() {
        requestPermissions(new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQ_ID);
    }

    private boolean checkSelfPermissions() {
        return checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID) &&
                checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID) &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_ID);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_ID) {

            boolean granted = true;
            for (int result : grantResults) {
                granted = (result == PackageManager.PERMISSION_GRANTED);
                if (!granted) break;
            }

            if (granted) {
                initAgoraEngineAndJoinChannel();
            } else {
                Toast.makeText(this, R.string.permissions_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            askPermission();
            return false;
        }
        return true;
    }

    @Override
    public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setupRemoteVideo(uid);
            }
        });
    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {
        rtcEngine().removePublishStreamUrl(mPublishUrl);
    }

    @Override
    public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                selfuid = uid;

                AgoraUserInfo mUser = new AgoraUserInfo();
                mUser.view = selfView;
                mUser.uid = selfuid;
                mUser.view.setZOrderOnTop(true);
                agoraUsers.put(selfuid, mUser);

                //setTranscoding();
                rtcEngine().addPublishStreamUrl(mPublishUrl, false);
            }
        });
    }

    @Override
    public void onUserOffline(int uid, int reason) {

    }

    @Override
    public void onUserJoined(final int uid, int elapsed) {
        Log.d(TAG, "UID: " + uid + " Elapsed: " + elapsed);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                AgoraUserInfo agoraUser = new AgoraUserInfo();
                agoraUser.view = RtcEngine.CreateRendererView(VideoActivity.this);
                agoraUser.uid = uid;
                agoraUser.view.setZOrderOnTop(true);
                agoraUsers.put(uid, agoraUser);
            }
        });
    }

    @Override
    public void onStreamPublished(String s, int i) {
        Log.d(TAG, "URL: " + s + " Error: " + i);
    }

    @Override
    public void onUserInfoUpdated(int i, UserInfo userInfo) {

    }

    @Override
    public void onUserMuteVideo(final int uid, final boolean muted) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onRemoteUserVideoMuted(uid, muted);
            }
        });
    }

    @Override
    public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {

    }

    @Override
    public void onRtmpStreamingStateChanged(String url, int state, int errCode) {
        Log.d(TAG, "URL: " + url + " Error: " + errCode);
    }

    public static ArrayList<AgoraUserInfo> getAgoraUsers(Map<Integer, AgoraUserInfo> userInfo) {
        ArrayList<AgoraUserInfo> users = new ArrayList<>();
        Iterator<Map.Entry<Integer, AgoraUserInfo>> iterator = userInfo.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, AgoraUserInfo> entry = iterator.next();
            AgoraUserInfo user = entry.getValue();
            users.add(user);
        }
        return users;
    }

    private void initTranscoding(int width, int height, int bitrate) {
        if (mLiveTranscoding == null) {
            mLiveTranscoding = new LiveTranscoding();
            mLiveTranscoding.width = width;
            mLiveTranscoding.height = height;
            mLiveTranscoding.videoBitrate = bitrate;
            mLiveTranscoding.videoFramerate = 15;
        }
    }

    private void setTranscoding() {
        ArrayList<LiveTranscoding.TranscodingUser> transcodingUsers;
        ArrayList<AgoraUserInfo> videoUsers = getAgoraUsers(agoraUsers);

        transcodingUsers = getTranscodedLayout(selfuid, videoUsers, mLiveTranscoding.width, mLiveTranscoding.height);

        mLiveTranscoding.setUsers(transcodingUsers);
        rtcEngine().setLiveTranscoding(mLiveTranscoding);
    }

    public static ArrayList<LiveTranscoding.TranscodingUser> getTranscodedLayout(int selfUid, ArrayList<AgoraUserInfo> hosts,
                                                                                 int canvasWidth,
                                                                                 int canvasHeight) {

        ArrayList<LiveTranscoding.TranscodingUser> users = new ArrayList<>(hosts.size());
        int index = 0;
        float xIndex, yIndex;
        int viewWidth;
        int viewHEdge;

        if (hosts.size() <= 1)
            viewWidth = canvasWidth;
        else
            viewWidth = canvasWidth / 2;

        if (hosts.size() <= 2)
            viewHEdge = canvasHeight;
        else
            viewHEdge = canvasHeight / ((hosts.size() - 1) / 2 + 1);

        LiveTranscoding.TranscodingUser user0 = new LiveTranscoding.TranscodingUser();
        user0.uid = selfUid;
        user0.alpha = 1;
        user0.zOrder = 0;
        user0.audioChannel = 0;

        user0.x = 0;
        user0.y = 0;
        user0.width = viewWidth;
        user0.height = viewHEdge;
        users.add(user0);

        index++;
        for (AgoraUserInfo entry : hosts) {
            if (entry.uid == selfUid)
                continue;

            xIndex = index % 2;
            yIndex = index / 2;
            LiveTranscoding.TranscodingUser user1 = new LiveTranscoding.TranscodingUser();
            user1.uid = entry.uid;
            user1.x = (int) ((xIndex) * viewWidth);
            user1.y = (int) (viewHEdge * (yIndex));
            user1.width = viewWidth;
            user1.height = viewHEdge;
            user1.zOrder = index + 1;
            user1.audioChannel = 0;
            user1.alpha = 1f;

            users.add(user1);
            index++;
        }

        return users;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rtcEngine().removePublishStreamUrl(mPublishUrl);
    }
}

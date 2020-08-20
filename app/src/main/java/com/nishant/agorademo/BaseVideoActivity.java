package com.nishant.agorademo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.nishant.agorademo.handler.EventHandler;
import com.nishant.agorademo.utils.Constant;
import com.nishant.agorademo.utils.ToastUtils;

import java.lang.ref.WeakReference;
import java.util.UUID;

import io.agora.AgoraAPI;
import io.agora.IAgoraAPI;
import io.agora.rtc.RtcEngine;

public class BaseVideoActivity extends AppCompatActivity {

    protected final String TAG = BaseVideoActivity.this.getClass().getName();
    protected String channelName = "driod86";
    protected int viewersCount = 0;
    protected String uid;
    protected SharedPreferences agoraPrefs = null;
    protected SharedPreferences.Editor agoraPrefsEditor = null;
    protected String mPublishUrl = "";
    protected MutableLiveData<Integer> viewersListener = new MutableLiveData<>();


    protected AgoraDemoApplication application() {
        return (AgoraDemoApplication) getApplication();
    }

    protected RtcEngine rtcEngine() {
        return application().rtcEngine();
    }

    protected void registerRtcEventHandler(EventHandler handler) {
        application().registerEventHandler(handler);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        agoraPrefs = getSharedPreferences("agoraPrefs", MODE_PRIVATE);
        agoraPrefsEditor = agoraPrefs.edit();
        addCallback();

        initLogin();
    }

    private void initLogin() {
        uid = agoraPrefs.getString(Constant.USER_ID, "");
        if (uid.equals("")) {
            uid = UUID.randomUUID().toString();
        }
        AgoraDemoApplication.getInstance().getAgoraAPI().login2(getString(R.string.agora_app_id), uid, "_no_need_token", 0, "", 5, 1);
    }

    private void initUI() {
        AgoraDemoApplication.getInstance().getAgoraAPI().channelJoin(channelName);
    }

    private void addCallback() {

        AgoraDemoApplication.getInstance().getAgoraAPI().callbackSet(new AgoraAPI.CallBack() {

            @Override
            public void onLoginSuccess(int i, int i1) {
                Log.i(TAG, "onLoginSuccess " + i + "  " + i1);
                initUI();
                agoraPrefsEditor.putString(Constant.USER_ID, uid);
                agoraPrefsEditor.commit();
            }

            @Override
            public void onLoginFailed(final int i) {
                Log.i(TAG, "onLoginFailed " + i);
            }

            @Override
            public void onError(String s, int i, String s1) {
                Log.i(TAG, "onError s:" + s + " s1:" + s1);
            }

            @Override
            public void onChannelJoined(String channelID) {
                super.onChannelJoined(channelID);

            }

            @Override
            public void onChannelJoinFailed(String channelID, int ecode) {
                super.onChannelJoinFailed(channelID, ecode);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.show(new WeakReference<Context>(BaseVideoActivity.this), getString(R.string.err_join_channe_failed));
                    }
                });
            }

            @Override
            public void onChannelUserList(String[] accounts, final int[] uids) {
                super.onChannelUserList(accounts, uids);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        viewersCount = uids.length;
                        viewersListener.setValue(viewersCount);
                    }
                });
            }

            @Override
            public void onLogout(final int i) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (i == IAgoraAPI.ECODE_LOGOUT_E_KICKED) {
                            ToastUtils.show(new WeakReference<Context>(BaseVideoActivity.this), "you are logedout.");
                        } else if (i == IAgoraAPI.ECODE_LOGOUT_E_NET) {
                            ToastUtils.show(new WeakReference<Context>(BaseVideoActivity.this), "You are loged out of network..");
                        }

                        finish();
                    }
                });

            }

            @Override
            public void onMessageInstantReceive(final String account, int uid, final String msg) {
            }

        });
    }

    protected void joinChannel() {
        rtcEngine().joinChannel(null, channelName, "Optional Data", 0);
    }

    protected void leaveChannel() {
        rtcEngine().removePublishStreamUrl(mPublishUrl);
        rtcEngine().leaveChannel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        addCallback();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    protected void logout() {
        AgoraDemoApplication.getInstance().getAgoraAPI().logout();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        leaveChannel();
        logout();
        RtcEngine.destroy();
    }
}

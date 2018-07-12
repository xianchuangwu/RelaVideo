package video.com.relavideodemo.surface;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.thel.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.microedition.khronos.egl.EGLContext;

import io.agora.live.LiveTranscoding;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.AgoraVideoFrame;
import io.agora.rtc.video.VideoCanvas;
import io.reactivex.functions.Consumer;
import video.com.relavideodemo.SmallAdapter;
import video.com.relavideodemo.UserInfo;
import video.com.relavideolibrary.camera.CameraView;

public class AgoraVideoActivity extends AppCompatActivity {

    public static final String LOG_TAG = "VideoChatViewActivity";
    private String mPublishUrl = "rtmp://rtmp.push-bs.rela.me/live/103351835test";
    private RecyclerView mSmallView;
    private SmallAdapter mSmallAdapter;
    private Map<Integer, UserInfo> mUserInfo = new HashMap<>();
    //before join channel success, big-uid is zero, after join success big-uid will modify by onJoinChannel-uid
    private int mBigUserId = 0;
    private LiveTranscoding mLiveTranscoding;

    private boolean DBG = false;
    private ExecutorService executorService;
    private volatile boolean mJoined = false;
    private RtcEngine mRtcEngine;// Tutorial Step 1
    private CameraView mCustomizedCameraRenderer; // Tutorial Step 3
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, final int width, final int height, int elapsed) { // Tutorial Step 5
            Log.d(LOG_TAG, "onFirstRemoteVideoDecoded");
        }

        @Override
        public void onError(int errorCode) {
            super.onError(errorCode);
            Log.d(LOG_TAG, "-->onError<--" + errorCode);
        }

        @Override
        public void onWarning(int warn) {
            super.onWarning(warn);
            Log.d(LOG_TAG, "-->onWarning<--" + warn);
        }

        @Override
        public void onStreamUnpublished(String url) {
            super.onStreamUnpublished(url);
            Log.d(LOG_TAG, "-->onStreamUrlUnpublished<--" + url);
        }

        @Override
        public void onStreamPublished(String url, final int error) {
            super.onStreamPublished(url, error);
            Log.d(LOG_TAG, "-->onStreamUrlPublished<--" + url + " -->error code<--" + error);
        }

        @Override
        public void onTranscodingUpdated() {
            super.onTranscodingUpdated();
            Log.d(LOG_TAG, "-->onTranscodingUpdated<--");
        }

        @Override
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            Log.d(LOG_TAG, "onJoinChannelSuccess channel: " + channel);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBigUserId = uid;
                    UserInfo mUI = new UserInfo();
                    mUI.uid = mBigUserId;
                    mUserInfo.put(mBigUserId, mUI);

                    setTranscoding();
                    mRtcEngine.addPublishStreamUrl(mPublishUrl, true);
                }
            });
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            super.onLeaveChannel(stats);
            Log.d(LOG_TAG, "onLeaveChannel");
        }

        @Override
        public void onUserJoined(final int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            Log.d(LOG_TAG, "-->onUserJoined uid<--" + uid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UserInfo mUI = new UserInfo();
                    mUI.view = RtcEngine.CreateRendererView(AgoraVideoActivity.this);
                    mUI.uid = uid;
                    mUI.view.setZOrderOnTop(true);
                    mUserInfo.put(uid, mUI);
                    mSmallAdapter.update(getSmallVideoUser(mUserInfo, mBigUserId));
                    mRtcEngine.setupRemoteVideo(new VideoCanvas(mUI.view, Constants.RENDER_MODE_HIDDEN, uid));
                    setTranscoding();
                }
            });
        }

        @Override
        public void onUserOffline(final int uid, int reason) { // Tutorial Step 7
            Log.d(LOG_TAG, "onUserOffline");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mUserInfo.remove(uid);
                    mSmallAdapter.update(getSmallVideoUser(mUserInfo, mBigUserId));

                    setTranscoding();
                }
            });
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_video);

//        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//
//        }
        new RxPermissions(this).request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                if (aBoolean) {
                    mSmallView = findViewById(R.id.video_view_list);
                    mSmallView.setHasFixedSize(true);
                    GridLayoutManager glm = new GridLayoutManager(AgoraVideoActivity.this, 3);
                    mSmallAdapter = new SmallAdapter(AgoraVideoActivity.this, getSmallVideoUser(mUserInfo, mBigUserId));
                    mSmallView.setLayoutManager(glm);
                    mSmallView.setAdapter(mSmallAdapter);

                    executorService = Executors.newSingleThreadExecutor();
                    initAgoraEngineAndJoinChannel();
                }
            }
        });


    }

    @Override
    protected void onDestroy() {
        leaveChannel();
        super.onDestroy();
    }

    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();     // Tutorial Step 1
        setupVideoProfile();         // Tutorial Step 2
        setupLocalVideo(getApplicationContext()); // Tutorial Step 3
        initTranscoding(720, 1080, 1800);
        setTranscoding();
    }

    // Tutorial Step 1
    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), "aab8b8f5a8cd4469a63042fcfafe7063", mRtcEventHandler);
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));

            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        mRtcEngine.disableAudio();
    }

    // Tutorial Step 2
    private void setupVideoProfile() {
        mRtcEngine.enableVideo();

        if (mRtcEngine.isTextureEncodeSupported()) {
            mRtcEngine.setExternalVideoSource(true, true, true);
        } else {
            throw new RuntimeException("Can not work on device do not supporting texture" + mRtcEngine.isTextureEncodeSupported());
        }
        mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_720P, true);
    }

    // Tutorial Step 3
    private void setupLocalVideo(Context ctx) {
        FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);

        final CameraView surfaceV = new CameraView(ctx);
        mCustomizedCameraRenderer = surfaceV;
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//                mCustomizedCameraRenderer.changeBeautyLevel(1);//开启美颜
//            }
//        });
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//                mCustomizedCameraRenderer.switchCamera();
//            }
//        });
        mCustomizedCameraRenderer.setOnFrameAvailableHandler(new CameraView.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(EGLContext eglContext, int textureId, int width, int height) {
                AgoraVideoFrame vf = new AgoraVideoFrame();
                vf.format = AgoraVideoFrame.FORMAT_TEXTURE_2D;
                vf.timeStamp = System.currentTimeMillis();
                vf.stride = width;
                vf.height = height;
                vf.textureID = textureId;
                vf.syncMode = true;
                vf.eglContext11 = eglContext;
                //OES transform
//                vf.transform = new float[]{
//                        0.0f, 1.0f, 0.0f, 0.0f,
//                        1.0f, 0.0f, 0.0f, 0.0f,
//                        0.0f, 0.0f, 1.0f, 0.0f,
//                        0.0f, 0.0f, 0.0f, 1.0f
//                };
                //texture2d transform
                vf.transform = new float[]{
                        1.0f, 0.0f, 0.0f, 0.0f,
                        0.0f, 1.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, 1.0f, 0.0f,
                        0.0f, 0.0f, 0.0f, 1.0f
                };

                boolean result = mRtcEngine.pushExternalVideoFrame(vf);
                if (DBG) {
                    Log.d(LOG_TAG, "onFrameAvailable \neglContext:" + eglContext + "\ntextureId: " + textureId + "\npushExternalVideoFrame return: " + result + "\nwidth:" + width + "\nheight:" + height);
                }
            }
        });

        mCustomizedCameraRenderer.setOnEGLContextHandler(new CameraView.OnEGLContextListener() {
            @Override
            public void onEGLContextReady() {
                if (!mJoined) {
                    Log.d(LOG_TAG, "joinChannel");
                    joinChannel(); // Tutorial Step 4
                    mJoined = true;
                }
            }
        });

//        surfaceV.setZOrderMediaOverlay(true);

        container.addView(surfaceV);
    }

    // Tutorial Step 4
    private void joinChannel() {
        mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        mRtcEngine.joinChannel(null, "CustomizedVideoSourceChannel1", "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
    }

    // Tutorial Step 6
    private void leaveChannel() {
        if (mRtcEngine != null) {
            mRtcEngine.removePublishStreamUrl(mPublishUrl);
            mRtcEngine.leaveChannel();
        }
        RtcEngine.destroy();
        mRtcEngine = null;
    }

    public void beautiful(final View view) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                String tag = (String) view.getTag();
                if (tag.equals("close")) {
                    mCustomizedCameraRenderer.changeBeautyLevel(1);//开启美颜
                    view.setTag("open");
                } else {
                    mCustomizedCameraRenderer.changeBeautyLevel(0);//关闭美颜
                    view.setTag("close");
                }
            }
        });
    }

    public void swtichCamera(View view) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                mCustomizedCameraRenderer.switchCamera();
            }
        });
    }

    public void LogToggle(View view) {
        DBG = !DBG;
    }

    private void initTranscoding(int width, int height, int bitrate) {
        if (mLiveTranscoding == null) {
            mLiveTranscoding = new LiveTranscoding();
            mLiveTranscoding.width = width;
            mLiveTranscoding.height = height;
            mLiveTranscoding.videoBitrate = bitrate;
            // if you want high fps, modify videoFramerate
            mLiveTranscoding.videoFramerate = 15;
        }
    }

    private void setTranscoding() {
        ArrayList<LiveTranscoding.TranscodingUser> transcodingUsers;
        ArrayList<UserInfo> videoUsers = getAllVideoUser(mUserInfo);

        transcodingUsers = cdnLayout(mBigUserId, videoUsers, mLiveTranscoding.width, mLiveTranscoding.height);

        mLiveTranscoding.setUsers(transcodingUsers);
        mLiveTranscoding.userCount = transcodingUsers.size();
        mRtcEngine.setLiveTranscoding(mLiveTranscoding);
    }

    public static ArrayList<UserInfo> getSmallVideoUser(Map<Integer, UserInfo> userInfo, int bigUserId) {
        ArrayList<UserInfo> users = new ArrayList<>();
        Iterator<Map.Entry<Integer, UserInfo>> iterator = userInfo.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, UserInfo> entry = iterator.next();
            UserInfo user = entry.getValue();
            if (user.uid == bigUserId) {
                continue;
            }
            users.add(user);
        }
        return users;
    }

    public static ArrayList<UserInfo> getAllVideoUser(Map<Integer, UserInfo> userInfo) {
        ArrayList<UserInfo> users = new ArrayList<>();
        Iterator<Map.Entry<Integer, UserInfo>> iterator = userInfo.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, UserInfo> entry = iterator.next();
            UserInfo user = entry.getValue();
            users.add(user);
        }
        return users;
    }

    public static ArrayList<LiveTranscoding.TranscodingUser> cdnLayout(int bigUserId, ArrayList<UserInfo> publishers,
                                                                       int canvasWidth,
                                                                       int canvasHeight) {

        ArrayList<LiveTranscoding.TranscodingUser> users;
        int index = 0;
        float xIndex, yIndex;
        int viewWidth;
        int viewHEdge;

        if (publishers.size() <= 1)
            viewWidth = canvasWidth;
        else
            viewWidth = canvasWidth / 2;

        if (publishers.size() <= 2)
            viewHEdge = canvasHeight;
        else
            viewHEdge = canvasHeight / ((publishers.size() - 1) / 2 + 1);

        users = new ArrayList<>(publishers.size());

        LiveTranscoding.TranscodingUser user0 = new LiveTranscoding.TranscodingUser();
        user0.uid = bigUserId;
        user0.alpha = 1;
        user0.zOrder = 0;
        user0.audioChannel = 0;

        user0.x = 0;
        user0.y = 0;
        user0.width = viewWidth;
        user0.height = viewHEdge;
        users.add(user0);

        index++;
        for (UserInfo entry : publishers) {
            if (entry.uid == bigUserId)
                continue;

            xIndex = index % 2;
            yIndex = index / 2;
            LiveTranscoding.TranscodingUser tmpUser = new LiveTranscoding.TranscodingUser();
            tmpUser.uid = entry.uid;
            tmpUser.x = (int) ((xIndex) * viewWidth);
            tmpUser.y = (int) (viewHEdge * (yIndex));
            tmpUser.width = viewWidth;
            tmpUser.height = viewHEdge;
            tmpUser.zOrder = index + 1;
            tmpUser.audioChannel = 0;
            tmpUser.alpha = 1f;

            users.add(tmpUser);
            index++;
        }

        return users;
    }
}

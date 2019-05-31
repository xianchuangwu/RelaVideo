package video.com.relavideolibrary.surface;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import video.com.relavideolibrary.BaseActivity;
import video.com.relavideolibrary.R;
import video.com.relavideolibrary.Utils.Constant;
import video.com.relavideolibrary.adapter.GalleryVideoAdapter;
import video.com.relavideolibrary.manager.VideoManager;
import video.com.relavideolibrary.model.MediaModel;
import video.com.relavideolibrary.model.VideoBean;
import video.com.relavideolibrary.service.ScanningVideoService;

public class GalleryActivity extends BaseActivity implements ScanningVideoService.QueryMediaStoreListener, View.OnClickListener, GalleryVideoAdapter.SelectCallback {

    private RecyclerView recyclerView;

    private GalleryVideoAdapter mVideoAdapter;

    private ScanningVideoService scanningVideoService;

    private String selectVideoPath;

    private long selectVideoDuration;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            scanningVideoService = ((ScanningVideoService.ScanningVideoBinder) service).getService();
            scanningVideoService.setQueryMediaStoreListener(GalleryActivity.this);
            scanningVideoService.startScanning();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            scanningVideoService.setQueryMediaStoreListener(null);
            scanningVideoService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //        andorid 7.0 FileUriExposedException
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        setTranslucentBar();
        setContentView(R.layout.activity_gallery);
        showTranslucentView();
        findViewById(R.id.preview).setOnClickListener(this);
        findViewById(R.id.complete).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        recyclerView = findViewById(R.id.recycler);

        bindService(new Intent(this, ScanningVideoService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    public void queryVideo(final ArrayList<MediaModel> mGalleryModelList) {
        //插入一个空的MediaModel站位，作为拍照按钮
        mGalleryModelList.set(0, new MediaModel("", false, 0, 0));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
                recyclerView.setHasFixedSize(true);
                mVideoAdapter = new GalleryVideoAdapter(R.layout.view_grid_item_media_chooser, recyclerView, mGalleryModelList);
                mVideoAdapter.setSelectCallback(GalleryActivity.this);
                recyclerView.setAdapter(mVideoAdapter);
            }
        });
    }

    @Override
    public void loadingStart() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showDialog();
            }
        });
    }

    @Override
    public void loadingStop() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissDialog();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.preview) {
            if (TextUtils.isEmpty(selectVideoPath)) {
                Toast.makeText(this, "video path empty", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, PreviewActivity.class);
            intent.putExtra("path", selectVideoPath);
            startActivityForResult(intent, Constant.IntentCode.REQUEST_CODE_PREVIEW);
        } else if (id == R.id.complete) {
            complete();
        } else if (id == R.id.cancel) {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.IntentCode.REQUEST_CODE_PREVIEW && resultCode == Activity.RESULT_OK) {
            complete();
        } /*else if (requestCode == Constant.IntentCode.REQUEST_CODE_CUT && resultCode == Activity.RESULT_OK) {
            selectVideoPath = data.getStringExtra(Constant.BundleConstants.CUT_VIDEO_PATH);
            VideoBean videoBean = new VideoBean();
            videoBean.videoPath = selectVideoPath;
            VideoManager.getInstance().setVideoBean(videoBean);
            setResult(Constant.IntentCode.RESULT_CODE_GALLERY_OK);
            finish();
        } */ else if (requestCode == Constant.IntentCode.REQUEST_CODE_RECORDING && resultCode == Activity.RESULT_OK) {
            finish();
        } else if (requestCode == Constant.IntentCode.REQUEST_CODE_EDIT && resultCode == Activity.RESULT_OK) {
            //从编辑页回来，清空视频管理类
            VideoManager.getInstance().clean();
            finish();
        }
    }

    @Override
    public void selected(String path, long duration) {
        TextView preview = findViewById(R.id.preview);
        TextView complete = findViewById(R.id.complete);
        preview.setAlpha(TextUtils.isEmpty(path) ? 0.5f : 1f);
        complete.setAlpha(TextUtils.isEmpty(path) ? 0.5f : 1f);
        preview.setEnabled(!TextUtils.isEmpty(path));
        complete.setEnabled(!TextUtils.isEmpty(path));
        selectVideoPath = path;
        selectVideoDuration = duration;
    }

    private void complete() {
        if (!TextUtils.isEmpty(selectVideoPath)) {
            //超过最大时长限制，跳到裁剪页
//            if (selectVideoDuration > Constant.VideoConfig.MAX_VIDEO_DURATION) {
//                CutActivity.startCut(this, selectVideoPath);
//            } else {
//                VideoBean videoBean = new VideoBean();
//                videoBean.videoPath = selectVideoPath;
//                VideoManager.getInstance().setVideoBean(videoBean);
//                setResult(Constant.IntentCode.RESULT_CODE_GALLERY_OK);
//                finish();
//            }
            VideoBean bean = new VideoBean();
            bean.videoPath = selectVideoPath;
            VideoManager.getInstance().setVideoBean(bean);
            startActivityForResult(new Intent(GalleryActivity.this, EditActivity.class), Constant.IntentCode.REQUEST_CODE_EDIT);
        } else {
            Toast.makeText(this, "video path empty", Toast.LENGTH_SHORT).show();
        }
    }
}

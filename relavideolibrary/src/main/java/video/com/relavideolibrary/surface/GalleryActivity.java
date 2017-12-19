package video.com.relavideolibrary.surface;

import android.app.Dialog;
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
import video.com.relavideolibrary.view.LoadingDialog;

public class GalleryActivity extends BaseActivity implements ScanningVideoService.QueryMediaStoreListener, View.OnClickListener, GalleryVideoAdapter.SelectCallback {

    private RecyclerView recyclerView;

    private GalleryVideoAdapter mVideoAdapter;

    private ScanningVideoService scanningVideoService;

    private Dialog loadingDialog;

    private String selectVideoPath;

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

        setContentView(R.layout.activity_gallery);
        findViewById(R.id.preview).setOnClickListener(this);
        findViewById(R.id.complete).setOnClickListener(this);
        recyclerView = findViewById(R.id.recycler);

        loadingDialog = new LoadingDialog(this).getLoadingDialog();

        bindService(new Intent(this, ScanningVideoService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    public void queryVideo(final ArrayList<MediaModel> mGalleryModelList) {
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
                loadingDialog.show();
            }
        });
    }

    @Override
    public void loadingStop() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingDialog.dismiss();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.preview) {

        } else if (id == R.id.complete) {
            if (!TextUtils.isEmpty(selectVideoPath)) {
                VideoBean videoBean = new VideoBean();
                videoBean.videoPath = selectVideoPath;
                VideoManager.getInstance().setVideoBean(videoBean);
                setResult(Constant.IntentCode.RESULT_CODE_GALLERY_OK);
                finish();
            } else {
                Toast.makeText(this, "video path empty", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void selected(String path) {
        selectVideoPath = path;
    }
}

package video.com.relavideolibrary.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import video.com.relavideolibrary.R;
import video.com.relavideolibrary.model.GalleryFolderBean;
import video.com.relavideolibrary.model.MediaModel;

public class ScanningVideoService extends Service {

    public static final String TAG = "ScanningVideoService";

    private Context context;

    private QueryMediaStoreListener queryMediaStoreListener;

    public void setQueryMediaStoreListener(QueryMediaStoreListener queryMediaStoreListener) {
        this.queryMediaStoreListener = queryMediaStoreListener;
    }

    public interface QueryMediaStoreListener {
//        void queryVideoFolder(ArrayList<GalleryFolderBean> galleryFolderBeanArrayList, int videoCount);

        void queryVideo(ArrayList<MediaModel> mGalleryModelList);

        void loadingStart();

        void loadingStop();
    }

    public class ScanningVideoBinder extends Binder {
        public ScanningVideoService getService() {
            return ScanningVideoService.this;
        }
    }

    public ScanningVideoBinder mBinder = new ScanningVideoBinder();

    public ScanningVideoService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        startScanning();
        return super.onStartCommand(intent, flags, startId);
    }

    public void startScanning() {
        new QueryMediaStoreThread().start();
    }

    private class QueryMediaStoreThread extends Thread {

        public QueryMediaStoreThread() {
        }

        @Override
        public void run() {

            cursorVideoByFolderName("");
        }

        /**
         * 扫描手机里所有的视频文件夹
         */
        private void cursorVideoFolder() {
            if (queryMediaStoreListener != null) {
                queryMediaStoreListener.loadingStart();
            }

            int allVideoCount = 0;

            // The indices should match the following projections.
            final int INDEX_BUCKET_ID = 0;
            final int INDEX_BUCKET_NAME = 1;
            final int INDEX_BUCKET_URL = 2;
            final int INDEX_BUCKET_COUNT = 3;
            final String[] PROJECTION_BUCKET = {
                    MediaStore.Video.VideoColumns.BUCKET_ID,
                    MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Video.VideoColumns.DATA
//                , MediaStore.Video.VideoColumns._COUNT //查不到_count,报错
            };
            final String orderByVideo = MediaStore.Video.Media.DATE_TAKEN;
            Cursor mCursorVideo = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, PROJECTION_BUCKET, null, null, orderByVideo + " DESC");
            ArrayList<GalleryFolderBean> bufferVideo = new ArrayList<>();
            try {
                while (mCursorVideo.moveToNext()) {
                    GalleryFolderBean entry = new GalleryFolderBean(
                            mCursorVideo.getInt(INDEX_BUCKET_ID)
                            , mCursorVideo.getString(INDEX_BUCKET_NAME)
                            , mCursorVideo.getString(INDEX_BUCKET_URL)
                            , 0);//文件夹中的视频数
                    Log.d(TAG, "扫描视频文件夹:" + entry.toString());

                    if (!bufferVideo.contains(entry)) {
                        int videoCount = cursorVideoCountByFolderName(mCursorVideo.getString(INDEX_BUCKET_NAME), context);
                        entry.setVideoCount(videoCount);
                        allVideoCount = allVideoCount + videoCount;
                        bufferVideo.add(entry);
                    }
                }
                if (mCursorVideo.getCount() > 0) {//相册总数
                    Log.i("gallery", bufferVideo.toString());
//                    if (queryMediaStoreListener != null)
//                        queryMediaStoreListener.queryVideoFolder(bufferVideo, allVideoCount);
                } else {
                    Toast.makeText(context, context.getString(R.string.no_media_file_available), Toast.LENGTH_SHORT).show();
                }

            } finally {
                mCursorVideo.close();
            }
            if (queryMediaStoreListener != null) {
                queryMediaStoreListener.loadingStop();
            }
        }

        /**
         * 根据文件夹名查询对应的视频文件
         *
         * @param bucketName 传空时查询所有视频
         */
        private void cursorVideoByFolderName(@NonNull String bucketName) {

            if (queryMediaStoreListener != null) {
                queryMediaStoreListener.loadingStart();
            }

            ArrayList<MediaModel> mGalleryModelList = null;
            Cursor mCursor;
            int mIDColumnIndex;
            int mDataColumnIndex;
            int mDurationColumnIndex;
            // MediaStore下有图片,视频,音频,文件等等MediaStore.Images MediaStore.Files MediaStore.Autio 具体可以查看MediaStore源码
            final String[] PROJECTION_BUCKET = {MediaStore.Video.Media._ID, MediaStore.Video.VideoColumns.DATA, MediaStore.Video.VideoColumns.DURATION};
            final String orderByVideo = MediaStore.Video.Media.DATE_TAKEN;
            try {

                if (bucketName.equals(context.getResources().getString(R.string.all_video)) || TextUtils.isEmpty(bucketName)) {
                    //查询所有视频
                    mCursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, PROJECTION_BUCKET, null, null, orderByVideo + " DESC");

                } else {
                    //查询文件夹名为bucketName下的视频
                    String searchParams = "bucket_display_name = \"" + bucketName + "\"";
                    mCursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, PROJECTION_BUCKET, searchParams, null, orderByVideo + " DESC");
                }

                if (mCursor.getCount() > 0) {
                    mIDColumnIndex = mCursor.getColumnIndex(PROJECTION_BUCKET[0]);
                    mDataColumnIndex = mCursor.getColumnIndex(PROJECTION_BUCKET[1]);
                    mDurationColumnIndex = mCursor.getColumnIndex(PROJECTION_BUCKET[2]);

                    //move position to first element
                    mCursor.moveToFirst();

                    mGalleryModelList = new ArrayList<>();

                    for (int i = 0; i < mCursor.getCount(); i++) {
                        mCursor.moveToPosition(i);
                        int id = mCursor.getInt(mIDColumnIndex);
                        String url = mCursor.getString(mDataColumnIndex);
                        long duration = mCursor.getLong(mDurationColumnIndex);
                        mGalleryModelList.add(new MediaModel(url, false, duration, id));
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.no_media_file_available), Toast.LENGTH_SHORT).show();
                }

                if (queryMediaStoreListener != null) {
                    queryMediaStoreListener.loadingStop();
                    if (mGalleryModelList != null)
                        queryMediaStoreListener.queryVideo(mGalleryModelList);
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (queryMediaStoreListener != null) {
                    queryMediaStoreListener.loadingStop();
                }
            }

        }

        private int cursorVideoCountByFolderName(String bucketName, Context context) {

            final String[] PROJECTION_BUCKET = {MediaStore.Video.VideoColumns.DATA};
            final String orderByVideo = MediaStore.Video.Media.DATE_TAKEN;

            try {

                String searchParams = "bucket_display_name = \"" + bucketName + "\"";
                Cursor mCursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, PROJECTION_BUCKET, searchParams, null, orderByVideo + " DESC");

                if (mCursor.getCount() > 0) {

                    //move position to first element
                    mCursor.moveToFirst();
                    String[] ss = new String[mCursor.getCount()];
                    for (int i = 0; i < mCursor.getCount(); i++) {
                        mCursor.moveToPosition(i);
                        String url = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                        ss[i] = url;
                    }
                    Log.d(TAG, bucketName + "文件夹的视频总数:" + ss.length);
                    return ss.length;
                } else {
                    Toast.makeText(context, context.getString(R.string.no_media_file_available), Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

    }
}

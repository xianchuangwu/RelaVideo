package video.com.relavideodemo.surface;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.thel.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import video.com.relavideodemo.imageviewer.ImageLoader;
import video.com.relavideodemo.imageviewer.ViewData;
import video.com.relavideodemo.imageviewer.dragger.ImageDraggerType;
import video.com.relavideodemo.imageviewer.widget.ImageViewer;
import video.com.relavideodemo.imageviewer.widget.ScaleImageView;
import video.com.relavideolibrary.BaseRecyclerAdapter;
import video.com.relavideolibrary.BaseViewHolder;

public class ImagePreviewActivity extends AppCompatActivity {

    protected List<String> mImageList = new ArrayList<>();
    protected List<ViewData> mViewList = new ArrayList<>();

    private RecyclerView recycler;
    private ImageViewer imagePreivew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mImageList = new ArrayList<>();
        String url0 = "http://img5.duitang.com/uploads/item/201404/11/20140411214939_XswXa.jpeg";
        String url1 = "http://att.bbs.duowan.com/forum/201210/20/210446opy9p5pghu015p9u.jpg";
        String url2 = "https://b-ssl.duitang.com/uploads/item/201505/09/20150509221719_kyNrM.jpeg";
        String url3 = "https://b-ssl.duitang.com/uploads/item/201709/26/20170926131419_8YhLA.jpeg";
        String url4 = "https://b-ssl.duitang.com/uploads/item/201505/11/20150511122951_MAwVZ.jpeg";
        String url5 = "https://b-ssl.duitang.com/uploads/item/201704/23/20170423205828_BhNSv.jpeg";
        String url6 = "https://b-ssl.duitang.com/uploads/item/201706/30/20170630181644_j4mh5.jpeg";
        String url7 = "https://b-ssl.duitang.com/uploads/item/201407/22/20140722172759_iPCXv.jpeg";
        String url8 = "https://b-ssl.duitang.com/uploads/item/201511/11/20151111103149_mrRfd.jpeg";
        String url9 = "https://b-ssl.duitang.com/uploads/item/201510/14/20151014172010_RnJVz.jpeg";
        mImageList.add(url0);
        mImageList.add(url1);
        mImageList.add(url2);
        mImageList.add(url3);
        mImageList.add(url4);
        mImageList.add(url5);
        mImageList.add(url6);
        mImageList.add(url7);
        mImageList.add(url8);
        mImageList.add(url9);
        for (int i = 0, len = mImageList.size(); i < len; i++) {
            ViewData viewData = new ViewData();
            mViewList.add(viewData);
        }

        recycler = findViewById(R.id.recycler);
        imagePreivew = findViewById(R.id.imagePreivew);


        ImageAdapter imageAdapter = new ImageAdapter(R.layout.item_image, recycler, mImageList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recycler.setLayoutManager(gridLayoutManager);
        recycler.setAdapter(imageAdapter);
        imageAdapter.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(int position) {
                for (int i = 0; i < recycler.getChildCount(); i++) {
                    int[] location = new int[2];
                    // 获取在整个屏幕内的绝对坐标
                    recycler.getChildAt(i).getLocationOnScreen(location);
                    ViewData viewData = mViewList.get(i);
                    viewData.setTargetX(location[0]);
                    // 此处注意，获取 Y 轴坐标时，需要根据实际情况来处理《状态栏》的高度，判断是否需要计算进去
                    viewData.setTargetY(location[1]);
                    viewData.setTargetWidth(recycler.getChildAt(i).getMeasuredWidth());
                    viewData.setTargetHeight(recycler.getChildAt(i).getMeasuredHeight());
                    mViewList.set(i, viewData);
                }
                imagePreivew.setStartPosition(position);

                imagePreivew.setViewData(mViewList);
                imagePreivew.watch();
            }
        });

        imagePreivew.doDrag(true);
        imagePreivew.setDragType(ImageDraggerType.DRAG_TYPE_WX);
        imagePreivew.setImageData(mImageList);
        imagePreivew.setImageLoader(new ImageLoader<String>() {

            @Override
            public void displayImage(final int position, String src, final ImageView imageView) {
                final ScaleImageView scaleImageView = (ScaleImageView) imageView.getParent();
                Glide.with(ImagePreviewActivity.this).load(src).centerCrop().into(new SimpleTarget<GlideDrawable>() {

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        scaleImageView.showProgess();
                        imageView.setImageDrawable(placeholder);
                    }

                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        scaleImageView.removeProgressView();
                        imageView.setImageDrawable(resource);
                    }
                });
            }
        });
    }

    /**
     * 监听返回键
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean b = imagePreivew.onKeyDown(keyCode, event);
        if (b) {
            return b;
        }
        return super.onKeyDown(keyCode, event);
    }

    public class ImageAdapter extends BaseRecyclerAdapter<String> {

        public ImageAdapter(int layoutId, RecyclerView recyclerView, Collection<String> list) {
            super(layoutId, recyclerView, list);
        }

        @Override
        public void dataBinding(BaseViewHolder holder, String item, final int position) {

            final ImageView imageView = holder.getView(R.id.image);
            mViewList.get(position).setImageWidth(imageView.getWidth());
            mViewList.get(position).setImageHeight(imageView.getHeight());
            imagePreivew.setViewData(mViewList);
            Glide.with(mContext).load(item).centerCrop().crossFade().into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null)
                        itemClickListener.onClick(position);
                }
            });
        }

        public ItemClickListener itemClickListener;

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }
    }

    public interface ItemClickListener {
        void onClick(int position);
    }
}

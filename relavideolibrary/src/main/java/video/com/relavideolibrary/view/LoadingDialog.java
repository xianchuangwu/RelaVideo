package video.com.relavideolibrary.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import video.com.relavideolibrary.R;

/**
 * Created by chad
 * Time 16/11/7
 * Email: wuxianchuang@foxmail.com
 * Description: TODO loading提示dailog
 */

public class LoadingDialog {

    public TextView tv_tips;

    private Context mContext;

    public LoadingDialog(Context context) {
        this.mContext = context;
    }

    public Dialog getLoadingDialog() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        //加载loading_dialog.xml
        View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view

        // loading_dialog.xml中的LinearLayout
        RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.dialog_view);// 加载布局

        // loading_dialog.xml中的TextView
        tv_tips = (TextView) v.findViewById(R.id.tv_tips);
        ImageView imageView = (ImageView) v.findViewById(R.id.iv_loading_dog);
        String url = "file:///android_asset/" + "loading_cat.gif";
        //读取本地gif需要加diskCacheStrategy(DiskCacheStrategy.SOURCE)，否则有时读不出来
        Glide.with(mContext).load(url)
                .asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(imageView);

        //旋转动画效果
        /*// loading_dialog.xml中的ImageView
        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
        // 加载动画load_animation.xml
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.load_animation);
        // 使用ImageView显示动画
        spaceshipImage.startAnimation(anim);*/

        // 创建自定义样式loading_dialog
        Dialog loadingDialog = new Dialog(mContext, R.style.loading_dialog);
        loadingDialog.setCancelable(false);// 设置是否可用“返回键”取消
        loadingDialog.setCanceledOnTouchOutside(false);
        // 设置布局
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));

        return loadingDialog;
    }
}

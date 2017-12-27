package video.com.relavideolibrary.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by chad
 * Time 17/12/18
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class VideoBean implements Parcelable {

    public String videoPath;

    public int filterId;

    public VideoBean() {
    }


    protected VideoBean(Parcel in) {
        videoPath = in.readString();
        filterId = in.readInt();
    }

    public static final Creator<VideoBean> CREATOR = new Creator<VideoBean>() {
        @Override
        public VideoBean createFromParcel(Parcel in) {
            return new VideoBean(in);
        }

        @Override
        public VideoBean[] newArray(int size) {
            return new VideoBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(videoPath);
        dest.writeInt(filterId);
    }
}

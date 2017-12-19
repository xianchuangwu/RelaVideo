package video.com.relavideolibrary.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaModel implements Parcelable {

    public String url = null;
    public boolean status = false;
    public int selectIndex = 0;
    public boolean isCallStarted = false;
    public long duration = 0;
    /**
     * BitmapFactory.Options options = new BitmapFactory.Options();
     * options.inDither = false;
     * options.inPreferredConfig = Bitmap.Config.ARGB_8888;
     * 媒体库中的视频缩略图
     * Bitmap mThumbnail = MediaStore.Video.Thumbnails.getThumbnail(activity.getContentResolver(), id, MediaStore.Images.Thumbnails.MINI_KIND, options);
     */
    public long id;

    public MediaModel(String url, boolean status, long duration, long id) {
        this.url = url;
        this.status = status;
        this.duration = duration;
        this.id = id;
    }


    protected MediaModel(Parcel in) {
        url = in.readString();
        status = in.readByte() != 0;
        selectIndex = in.readInt();
        isCallStarted = in.readByte() != 0;
        duration = in.readLong();
        id = in.readLong();
    }

    public static final Creator<MediaModel> CREATOR = new Creator<MediaModel>() {
        @Override
        public MediaModel createFromParcel(Parcel in) {
            return new MediaModel(in);
        }

        @Override
        public MediaModel[] newArray(int size) {
            return new MediaModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeByte((byte) (status ? 1 : 0));
        dest.writeInt(selectIndex);
        dest.writeByte((byte) (isCallStarted ? 1 : 0));
        dest.writeLong(duration);
        dest.writeLong(id);
    }
}

package video.com.relavideolibrary.model;


import android.os.Parcel;
import android.os.Parcelable;

public class GalleryFolderBean implements Parcelable {
    //id用于判断,避免重复添加
    public int bucketId;
    public String bucketName;
    public String bucketUrl = null;
    public int videoCount;

    protected GalleryFolderBean(Parcel in) {
        bucketId = in.readInt();
        bucketName = in.readString();
        bucketUrl = in.readString();
        videoCount = in.readInt();
    }

    public static final Creator<GalleryFolderBean> CREATOR = new Creator<GalleryFolderBean>() {
        @Override
        public GalleryFolderBean createFromParcel(Parcel in) {
            return new GalleryFolderBean(in);
        }

        @Override
        public GalleryFolderBean[] newArray(int size) {
            return new GalleryFolderBean[size];
        }
    };

    public int getBucketId() {
        return bucketId;
    }

    public void setBucketId(int bucketId) {
        this.bucketId = bucketId;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketUrl() {
        return bucketUrl;
    }

    public void setBucketUrl(String bucketUrl) {
        this.bucketUrl = bucketUrl;
    }

    public GalleryFolderBean() {
    }

    public GalleryFolderBean(int id, String name, String url, int videoCount) {
        bucketId = id;
        bucketName = ensureNotNull(name);
        bucketUrl = url;
        this.videoCount = videoCount;
    }

    @Override
    public int hashCode() {
        return bucketId;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof GalleryFolderBean)) return false;
        GalleryFolderBean entry = (GalleryFolderBean) object;
        return bucketId == entry.bucketId;
    }

    public static String ensureNotNull(String value) {
        return value == null ? "" : value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(bucketId);
        parcel.writeString(bucketName);
        parcel.writeString(bucketUrl);
        parcel.writeInt(videoCount);
    }

    @Override
    public String toString() {
        return "GalleryFolderBean{" +
                "bucketId=" + bucketId +
                ", bucketName='" + bucketName + '\'' +
                ", bucketUrl='" + bucketUrl + '\'' +
                ", videoCount=" + videoCount +
                '}';
    }
}

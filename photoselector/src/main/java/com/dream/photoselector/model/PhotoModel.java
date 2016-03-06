package com.dream.photoselector.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 照片
 */
public class PhotoModel implements Parcelable {

    private String originalPath;
    private boolean isChecked;

    public PhotoModel(String originalPath, boolean isChecked) {
        super();
        this.originalPath = originalPath;
        this.isChecked = isChecked;
    }

    public PhotoModel(String originalPath) {
        this.originalPath = originalPath;
    }

    public PhotoModel() {
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    @Override
    public String toString() {
        return "PhotoModel{" +
                "originalPath='" + originalPath + '\'' +
                ", isChecked=" + isChecked +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhotoModel that = (PhotoModel) o;

        return !(originalPath != null ? !originalPath.equals(that.originalPath) : that.originalPath != null);

    }

    @Override
    public int hashCode() {
        return originalPath != null ? originalPath.hashCode() : 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.originalPath);
        dest.writeByte(isChecked ? (byte) 1 : (byte) 0);
    }

    protected PhotoModel(Parcel in) {
        this.originalPath = in.readString();
        this.isChecked = in.readByte() != 0;
    }

    public static final Creator<PhotoModel> CREATOR = new Creator<PhotoModel>() {
        public PhotoModel createFromParcel(Parcel source) {
            return new PhotoModel(source);
        }

        public PhotoModel[] newArray(int size) {
            return new PhotoModel[size];
        }
    };
}

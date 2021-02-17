package com.vadim.model;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

@SuppressLint("ParcelCreator")
public class QrData implements Parcelable, Comparable<QrData> {
    private String name;

    private String originalPhoto;

    private String content;

    private String date;

    public QrData() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginalPhoto() {
        return originalPhoto;
    }

    public void setOriginalPhoto(String originalPhoto) {
        this.originalPhoto = originalPhoto;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QrData)) return false;
        QrData qrData = (QrData) o;
        return content.equals(qrData.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }

    @Override
    public int compareTo(QrData qrData) {
        if (qrData != null && (date == null || qrData.date == null))
            return 0;
        return date.compareTo(qrData.date);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(originalPhoto);
        dest.writeString(content);
        dest.writeString(date);
    }

}

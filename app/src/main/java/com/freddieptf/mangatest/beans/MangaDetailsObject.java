package com.freddieptf.mangatest.beans;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fred on 4/27/15.
 */
public class MangaDetailsObject implements Parcelable {

    public static final Creator<MangaDetailsObject> CREATOR = new Creator<MangaDetailsObject>() {
        @Override
        public MangaDetailsObject createFromParcel(Parcel parcel) {
            return new MangaDetailsObject(parcel);
        }

        @Override
        public MangaDetailsObject[] newArray(int i) {
            return new MangaDetailsObject[i];
        }
    };
    String name;
    String status;
    String info;
    String cover;
    String author;
    String chapters;
    String lastUpdate;
    String source;

    public MangaDetailsObject() {
        super();
    }

    protected MangaDetailsObject(Parcel in) {
        readFromParcel(in);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getChapters() {
        return chapters;
    }

    public void setChapters(String chapters) {
        this.chapters = chapters;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(status);
        parcel.writeString(info);
        parcel.writeString(cover);
        parcel.writeString(author);
        parcel.writeString(chapters);
        parcel.writeString(lastUpdate);
        parcel.writeString(source);

    }

    void readFromParcel(Parcel in){
        name = in.readString();
        status = in.readString();
        info = in.readString();
        cover = in.readString();
        author = in.readString();
        chapters = in.readString();
        lastUpdate = in.readString();
        source = in.readString();
    }




}
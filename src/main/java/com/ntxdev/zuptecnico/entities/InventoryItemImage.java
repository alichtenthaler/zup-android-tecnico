package com.ntxdev.zuptecnico.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by igorlira on 4/30/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryItemImage implements Serializable, Parcelable {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Versions implements Serializable, Parcelable
    {
        public String high;
        public String low;
        public String thumb;

        public Versions()
        {

        }

        public Versions(Parcel in)
        {
            high = in.readString();
            low = in.readString();
            thumb = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(high);
            parcel.writeString(low);
            parcel.writeString(thumb);
        }

        public static final Parcelable.Creator<Versions> CREATOR
                = new Parcelable.Creator<Versions>() {
            public Versions createFromParcel(Parcel in) {
                return new Versions(in);
            }

            public Versions[] newArray(int size) {
                return new Versions[size];
            }
        };
    }

    public InventoryItemImage()
    {

    }

    public InventoryItemImage(Parcel in)
    {
        inventory_item_data_id = in.readInt();
        url = in.readString();
        versions = in.readParcelable(this.getClass().getClassLoader());
        content = in.readString();
    }

    public int inventory_item_data_id;
    public String url;
    public Versions versions;
    public String content;

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean equals(InventoryItemImage o) {
        if(this.content != null && o.content != null && this.content.equals(o.content))
            return true;
        else if (this.versions != null && o.versions != null && this.versions.high.equals(o.versions.high) && this.versions.low.equals(o.versions.low) && this.versions.thumb.equals(o.versions.thumb))
            return true;
        else
            return false;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(inventory_item_data_id);
        parcel.writeString(url);
        parcel.writeParcelable(versions, i);
        parcel.writeString(content);
    }

    public static final Parcelable.Creator<InventoryItemImage> CREATOR
            = new Parcelable.Creator<InventoryItemImage>() {
        public InventoryItemImage createFromParcel(Parcel in) {
            return new InventoryItemImage(in);
        }

        public InventoryItemImage[] newArray(int size) {
            return new InventoryItemImage[size];
        }
    };

}

package com.ntxdev.zuptecnico.entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by igorlira on 7/13/15.
 */
public class Position implements Parcelable
{
    public float latitude;
    public float longitude;

    public Position(){}

    public Position(Parcel in){
        latitude = in.readFloat();
        longitude = in.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(latitude);
        dest.writeFloat(longitude);
    }

    public static final Parcelable.Creator<Position> CREATOR = new Creator<Position>() {
        @Override
        public Position createFromParcel(Parcel source) {
            return new Position(source);
        }

        @Override
        public Position[] newArray(int size) {
            return new Position[size];
        }
    };
}

package com.ntxdev.zuptecnico.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;
import android.util.Log;

import com.ntxdev.zuptecnico.ZupApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GPSUtils {

    public static List<Address> getFromLocationName(Context context, String s) {
        try {
            Geocoder geocoder = new Geocoder(context);
            List<Address> list = filterResults(geocoder.getFromLocationName(s, 10));
            if (!list.isEmpty()) return list;
        } catch (IOException e) {
            Log.w("ZUP", e.getMessage(), e);
        }

        try {
            Geocoder geocoder = new Geocoder(ZupApplication.getContext(), Locale.getDefault());
            List<Address> list = filterResults(geocoder.getFromLocationName(s, 10));
            return list;
        } catch (IOException e) {
            Log.e("ZUP", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public static List<Address> getFromLocation(Context context, double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(ZupApplication.getContext(), Locale.getDefault());
            List<Address> list = filterResults(geocoder.getFromLocation(latitude, longitude, 10));
            return list;
        } catch (IOException e) {
            Log.e("ZUP", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private static List<Address> filterResults(List<Address> list) {
        List<Address> arraylist = new ArrayList<Address>();
        for (Address address : list) {
            if (!Strings.isTrimmedNullOrEmpty(address.getLocality()) || !Strings.isTrimmedNullOrEmpty(address.getSubAdminArea())) {
                arraylist.add(address);
            }
        }
        return arraylist;
    }

    public static String formatAddress(Address address) {
        ArrayList<String> components = new ArrayList<>();

        if(address.getThoroughfare() != null)
            components.add(address.getThoroughfare());

        if(address.getFeatureName() != null)
            components.add(address.getFeatureName());

        if(address.getSubLocality() != null)
            components.add(address.getSubLocality());

        if(address.getSubAdminArea() != null)
            components.add(address.getSubAdminArea());

        if(address.getAdminArea() != null)
            components.add(address.getAdminArea());

        if(address.getPostalCode() != null)
            components.add(address.getPostalCode());

        if(address.getCountryName() != null)
            components.add(address.getCountryName());

        return TextUtils.join(", ", components);
    }
}
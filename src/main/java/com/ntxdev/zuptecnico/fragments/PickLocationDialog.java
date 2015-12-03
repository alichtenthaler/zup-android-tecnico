package com.ntxdev.zuptecnico.fragments;

import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.ReportCategory;
import com.ntxdev.zuptecnico.entities.responses.PositionValidationResponse;
import com.ntxdev.zuptecnico.util.ResizeAnimation;
import com.ntxdev.zuptecnico.util.Utilities;

import retrofit.RetrofitError;

/**
 * Created by igorlira on 7/21/15.
 */
public class PickLocationDialog extends DialogFragment
        implements PickLocationFragment.OnLocationValidatedListener {

    public interface OnLocationSetListener {
        void onLocationSet(double latitude, double longitude, Address address, String reference);
    }

    private OnLocationSetListener listener;
    private NewPickLocationFragment fragment;
    private boolean isValidPosition;

    public void setOnLocationSetListener(OnLocationSetListener listener) {
        this.listener = listener;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.dialog_pick_location, container, false);
        View confirm = view.findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });
        return view;
    }

    void showOffLineError(){
        getView().findViewById(R.id.offline_warning).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.confirm).setVisibility(View.GONE);
    }

    void hideOfflineError(){
        getView().findViewById(R.id.offline_warning).setVisibility(View.GONE);
        getView().findViewById(R.id.confirm).setVisibility(View.VISIBLE);
    }


    void confirm() {
        if(!isValidPosition)
            return;

        if(this.listener != null) {
            /*double latitude = fragment.getLatitude();
            double longitude = fragment.getLongitude();
            Address address = fragment.getAddress();
            String reference = fragment.getReference();*/

            double latitude = fragment.latitude;
            double longitude = fragment.longitude;
            Address address = fragment.getAddress();
            String reference = fragment.getReference();

            this.listener.onLocationSet(latitude, longitude, address, reference);
        }

        dismiss();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.fragment = new NewPickLocationFragment();
        this.fragment.setArguments(getArguments());
        this.fragment.setListener(this);
        getChildFragmentManager().beginTransaction().add(R.id.fragment_container, this.fragment, "pick_location_fragment").commit();
        view.findViewById(R.id.offline_warning).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utilities.isConnected(getActivity())){
                    hideOfflineError();
                    fragment.reload();
                }else{
                    Toast.makeText(getActivity(), R.string.error_no_internet_toast, Toast.LENGTH_SHORT).show();
                }

            }
        });
        if(Utilities.isConnected(getActivity())){
            hideOfflineError();
        }else{
            showOffLineError();
        }
    }

    void setValidPosition(boolean valid)
    {
        this.isValidPosition = valid;
        View button = getView().findViewById(R.id.confirm);

        if(valid)
            button.setAlpha(1);
        else
            button.setAlpha(.5f);
    }

    @Override
    public void onValidLocationSet() {
        setValidPosition(true);
    }

    @Override
    public void onInvalidLocationSet() {
        setValidPosition(false);
    }
}

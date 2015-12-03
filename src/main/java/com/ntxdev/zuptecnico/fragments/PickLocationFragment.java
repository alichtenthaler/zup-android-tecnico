package com.ntxdev.zuptecnico.fragments;

import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.adapters.AddressAdapter;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.ReportCategory;
import com.ntxdev.zuptecnico.entities.responses.PositionValidationResponse;
import com.ntxdev.zuptecnico.ui.WebImageView;
import com.ntxdev.zuptecnico.util.GPSUtils;
import com.ntxdev.zuptecnico.util.GeoUtils;
import com.ntxdev.zuptecnico.util.ResizeAnimation;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by igorlira on 7/21/15.
 */

// FIXME: 8/3/15
public class PickLocationFragment extends Fragment implements GoogleMap.OnCameraChangeListener {
    GoogleMap map;
    boolean hasLocation;
    AddressLoader addressLoader;
    PositionLoader positionLoader;
    PositionValidateTask validationTask;

    Address address;
    boolean isAutomaticStreet = true;
    boolean isAutomaticNumber = true;

    ReportCategory category;
    View rootView;

    AddressAdapter nullAdapter;
    AddressAdapter adapter;

    double latitude, longitude;

    public interface OnLocationValidatedListener {
        void onValidLocationSet();
        void onInvalidLocationSet();
    }

    private OnLocationValidatedListener listener;

    public void setListener(OnLocationValidatedListener listener) {
        this.listener = listener;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        int categoryId = args.getInt("categoryId");
        address = args.getParcelable("address");
        category = Zup.getInstance().getReportCategoryService().getReportCategory(categoryId);

        if(rootView != null) {
            WebImageView markerView = (WebImageView) rootView.findViewById(R.id.address_marker);
            category.loadMarkerInto(markerView);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pick_map_location_new, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().
                                            findFragmentById(R.id.pick_location_map);

        map = mapFragment.getMap();
        setupMap(view);

        rootView = view;

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().
                findFragmentById(R.id.pick_location_map);
        if(mapFragment.getView() != null)
            mapFragment.getView().setFocusable(true);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Address getAddress() {
        if(this.address == null && getView() != null) {
            TextView txtStreet = (TextView) getView().findViewById(R.id.address_street);
            TextView txtNumber = (TextView) getView().findViewById(R.id.address_number);

            Address newAddress = new Address(null);
            newAddress.setLatitude(getLatitude());
            newAddress.setLongitude(getLongitude());
            newAddress.setThoroughfare(txtStreet.getText().toString());
            newAddress.setFeatureName(txtNumber.getText().toString());

            return newAddress;
        } else {
            return this.address;
        }
    }

    public String getReference() {
        if(getView() == null)
            return "";

        TextView txtReference = (TextView) getView().findViewById(R.id.address_reference);
        return txtReference.getText().toString();
    }

    void showStreetLoading() {
        if(getView() == null)
            return;

        TextView txtStreet = (TextView) getView().findViewById(R.id.address_street);
        txtStreet.setInputType(InputType.TYPE_NULL);
        rootView.findViewById(R.id.address_street_progress).setVisibility(View.VISIBLE);
    }

    void hideStreetLoading() {
        if(getView() == null)
            return;

        TextView txtStreet = (TextView) getView().findViewById(R.id.address_street);
        txtStreet.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        rootView.findViewById(R.id.address_street_progress).setVisibility(View.GONE);
    }

    void showNumberLoading() {
        if(getView() == null)
            return;

        TextView txtNumber = (TextView) getView().findViewById(R.id.address_number);
        txtNumber.setInputType(InputType.TYPE_NULL);
        rootView.findViewById(R.id.address_number_progress).setVisibility(View.VISIBLE);
    }

    void hideNumberLoading() {
        if(getView() == null)
            return;

        TextView txtNumber = (TextView) getView().findViewById(R.id.address_number);
        txtNumber.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        rootView.findViewById(R.id.address_number_progress).setVisibility(View.GONE);
    }

    void setupMap(View root) {
        if(category != null) {
            WebImageView markerView = (WebImageView) root.findViewById(R.id.address_marker);
            category.loadMarkerInto(markerView);
        }

        AutoCompleteTextView txtStreet = (AutoCompleteTextView) root.findViewById(R.id.address_street);
        TextView txtNumber = (TextView) root.findViewById(R.id.address_number);

        this.adapter = new AddressAdapter(this.getActivity(), R.layout.pick_map_location_suggestion, new AddressAdapter.PositionManager() {
            @Override
            public double getLatitude() {
                return PickLocationFragment.this.getLatitude();
            }

            @Override
            public double getLongitude() {
                return PickLocationFragment.this.getLongitude();
            }
        });

        txtStreet.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b)
                    streetFocused();
                else
                    streetUnfocused();
            }
        });
        txtStreet.setAdapter(this.adapter);
        txtStreet.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                suggestionSelected(adapter.getItem(i), adapter.getFullItem(i));
            }
        });
        if(address != null)
            txtStreet.setText(address.getThoroughfare());

        txtNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b)
                    numberFocused();
                else
                    numberUnfocused();
            }
        });
        if(address != null)
            txtNumber.setText(address.getFeatureName());

        map.setMyLocationEnabled(true);

        if(address != null) {
            moveTo(address.getLatitude(), address.getLongitude());
        } else {
            map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    if (!hasLocation) {
                        moveTo(location.getLatitude(), location.getLongitude());
                    }
                    hasLocation = true;
                }
            });
        }
        map.setOnCameraChangeListener(this);
    }

    void suggestionSelected(String suggestion, String fullDescription) {
        if(getView() == null) {
            return;
        }

        isAutomaticStreet = true;
        isAutomaticNumber = true;

        TextView txtStreet = (TextView) getView().findViewById(R.id.address_street);
        txtStreet.setText(suggestion);

        positionLoader = new PositionLoader(getLatitude(), getLongitude(), fullDescription);
        positionLoader.execute();
    }

    void streetFocused() {
        if(getView() == null)
            return;

        TextView txtStreet = (TextView) getView().findViewById(R.id.address_street);

        //if(isAutomaticStreet) {
            isAutomaticStreet = false;
            txtStreet.setText("");
        //}
    }

    void streetUnfocused() {
        if(getView() == null)
            return;

        TextView txtStreet = (TextView) getView().findViewById(R.id.address_street);

        if(txtStreet.getText().length() == 0) {
            isAutomaticStreet = true;
            if(address != null)
                txtStreet.setText(address.getThoroughfare());
        }
    }

    void numberFocused() {

    }

    void numberUnfocused() {
        if(getView() == null)
            return;

        TextView txtNumber = (TextView) getView().findViewById(R.id.address_number);
        if(address == null || txtNumber.getText().length() > 0 && !txtNumber.getText().toString().equals(address.getFeatureName())) {
            isAutomaticNumber = false;
        } else {
            isAutomaticNumber = true;
        }
    }

    void moveTo(double latitude, double longitude) {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16);
        map.moveCamera(update);
    }

    void smoothMoveTo(double latitude, double longitude) {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16);
        map.animateCamera(update);
    }

    void addressLoaded(Address address, boolean moveTo) {
        this.address = address;

        if(getView() == null)
            return;

        String street = "";
        String number = "";
        if(address != null) {
            street = address.getThoroughfare();
            number = address.getFeatureName();
        }

        AutoCompleteTextView txtStreet = (AutoCompleteTextView) getView().findViewById(R.id.address_street);
        TextView txtNumber = (TextView) getView().findViewById(R.id.address_number);

        txtStreet.setAdapter(nullAdapter);
        txtStreet.setText(street);
        txtStreet.setAdapter(this.adapter);
        if(isAutomaticNumber && (number == null || number.length() == 0 || !number.equals(street)))
            txtNumber.setText(number);

        if(moveTo)
            smoothMoveTo(address.getLatitude(), address.getLongitude());
    }

    void showInvalidPositionBar()
    {
        if(getView() == null) {
            return;
        }
        View view = getView().findViewById(R.id.pickmap_invalid);
        view.setVisibility(View.VISIBLE);
    }

    void hideInvalidPositionBar()
    {
        if(getView() == null) {
            return;
        }
        View view = getView().findViewById(R.id.pickmap_invalid);
        view.setVisibility(View.GONE);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if(addressLoader != null)
            addressLoader.cancel(true);

        if(validationTask != null)
            validationTask.cancel(true);

        addressLoader = new AddressLoader(cameraPosition.target.latitude, cameraPosition.target.longitude);
        addressLoader.execute();

        validationTask = new PositionValidateTask();
        validationTask.execute(cameraPosition.target.latitude, cameraPosition.target.longitude);

        isAutomaticNumber = true;
        isAutomaticStreet = true;

        latitude = cameraPosition.target.latitude;
        longitude = cameraPosition.target.longitude;
    }

    class AddressLoader extends AsyncTask<Void, Void, List<Address>> {
        double latitude;
        double longitude;

        public AddressLoader(double lat, double lon) {
            this.latitude = lat;
            this.longitude = lon;

            addressLoaded(null, false);

            showStreetLoading();
            if(isAutomaticNumber)
                showNumberLoading();
        }

        @Override
        protected List<Address> doInBackground(Void... voids) {
            try {
                // Wait for some time before actually making the request
                Thread.sleep(500);
                return GPSUtils.getFromLocation(getActivity(), latitude, longitude);
            }
            catch (InterruptedException ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            hideStreetLoading();
            hideNumberLoading();

            if(addresses == null || addresses.size() == 0)
                return;

            addressLoaded(addresses.get(0), false);
            addressLoader = null;
        }
    }

    class PositionLoader extends AsyncTask<Void, Void, List<Address>> {
        double latitude;
        double longitude;
        String query;

        public PositionLoader(double lat, double lon, String query) {
            this.latitude = lat;
            this.longitude = lon;
            this.query = query;
        }

        @Override
        protected void onPreExecute() {
            if(getView() == null) {
                return;
            }
            showNumberLoading();
            TextView txtNumber = (TextView) getView().findViewById(R.id.address_number);
            txtNumber.setText("");
        }

        @Override
        protected List<Address> doInBackground(Void... voids) {
            return GPSUtils.getFromLocationName(getActivity(), query);
        }

        @Override
        protected void onPostExecute(List<Address> address) {
            hideNumberLoading();

            if(address == null || address.isEmpty())
                return;

            addressLoaded(address.get(0), true);
            positionLoader = null;
        }
    }

    class PositionValidateTask extends AsyncTask<Double, Void, Boolean>
    {
        @Override
        protected void onPreExecute()
        {
            hideInvalidPositionBar();
            if(listener != null)
                listener.onInvalidLocationSet();

            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Double... doubles)
        {
            try
            {
                PositionValidationResponse result = Zup.getInstance().getService().validatePosition(doubles[0], doubles[1]);
                return result != null && ((result.inside_boundaries == null) || result.inside_boundaries);
            }
            catch (RetrofitError error)
            {
                Log.e("Retrofit", "Could not validate boundaries", error);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean)
        {
            super.onPostExecute(aBoolean);
            if(aBoolean) {
                hideInvalidPositionBar();
                if (listener != null)
                    listener.onValidLocationSet();
            }
            else {
                showInvalidPositionBar();
                if (listener != null)
                    listener.onInvalidLocationSet();
            }
        }
    }
}

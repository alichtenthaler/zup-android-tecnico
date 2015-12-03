package com.ntxdev.zuptecnico.fragments;

import android.app.Activity;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.config.Constants;
import com.ntxdev.zuptecnico.entities.Place;
import com.ntxdev.zuptecnico.entities.ReportCategory;
import com.ntxdev.zuptecnico.entities.responses.PositionValidationResponse;
import com.ntxdev.zuptecnico.ui.WebImageView;
import com.ntxdev.zuptecnico.util.GPSUtils;
import com.ntxdev.zuptecnico.util.GeoUtils;
import com.ntxdev.zuptecnico.util.Utilities;
import com.ntxdev.zuptecnico.util.ViewUtils;
import com.ntxdev.zuptecnico.widgets.PlacesAutoCompleteAdapter;

import java.util.List;

import br.com.rezende.mascaras.Mask;

/**
 * Created by igorlira on 7/21/15.
 */

// FIXME: 8/3/15
public class NewPickLocationFragment extends Fragment implements GoogleMap.OnCameraChangeListener,
        AdapterView.OnItemClickListener {

    private TimerEndereco task;
    private GoogleMap map;
    public static double latitude, longitude;
    private AutoCompleteTextView tvAddress;
    private EditText tvNumber;
    private EditText tvReference;
    private EditText tvZipCode;
    private EditText tvNeighborhood;

    private SearchTask searchTask = null;
    private GeocoderTask geocoderTask = null;
    private AddressTask addressTask = null;

    private Address enderecoAtual = null;

    private String streetName = "", streetNumber = "", streetZipCode = "", streetNeighborhood = "";
    private float zoomAtual;
    private boolean ignoreUpdate = false;

    private boolean valid = false;
    private boolean hasMyLocation = false;

    private PickLocationFragment.OnLocationValidatedListener listener;

    public void setListener(PickLocationFragment.OnLocationValidatedListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pick_map_location_new, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().
                findFragmentById(R.id.pick_location_map);

        tvAddress = (AutoCompleteTextView) view.findViewById(R.id.address_street);
        tvNumber = (EditText) view.findViewById(R.id.address_number);

        tvReference = (EditText) view.findViewById(R.id.address_reference);
        tvZipCode = (EditText) view.findViewById(R.id.address_zip_number);
        tvNeighborhood = (EditText) view.findViewById(R.id.address_neighborhood);

        tvAddress.setOnItemClickListener(this);
        tvAddress.setImeActionLabel(getActivity().getString(R.string.search_title), EditorInfo.IME_ACTION_SEARCH);
        tvAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    realizarBuscaAutocomplete(v.getText().toString());
                    ViewUtils.hideKeyboard(getActivity(), v);
                    handled = true;
                }
                return handled;
            }
        });

        tvNumber.setImeActionLabel(getActivity().getString(R.string.search_title), EditorInfo.IME_ACTION_SEARCH);
        tvNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchByAddressAndNumber();
                    ViewUtils.hideKeyboard(getActivity(), v);
                    handled = true;
                }
                return handled;
            }
        });

        tvZipCode.setImeActionLabel(getActivity().getString(R.string.search_title), EditorInfo.IME_ACTION_SEARCH);
        tvZipCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchByAddressAndNumber();
                    ViewUtils.hideKeyboard(getActivity(), v);
                    handled = true;
                }
                return handled;
            }
        });

        tvNeighborhood.setImeActionLabel(getActivity().getString(R.string.search_title), EditorInfo.IME_ACTION_SEARCH);
        tvNeighborhood.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchByAddressAndNumber();
                    ViewUtils.hideKeyboard(getActivity(), v);
                    handled = true;
                }
                return handled;
            }
        });

        setAddressLoaderVisible(false);

        map = mapFragment.getMap();
        if(Utilities.isConnected(getActivity())) {
            setupMap();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        task = new TimerEndereco();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    private ReportCategory getCategory() {
        if(getArguments().get("categoryId") == null || getArguments().get("categoryId") == -1) {
            return null;
        }

        int categoryId = getArguments().getInt("categoryId");
        return Zup.getInstance().getReportCategoryService().getReportCategory(categoryId);
    }

    private void setupMap() {
        View root = getView();
        if(getCategory() != null) {
            WebImageView markerView = (WebImageView) root.findViewById(R.id.address_marker);
            getCategory().loadMarkerInto(markerView);
        }

        map.setMyLocationEnabled(true);

        if(getArguments() != null && getArguments().get("address") != null) {
            Address address = getArguments().getParcelable("address");

            valid = true;
            enderecoAtual = address;
            latitude = address.getLatitude();
            longitude = address.getLongitude();

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(address.getLatitude(), address.getLongitude()), 12));
            map.setOnCameraChangeListener(this);

            tvAddress.setText(address.getThoroughfare());
            tvNumber.setText(address.getFeatureName());

            if(getArguments().get("reference") != null)
                tvReference.setText(getArguments().getString("reference"));

            hasMyLocation = true;
        }
        else {
            map.setOnCameraChangeListener(this);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(Constants.DEFAULT_LAT, Constants.DEFAULT_LON),
                    Constants.MAP_DEFAULT_ZOOM));
        }

        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if(!hasMyLocation) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location.getLongitude()),
                            Constants.MAP_DEFAULT_ZOOM));

                    hasMyLocation = true;
                }
            }
        });
    }

    public Address getAddress() {
        if(enderecoAtual != null) {
            enderecoAtual.setFeatureName(tvNumber.getText().toString());
        }
        return enderecoAtual;
    }

    public String getReference() {
        if(getView() == null)
            return "";

        TextView txtReference = (TextView) getView().findViewById(R.id.address_reference);
        return txtReference.getText().toString();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        latitude = cameraPosition.target.latitude;
        longitude = cameraPosition.target.longitude;
        zoomAtual = cameraPosition.zoom;
        tvAddress.setAdapter(null);
        tvAddress.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.pick_map_location_suggestion, NewPickLocationFragment.class));
        tvAddress.dismissDropDown();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        realizarBuscaAutocomplete((Place) parent.getItemAtPosition(position));
        ViewUtils.hideKeyboard(getActivity(), tvAddress);
    }

    private void realizarBuscaAutocomplete(Place place) {
        if (geocoderTask != null) {
            geocoderTask.cancel(true);
        }

        geocoderTask = new GeocoderTask();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            geocoderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, place);
        } else {
            geocoderTask.execute(place);
        }
    }

    private void realizarBuscaAutocomplete(String query) {
        if (searchTask != null) {
            searchTask.cancel(true);
        }

        searchTask = new SearchTask();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
        } else {
            searchTask.execute(query);
        }
    }

    public void reload() {
        setupMap();
    }

    private class TimerEndereco extends AsyncTask<Void, String, Void> {

        private double lat, lon;
        private boolean run = true;

        public TimerEndereco() {
            lat = latitude;
            lon = longitude;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            while (run) {

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    Log.e("ZUP", e.getMessage(), e);
                }

                if(getActivity() == null)
                    continue;

                if (lat != latitude && lon != longitude) {
                    lat = latitude;
                    lon = longitude;

                    if (ignoreUpdate) {
                        ignoreUpdate = false;
                        continue;
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setAddressLoaderVisible(true);
                        }
                    });

                    List<Address> addresses = GPSUtils.getFromLocation(getActivity(), lat, lon);
                    if (!addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        if (address.getThoroughfare() != null) {
                            enderecoAtual = address;
                            try {
                                PositionValidationResponse result = Zup.getInstance().getService().validatePosition(latitude, longitude);

                                valid = result != null && ((result.inside_boundaries == null) || result.inside_boundaries);
                            } catch (Exception e) {
                                Log.e("Boundary validation", "Failed to validate boundary", e);
                            }

                            if (!address.getThoroughfare().startsWith("null")) {

                                publishProgress(address.getThoroughfare(), address.getFeatureName(), address.getPostalCode(), address.getSubLocality());
                            }
                        }
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setAddressLoaderVisible(false);
                        }
                    });
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            streetName = values[0];
            tvAddress.setAdapter(null);
            tvAddress.setText(values[0]);

            verifyValid();

            try {
                if (!values[2].isEmpty()){
                    streetZipCode = values[2];
                    tvZipCode.setText(values[2]);
                }else{
                    streetZipCode = "";
                    tvZipCode.setText("");
                }
                if (!values[3].isEmpty()){
                    streetNeighborhood = values[3];
                    tvNeighborhood.setText(values[3]);
                }else{
                    streetNeighborhood = "";
                    tvNeighborhood.setText("");
                }
                if (!values[1].isEmpty() && TextUtils.isDigitsOnly(values[1].substring(0, 1))) {
                    streetNumber = values[1];
                    tvNumber.setText(values[1]);
                } else {
                    streetNumber = "";
                    tvNumber.setText("");
                }
            } catch (Exception e) {
                Log.w("ZUP", e.getMessage() != null ? e.getMessage() : "null", e);
                streetNumber = "";
                tvNumber.setText("");
            }
            if (getActivity() != null) {
                tvAddress.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.pick_map_location_suggestion, NewPickLocationFragment.class));
                tvAddress.dismissDropDown();
            }
        }
    }

    private class AddressTask extends AsyncTask<Void, Void, Address> {

        @Override
        protected void onPreExecute() {
            setAddressLoaderVisible(true);
        }

        @Override
        protected Address doInBackground(Void... params) {
            try {
                PositionValidationResponse result = Zup.getInstance().getService().validatePosition(latitude, longitude);

                valid = result != null && ((result.inside_boundaries == null) || result.inside_boundaries);
                return GPSUtils.getFromLocation(getActivity(), latitude, longitude).get(0);
            } catch (Exception e) {
                Log.e("ZUP", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Address addr) {
            setAddressLoaderVisible(false);
            verifyValid();
            if (addr != null) {
                streetName = addr.getThoroughfare();
                if(!addr.getSubLocality().isEmpty()){
                    streetNeighborhood = addr.getSubLocality();
                    tvNeighborhood.setText(addr.getSubLocality());
                }else{
                    streetNeighborhood = "";
                    tvNeighborhood.setText("");
                }
                if(!addr.getPostalCode().isEmpty()){
                    streetZipCode = addr.getPostalCode();
                    tvZipCode.setText(addr.getPostalCode());
                }else{
                    streetZipCode = "";
                    tvZipCode.setText("");
                }
                if (!addr.getFeatureName().isEmpty() && TextUtils.isDigitsOnly(addr.getFeatureName().substring(0, 1))) {
                    streetNumber = addr.getFeatureName();
                    tvNumber.setText(addr.getFeatureName());
                } else {
                    streetNumber = "";
                    tvNumber.setText("");
                }
                tvAddress.setAdapter(null);
                tvAddress.setText(addr.getThoroughfare());
                if (getActivity() != null) {
                    tvAddress.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.pick_map_location_suggestion, NewPickLocationFragment.class));
                    tvAddress.dismissDropDown();
                }
            }
        }
    }

    private void verifyValid() {
        if (!isAdded()) return;

        if (listener != null && valid)
            listener.onValidLocationSet();
        else if(listener != null)
            listener.onInvalidLocationSet();

        if (!valid) {
            showInvalidPositionBar();
        } else if (valid) {
            hideInvalidPositionBar();
        }
    }

    void showInvalidPositionBar()
    {
        View view = getView().findViewById(R.id.pickmap_invalid);
        view.setVisibility(View.VISIBLE);
    }

    void hideInvalidPositionBar()
    {
        View view = getView().findViewById(R.id.pickmap_invalid);
        view.setVisibility(View.GONE);
    }

    void setAddressLoaderVisible(boolean visible) {
        if(getView() == null)
            return;

        tvAddress.setInputType(visible ? InputType.TYPE_NULL : InputType.TYPE_CLASS_TEXT);
        tvNumber.setInputType(visible ? InputType.TYPE_NULL : InputType.TYPE_CLASS_NUMBER);
        tvNeighborhood.setInputType(visible ? InputType.TYPE_NULL : InputType.TYPE_CLASS_TEXT);
        tvZipCode.setInputType(visible ? InputType.TYPE_NULL : InputType.TYPE_CLASS_NUMBER);
        int textColor = getResources().getColor(R.color.pick_map_location_text);
        if(visible) {
            // Trick to hide the text without actually removing it
            textColor = 0x00000000;
        }

        tvAddress.setTextColor(textColor);
        tvNumber.setTextColor(textColor);
        tvNeighborhood.setTextColor(textColor);
        tvZipCode.setTextColor(textColor);

        tvAddress.clearFocus();
        tvNumber.clearFocus();
        tvNeighborhood.clearFocus();
        tvZipCode.clearFocus();

        getView().findViewById(R.id.address_street_progress).setVisibility(visible ? View.VISIBLE : View.GONE);
        getView().findViewById(R.id.address_number_progress).setVisibility(visible ? View.VISIBLE : View.GONE);
        getView().findViewById(R.id.address_zip_number_progress).setVisibility(visible ? View.VISIBLE : View.GONE);
        getView().findViewById(R.id.address_neighborhood_progress).setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public boolean validarEndereco() {
        if (!streetName.equalsIgnoreCase(tvAddress.getText().toString())) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getActivity().getString(R.string.error_invalid_address))
                    .setMessage(getActivity().getString(R.string.error_invalid_address_message))
                    .setNegativeButton(getActivity().getString(R.string.lab_ok), null)
            .show();
            return false;
        }

        return validarEndereco(streetName, streetNumber);
    }

    private boolean validarEndereco(final String r, final String num) {
        if (r.isEmpty()) return false;

        return true;
    }

    private void searchByAddressAndNumber() {
        final String number = tvNumber.getText().toString();
        final String street = tvAddress.getText().toString();
        final String zipCode = tvZipCode.getText().toString();
        final String neighborhood = tvNeighborhood.getText().toString();

        if (validarEndereco(street, number)) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    if (enderecoAtual == null) {
                        return null;
                    }
                    List<Address> addresses = GPSUtils.getFromLocationName(getActivity(), street + ", " + number + " - " +
                            neighborhood + ", " + enderecoAtual.getSubAdminArea() != null ? enderecoAtual.getSubAdminArea() : enderecoAtual.getLocality()
                            + ", " + zipCode);
                    if (addresses.isEmpty()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), getActivity().getString(R.string.error_address_not_found), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        final Address address = addresses.get(0);

                        streetName = address.getThoroughfare();
                        if(address.getPostalCode() == null || address.getPostalCode().isEmpty()){
                            streetZipCode = "";
                        }else{
                            streetZipCode = address.getPostalCode();
                        }
                        if(address.getSubLocality() == null || address.getSubLocality().isEmpty()){
                            streetNeighborhood = "";
                        }else{
                            streetNeighborhood = address.getSubLocality();
                        }
                        if (!number.isEmpty() && TextUtils.isDigitsOnly(number.substring(0, 1))) {
                            streetNumber = number;
                        } else {
                            streetNumber = "";
                        }
                        PositionValidationResponse result = Zup.getInstance().getService()
                                .validatePosition(address.getLatitude(), address.getLongitude());
                        valid = result != null && ((result.inside_boundaries == null) || result.inside_boundaries);

                        getActivity().runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        verifyValid();
                                        ignoreUpdate = true;
                                        CameraPosition position = new CameraPosition.Builder().target(new LatLng(address.getLatitude(), address.getLongitude())).zoom(zoomAtual).build();
                                        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
                                        map.animateCamera(update);

                                        tvAddress.setAdapter(null);
                                        tvAddress.setText(streetName);
                                        tvAddress.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.pick_map_location_suggestion, NewPickLocationFragment.class));
                                        tvNumber.setText(streetNumber);
                                        tvNeighborhood.setText(streetNeighborhood);
                                        tvZipCode.setText(streetZipCode);
                                    }
                                });
                    }
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class SearchTask extends AsyncTask<String, Void, Address> {

        @Override
        protected void onPreExecute() {
            setAddressLoaderVisible(true);
        }

        @Override
        protected Address doInBackground(String... params) {
            try {
                return GeoUtils.search(params[0]);
            } catch (Exception e) {
                Log.e("ZUP", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Address addr) {
            setAddressLoaderVisible(false);
            if (!isCancelled()) {
                if (addr != null) {
                    CameraPosition p = new CameraPosition.Builder().target(new LatLng(addr.getLatitude(),
                            addr.getLongitude())).zoom(16f).build();
                    CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
                    map.animateCamera(update);
                }
            }
        }
    }

    private class GeocoderTask extends AsyncTask<Place, Void, Address> {

        @Override
        protected void onPreExecute() {
            setAddressLoaderVisible(true);
        }

        @Override
        protected Address doInBackground(Place... params) {
            try {
                return GeoUtils.getFromPlace(params[0]);
            } catch (Exception e) {
                Log.e("ZUP", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Address addr) {
            setAddressLoaderVisible(false);
            if (addr != null) {
                CameraPosition p = new CameraPosition.Builder().target(new LatLng(addr.getLatitude(),
                        addr.getLongitude())).zoom(16f).build();
                CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
                map.animateCamera(update);
            }
        }
    }
}

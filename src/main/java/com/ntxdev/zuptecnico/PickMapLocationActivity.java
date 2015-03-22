package com.ntxdev.zuptecnico;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.api.callbacks.ResourceLoadedListener;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.util.GPSUtils;
import com.ntxdev.zuptecnico.util.GeoUtils;
import com.ntxdev.zuptecnico.util.ViewUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class PickMapLocationActivity extends ActionBarActivity implements ResourceLoadedListener, GoogleMap.OnCameraChangeListener, AdapterView.OnItemClickListener {

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private int googlePlayStatus;
    //private Marker marker;
    private LocationClient locationClient;
    private boolean locationAcquired;

    private boolean gavePosition;
    private double givenLatitude;
    private double givenLongitude;

    private AddressWaitTask addressWaitTask;
    private AddressTask addressTask;
    private GeocoderTask geocoderTask;
    private NumberEditTask numberEditTask;
    private boolean manualNumber = false;
    private boolean manualNumberAnimationPending = false;

    private double latitude, longitude;

    private Address enderecoAtual = new Address(Locale.CANADA);

    private InventoryCategory category;

    class AddressAdapter extends ArrayAdapter<String> implements Filterable
    {
        private ArrayList<String> resultList;

        private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
        private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
        private static final String OUT_JSON = "/json";

        public AddressAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public int getCount() {
            if(resultList == null)
                return 0;

            return resultList.size();
        }

        @Override
        public String getItem(int position) {
            return resultList.get(position);
        }

        private ArrayList<String> autocomplete(String input) {
            ArrayList<String> resultList = null;

            HttpURLConnection conn = null;
            StringBuilder jsonResults = new StringBuilder();
            try {
                StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
                sb.append("?sensor=true&key=" + GeoUtils.PLACES_KEY);
                sb.append("&components=country:br");
                sb.append("&language=pt-BR");
                //sb.append("&types=(regions)");
                sb.append("&location=").append(getLatitude()).append(",").append(getLongitude());
                sb.append("&input=" + URLEncoder.encode(input, "utf8"));

                URL url = new URL(sb.toString());
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());

                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
            } catch (MalformedURLException e) {
                Log.e("ZUP", "Error processing Places API URL", e);
                return resultList;
            } catch (IOException e) {
                Log.e("ZUP", "Error connecting to Places API", e);
                return resultList;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            try {
                // Create a JSON object hierarchy from the results
                JSONObject jsonObj = new JSONObject(jsonResults.toString());
                JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

                // Extract the Place descriptions from the results
                resultList = new ArrayList<String>(predsJsonArray.length());
                for (int i = 0; i < predsJsonArray.length(); i++) {
                    resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
                }
            } catch (JSONException e) {
                Log.e("ZUP", "Cannot process JSON results", e);
            }

            return resultList;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }
            };
        }
    }

    double getLatitude()
    {
        return latitude;
    }

    double getLongitude()
    {
        return longitude;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_map_location);

        Zup.getInstance().initStorage(getApplicationContext());

        getSupportActionBar().hide();

        if(getIntent().hasExtra("position_latitude") && getIntent().hasExtra("position_longitude"))
        {
            gavePosition = true;
            givenLatitude = getIntent().getDoubleExtra("position_latitude", 0);
            givenLongitude = getIntent().getDoubleExtra("position_longitude", 0);
        }

        googlePlayStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        MapsInitializer.initialize(this);

        mapFragment = new SupportMapFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.mapcontainer, mapFragment).commit();

        final AutoCompleteTextView tvEndereco = (AutoCompleteTextView) findViewById(R.id.pick_location_address);
        tvEndereco.setAdapter(new AddressAdapter(this, R.layout.pick_map_location_suggestion));
        tvEndereco.setOnItemClickListener(this);

        final EditText tvNumero = (EditText) findViewById(R.id.pick_location_number);
        tvNumero.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                numeroEditado(tvNumero.getText().toString());
                return false;
            }
        });

        tvEndereco.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                {
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(tvEndereco.getWindowToken(), 0);
                }
                return false;
            }
        });

        if(getIntent().hasExtra("inventory_category_id"))
        {
            this.category = Zup.getInstance().getInventoryCategory(getIntent().getIntExtra("inventory_category_id", -1));
            if(this.category != null) {
                ImageView markerView = (ImageView) findViewById(R.id.pickmap_pin);

                Bitmap markerIcon = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_marker)).getBitmap();
                int resourceId = Zup.getInstance().getInventoryCategoryPinResourceId(category.id);
                if (resourceId != 0 && Zup.getInstance().isResourceLoaded(resourceId)) {
                    markerIcon = Zup.getInstance().getBitmap(resourceId);
                }

                markerView.setImageBitmap(markerIcon);

                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) markerView.getLayoutParams();
                lp.topMargin = -(markerIcon.getHeight() / 2);
            }
        }

        setUpMapIfNeeded();
    }

    @Override
    public void onResourceLoaded(String url, int resourceId)
    {
        if(this.category != null) {
            ImageView markerView = (ImageView) findViewById(R.id.pickmap_pin);

            Bitmap markerIcon = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_marker)).getBitmap();
            int catresourceId = Zup.getInstance().getInventoryCategoryPinResourceId(category.id);
            if (resourceId == catresourceId && catresourceId != 0 && Zup.getInstance().isResourceLoaded(catresourceId)) {
                markerIcon = Zup.getInstance().getBitmap(resourceId);
            }

            markerView.setImageBitmap(markerIcon);

            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) markerView.getLayoutParams();
            lp.topMargin = -(markerIcon.getHeight() / 2);
        }
    }

    void numeroEditado(String texto)
    {
        if(numberEditTask != null)
            numberEditTask.cancel(true);

        AutoCompleteTextView tvEndereco = (AutoCompleteTextView) findViewById(R.id.pick_location_address);

        String endereco = tvEndereco.getText().toString();
        String numero = texto;

        numberEditTask = new NumberEditTask();
        if(Build.VERSION.SDK_INT >= 11)
            numberEditTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, endereco, numero);
        else
            numberEditTask.execute(endereco, numero);
    }

    public void cancel(View view)
    {
        setResult(0);
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        String addr = (String)adapterView.getItemAtPosition(i);

        if(geocoderTask != null)
            geocoderTask.cancel(true);
        if(numberEditTask != null)
            numberEditTask.cancel(true);

        manualNumber = false;
        manualNumberAnimationPending = false;

        geocoderTask = new GeocoderTask();
        if(Build.VERSION.SDK_INT >= 11)
            geocoderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, addr);
        else
            geocoderTask.execute(addr);

        AutoCompleteTextView tvAddress = (AutoCompleteTextView)findViewById(R.id.pick_location_address);
        ViewUtils.hideKeyboard(this, tvAddress);
        tvAddress.dismissDropDown();
    }

    public void sendLocation(View view)
    {
        if(enderecoAtual == null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Aviso!");
            builder.setMessage("O endereço com base na localização ainda não foi carregado. Deste modo, dados como Endereço, número, cidade etc. deverão ser digitados manualmente.\r\n\r\nDeseja enviar mesmo assim ou esperar o endereço ser carregado?");
            builder.setPositiveButton("Enviar mesmo assim", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    sendLocation2(null);
                }
            });

            builder.setNegativeButton("Esperar", null);
            builder.show();
        }
        else
        {
            sendLocation2(view);
        }
    }

    public void sendLocation2(View view)
    {
        if(manualNumber)
        {
            EditText tvNumero = (EditText) findViewById(R.id.pick_location_number);
            enderecoAtual.setFeatureName(tvNumero.getText().toString());
        }
        getIntent().putExtra("result_latitude", map.getCameraPosition().target.latitude);
        getIntent().putExtra("result_longitude", map.getCameraPosition().target.longitude);
        getIntent().putExtra("result_address", enderecoAtual);
        //getIntent().putExtra("result_latitude", marker.getPosition().latitude);
        //getIntent().putExtra("result_longitude", marker.getPosition().longitude);
        setResult(1, getIntent());
        finish();
    }

    private void setUpMapIfNeeded()
    {
        if(map == null)
        {
            map = mapFragment.getMap();
            if(map != null)
            {
                setUpMap();
            }
        }
    }

    private void setUpMap()
    {
        if(googlePlayStatus == ConnectionResult.SUCCESS)
        {
            LatLng pos = new LatLng(-23.548338, -46.634845);
            if(gavePosition)
            {
                locationAcquired = true;
                pos = new LatLng(givenLatitude, givenLongitude);
            }

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(pos, 15);
            map.moveCamera(update);

            map.setMyLocationEnabled(true);
            if(locationClient == null)
            {
                locationClient = new LocationClient(this, new GooglePlayServicesClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        locationConnected();
                    }

                    @Override
                    public void onDisconnected() {

                    }
                }, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {

                    }
                });

                locationClient.connect();
            }

            map.setOnCameraChangeListener(this);

            //MarkerOptions markerOptions = new MarkerOptions();
            //markerOptions.position(pos);
            //markerOptions.draggable(true);
            //marker = map.addMarker(markerOptions);
        }
    }

    private void locationConnected()
    {
        locationClient.requestLocationUpdates(LocationRequest.create(), new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(!locationAcquired) {
                    locationAcquired = true;
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                    //marker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                }
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();

        Zup.getInstance().setResourceLoadedListener(this);

        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pick_map_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if(addressTask != null)
            addressTask.cancel(true);
        if(numberEditTask != null)
            numberEditTask.cancel(true);
        if(addressWaitTask != null)
            addressWaitTask.cancel(true);

        addressWaitTask = new AddressWaitTask();

        //addressTask = new AddressTask();
        double lat = cameraPosition.target.latitude;
        double lng = cameraPosition.target.longitude;

        if(manualNumber && manualNumberAnimationPending) {
            manualNumberAnimationPending = false;
        }
        else
        {
            manualNumber = false;

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                addressWaitTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, lat, lng);
                //addressTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, lat, lng);
            } else {
                addressWaitTask.execute(lat, lng);
                //addressTask.execute(lat, lng);
            }
        }
    }

    class GeocoderTask extends AsyncTask<String, Void, Address>
    {

        @Override
        protected Address doInBackground(String... strings) {
            List<Address> addressList = GPSUtils.getFromLocationName(PickMapLocationActivity.this, strings[0]);
            if(addressList.size() > 0)
                return addressList.get(0);

            return null;
        }

        @Override
        protected void onPostExecute(Address address) {
            if(address == null)
                return;

            enderecoAtual = address;

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), 15);
            map.animateCamera(update);
        }
    }

    class NumberEditTask extends AsyncTask<String, Void, Address>
    {
        @Override
        protected Address doInBackground(String... strings) {
            String endereco = strings[0];
            String numero = strings[1];

            try
            {
                Thread.sleep(300);
            }
            catch (InterruptedException ex)
            {
                return null;
            }

            List<Address> addressList = GPSUtils.getFromLocationName(PickMapLocationActivity.this, endereco + (numero.length() > 0 ? ", " + numero : "") + (
                    enderecoAtual.getSubAdminArea() != null ? enderecoAtual.getSubAdminArea() : enderecoAtual.getLocality()));

            if(addressList.size() > 0)
                return addressList.get(0);

            return null;
        }

        protected void onPostExecute(Address address) {
            if(address == null)
                return;

            enderecoAtual = address;

            manualNumber = true;
            manualNumberAnimationPending = true;
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), 15);
            map.animateCamera(update);
        }
    }

    class AddressWaitTask extends AsyncTask<Double, Void, Boolean>
    {
        double lat;
        double lng;

        @Override
        protected Boolean doInBackground(Double... doubles) {
            this.lat = doubles[0];
            this.lng = doubles[1];

            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException ex)
            {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean load) {
            super.onPostExecute(load);

            if(load)
            {
                if(addressTask != null)
                    addressTask.cancel(true);

                addressTask = new AddressTask();
                addressTask.execute(lat, lng);
            }
        }
    }

    class AddressTask extends AsyncTask<Double, Void, Address>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            enderecoAtual = null;

            AutoCompleteTextView tvEndereco = (AutoCompleteTextView) findViewById(R.id.pick_location_address);
            TextView tvNumero = (TextView) findViewById(R.id.pick_location_number);

            tvEndereco.setText("Carregando...");
            if(!manualNumber)
                tvNumero.setText("");
        }

        @Override
        protected Address doInBackground(Double... params) {
            List<Address> addressList = GPSUtils.getFromLocation(PickMapLocationActivity.this, params[0], params[1]);
            if(addressList.size() > 0)
                return addressList.get(0);
            else
                return null;
        }

        @Override
        protected void onPostExecute(Address address) {

            AutoCompleteTextView tvEndereco = (AutoCompleteTextView) findViewById(R.id.pick_location_address);
            TextView tvNumero = (TextView) findViewById(R.id.pick_location_number);

            if(address == null)
            {
                tvEndereco.setText("Indisponível");
                if(!manualNumber)
                    tvNumero.setText("0");

                return;
            }

            String rua, numero;
            rua = address.getThoroughfare();
            if (address.getFeatureName() != null && !address.getFeatureName().isEmpty()) {
                numero = address.getFeatureName();
            } else {
                numero = "";
            }

            AddressAdapter nullAdapter = null;
            tvEndereco.setAdapter(nullAdapter);
            tvEndereco.setText(rua);
            tvEndereco.setAdapter(new AddressAdapter(PickMapLocationActivity.this, R.layout.pick_map_location_suggestion));

            if(!manualNumber)
                tvNumero.setText(numero);

            enderecoAtual = address;

            super.onPostExecute(address);
        }
    }

}

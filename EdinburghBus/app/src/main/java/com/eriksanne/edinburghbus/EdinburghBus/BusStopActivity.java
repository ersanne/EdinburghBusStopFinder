package com.eriksanne.edinburghbus.EdinburghBus;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.eriksanne.edinburghbus.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * This class handles the BusStop Activity which displays google maps with a marker at the
 * BusStop location.
 * @author Erik Sanne
 */
public class BusStopActivity extends AppCompatActivity implements OnMapReadyCallback {

    LatLng stopPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stop);

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        String stringLatLong;

        if(savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if(extras == null){
                stringLatLong = null;
            } else {
                stringLatLong = extras.getString("latLang");
            }
        } else {
            stringLatLong = (String) savedInstanceState.getSerializable("latLang");
        }

        //Get the LatLng for the map pin
        String[] latlong =  stringLatLong.split(",");
        double latitude = Double.parseDouble(latlong[0]);
        double longitude = Double.parseDouble(latlong[1]);
        stopPosition = new LatLng(latitude, longitude);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        googleMap.addMarker(new MarkerOptions().position(stopPosition)
                .title("Marker"));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(stopPosition,14));



    }

}

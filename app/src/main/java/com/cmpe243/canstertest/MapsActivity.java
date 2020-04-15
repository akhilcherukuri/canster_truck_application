package com.cmpe243.canstertest;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Button startTrip, endTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in SJSU and move the camera
        LatLng SJSU = new LatLng(37.336212, -121.882324);
        mMap.addMarker(new MarkerOptions().position(SJSU).title("San Jose State University"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(SJSU));
        mMap.animateCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(SJSU.latitude,SJSU.longitude),20.0f ));
    }
}

package com.example.placestovisit;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.CaseMap;


import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Tile;

import java.io.IOException;
import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation, "Your Location");
            }
        }
    }

    public void centerMapOnLocation(Location location, String title) {
        LatLng userlocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        if(title!="Your Location"){
            Marker marker = mMap.addMarker(new MarkerOptions().position(userlocation).title(title));
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userlocation,6));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent = getIntent();
        if (intent.getIntExtra("place number", 0) == 0) {
            //zoom in user location
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    centerMapOnLocation(location, "Your Location");

                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };
            if (Build.VERSION.SDK_INT <23) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
          }
          else {
              if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
                  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                  Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                  centerMapOnLocation(lastKnownLocation, "Your Location");
              }
              else {
              ActivityCompat.requestPermissions(this ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
              }
            }


    }
        else {
            Location placeLocation=new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude((MainActivity.Location.get(intent.getIntExtra("place number", 0))).latitude);
            placeLocation.setLongitude((MainActivity.Location.get(intent.getIntExtra("place number", 0))).longitude);
            centerMapOnLocation(placeLocation,MainActivity.Places.get(intent.getIntExtra("place number", 0)));
            mMap.addMarker(new MarkerOptions().position(MainActivity.Location.get(intent.getIntExtra("place number", 0))).title(MainActivity.Places.get(intent.getIntExtra("place number", 0))));
        }
}

    @Override
    public void onMapLongClick(LatLng latLng) {
        String address="";
        Geocoder geocoder=new Geocoder(getApplicationContext(), Locale.getDefault());
        try {


            List<Address> listAddress = (List<Address>) geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if(listAddress!=null&& listAddress.size()>0){
                if(listAddress.get(0).getThoroughfare()!=null){
                    if(listAddress.get(0).getSubThoroughfare()!=null){
                        address+=listAddress.get(0).getSubThoroughfare()+"";
                    }
                    address+=listAddress.get(0).getThoroughfare();
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        if(address ==""){
            SimpleDateFormat sdf = new SimpleDateFormat("mm:HH yyyy/MMdd/_HHmmss");
            address = sdf.format(new Date());

        }
         mMap.addMarker(new MarkerOptions().position(latLng).title(address));
        MainActivity.Places.add(address);
        MainActivity.Location.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged();
        Toast.makeText(this,"location saved",Toast.LENGTH_SHORT).show();
    }
}
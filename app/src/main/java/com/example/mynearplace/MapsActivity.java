package com.example.mynearplace;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String[] PERMISSIONS_REQUIRED = new String[] {"android.permission.ACCESS_FINE_LOCATION"};
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    //Please replace localhost with IP 10.0.2.2 when your web service deployed in localhost
    final String phpURL = "http://192.168.1.13/myplace/CPlace/getListNearPlaceWithRadiusToJson";
    public MyHanlder mHander;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final double RADIUS_KM = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mHander = new MyHanlder();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

       if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
       ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

           requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE);
           return;
       }
       //Enable MyLocation button on Map
       mMap.setMyLocationEnabled(true);
       //Enable Zoom control button on Map
        mMap.getUiSettings().setZoomControlsEnabled(true);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    //Zoom level
                    float zoom = 13.0f;
                    //Move camera to current location and apply zoom level
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoom));

                    //Draw a circle with center point is current position
                    drawCircle(new LatLng(location.getLatitude(), location.getLongitude()), RADIUS_KM);
                    //Create thread to get data from php and then display result in map
                    ThreadGetData thread = new ThreadGetData();
                    thread.setRadius(RADIUS_KM);
                    thread.setLon(location.getLongitude());
                    thread.setLat(location.getLatitude());
                    thread.start();
                }

            }
        });
    }

    private void drawCircle(LatLng point, double radius_km) {
        CircleOptions circleOptions = new CircleOptions();
        //Set center of circle
        circleOptions.center(point);
        //Set radius of circle
        circleOptions.radius(radius_km * 1000);
        //Set border color of circle
        circleOptions.strokeColor(Color.BLUE);
        //Set border width of circle
        circleOptions.strokeWidth(2);
        //Adding circle to map
        Circle mapCircle = mMap.addCircle(circleOptions);
        //We can remove above circle with code bellow
        //mapCircle.remove();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                onMapReady(mMap);
            } else {
                Toast.makeText(getApplicationContext(), "Permission request denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class ThreadGetData extends Thread{
        private double radius;
        private double lat;
        private double lon;

        public void setRadius(double radius) {
            this.radius = radius;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        @Override
        public void run() {
            super.run();
            //Building a post parameter

            Map<String, String> postParam = new HashMap<>();
            postParam.put("radius", String.valueOf(radius));
            postParam.put("lat", String.valueOf(lat));
            postParam.put("lon", String.valueOf(lon));

            String jsonData = null;

            try {
                jsonData = AccessServiceAPI.getJSONStringWithParam_POST(phpURL, postParam);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Send the result get from php to Handler
            Message msg = mHander.obtainMessage(1, jsonData);
            mHander.sendMessage(msg);

        }
    }

    public class MyHanlder extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    ArrayList<PlaceMarker> listMarker = PlaceMarker.fromJsonArray(String.valueOf(msg.obj));
                    for (PlaceMarker marker : listMarker) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(new LatLng(marker.getLat(), marker.getLon()));
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        markerOptions.title(marker.getName() + " _ " + marker.getDistance() + "(km)");
                        mMap.addMarker(markerOptions);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
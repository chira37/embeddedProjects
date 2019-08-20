/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mapdemo;


import android.content.SearchRecentSuggestionsProvider;
import android.os.Bundle;

import com.example.mapdemo.directionhelpers.TaskLoadedCallback;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;
import com.example.mapdemo.directionhelpers.FetchURL;


public class MyLocationDemoActivity extends AppCompatActivity
        implements

        TaskLoadedCallback,
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private GoogleMap mMap;
    private MarkerOptions place1, place2, tracker, currentPlace;
    private Polyline currentPolyline;
    private DatabaseReference reff;
    private Location currentLocation;
    private String showMsg = "0";
    private String details = "Loading...";

    public static TextView mTime,mCount,mDetails;

    private int seat;
    private int currentPassengers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        setContentView(R.layout.my_location_demo);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mCount = (TextView)findViewById(R.id.count);
        mTime = (TextView)findViewById(R.id.time);
        mDetails = (TextView)findViewById(R.id.details);

        printDetails.setDistanceTime("0","0");


    }

    @Override
    public void onMapReady(GoogleMap map) {


        mMap = map;

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();



        LatLng newposition = new LatLng(7, 80);
        final MarkerOptions a = new MarkerOptions().position(newposition);
        final Marker myMarker = mMap.addMarker(a);



        reff = FirebaseDatabase.getInstance().getReference().child("users").child("bus1");
        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                double lan = Double.parseDouble(dataSnapshot.child("lat").getValue().toString());
                double lat = Double.parseDouble(dataSnapshot.child("lon").getValue().toString());
                LatLng newposition = new LatLng(lan, lat);

                seat = Integer.valueOf(dataSnapshot.child("seats").getValue().toString());
                currentPassengers = Integer.valueOf(dataSnapshot.child("passengers").getValue().toString());

                String id = dataSnapshot.child("id").getValue().toString();

                details = "BUS ID : " + id + "     |      Seats : " + seat;





                if(currentPassengers > 0) {
                    showMsg = "Passengers : " + (currentPassengers - 1) + "-" + (currentPassengers + 1);
                }

                else{
                    showMsg = "Passengers : 0";
                }

                if (Boolean.valueOf(dataSnapshot.child("status").getValue().toString())) {
                    myMarker.setPosition(newposition);
                    myMarker.setVisible(true);

                    enableMyLocation();

                    currentPlace = new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                    tracker = new MarkerOptions().position(newposition).title("tracker");

                    String url = getUrl(tracker.getPosition(), currentPlace.getPosition(), "driving");
                    new FetchURL(MyLocationDemoActivity.this).execute(url, "driving");

                    mCount.setText(showMsg);


                }

                else{
                    myMarker.setVisible(false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            currentLocation = mMap.getMyLocation();

        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            enableMyLocation();
        } else {
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }




    private String getUrl(LatLng origin, LatLng dest, String directionMode){
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=" + directionMode;

        String parameters = str_origin + "&" + str_dest + "&" + mode;

        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters +
                "&key=AIzaSyD6dw0GLco2k2Lg3kIz43MWUu3kd3BBEf4" ;

        return url;


    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();

        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
        mTime.setText(printDetails.getTime());
        mDetails.setText(details);


    }


}

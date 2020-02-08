package com.hackthesouth2020;

import androidx.fragment.app.FragmentActivity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.StrictMode;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnCameraMoveListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private Map<LatLng,Marker> visibleBins = new HashMap<>();
    private ServerHandler server = new ServerHandler("http://10.14.141.172:2000/trashandgo/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnCameraMoveListener(this);

//        Bin images
        BitmapDrawable smallCanDraw = (BitmapDrawable) getResources().getDrawable(R.drawable.trashcan, null);
        Bitmap smallCan = Bitmap.createScaledBitmap(smallCanDraw.getBitmap(), 100, 100, false);

        LatLng latLng = new LatLng(50.934590,-1.396520);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        mMap.setMinZoomPreference(10);

//        Get new bins from back-end, and add to the map (back-end returns .csv)
        try {

//            response = (50.932280,-1.395330,30%), (50.932470,-1.395400,0%), (50.937270,-1.401370,10%)
            String response = server.getRequest("bruhinfo"); // TODO - enter latlon
            String[] newBins = response.split(":");

            for (int i = 0; i < newBins.length; i++) {
                String[] data = newBins[i].replaceAll("[()%]","").split(",");
                System.out.println("Bin " + data[0] + " " + data[1] + " " + data[2]);

                LatLng coords = new LatLng(Double.parseDouble(data[0]), Double.parseDouble(data[1]));
                int full = Integer.parseInt(data[2]);

                visibleBins.put(coords, mMap.addMarker(new MarkerOptions().position(coords)
                        .title(full + "% full bin").icon(getBinImage(full))));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Unable to parse data sent from the back-end");
            e.printStackTrace();
        }

    }

    @Override
    public void onCameraMove() {

        for (Map.Entry<LatLng, Marker> marker : visibleBins.entrySet()) {

            double lat = marker.getValue().getPosition().latitude, lon = marker.getValue().getPosition().longitude;

            if (mMap.getCameraPosition().zoom < 12) {
                marker.getValue().setVisible(false);
            } else {
                marker.getValue().setVisible(true);
            }

        }

    }

    private BitmapDescriptor getBinImage (int full) {

        Bitmap trash;

        if (full < 33)
            trash = Bitmap.createScaledBitmap(((BitmapDrawable) getResources().getDrawable(R.drawable.greentrash, null)).getBitmap(),
                    100, 100, false);
        else if (full < 66)
            trash = Bitmap.createScaledBitmap(((BitmapDrawable) getResources().getDrawable(R.drawable.orangetrash, null)).getBitmap(),
                    100, 100, false);
        else
            trash = Bitmap.createScaledBitmap(((BitmapDrawable) getResources().getDrawable(R.drawable.redtrash, null)).getBitmap(),
                    100, 100, false);


        return BitmapDescriptorFactory.fromBitmap(trash);
    }
}

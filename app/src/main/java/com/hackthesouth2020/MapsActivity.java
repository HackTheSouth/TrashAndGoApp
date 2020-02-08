package com.hackthesouth2020;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import android.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements
        GoogleMap.OnCameraMoveListener,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.InfoWindowAdapter {
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.provider.MediaStore;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnCameraMoveListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private Map<LatLng,Marker> visibleBins = new HashMap<>();
    private ServerHandler server = new ServerHandler("http://10.14.141.172:2000/trashandgo/");
    private static final int REQUEST_LOCATION = 99;
    private LocationManager locationManager;
    private String provider;

    public static final int CAMERA_REQUEST = 9999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkLocationPermission();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public void openCamera(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();

            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

            System.out.println("BRUH BRUH BRUH BRUH BRUH BRUH BRUH BRUH BRUH BRUH BRUH BRUH BRUH BRUH BRUH BRUH BRUH BRUH BRUH ");
            System.out.println("(" + encoded + ")");
        }
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


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.934590,-1.396520), 16));
        mMap.setMinZoomPreference(10);

        mMap.setInfoWindowAdapter(this);


//        Get new bins from back-end, and add to the map (back-end returns .csv)
        try {

//            response = (50.932280,-1.395330,30%,N), (50.932470,-1.395400,0%,R), (50.937270,-1.401370,10%,R)
            String response = server.getRequest("bruhinfo"); // TODO - enter latlon
            String[] newBins = response.split(":");

            for (int i = 0; i < newBins.length; i++) {
                String[] data = newBins[i].replaceAll("[()%]","").split(",");

                LatLng coords = new LatLng(Double.parseDouble(data[0]), Double.parseDouble(data[1]));
                int full = Integer.parseInt(data[2]);
                char type = Character.toUpperCase(data[3].charAt(0));

                Marker marker = mMap.addMarker(new MarkerOptions().position(coords)
                        .title(((type == 'R') ? "Recycling" : "Normal") +" bin").icon(getBinImage(full, type)));
                marker.setSnippet(full + "," + type);//((type == 'R') ? "Recycling bin" : "Normal waste bin") + ", \n" + full + "% full.");

                visibleBins.put(coords, marker);


            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Unable to parse data sent from the back-end");
            e.printStackTrace();
        }

//        Setting personal location data
        mMap.setMyLocationEnabled(true);


    }

    @Override
    public void onCameraMove() {

        for (Map.Entry<LatLng, Marker> marker : visibleBins.entrySet()) {

            if (mMap.getCameraPosition().zoom < 14) {
                marker.getValue().setVisible(false);
            } else {
                marker.getValue().setVisible(true);
            }

        }

    }

    private BitmapDescriptor getBinImage(int full, char type) {

        Bitmap trash;

        if (type == 'R') {
            if (full < 33)
                trash = Bitmap.createScaledBitmap(((BitmapDrawable) getResources().getDrawable(R.drawable.greenrecycle, null)).getBitmap(),
                        100, 100, false);
            else if (full < 66)
                trash = Bitmap.createScaledBitmap(((BitmapDrawable) getResources().getDrawable(R.drawable.orangerecycle, null)).getBitmap(),
                        100, 100, false);
            else
                trash = Bitmap.createScaledBitmap(((BitmapDrawable) getResources().getDrawable(R.drawable.redrecycle, null)).getBitmap(),
                        100, 100, false);

        } else {

            if (full < 33)
                trash = Bitmap.createScaledBitmap(((BitmapDrawable) getResources().getDrawable(R.drawable.greentrash, null)).getBitmap(),
                        100, 100, false);
            else if (full < 66)
                trash = Bitmap.createScaledBitmap(((BitmapDrawable) getResources().getDrawable(R.drawable.orangetrash, null)).getBitmap(),
                        100, 100, false);
            else
                trash = Bitmap.createScaledBitmap(((BitmapDrawable) getResources().getDrawable(R.drawable.redtrash, null)).getBitmap(),
                        100, 100, false);

        }


        return BitmapDescriptorFactory.fromBitmap(trash);
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                String text = "Please give location permission to use this app.";
                new AlertDialog.Builder(this)
                        .setTitle(text)
                        .setMessage(text)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
//                Request permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == REQUEST_LOCATION) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED)
                    locationManager.requestLocationUpdates(provider, 400, 1, this);

            } else {

                System.err.println("ERROR: Location denied.");

            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public View getInfoWindow(Marker arg0) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        //marker snippet = "fullPercentage,type"
        int full = Integer.parseInt(marker.getSnippet().split(",")[0]);
        String type = (marker.getSnippet().split(",")[1] == "R") ? "Recycling" : "Normal waste";

        LinearLayout info = new LinearLayout(MapsActivity.this);
        info.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(MapsActivity.this);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, Typeface.BOLD);
        title.setText(marker.getTitle());

        TextView snippet = new TextView(MapsActivity.this);
        snippet.setText();

//        TextView snippet1 = new TextView(MapsActivity.this);
//        snippet1.setTextColor(Color.GRAY);
//        snippet1.setText(type + "bin, ");
//
//        TextView snippet2 = new TextView(MapsActivity.this);
//        snippet2.setTextColor(Color.GREEN);
//        snippet2.setText(full + "%");
//
//        TextView snippet3 = new TextView(MapsActivity.this);
//        snippet3.setTextColor(Color.GRAY);
//        snippet3.setText(" full");
//        TextView s = new TextView()

        info.addView(title);
        info.addView(snippet);
//        info.addView(snippet2);
//        info.addView(snippet3);

        return info;
    }
}

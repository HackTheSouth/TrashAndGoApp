package com.hackthesouth2020;

import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import android.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.provider.MediaStore;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnCameraMoveListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private Map<LatLng,Marker> visibleBins = new HashMap<>();
    private ServerHandler server = new ServerHandler("http://10.14.141.172:2000/trashandgo/");

    public static final int CAMERA_REQUEST = 9999;

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
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

            //System.out.println("(" + encoded + ")");

            int postDataLength = byteArray.length;
            String request = "http://10.14.141.172:2000/trashandgo/image";
            URL url = null;
            try {
                url = new URL(request);

            } catch (MalformedURLException e) {
                e.printStackTrace();

            }
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                conn.setRequestMethod("POST");
            } catch (ProtocolException e) {

                e.printStackTrace();
            }
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(byteArray.length));
            conn.setDoOutput(true);
            try {
                conn.getOutputStream().write(byteArray);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Reader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                for (int c; (c = in.read()) >= 0; )
                    System.out.print((char) c);
            }
            catch(Exception e){System.out.println("dsdsdadssdadsaddasddsdsadsadsadsa");}
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

//        Bin images
        BitmapDrawable smallCanDraw = (BitmapDrawable) getResources().getDrawable(R.drawable.trashcan, null);
        Bitmap smallCan = Bitmap.createScaledBitmap(smallCanDraw.getBitmap(), 100, 100, false);

        LatLng latLng = new LatLng(50.934590,-1.396520);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        mMap.setMinZoomPreference(10);
//        mMap.setMyLocationEnabled(true);


//        Get new bins from back-end, and add to the map (back-end returns .csv)
        try {

//            response = (50.932280,-1.395330,30%,N), (50.932470,-1.395400,0%,R), (50.937270,-1.401370,10%,R)
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

            if (mMap.getCameraPosition().zoom < 14) {
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

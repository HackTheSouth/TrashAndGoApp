package com.hackthesouth2020;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements
        GoogleMap.OnCameraMoveListener,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.InfoWindowAdapter {

    private GoogleMap mMap;
    private Map<LatLng,Marker> visibleBins = new HashMap<>();
    private ServerHandler server = new ServerHandler("http://10.14.141.172:2000/trashandgo/");
    private static final int REQUEST_LOCATION = 99;
    private LocationManager locationManager;
    private String provider;
    protected static int trashPoints = 0;

    public static final int CAMERA_REQUEST = 1;
    private static final int SCALE_RATIO = 3;
    private static final String TAG = "MainActivity";
    private LinearLayout popups;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkLocationPermission();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_maps);

        popups = (LinearLayout) findViewById(R.id.popup_layout);

        if (popups.getParent() != null)
            ((ViewGroup) popups.getParent()).removeView(popups);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public void openCamera(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.hackthesouth2020.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth)
    {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }


    public void openRewardMenu(View view){
        startActivity(new Intent(MapsActivity.this, Reward.class));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST) {
            File file = new File(mCurrentPhotoPath);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media
                        .getBitmap(getContentResolver(), Uri.fromFile(file));
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Resize bitmap so it doesn't crash the app
            bitmap = getResizedBitmap(bitmap, bitmap.getWidth() / SCALE_RATIO, bitmap.getHeight() / SCALE_RATIO);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

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

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                String reply;
                while ((reply = in.readLine()) != null) {
                    long barcode = Long.parseLong(reply.split(",")[0]);
                    String name = reply.split(",")[1].replace(":", "");

                    int points = (int) (barcode % 5) + 5;
                    trashPoints += points;

                    createDialog(points, barcode, name);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
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
                marker.setSnippet(full + "," + type);

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
        String type = (marker.getSnippet().split(",")[1].trim().equals("R")) ? "Recycling" : "Normal waste";

        LinearLayout info = new LinearLayout(MapsActivity.this);
        info.setOrientation(LinearLayout.VERTICAL);
        StyleSpan bold = new StyleSpan(Typeface.BOLD);

        TextView title = new TextView(MapsActivity.this);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, Typeface.BOLD);
        title.setText(marker.getTitle());

        TextView binSnippet = new TextView(MapsActivity.this);
        SpannableString span1 = new SpannableString(type + " bin,");
        ForegroundColorSpan colour1 = new ForegroundColorSpan((type.charAt(0) == 'R') ? Color.rgb(11, 122, 238) : Color.rgb(11, 111, 0));
        span1.setSpan(colour1, 0, type.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binSnippet.setText(span1);

        TextView fullSnippet = new TextView(MapsActivity.this);
        SpannableString span2 = new SpannableString(full + "% full.");
        ForegroundColorSpan colour2 = new ForegroundColorSpan(
                (full < 33) ? Color.rgb(15, 240, 15) : (full < 66) ? Color.rgb(255, 153, 0) : Color.rgb(255, 0, 0));
        span2.setSpan(colour2, 0, String.valueOf(full).length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span2.setSpan(bold, 0, String.valueOf(full).length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        fullSnippet.setText(span2);

        info.addView(title);
        info.addView(binSnippet);
        info.addView(fullSnippet);

        return info;
    }

    public void createDialog(int pointsEarned, long barcode, String name) {

        String replyText = "Congratulations for putting your " + (name.equals("Unknown Product") ? "stuff (barcode: " + barcode + ")" : name) + " in the trash, you earned " + pointsEarned + " trash points!";

        if (popups == null) {
            System.err.println("ERROR: in createDialog() method, LinearLayout is null.");
            Toast.makeText(MapsActivity.this, replyText, Toast.LENGTH_SHORT).show();
            return;
        }

        if (popups.getParent() != null)
            ((ViewGroup) popups.getParent()).removeView(popups);

        for (int i = 0; i < popups.getChildCount(); i++) {
            if (popups.getChildAt(i) instanceof TextView) {
                ((TextView) popups.getChildAt(i)).setText(replyText);
            }
        }

        new AlertDialog.Builder(this).setView(popups)
                .setNegativeButton("View points",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: View Points Called.");
                        Toast.makeText(MapsActivity.this, "Your points",Toast.LENGTH_SHORT).show();
                        openRewardMenu(popups);


                    }
                })
                .setPositiveButton(
                "Okay",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "onClick: OK Called.");
                        Toast.makeText(MapsActivity.this, "Great job",Toast.LENGTH_SHORT).show();
                    }
                }).create().show();
    }

}

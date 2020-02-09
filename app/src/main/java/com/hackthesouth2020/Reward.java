package com.hackthesouth2020;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;

public class Reward extends AppCompatActivity {

    private Button mButton1,mButton2,mButton3,mButton4,mButton5,mButton6,mButton7,mButton8;

    private static final String TAG = "MainActivity";
    private LinearLayout popups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward);

        popups = (LinearLayout) findViewById(R.id.popup_layout);

        if (popups.getParent() != null)
            ((ViewGroup) popups.getParent()).removeView(popups);

        mButton1 = (Button) findViewById(R.id.sals);
        mButton2 = (Button) findViewById(R.id.dominos);
        mButton3 = (Button) findViewById(R.id.urban_outfitters);
        mButton4 = (Button) findViewById(R.id.my_protein);
        mButton5 = (Button) findViewById(R.id.game);
        mButton6 = (Button) findViewById(R.id.apple);
        mButton7 = (Button) findViewById(R.id.puregym);
        mButton8 = (Button) findViewById(R.id.nandos);

        TextView text = (TextView) findViewById(R.id.pointtotal);
        text.setText("Points: " + MapsActivity.trashPoints);


        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            //message = cost +
            public void onClick(View view) {
                customDialog("Sals £10 voucher","Redeem a £10 voucher for sals! This will cost you 100 TP.");
            }
        });

        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog("35% off at Dominos","Get 35% off on two large pizzas on your " +
                        "next order from dominos! This will cost you 80 TP.");
            }
        });

        mButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog("Get 30% off at Urban Outfitters","Redeem a voucher for 30% off " +
                        "at Urban Outfitters! This will cost you 800 TP.");
            }
        });

        mButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog("60% off at My Protein","Get 60% off your next purchase at My " +
                        "Protein. This will cost you 600 TP.");
            }
        });

        mButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog("£10 off at Game","Get £10 off your next purchase at Game! " +
                        "This will cost you 400 TP.");
            }
        });

        mButton6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog("Get £10 off at Apple","Earn £10 off your next purchase at Apple! " +
                        "This will cost you 400 TP.");
            }
        });

        mButton7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog("Get 40% off at PureGym","Get 40% off your first months membership " +
                        "at PureGym. This will cost you 350 TP.");
            }
        });

        mButton8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog("Get a free main course Nandos","Get any main course from Nandos for " +
                        "free! This will cost you 750 TP.");
            }
        });
    }

    private void cancelMethod(){
        Log.d(TAG, "cancelMethod: Called.");
        toastMessage("Cancelled.");
    }

    private void okMethod(){
        Log.d(TAG, "okMethod: Called.");
        toastMessage("Code Redeemed.");
    }

    public void customDialog(String title, String message){

        if (popups == null)
            return;

        if (popups.getParent() != null)
            ((ViewGroup) popups.getParent()).removeView(popups);

        ((TextView) popups.getChildAt(1)).setText(title);
        ((TextView) popups.getChildAt(2)).setText(message);

        new android.app.AlertDialog.Builder(this).setView(popups)
                .setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "onClick: Cancel Called.");
                                cancelMethod();

                            }
                        })
                .setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(TAG, "onClick: OK Called.");
                                okMethod();
                            }
                        }).create().show();
    }

    public void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    public void backButton(View view){
        startActivity(new Intent(Reward.this, MapsActivity.class));
    }
}

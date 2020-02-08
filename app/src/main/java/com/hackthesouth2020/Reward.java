package com.hackthesouth2020;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.DialogInterface;

public class Reward extends AppCompatActivity {

    private Button mButton1,mButton2,mButton3,mButton4,mButton5,mButton6,mButton7,mButton8;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward);

        mButton1 = (Button) findViewById(R.id.sals);
        mButton2 = (Button) findViewById(R.id.dominos);
        mButton3 = (Button) findViewById(R.id.urban_outfitters);
        mButton4 = (Button) findViewById(R.id.my_protein);
        mButton5 = (Button) findViewById(R.id.game);
        mButton6 = (Button) findViewById(R.id.apple);
        mButton7 = (Button) findViewById(R.id.puregym);
        mButton8 = (Button) findViewById(R.id.nandos);


        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            //message = cost +
            public void onClick(View view) {
                customDialog("Redeem Code","100TP \n" +
                        "£4 off meal");
            }
        });

        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog("Redeem Code","300TP \n" +
                        "35% off on two large pizzas");
            }
        });

        mButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog("Redeem Code","800TP \n" +
                        "30% off on purchase");
            }
        });

        mButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog("Redeem Code","600TP \n" +
                        "60% off on whey protein purchases");
            }
        });

        mButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog("Redeem Code","800TP \n" +
                        "£10 off on purchase");
            }
        });

        mButton6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog("Redeem Code","1000TP \n" +
                        "£10 gift card");
            }
        });

        mButton7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog("Redeem Code","500TP \n" +
                        "40% off for a month");
            }
        });

        mButton8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog("Redeem Code","1500TP \n" +
                        "100% off meal");
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

    /**
     * Custom alert dialog that will execute method in the class
     * @param title
     * @param message
     */
    public void customDialog(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.nandos);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Cancel Called.");
                        cancelMethod();

                    }
                });

        builder.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "onClick: OK Called.");
                        okMethod();
                    }
                });
        builder.show();
    }

    /**
     * customizable toast
     * @param message
     */
    public void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    public void backButton(View view){
        startActivity(new Intent(Reward.this, MapsActivity.class));
    }
}

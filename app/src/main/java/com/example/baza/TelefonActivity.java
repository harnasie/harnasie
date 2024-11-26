package com.example.baza;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TelefonActivity extends AppCompatActivity {

    private ImageView phone1, phone2, phone3, copy1, copy2, copy3;
    private LinearLayout menuLayout;
    private Button btnchart, btnuser, btndanger, btnmap, btnTelefon, btnMenu;
    private FrameLayout background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarmphones);
        setTitle("Telefony alarmowe");
        this.phone1 = findViewById(R.id.phone1);
        this.phone2 = findViewById(R.id.phone2);
        this.phone3 = findViewById(R.id.phone3);
        this.copy1 = findViewById(R.id.copy1);
        this.copy2 = findViewById(R.id.copy2);
        this.copy3 = findViewById(R.id.copy3);

        this.phone1.setOnClickListener(view -> this.call_phone1());
        this.phone2.setOnClickListener(view -> this.call_phone2());
        this.phone3.setOnClickListener(view -> this.call_phone3());
        this.copy1.setOnClickListener(view -> this.copy_phone1());
        this.copy2.setOnClickListener(view -> this.copy_phone2());
        this.copy3.setOnClickListener(view -> this.copy_phone3());

        menuLayout = findViewById(R.id.menuLayout);
        btnchart = findViewById(R.id.chart);
        btnuser = findViewById(R.id.userView);
        btnTelefon = findViewById(R.id.buttonTelefon);
        btndanger = findViewById(R.id.danger);
        btnMenu = findViewById(R.id.showMenuButton);
        background = findViewById(R.id.background);
        btnmap = findViewById(R.id.btnmap);
                btnmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TelefonActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });



        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuLayout.setVisibility(View.VISIBLE);
                btnMenu.setVisibility(View.GONE);
            }
        });



        btnchart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TelefonActivity.this, ChartActivity.class);
                startActivity(intent);
            }
        });

        btnTelefon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TelefonActivity.this, TelefonActivity.class);
                startActivity(intent);
            }
        });



        btndanger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TelefonActivity.this, DangerActivity.class);
                startActivity(intent);
            }
        });

        btnuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TelefonActivity.this, UserActivity.class);
                startActivity(intent);
            }
        });
    }

    private void call_phone1(){
        callPhoneNumber("515964042");

    }
    private void call_phone2(){
        callPhoneNumber("112");
    }
    private void call_phone3(){

    }
    private void copy_phone1(){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipdata = ClipData.newPlainText(null, "112");
        if(clipboard == null) return;
        clipboard.setPrimaryClip(clipdata);
        Toast.makeText(this, "Numer alarmowy został skopiowany", Toast.LENGTH_SHORT).show();
    }

    private void copy_phone2(){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipdata = ClipData.newPlainText(null, "985");
        if(clipboard == null) return;
        clipboard.setPrimaryClip(clipdata);
        Toast.makeText(this, "Numer alarmowy został skopiowany", Toast.LENGTH_SHORT).show();
    }
    private void copy_phone3(){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipdata = ClipData.newPlainText(null, "601 100 300");
        if(clipboard == null) return;
        clipboard.setPrimaryClip(clipdata);
        Toast.makeText(this, "Numer alarmowy został skopiowany", Toast.LENGTH_SHORT).show();
    }

    public void callPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

}



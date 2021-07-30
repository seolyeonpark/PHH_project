package com.example.hospital;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Intent intent = getIntent();
        TextView View1 = (TextView)findViewById(R.id.patient_year);
        String textyear = intent.getExtras().getString("textyear");
        TextView View2 = (TextView)findViewById(R.id.patient_time);
        String texttime = intent.getExtras().getString("texttime");
        //TextView View3 = (TextView)findViewById(R.id.patient_name);
        //String textname1 = intent.getExtras().getString("textname1");
        //TextView View4 = (TextView)findViewById(R.id.patient_name2);
        //String textname2 = intent.getExtras().getString("textname2");
        View1.setText(textyear);
        View2.setText(texttime);
        //View3.setText(textname1);
        //View4.setText(textname2);
        TextView textyear2 = findViewById(R.id.patient_year2);
        TextView texttime2 = findViewById(R.id.patient_time2);
        textyear2.setText("2021/8/3");
        texttime2.setText("7:45");


        Button button1 = findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SubActivity1.class);
                startActivity(intent);
            }
        });

        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SubActivity2.class);
                startActivity(intent);
            }
        });
    }
}
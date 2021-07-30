package com.example.phh;

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
        //환자 이름 값 받기
        Intent intent = getIntent();
        TextView View1 = (TextView)findViewById(R.id.TextView);
        String sendData = intent.getExtras().getString("sendData");
        View1.setText("환자 이름: " + sendData);
        //환자 버튼
        Button button1 = findViewById(R.id.patient);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //화면 전환 + 환자 이름 값 넘기기(환자 화면)
                Intent intent1 = new Intent(getApplicationContext(), PatientMainActivity.class);
                intent1.putExtra("sendData", sendData);
                startActivity(intent1);
                finish();
            }
        });
        //보호자 버튼
        Button button2 = findViewById(R.id.guard);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //화면 전환 + 환자 이름 값 넘기기(보호자 화면)
                Intent intent2 = new Intent(getApplicationContext(), GuardMainActivity.class);
                intent2.putExtra("sendData", sendData);
                startActivity(intent2);
                finish();
            }
        });
    }
}
package com.example.phh;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.phh.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class GuardMainActivity extends AppCompatActivity {
    private TextView O2_move;
    private TextView heart_move;
    private  TextView Medical_appointment_move;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guard_main);
        //환자 이름 값 받기 + 출력
        TextView View2 = (TextView)findViewById(R.id.textView8);
        Intent intent = getIntent();
        String sendData = intent.getExtras().getString("sendData");
        View2.setText(sendData+" 님");

        O2_move = findViewById(R.id.O2);
        heart_move = findViewById(R.id.heart);
        Medical_appointment_move = findViewById(R.id.MedicalAppointment);

        O2_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //화면 전환 + 환자 이름 값 넘기기
                Intent intent1 = new Intent(GuardMainActivity.this, GuardO2chart.class);
                intent1.putExtra("sendData", sendData);
                startActivity(intent1);
                finish(); //<-보호자산소포화도화면으로 이동
            }
        });

        heart_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //화면 전환 + 환자 이름 값 넘기기
                Intent intent2 = new Intent(GuardMainActivity.this, GuardHeartchart.class);
                intent2.putExtra("sendData", sendData);
                startActivity(intent2);
                finish(); // <-보호자심박수화면으로 이동
            }
        });

        Medical_appointment_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //화면 전환 + 환자 이름 값 넘기기
                Intent intent3 = new Intent(GuardMainActivity.this, GuardAppointment.class);
                intent3.putExtra("sendData", sendData);
                startActivity(intent3);
                finish(); //<-진료예약화면으로 이동
            }
        });

        //진료차트
        BarChart barChart = findViewById(R.id.barChart);

        ArrayList<BarEntry> visitors = new ArrayList<>();
        visitors.add(new BarEntry(0401,75));
        visitors.add(new BarEntry(0401,99));
        visitors.add(new BarEntry(0402,77));
        visitors.add(new BarEntry(0402,98));
        visitors.add(new BarEntry(0403,78));
        visitors.add(new BarEntry(0403,98));
        visitors.add(new BarEntry(0404,80));
        visitors.add(new BarEntry(0404,100));

        BarDataSet barDataSet = new BarDataSet(visitors, "visitors");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f);

        BarData barData = new BarData(barDataSet);

        barChart.setFitBars(true);
        barChart.setData(barData);
        barChart.getDescription().setText("Bar Chart Example");
        barChart.animateY(2000);
    }
}
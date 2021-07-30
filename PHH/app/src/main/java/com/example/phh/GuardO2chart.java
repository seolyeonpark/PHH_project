package com.example.phh;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.example.phh.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class GuardO2chart extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guard_o2chart);
        //환자 이름 값 받기 + 출력
        TextView View3 = (TextView)findViewById(R.id.textView8);
        Intent intent = getIntent();
        String sendData = intent.getExtras().getString("sendData");
        View3.setText(sendData+" 님");

        BarChart barChart = findViewById(R.id.barChart);

        ArrayList<BarEntry> patientO2 = new ArrayList<>();
        patientO2.add(new BarEntry(0401,75));
        patientO2.add(new BarEntry(0401,99));
        patientO2.add(new BarEntry(0402,77));
        patientO2.add(new BarEntry(0402,98));
        patientO2.add(new BarEntry(0403,78));
        patientO2.add(new BarEntry(0403,98));
        patientO2.add(new BarEntry(0404,80));
        patientO2.add(new BarEntry(0404,100));

        BarDataSet barDataSet = new BarDataSet(patientO2, "일일 산소포화도");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f);

        BarData barData = new BarData(barDataSet);

        barChart.setFitBars(true);
        barChart.setData(barData);
        barChart.getDescription().setText("환자의 산소포화도 차트");
        barChart.animateY(2000);
    }
}
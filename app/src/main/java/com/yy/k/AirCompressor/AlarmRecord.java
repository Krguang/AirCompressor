package com.yy.k.AirCompressor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AlarmRecord extends Activity {



    private List<String> listTime=new ArrayList<>();
    private List<String> listData=new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_record);

        ListView listView = findViewById(R.id.lv_alarm_record);


        for (int i=0;i<200;i++){
            listTime.add("2018-08-18 11:"+i);
        }

        for (int i=0;i<200;i++){
            listData.add("压强过高");
        }

        final MyAdapter adapter=new MyAdapter();

        listView.setAdapter(adapter);

    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return listTime.size();
        }

        @Override
        public Object getItem(int i) {
            return listTime.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if (view==null){
                view= LayoutInflater.from(AlarmRecord.this).inflate(R.layout.listview_item,null);
            }

            TextView tv_time= view.findViewById(R.id.tv_time);
            TextView tv_data= view.findViewById(R.id.tv_data);

            tv_time.setText(listTime.get(i));
            tv_data.setText(listData.get(i));

            return view;
        }


    }
}


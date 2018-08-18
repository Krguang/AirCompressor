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


    MyAdapter adapter;
    ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_record);

        listView = findViewById(R.id.lv_alarm_record);
        adapter=new MyAdapter();
        listView.setAdapter(adapter);
    }

    public void bt_alarm_clean(View view) {

        MainActivity.listData.clear();
        MainActivity.listTime.clear();

        adapter=new MyAdapter();
        listView.setAdapter(adapter);
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return MainActivity.listTime.size();
        }

        @Override
        public Object getItem(int i) {
            return MainActivity.listTime.get(i);
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

            tv_time.setText(MainActivity.listTime.get(i));
            tv_data.setText(MainActivity.listData.get(i));

            if (MainActivity.listData.get(i) == "超压"){
                tv_data.setTextColor(Color.RED);
            }

            if (MainActivity.listData.get(i) == "欠压"){
                tv_data.setTextColor(Color.YELLOW);
            }

            if (MainActivity.listData.get(i) == "恢复正常"){
                tv_data.setTextColor(Color.BLACK);
            }

            return view;
        }

    }
}


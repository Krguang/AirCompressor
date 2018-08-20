package com.yy.k.AirCompressor;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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

    private  List<String> listTime=new ArrayList<>();
    private  List<String> listData=new ArrayList<>();

    SharedPreferences sharedPreferencesAlarmRecord;
    SharedPreferences.Editor editorAlarmRecord;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_record);

        sharedPreferencesAlarmRecord = this.getSharedPreferences("PreferencesAlarmRecord",this.MODE_WORLD_WRITEABLE);
        editorAlarmRecord = sharedPreferencesAlarmRecord.edit();

        listData.clear();
        listTime.clear();

        int size = sharedPreferencesAlarmRecord.getInt("listTime_size",0);

        if (size > 10000){      //报警记录大于10000条，自动清空

            alarmClear();

        }

        for (int i = 0;i<size;i++){

            listTime.add(sharedPreferencesAlarmRecord.getString("listTime_"+i,null));
            listData.add(sharedPreferencesAlarmRecord.getString("listData_"+i,null));
        }

        listView = findViewById(R.id.lv_alarm_record);
        adapter=new MyAdapter();
        listView.setAdapter(adapter);

    }

    public void bt_alarm_clean(View view) {

        alarmClear();
    }


    private void alarmClear(){

        editorAlarmRecord.clear();          //清空sharedPreferences
        editorAlarmRecord.apply();

        listData.clear();                   //清空arraylist
        listTime.clear();

        adapter=new MyAdapter();
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

            if (listData.get(i).equals("超压")){
                tv_data.setTextColor(Color.RED);
            }

            if (listData.get(i).equals("欠压")){
                tv_data.setTextColor(0xffffff00);
            }

            if (listData.get(i).equals("恢复正常")){
                tv_data.setTextColor(0xff00cc00);
            }

            return view;
        }

    }
}


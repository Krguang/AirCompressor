package com.yy.k.AirCompressor;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PastRecord extends Activity {

    private List<String> listTime=new ArrayList<>();
    private List<String> listTemp=new ArrayList<>();
    private List<String> listHumi=new ArrayList<>();
    private List<String> listPress=new ArrayList<>();

    SharedPreferences sharedPreferencesPastRecord;
    SharedPreferences.Editor editorPastRecord;

    PastRecord.MyAdapter adapter;
    ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.past_record);

        sharedPreferencesPastRecord = this.getSharedPreferences("PreferencesPastRecord",this.MODE_WORLD_WRITEABLE);
        editorPastRecord = sharedPreferencesPastRecord.edit();

        listTime.clear();
        listTemp.clear();
        listHumi.clear();
        listPress.clear();

        int size = sharedPreferencesPastRecord.getInt("listPastTime_size",0);

        for (int i = 0;i<size;i++){

            listTime.add(sharedPreferencesPastRecord.getString("listPastTime_"+i,null));
            listTemp.add(sharedPreferencesPastRecord.getString("listPastTemp_"+i,null));
            listHumi.add(sharedPreferencesPastRecord.getString("listPastHumi_"+i,null));
            listPress.add(sharedPreferencesPastRecord.getString("listPastPress_"+i,null));

        }

        listView = findViewById(R.id.lv_past_record);
        adapter=new PastRecord.MyAdapter();
        listView.setAdapter(adapter);
    }


    private void alarmClear(){

        MainActivity.listPastTime.clear();
        MainActivity.listPastTemp.clear();
        MainActivity.listPastHumi.clear();
        MainActivity.listPastPress.clear();

        editorPastRecord.clear();          //清空sharedPreferences
        editorPastRecord.apply();

        listTime.clear();
        listTemp.clear();
        listHumi.clear();
        listPress.clear();

        adapter=new PastRecord.MyAdapter();
        listView.setAdapter(adapter);
    }

    public void bt_past_clean(View view) {

        alarmClear();
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
                view= LayoutInflater.from(PastRecord.this).inflate(R.layout.listview_item_pastrecord,null);
            }

            TextView tv_time = view.findViewById(R.id.tv_pr_time);
            TextView tv_temp = view.findViewById(R.id.tv_pr_temp);
            TextView tv_Humi = view.findViewById(R.id.tv_pr_humi);
            TextView tv_Press = view.findViewById(R.id.tv_pr_press);

            tv_time.setText(listTime.get(i));
            tv_temp.setText(listTemp.get(i));
            tv_Humi.setText(listHumi.get(i));
            tv_Press.setText(listPress.get(i));

            return view;
        }
    }
}

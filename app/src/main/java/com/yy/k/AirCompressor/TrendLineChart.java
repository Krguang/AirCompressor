package com.yy.k.AirCompressor;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Window;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;


public class TrendLineChart extends Activity{


    LineChart lineChart;
    Description description;
    List<Entry> entriesTemp;
    List<Entry> entriesHumi;
    List<Entry> entriesPress;

    private Timer timer1s=new Timer();

    float[] tempArray = new float[30];
    float[] humiArray = new float[30];
    float[] pressArray = new float[30];

    SharedPreferences sharedPreferencesSystemSet;
    private int pressUpperLimit;
    private int pressLowLimit;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.line_chart);

        sharedPreferencesSystemSet = this.getSharedPreferences("PreferencesSystemSet",this.MODE_WORLD_WRITEABLE);

        pressUpperLimit = sharedPreferencesSystemSet.getInt("压差上限",500);
        pressLowLimit = sharedPreferencesSystemSet.getInt("压差下限",-500);

        lineChartInit();

        timer1s.schedule(taskPoll,1000,1000);//5ms后开始，每5ms轮询一次
    }


    TimerTask taskPoll = new TimerTask() {
        @Override
        public void run() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    lineChartUpdate(ModbusSlave.temperature,ModbusSlave.humidity,ModbusSlave.pressure);//每秒刷新一次图表

                }
            });
        }
    };

    private void lineChartUpdate(int temp,int humi,int press){

        for (int i = 0;i<29;i++ ){                  //数组向前移位

            tempArray[i] = tempArray[i+1];
            humiArray[i] = humiArray[i+1];
            pressArray[i] = pressArray[i+1];

        }

        tempArray[29] = (float) (temp*(pressUpperLimit-pressLowLimit)/1000.0 + pressLowLimit);  //读取的实时数据填补到最后一位
        humiArray[29] = (float) (humi*(pressUpperLimit-pressLowLimit)/1000.0 + pressLowLimit);
        pressArray[29] = (float) (press*(pressUpperLimit-pressLowLimit)/1000.0 + pressLowLimit);   //0-1000转换成-250 -- 50

        if (entriesTemp != null){                   //清空上一次的List
            entriesTemp.clear();
        }

        if (entriesHumi != null){

            entriesHumi.clear();
        }

        if (entriesPress != null){

            entriesPress.clear();
        }

        for (int i = 0 ; i < 30 ; i++){                     //把数组数据填到List中
            entriesTemp.add(new Entry(i ,tempArray[i]));
            entriesHumi.add(new Entry(i ,humiArray[i]));
            entriesPress.add(new Entry(i ,pressArray[i]));
        }

        LineDataSet set = new LineDataSet(entriesTemp, "压差1(KPa)");
        set.setColor(Color.RED);                                     //设置线条颜色
        set.setDrawValues(true);                                      //设置显示数据点值
        set.setValueTextColor(Color.RED);                             //设置显示值的字体颜色
        set.setValueTextSize(12);                                     //设置显示值的字体大小

        //再创建一个折线对象set1：
        LineDataSet set1 = new LineDataSet(entriesHumi,"压差2(KPa)");
        set1.setColor(getResources().getColor(R.color.colorPrimaryDark));    //给线条设置颜色
        //set1.setAxisDependency(YAxis.AxisDependency.RIGHT);                  //设置该折线的y值依赖右y轴 也可不设置 默认都依赖左y轴
        set1.setDrawValues(true);
        set1.setValueTextColor(getResources().getColor(R.color.colorPrimaryDark));
        set1.setValueTextSize(12);

        //再创建一个折线对象set1：
        LineDataSet set2 = new LineDataSet(entriesPress,"压差3(KPa)");
        set2.setColor(Color.BLACK);    //给线条设置颜色
        set2.setAxisDependency(YAxis.AxisDependency.RIGHT);                  //设置该折线的y值依赖右y轴 也可不设置 默认都依赖左y轴
        set2.setDrawValues(true);
        set2.setValueTextColor(Color.BLACK);
        set2.setValueTextSize(12);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);
        dataSets.add(set1);
        dataSets.add(set2);

        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);
        lineChart.setDescription(description);
        lineChart.invalidate();                                            //刷新显示绘图
    }

    private void lineChartInit(){                   //图标初始化

        lineChart = findViewById(R.id.linechart);

        description = new Description();    //设置描述信息
        description.setText("");
        description.setTextSize(12);


        YAxis leftAxis = lineChart.getAxisLeft();       //左Y轴属性

        leftAxis.setTextSize(12f);
        leftAxis.setAxisLineWidth(2);
        leftAxis.setAxisMaximum(pressUpperLimit);
        leftAxis.setAxisMinimum(pressLowLimit);
        //  leftAxis.setDrawGridLines(false);              //网格线

        YAxis rightAxis = lineChart.getAxisRight();     //右Y轴属性

        rightAxis.setTextSize(12f);
        rightAxis.setAxisLineWidth(2);
        rightAxis.setAxisMaximum(pressUpperLimit);
        rightAxis.setAxisMinimum(pressLowLimit);

        XAxis xAxis = lineChart.getXAxis();             //X轴属性
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(12f);
        xAxis.setAxisLineWidth(2);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);

        //初始化一条折线的数据源 一个数据点对应一个Entry对象 Entry对象包含x值和y值
        entriesTemp = new ArrayList<>();
        entriesHumi = new ArrayList<>();
        entriesPress = new ArrayList<>();
        lineChart.setBackgroundColor(0xffc4d79b);  //设置LineChart的背景颜色

    }
}

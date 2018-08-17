package com.yy.k.AirCompressor;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TrendLineChart extends Activity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.line_chart);

        LineChart lineChart = findViewById(R.id.linechart);

        Description description = new Description();    //设置描述信息
        description.setText("温度/湿度/压差趋势");
        description.setTextSize(12);


        YAxis leftAxis = lineChart.getAxisLeft();       //左Y轴属性

        leftAxis.setTextSize(12f);
        leftAxis.setAxisLineWidth(2);
        leftAxis.setAxisMaximum(100);
        leftAxis.setAxisMinimum(-30);
      //  leftAxis.setDrawGridLines(false);              //网格线

        YAxis rightAxis = lineChart.getAxisRight();     //右Y轴属性

        rightAxis.setTextSize(12f);
        rightAxis.setAxisLineWidth(2);
        rightAxis.setAxisMaximum(50);
        rightAxis.setAxisMinimum(-250);
   //     rightAxis.setDrawGridLines(false);



        XAxis xAxis = lineChart.getXAxis();             //X轴属性
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(12f);
        xAxis.setAxisLineWidth(2);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);


        //初始化一条折线的数据源 一个数据点对应一个Entry对象 Entry对象包含x值和y值
        List<Entry> entriesTemp = new ArrayList<>();

        Random random = new Random();
        for (int i = 0 ; i < 30 ; i++){
            entriesTemp.add(new Entry(i + 1 , random.nextInt(10)+20 ));

        }

        LineDataSet set = new LineDataSet(entriesTemp, "温度(℃)");
        set.setColor(Color.RED);                                     //设置线条颜色
        set.setDrawValues(true);                                      //设置显示数据点值
        set.setValueTextColor(Color.RED);                             //设置显示值的字体颜色
        set.setValueTextSize(12);                                     //设置显示值的字体大小


        List<Entry> entriesHumi = new ArrayList<>();
        Random random1 = new Random();
        for (int i = 0 ; i < 30 ; i++){
            entriesHumi.add(new Entry(i + 1 , random1.nextInt(10) + 50));
        }
        //再创建一个折线对象set1：
        LineDataSet set1 = new LineDataSet(entriesHumi,"湿度(RH)");
        set1.setColor(getResources().getColor(R.color.colorPrimaryDark));    //给线条设置颜色
        //set1.setAxisDependency(YAxis.AxisDependency.RIGHT);                  //设置该折线的y值依赖右y轴 也可不设置 默认都依赖左y轴
        set1.setDrawValues(true);
        set1.setValueTextColor(getResources().getColor(R.color.colorPrimaryDark));
        set1.setValueTextSize(12);



        List<Entry> entriesPress = new ArrayList<>();
        Random random2 = new Random();
        for (int i = 0 ; i < 30 ; i++){
            entriesPress.add(new Entry(i + 1 , random2.nextInt(10)));
        }
        //再创建一个折线对象set1：
        LineDataSet set2 = new LineDataSet(entriesPress,"压差(Pa)");
        set2.setColor(Color.BLACK);    //给线条设置颜色
        set2.setAxisDependency(YAxis.AxisDependency.RIGHT);                  //设置该折线的y值依赖右y轴 也可不设置 默认都依赖左y轴
        set2.setDrawValues(true);
        set2.setValueTextColor(Color.BLACK);
        set2.setValueTextSize(12);


        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set);
        dataSets.add(set1);
        dataSets.add(set2);

        LineData lineData = new LineData(dataSets);

        lineChart.setData(lineData);

        lineChart.setDescription(description);


        lineChart.invalidate();                                            //刷新显示绘图
        lineChart.setBackgroundColor(0xffc4d79b);  //设置LineChart的背景颜色
    }



}

package com.yy.k.AirCompressor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

    DashBoardView tempDisplay;
    DashBoardView humiDisplay;
    DashBoardView pressDisplay;
    TextView tvTempValue;
    TextView tvHumiValue;
    TextView tvPressValue;
    Button bt_mute;
    Button bt_connectStatus;
    Intent intent = new Intent();
    private boolean muteFlag=true;

    ModbusSlave modbusSlave =new ModbusSlave();
    java.text.DecimalFormat myformat=new java.text.DecimalFormat("00.0");
    private SoundPool sp;
    private HashMap<Integer,Integer> spMap;

    Timer timer1 = new Timer();
    Timer timer2 = new Timer();
    TimerTask task1;
    TimerTask task2;

    SharedPreferences sharedParameterSet;
    SharedPreferences sharedPreferencesAlarmRecord;
    SharedPreferences sharedPreferencesPastRecord;

    private boolean alermFlag =false;

    private int overPressureTemp = 0;
    private int underPressureTemp = 0;


    private  List<String> listTime=new ArrayList<>();
    private  List<String> listData=new ArrayList<>();

    private  List<String> listPastTime=new ArrayList<>();
    private  List<String> listPastTemp=new ArrayList<>();
    private  List<String> listPastHumi=new ArrayList<>();
    private  List<String> listPastPress=new ArrayList<>();

    SimpleDateFormat simpleDateFormat;

    SharedPreferences.Editor editorAlarmRecord;
    SharedPreferences.Editor editorPastRecord;

    private int sendCount;

    String stringTempTemp;
    String stringHumiTemp;
    String stringPressTemp;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedParameterSet = this.getSharedPreferences("parameterSet",this.MODE_WORLD_WRITEABLE);
        sharedPreferencesAlarmRecord = this.getSharedPreferences("PreferencesAlarmRecord",this.MODE_WORLD_WRITEABLE);
        sharedPreferencesPastRecord = this.getSharedPreferences("PreferencesPastRecord",this.MODE_WORLD_WRITEABLE);

        editorAlarmRecord = sharedPreferencesAlarmRecord.edit();
        editorPastRecord = sharedPreferencesPastRecord.edit();

        simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss

        sp = new SoundPool(2, AudioManager.STREAM_MUSIC,0);
        spMap = new HashMap<Integer,Integer>();
        spMap.put(1, sp.load(this, R.raw.chaoya, 1));
        spMap.put(2, sp.load(this, R.raw.qianya, 1));
        playSounds(1,1);

        modbusSlave.start();
        tempDisplay = findViewById(R.id.temp_display);
        humiDisplay = findViewById(R.id.humi_display);
        pressDisplay = findViewById(R.id.press_display);
        bt_mute = findViewById(R.id.mute);
        bt_connectStatus = findViewById(R.id.connect_status);
        tvTempValue = findViewById(R.id.tv_temp_value);
        tvHumiValue = findViewById(R.id.tv_humi_value);
        tvPressValue = findViewById(R.id.tv_press_value);
        tempDisplayInit();
        humiDisplayInit();
        pressDisplayInit();

        bt_mute.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if (muteFlag){
                    bt_mute.setBackgroundResource(R.drawable.mute_up);
                    muteFlag=false;
                }else {
                    bt_mute.setBackgroundResource(R.drawable.mute_down);
                    muteFlag=true;
                }
            }
        });

        if (timer1 != null){
            if (task1 != null){
                task1.cancel();
            }
        }
        if (timer2 != null){
            if (task2 != null){
                task2.cancel();
            }
        }

        task1 = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        slaveAddressChange();
                        dataDispaly();
                        setBt_connectStatus();
                        createAlarmData();

                        sendCount++;
                        if (sendCount >= 2){
                            sendCount = 0;
                            createPastData();
                        }

                    }
                });
            }
        };

        task2 = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        modbusSlave.setGetDataFlag(false);
                    }
                });
            }
        };

        timer1.schedule(task1, 1000, 1000);
        timer2.schedule(task2, 1000, 10000);
    }


    private void createPastData(){

        Date date = new Date(System.currentTimeMillis());

        listPastTime.add(simpleDateFormat.format(date));
        listPastTemp.add(stringTempTemp);
        listPastHumi.add(stringHumiTemp);
        listPastPress.add(stringPressTemp);

        editorPastRecord.putInt("listPastTime_size",listPastTime.size());
        for (int i=0;i<listPastTime.size();i++){

            editorPastRecord.putString("listPastTime_"+i,listPastTime.get(i));
            editorPastRecord.putString("listPastTemp_"+i,listPastTemp.get(i));
            editorPastRecord.putString("listPastHumi_"+i,listPastHumi.get(i));
            editorPastRecord.putString("listPastPress_"+i,listPastPress.get(i));
        }
        editorPastRecord.apply();
    }


    @Override
    protected void onDestroy() {       // activity被销毁时关闭串口接收线程和串口
        super.onDestroy();
        modbusSlave.interrupt();
        modbusSlave.closeCom();
    }

    private void slaveAddressChange(){              //从机地址设定
        modbusSlave.setSLAV_addr(sharedParameterSet.getInt("从机地址",1));
    }

    private void setBt_connectStatus(){             //通讯指示

        if (modbusSlave.isGetDataFlag()){
            bt_connectStatus.setBackgroundResource(R.drawable.connected);
        }else {
            bt_connectStatus.setBackgroundResource(R.drawable.no_connect);
            modbusSlave.setOverPressure(0);
            modbusSlave.setUnderPressure(0);
        }
    }


    private void writeDataToShared(){               //把数据写入文件

        editorAlarmRecord.putInt("listTime_size",listTime.size());

        for (int i=0;i<listTime.size();i++){

            editorAlarmRecord.putString("listTime_"+i,listTime.get(i));
            editorAlarmRecord.putString("listData_"+i,listData.get(i));
        }

        editorAlarmRecord.apply();
    }

    private void createAlarmData(){     //产生报警数据

        if(overPressureTemp != modbusSlave.getOverPressure()){

            overPressureTemp = modbusSlave.getOverPressure();

            if (1 == overPressureTemp){

                Date date = new Date(System.currentTimeMillis());

                listTime.add(simpleDateFormat.format(date));
                listData.add("超压");
            }

            if (0 == overPressureTemp){

                Date date = new Date(System.currentTimeMillis());

                listTime.add(simpleDateFormat.format(date));
                listData.add("恢复正常");
            }

            writeDataToShared();

        }


        if(underPressureTemp != modbusSlave.getUnderPressure()){

            underPressureTemp = modbusSlave.getUnderPressure();

            if (1 == underPressureTemp){

                Date date = new Date(System.currentTimeMillis());
                listTime.add(simpleDateFormat.format(date));
                listData.add("欠压");
            }

            if (0 == underPressureTemp){

                Date date = new Date(System.currentTimeMillis());
                listTime.add(simpleDateFormat.format(date));
                listData.add("恢复正常");
            }

            writeDataToShared();

        }
    }

    private void dataDispaly(){         //数据处理和显示

        int getTempFromModbus = ModbusSlave.temperature;
        int getHumiFromModbus = ModbusSlave.humidity;
        int getPressFromModbus = ModbusSlave.pressure;

        double doubleTempTemp = getTempFromModbus/10.0;
        double doubleHumiTemp = getHumiFromModbus/10.0;
        double doublePressTemp = getPressFromModbus*0.3 - 250.0;

        stringTempTemp = myformat.format(doubleTempTemp);
        stringHumiTemp = myformat.format(doubleHumiTemp);
        stringPressTemp = myformat.format(doublePressTemp);

        tvTempValue.setText(stringTempTemp+"℃");
        tvHumiValue.setText(stringHumiTemp+"%H");
        tvPressValue.setText(stringPressTemp+"Pa");

        tvTempValue.setTextColor(0xdfffffff);
        tvHumiValue.setTextColor(0xdfffffff);
        tvPressValue.setTextColor(0xdfffffff);

        tempDisplay.setSpeed(getTempFromModbus+300);
        humiDisplay.setSpeed(getHumiFromModbus);
        pressDisplay.setSpeed(getPressFromModbus);

        alermFlag = !alermFlag;
        if (modbusSlave.getOverPressure() == 1){
            if (alermFlag){
                tvPressValue.setText("");
            }else{
                tvPressValue.setText(stringPressTemp+"Pa");
                tvPressValue.setTextColor(0xdfff0000);

                if (!muteFlag){
                    playSounds(1,1);
                }
            }
        }

        if (modbusSlave.getUnderPressure() == 1){
            if (alermFlag){
                tvPressValue.setText("");
            }else{
                tvPressValue.setText(stringPressTemp+"Pa");
                tvPressValue.setTextColor(0xDFFFFF00);
                if (!muteFlag){
                    playSounds(2,1);
                }
            }
        }
    }

    private void playSounds(int sound, int number){
        AudioManager am = (AudioManager)this.getSystemService(this.AUDIO_SERVICE);
        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        float volumnRatio = audioCurrentVolumn/audioMaxVolumn;
        sp.play(spMap.get(sound), volumnRatio, volumnRatio, 1, 0, 1);
}

    private void pressDisplayInit() {
        String[] pressFixedLevel={"-250 pa","-220 pa","-190 pa","-160 pa","-130 pa","-100 pa","-70 pa","-40 pa","-10 pa","20 pa","50 pa"};
        pressDisplay.setMark(" 当前压力");
        pressDisplay.setSpeedUint("pa");
        pressDisplay.setFixedLevel(pressFixedLevel);
    }

    private  void tempDisplayInit(){
        String[] tempFixedLevel={"-30 ℃","-20 ℃","-10 ℃","0 ℃","10 ℃","20 ℃","30 ℃","40 ℃","50 ℃","60 ℃","70 ℃"};
        tempDisplay.setMark("当前温度");
        tempDisplay.setSpeedUint("   ℃");
        tempDisplay.setFixedLevel(tempFixedLevel);
    }

    private void humiDisplayInit(){
        String[] humiFixedLevel={"0 %H","10 %H","20 %H","30 %H","40 %H","50 %H","60 %H","70 %H","80 %H","90 %H","100 %H"};
        humiDisplay.setMark("当前湿度");
        humiDisplay.setSpeedUint("  %h");
        humiDisplay.setFixedLevel(humiFixedLevel);
    }

    public void bt_alarm_record(View view) {

        intent.setClass(this,AlarmRecord.class);
        startActivity(intent);

    }

    public void bt_line_chart(View view) {

        intent.setClass(this,TrendLineChart.class);
        startActivity(intent);
    }

    public void bt_past_record(View view) {

        intent.setClass(this,PastRecord.class);
        startActivity(intent);

    }

    public void bt_system_set(View view) {
    }
}

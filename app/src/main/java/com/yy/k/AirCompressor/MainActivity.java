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

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;


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

    TextView tvTempName;
    TextView tvHumiName;
    TextView tvPressName;

    TextView tvUintTitle;
    TextView tvBeng1;
    TextView tvBeng2;
    TextView tvBeng3;

    Button bt_beng1run;
    Button bt_beng2run;
    Button bt_beng3run;

    Button bt_beng1error;
    Button bt_beng2error;
    Button bt_beng3error;

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
    SharedPreferences sharedPreferencesSystemSet;

    private boolean alermFlag =false;

    private int overPressureTemp = 0;
    private int underPressureTemp = 0;

    private int kongYaJiErrorTemp;
    private int beng1ErrorTemp;
    private int beng2ErrorTemp;
    private int beng3ErrorTemp;

    private int pressUpperLimit;
    private int pressLowLimit;

    public static    List<String> listTime=new ArrayList<>();
    public static    List<String> listData=new ArrayList<>();

    public static    List<String> listPastTime=new ArrayList<>();
    public static    List<String> listPastTemp=new ArrayList<>();
    public static    List<String> listPastHumi=new ArrayList<>();
    public static    List<String> listPastPress=new ArrayList<>();

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
        sharedPreferencesSystemSet = this.getSharedPreferences("PreferencesSystemSet",this.MODE_WORLD_WRITEABLE);

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

        tvTempName = findViewById(R.id.tv_temp_name);
        tvHumiName = findViewById(R.id.tv_humi_name);
        tvPressName = findViewById(R.id.tv_press_name);

        tvUintTitle = findViewById(R.id.uint_title);
        tvBeng1 = findViewById(R.id.beng1);
        tvBeng2 = findViewById(R.id.beng2);
        tvBeng3 = findViewById(R.id.beng3);

        bt_beng1run = findViewById(R.id.beng1run);
        bt_beng2run = findViewById(R.id.beng2run);
        bt_beng3run = findViewById(R.id.beng3run);

        bt_beng1error = findViewById(R.id.beng1error);
        bt_beng2error = findViewById(R.id.beng2error);
        bt_beng3error = findViewById(R.id.beng3error);



        //tempDisplayInit();
      //  humiDisplayInit();
      //  pressDisplayInit();

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

        loadShared();

    }


    private void loadShared(){      //开机时加载shared文件里的数据，防止被覆盖掉

        int size = sharedPreferencesAlarmRecord.getInt("listTime_size",0);

        for (int i = 0;i<size;i++){

            listTime.add(sharedPreferencesAlarmRecord.getString("listTime_"+i,null));
            listData.add(sharedPreferencesAlarmRecord.getString("listData_"+i,null));
        }

        size = sharedPreferencesPastRecord.getInt("listPastTime_size",0);

        for (int i = 0;i<size;i++){

            listPastTime.add(sharedPreferencesPastRecord.getString("listPastTime_"+i,null));
            listPastTemp.add(sharedPreferencesPastRecord.getString("listPastTemp_"+i,null));
            listPastHumi.add(sharedPreferencesPastRecord.getString("listPastHumi_"+i,null));
            listPastPress.add(sharedPreferencesPastRecord.getString("listPastPress_"+i,null));
        }
    }

    @Override
    protected void onPostResume() { //返回主界面时需要处理的程序
        super.onPostResume();

        pressUpperLimit = sharedPreferencesSystemSet.getInt("压差上限",500);
        pressLowLimit = sharedPreferencesSystemSet.getInt("压差下限",-500);

        int[] pressLimitTemp = new int[11];
        for (int i=0;i<11;i++){
            pressLimitTemp[i] = pressLowLimit + (pressUpperLimit-pressLowLimit)/10*i;
        }

        String[] pressFixedLevel={pressLimitTemp[0]+"kpa",pressLimitTemp[1]+"kpa",pressLimitTemp[2]+"kpa",pressLimitTemp[3]+"kpa",pressLimitTemp[4]+"kpa",pressLimitTemp[5]+"kpa",pressLimitTemp[6]+"kpa",pressLimitTemp[7]+"kpa",pressLimitTemp[8]+"kpa",pressLimitTemp[9]+"kpa",pressLimitTemp[10]+"kpa"};
        pressDisplay.setMark(" 当前压力");
        pressDisplay.setSpeedUint("kpa");
        pressDisplay.setFixedLevel(pressFixedLevel);

        tempDisplay.setMark(" 当前压力");
        tempDisplay.setSpeedUint("kpa");
        tempDisplay.setFixedLevel(pressFixedLevel);

        humiDisplay.setMark(" 当前压力");
        humiDisplay.setSpeedUint("kpa");
        humiDisplay.setFixedLevel(pressFixedLevel);
    }

    private void createPastData(){

        Date date = new Date(System.currentTimeMillis());

        listPastTime.add(simpleDateFormat.format(date));
        listPastTemp.add(stringTempTemp);
        listPastHumi.add(stringHumiTemp);
        listPastPress.add(stringPressTemp);


        Log.d(TAG, "listPastTime.size(): "+listPastTime.size());

        if (listPastTime.size() > 10000){

            listPastTime.clear();
            listPastTemp.clear();
            listPastHumi.clear();
            listPastPress.clear();
        }

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


        if (listTime.size() > 10000){

            listTime.clear();
            listData.clear();
        }

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
                listData.add("压差恢复正常");
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
                listData.add("压差恢复正常");
            }

            writeDataToShared();

        }

        if (kongYaJiErrorTemp != ModbusSlave.kongYaJiGuZhang){

            kongYaJiErrorTemp = ModbusSlave.kongYaJiGuZhang;

            if (1 == kongYaJiErrorTemp){
                Date date = new Date(System.currentTimeMillis());
                listTime.add(simpleDateFormat.format(date));
                listData.add("空压机故障");
            }
            if (0 == kongYaJiErrorTemp){

                Date date = new Date(System.currentTimeMillis());
                listTime.add(simpleDateFormat.format(date));
                listData.add("空压机恢复正常");
            }
            writeDataToShared();
        }

        if (beng1ErrorTemp != ModbusSlave.zhenKongBengGuZhang1){

            beng1ErrorTemp = ModbusSlave.zhenKongBengGuZhang1;
            if (1 == beng1ErrorTemp){

                Date date = new Date(System.currentTimeMillis());
                listTime.add(simpleDateFormat.format(date));
                listData.add("真空泵1故障");
            }
            if (0 == beng1ErrorTemp){

                Date date = new Date(System.currentTimeMillis());
                listTime.add(simpleDateFormat.format(date));
                listData.add("真空泵1恢复正常");
            }
            writeDataToShared();
        }

        if (beng2ErrorTemp != ModbusSlave.zhenKongBengGuZhang2){

            beng2ErrorTemp = ModbusSlave.zhenKongBengGuZhang2;
            if (1 == beng2ErrorTemp){

                Date date = new Date(System.currentTimeMillis());
                listTime.add(simpleDateFormat.format(date));
                listData.add("真空泵2故障");
            }
            if (0 == beng2ErrorTemp){

                Date date = new Date(System.currentTimeMillis());
                listTime.add(simpleDateFormat.format(date));
                listData.add("真空泵2恢复正常");
            }
            writeDataToShared();
        }

        if (beng3ErrorTemp != ModbusSlave.zhenKongBengGuZhang3){

            beng3ErrorTemp = ModbusSlave.zhenKongBengGuZhang3;
            if (1 == beng3ErrorTemp){

                Date date = new Date(System.currentTimeMillis());
                listTime.add(simpleDateFormat.format(date));
                listData.add("真空泵3故障");
            }
            if (0 == beng3ErrorTemp){

                Date date = new Date(System.currentTimeMillis());
                listTime.add(simpleDateFormat.format(date));
                listData.add("真空泵3恢复正常");
            }
            writeDataToShared();
        }

    }

    private void dataDispaly(){         //数据处理和显示

        int getTempFromModbus = ModbusSlave.temperature;
        int getHumiFromModbus = ModbusSlave.humidity;
        int getPressFromModbus = ModbusSlave.pressure;

        double doubleTempTemp = getTempFromModbus*(pressUpperLimit-pressLowLimit)/1000.0 + pressLowLimit;
        double doubleHumiTemp = getHumiFromModbus*(pressUpperLimit-pressLowLimit)/1000.0 + pressLowLimit;
        double doublePressTemp = getPressFromModbus*(pressUpperLimit-pressLowLimit)/1000.0 + pressLowLimit;


        stringTempTemp = myformat.format(doubleTempTemp);
        stringHumiTemp = myformat.format(doubleHumiTemp);
        stringPressTemp = myformat.format(doublePressTemp);

        tvTempValue.setText(stringTempTemp+"KPa");
        tvHumiValue.setText(stringHumiTemp+"KPa");
        tvPressValue.setText(stringPressTemp+"KPa");

        tvTempValue.setTextColor(0xdfffffff);
        tvHumiValue.setTextColor(0xdfffffff);
        tvPressValue.setTextColor(0xdfffffff);

        int uintNum = sharedPreferencesSystemSet.getInt("机组选择",0);
        switch(uintNum){       //根据选择的机组显示对应的空压机或真空泵，并处理相应的运行及报警信号。

            case 0:

                tvTempValue.setText(stringTempTemp+"KPa");
                tvHumiValue.setText(stringHumiTemp+"KPa");
                tvPressValue.setText(stringPressTemp+"KPa");

                tvTempValue.setTextColor(0xdfffffff);
                tvHumiValue.setTextColor(0xdfffffff);
                tvPressValue.setTextColor(0xdfffffff);

                tvUintTitle.setText("负压控制监控系统");
                tvBeng1.setText("真空泵1");
                tvBeng2.setText("真空泵2");
                tvBeng3.setText("真空泵3");

                tempDisplay.setAlpha(1);
                pressDisplay.setAlpha(1);

                tvTempName.setText("真空泵1");
                tvHumiName.setText("真空泵2");
                tvPressName.setText("真空泵3");

                if (ModbusSlave.zhenKongBengYunXing1 == 1){
                    bt_beng1run.setBackgroundResource(R.drawable.connected);
                }else {
                    bt_beng1run.setBackgroundResource(R.drawable.init_ing);
                }
                if (ModbusSlave.zhenKongBengGuZhang1 == 1){
                    bt_beng1error.setBackgroundResource(R.drawable.no_connect);
                }else {
                    bt_beng1error.setBackgroundResource(R.drawable.init_ing);
                }

                if (ModbusSlave.zhenKongBengYunXing2 == 1){
                    bt_beng2run.setBackgroundResource(R.drawable.connected);
                }else {
                    bt_beng2run.setBackgroundResource(R.drawable.init_ing);
                }
                if (ModbusSlave.zhenKongBengGuZhang2 == 1){
                    bt_beng2error.setBackgroundResource(R.drawable.no_connect);
                }else {
                    bt_beng2error.setBackgroundResource(R.drawable.init_ing);
                }

                if (ModbusSlave.zhenKongBengYunXing3 == 1){
                    bt_beng3run.setBackgroundResource(R.drawable.connected);
                }else {
                    bt_beng3run.setBackgroundResource(R.drawable.init_ing);
                }

                if (ModbusSlave.zhenKongBengGuZhang3 == 1){
                    bt_beng3error.setBackgroundResource(R.drawable.no_connect);
                }else {
                    bt_beng3error.setBackgroundResource(R.drawable.init_ing);
                }
                break;

            case 1:

                tvTempValue.setText("");
                tvHumiValue.setText(stringHumiTemp+"KPa");
                tvPressValue.setText("");

                tvHumiValue.setTextColor(0xdfffffff);

                tvTempName.setText("");
                tvHumiName.setText("空压机");
                tvPressName.setText("");

                tempDisplay.setAlpha(0);
                pressDisplay.setAlpha(0);

                tvUintTitle.setText("空压机房监控系统");
                tvBeng1.setText("空压机");
                tvBeng2.setText("");
                tvBeng3.setText("");
                if (ModbusSlave.kongYaJiYunXing == 1){
                    bt_beng1run.setBackgroundResource(R.drawable.connected);
                }else {
                    bt_beng1run.setBackgroundResource(R.drawable.init_ing);
                }
                if (ModbusSlave.kongYaJiGuZhang == 1){
                    bt_beng1error.setBackgroundResource(R.drawable.no_connect);
                }else {
                    bt_beng1error.setBackgroundResource(R.drawable.init_ing);
                }

                bt_beng2run.setBackgroundResource(R.drawable.nothing);
                bt_beng3run.setBackgroundResource(R.drawable.nothing);
                bt_beng2error.setBackgroundResource(R.drawable.nothing);
                bt_beng3error.setBackgroundResource(R.drawable.nothing);
                break;

            case 2:

                tempDisplay.setAlpha(1);
                pressDisplay.setAlpha(1);

                tvTempValue.setText(stringTempTemp+"KPa");
                tvHumiValue.setText(stringHumiTemp+"KPa");
                tvPressValue.setText(stringPressTemp+"KPa");

                tvTempValue.setTextColor(0xdfffffff);
                tvHumiValue.setTextColor(0xdfffffff);
                tvPressValue.setTextColor(0xdfffffff);

                tvUintTitle.setText("牙科抽吸机监控系统");
                tvBeng1.setText("真空泵1");
                tvBeng2.setText("真空泵2");
                tvBeng3.setText("");

                tvTempName.setText("真空泵1");
                tvHumiName.setText("真空泵2");
                tvPressName.setText("真空泵3");

                if (ModbusSlave.zhenKongBengYunXing1 == 1){
                    bt_beng1run.setBackgroundResource(R.drawable.connected);
                }else {
                    bt_beng1run.setBackgroundResource(R.drawable.init_ing);
                }
                if (ModbusSlave.zhenKongBengGuZhang1 == 1){
                    bt_beng1error.setBackgroundResource(R.drawable.no_connect);
                }else {
                    bt_beng1error.setBackgroundResource(R.drawable.init_ing);
                }

                if (ModbusSlave.zhenKongBengYunXing2 == 1){
                    bt_beng2run.setBackgroundResource(R.drawable.connected);
                }else {
                    bt_beng2run.setBackgroundResource(R.drawable.init_ing);
                }
                if (ModbusSlave.zhenKongBengGuZhang2 == 1){
                    bt_beng2error.setBackgroundResource(R.drawable.no_connect);
                }else {
                    bt_beng2error.setBackgroundResource(R.drawable.init_ing);
                }

                bt_beng3run.setBackgroundResource(R.drawable.nothing);
                bt_beng3error.setBackgroundResource(R.drawable.nothing);

                break;

            case 3:


                tvTempValue.setText("");
                tvHumiValue.setText(stringHumiTemp+"KPa");
                tvPressValue.setText("");

                tvHumiValue.setTextColor(0xdfffffff);

                tvTempName.setText("");
                tvPressName.setText("");

                tempDisplay.setAlpha(0);
                pressDisplay.setAlpha(0);

                tvUintTitle.setText("牙科空压机房监控系统");
                tvBeng1.setText("空压机");
                tvBeng2.setText("");
                tvBeng3.setText("");

                tvTempName.setText("");
                tvHumiName.setText("空压机");
                tvPressName.setText("");

                if (ModbusSlave.kongYaJiYunXing == 1){
                    bt_beng1run.setBackgroundResource(R.drawable.connected);
                }else {
                    bt_beng1run.setBackgroundResource(R.drawable.init_ing);
                }
                if (ModbusSlave.kongYaJiGuZhang == 1){
                    bt_beng1error.setBackgroundResource(R.drawable.no_connect);
                }else {
                    bt_beng1error.setBackgroundResource(R.drawable.init_ing);
                }

                bt_beng2run.setBackgroundResource(R.drawable.nothing);
                bt_beng3run.setBackgroundResource(R.drawable.nothing);
                bt_beng2error.setBackgroundResource(R.drawable.nothing);
                bt_beng3error.setBackgroundResource(R.drawable.nothing);
                break;

        }

        tempDisplay.setSpeed(getTempFromModbus);
        humiDisplay.setSpeed(getHumiFromModbus);
        pressDisplay.setSpeed(getPressFromModbus);

        alermFlag = !alermFlag;
        if (modbusSlave.getOverPressure() == 1){
            if (alermFlag){
                tvHumiValue.setText("");
            }else{
                tvHumiValue.setText(stringHumiTemp+"KPa");
                tvHumiValue.setTextColor(0xdfff0000);

                if (!muteFlag){
                    playSounds(1,1);
                }
            }
        }

        if (modbusSlave.getUnderPressure() == 1){
            if (alermFlag){
                tvHumiValue.setText("");
            }else{
                tvHumiValue.setText(stringHumiTemp+"KPa");
                tvHumiValue.setTextColor(0xDFFFFF00);
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

        intent.setClass(this,SystemSet.class);
        startActivity(intent);

    }
}

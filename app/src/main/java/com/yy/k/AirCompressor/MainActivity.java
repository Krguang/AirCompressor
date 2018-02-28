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

import java.util.HashMap;
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
    Button bt_setup;
    Button bt_mute;
    Intent intent = new Intent();

    private boolean muteFlag=true;

    ModbusSlave modbusSlave =new ModbusSlave();
    java.text.DecimalFormat myformat=new java.text.DecimalFormat("00.0");
    private SoundPool sp;
    private HashMap<Integer,Integer> spMap;

    Timer timer1 = new Timer();
    TimerTask task1;

    SharedPreferences sharedParameterSet;
    SharedPreferences.Editor editor;

    private boolean alermFlag =false;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedParameterSet = this.getSharedPreferences("parameterSet",this.MODE_WORLD_WRITEABLE);

        sp = new SoundPool(2, AudioManager.STREAM_MUSIC,0);
        spMap = new HashMap<Integer,Integer>();
        spMap.put(1, sp.load(this, R.raw.chaoya, 1));
        spMap.put(2, sp.load(this, R.raw.qianya, 1));
        playSounds(1,1);


        modbusSlave.start();
        tempDisplay = findViewById(R.id.temp_display);
        humiDisplay = findViewById(R.id.humi_display);
        pressDisplay = findViewById(R.id.press_display);
        bt_setup = findViewById(R.id.setup);
        bt_mute = findViewById(R.id.mute);
        tvTempValue = findViewById(R.id.tv_temp_value);
        tvHumiValue = findViewById(R.id.tv_humi_value);
        tvPressValue = findViewById(R.id.tv_press_value);

        tempDisplayInit();
        humiDisplayInit();
        pressDisplayInit();


        bt_setup.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.drawable.setup_down);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.drawable.setup_up);
                }
                return false;
            }

        });

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

        bt_setup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.setClass(MainActivity.this,CompressorSet.class);
                startActivity(intent);
            }
        });

        if (timer1 != null){
            if (task1 != null){
                task1.cancel();
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
                    }
                });
            }
        };

        timer1.schedule(task1, 1000, 1000);
    }

    @Override
    protected void onDestroy() {       // activity被销毁时关闭串口接收线程和串口
        super.onDestroy();
        modbusSlave.interrupt();
        modbusSlave.closeCom();
    }

    private void slaveAddressChange(){
        modbusSlave.setSLAV_addr(sharedParameterSet.getInt("从机地址",1));
    }

    private void dataDispaly(){
        int getTempFromModbus = modbusSlave.getTemperature();
        int getHumiFromModbus = modbusSlave.getHumidity();
        int getPressFromModbus = modbusSlave.getPressure();
        int pressureUpperLimit = sharedParameterSet.getInt("压力报警上限",800);
        int pressureLowerLimit = sharedParameterSet.getInt("压力报警下限",300);

        double doubleTempTemp = getTempFromModbus/10.0;
        double doubleHumiTemp = getHumiFromModbus/10.0;
        double doublePressTemp = getPressFromModbus/10.0;

        String stringTempTemp = myformat.format(doubleTempTemp);
        String stringHumiTemp = myformat.format(doubleHumiTemp);
        String stringPressTemp = myformat.format(doublePressTemp);

        tvTempValue.setText(stringTempTemp+"℃");
        tvHumiValue.setText(stringHumiTemp+"%H");
        tvPressValue.setText(stringPressTemp+"MPa");
        tvTempValue.setTextColor(0xdfffffff);
        tvHumiValue.setTextColor(0xdfffffff);
        tvPressValue.setTextColor(0xdfffffff);

        tempDisplay.setSpeed(getTempFromModbus);
        humiDisplay.setSpeed(getHumiFromModbus);
        pressDisplay.setSpeed(getPressFromModbus);

        alermFlag = !alermFlag;
        if (getPressFromModbus > pressureUpperLimit ){
            if (alermFlag){
                tvPressValue.setText("");
            }else{
                tvPressValue.setText(stringPressTemp+"MPa");
                tvPressValue.setTextColor(0xdfff0000);

                if (!muteFlag){
                    playSounds(1,1);
                }
            }
        }

        if (getPressFromModbus < pressureLowerLimit){
            if (alermFlag){
                tvPressValue.setText("");
            }else{
                tvPressValue.setText(stringPressTemp+"MPa");
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
        String[] pressFixedLevel={"0 Mpa","10 Mpa","20 Mpa","30 Mpa","40 Mpa","50 Mpa","60 Mpa","70Mpa","80Mpa","90Mpa","100Mpa"};
        pressDisplay.setMark(" 当前压力");
        pressDisplay.setSpeedUint("Mpa");
        pressDisplay.setFixedLevel(pressFixedLevel);
    }

    private  void tempDisplayInit(){
        String[] tempFixedLevel={"0 ℃","10 ℃","20 ℃","30 ℃","40 ℃","50 ℃","60 ℃","70 ℃","80 ℃","90 ℃","100 ℃"};
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
}

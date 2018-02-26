package com.yy.k.AirCompressor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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

    private boolean muteFlag=false;

    ModbusSlave modbusSlave =new ModbusSlave();
    java.text.DecimalFormat myformat=new java.text.DecimalFormat("00.0");
    private SoundPool sp;
    private HashMap<Integer,Integer> spMap;

    Timer timer1 = new Timer();
    TimerTask task1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            @Override
            public void onClick(View v) {
               // sp.resume(spMap.get(1));
                playSounds(1,1);
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
                        int getTempFromModbus =modbusSlave.getTemperature();
                        double doubleTemp = getTempFromModbus/10.0;
                        String stringTemp = myformat.format(doubleTemp);
                        tvTempValue.setText(stringTemp+"℃");
                        tempDisplay.setSpeed(getTempFromModbus);
                    }
                });
            }
        };

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

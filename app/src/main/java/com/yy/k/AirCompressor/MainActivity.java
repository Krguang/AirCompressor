package com.yy.k.AirCompressor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;


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
    private String speedNumber = "0";

    ModbusSlave modbusSlave =new ModbusSlave();
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

package com.yy.k.AirCompressor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.WindowManager;
import android.widget.SeekBar;


public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

    DashBoardView tempDisplay;
    DashBoardView humiDisplay;
    DashBoardView pressDisplay;
    SeekBar seekBar;

    ModbusSlave modbusSlave =new ModbusSlave();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        modbusSlave.start();
        tempDisplay = findViewById(R.id.temp_display);
        humiDisplay = findViewById(R.id.humi_display);
        pressDisplay = findViewById(R.id.press_display);
        seekBar = findViewById(R.id.seekBar);
        tempDisplayInit();
        humiDisplayInit();
        pressDisplayInit();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                humiDisplay.setSpeed(progress);
                tempDisplay.setSpeed(progress);
                pressDisplay.setSpeed(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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

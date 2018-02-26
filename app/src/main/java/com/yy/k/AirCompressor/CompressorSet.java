package com.yy.k.AirCompressor;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
        import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by K on 2018/2/6.
 */

public class CompressorSet extends Activity{

    Button bt_exit;
    SeekBar sb_pressure_upper_limit;
    SeekBar sb_pressure_lower_limit;
    SeekBar sb_slave_address;
    TextView tx_pressure_upper_limit;
    TextView tx_pressure_lower_limit;
    TextView tx_slave_address;

    private int pressureUpperLimit;
    private int pressureLowerLimit;
    private int slaveAddress;

    SharedPreferences sharedParameterSet;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compressor_set);

        java.text.DecimalFormat myformat=new java.text.DecimalFormat("00.0");

        sharedParameterSet = this.getSharedPreferences("parameterSet",this.MODE_WORLD_WRITEABLE);
        editor = sharedParameterSet.edit();

        bt_exit = findViewById(R.id.bt_exit);
        sb_pressure_upper_limit = findViewById(R.id.pressure_upper_limit);
        sb_pressure_lower_limit = findViewById(R.id.pressure_lower_limit);
        sb_slave_address = findViewById(R.id.slave_address);
        tx_pressure_upper_limit = findViewById(R.id.tx_pressure_upper_limit);
        tx_pressure_lower_limit = findViewById(R.id.tx_pressure_lower_limit);
        tx_slave_address = findViewById(R.id.tx_slave_address);

        pressureUpperLimit = sharedParameterSet.getInt("压力报警上限",800);
        pressureLowerLimit = sharedParameterSet.getInt("压力报警下限",300);
        slaveAddress = sharedParameterSet.getInt("从机地址",1);

        sb_pressure_upper_limit.setProgress(pressureUpperLimit);
        sb_pressure_lower_limit.setProgress(pressureLowerLimit);
        sb_slave_address.setProgress(slaveAddress);

        double upperTemp = pressureUpperLimit/10.0;
        double lowerTemp = pressureLowerLimit/10.0;
        String upperString = myformat.format(upperTemp);
        String lowerString = myformat.format(lowerTemp);

        tx_pressure_upper_limit.setText(upperString + "MPa");
        tx_pressure_lower_limit.setText(lowerString + "MPa");
        tx_slave_address.setText(slaveAddress+"");

        sb_pressure_upper_limit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                pressureUpperLimit = progress;
                if (pressureUpperLimit < pressureLowerLimit){
                    pressureUpperLimit = pressureLowerLimit;
                }
                editor.putInt("压力报警上限",pressureUpperLimit);
                editor.apply();
                double doubleTemp = pressureUpperLimit /10.0;
                java.text.DecimalFormat myformat=new java.text.DecimalFormat("00.0");
                String stringTemp = myformat.format(doubleTemp);
                tx_pressure_upper_limit.setText(stringTemp+"MPa");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_pressure_lower_limit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                pressureLowerLimit = progress;
                if (pressureLowerLimit > pressureUpperLimit){
                    pressureLowerLimit = pressureUpperLimit;
                }
                editor.putInt("压力报警下限",pressureLowerLimit);
                editor.apply();
                double doubleTemp = pressureLowerLimit /10.0;
                java.text.DecimalFormat myformat=new java.text.DecimalFormat("00.0");
                String stringTemp = myformat.format(doubleTemp);
                tx_pressure_lower_limit.setText(stringTemp+"MPa");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_slave_address.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                slaveAddress = progress;
                editor.putInt("从机地址",slaveAddress);
                editor.apply();
                tx_slave_address.setText(progress+"");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        bt_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}

package com.yy.k.AirCompressor;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class SystemSet extends Activity {


    private Spinner sp_systemSet;

    SharedPreferences sharedPreferencesSystemSet;
    SharedPreferences.Editor editorSystemSet;

    EditText pressUpper;
    EditText pressLow;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_set);

        sharedPreferencesSystemSet = this.getSharedPreferences("PreferencesSystemSet",this.MODE_WORLD_WRITEABLE);
        editorSystemSet = sharedPreferencesSystemSet.edit();

        sp_systemSet = findViewById(R.id.sp_unit_choice);
        pressUpper = findViewById(R.id.et_press_upper);
        pressLow = findViewById(R.id.et_press_low);


        sp_systemSet.setSelection(sharedPreferencesSystemSet.getInt("机组选择",0),true);
        pressUpper.setText(sharedPreferencesSystemSet.getInt("压差上限",500)+"");
        pressLow.setText(sharedPreferencesSystemSet.getInt("压差下限",-500)+"");


        sp_systemSet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                editorSystemSet.putInt("机组选择",i);
                editorSystemSet.apply();//提交修改
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        pressUpper.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                editorSystemSet.putInt("压差上限", Integer.parseInt(pressUpper.getText().toString()));
                editorSystemSet.apply();
                return false;
            }
        });

        pressLow.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                editorSystemSet.putInt("压差下限", Integer.parseInt(pressLow.getText().toString()));
                editorSystemSet.apply();
                return false;
            }
        });

    }
}

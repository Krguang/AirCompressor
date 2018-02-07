package com.yy.k.AirCompressor;

import android.app.Activity;
import android.os.Bundle;
        import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

/**
 * Created by K on 2018/2/6.
 */

public class CompressorSet extends Activity{

    Button bt_exit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compressor_set);

        bt_exit = findViewById(R.id.bt_exit);
        bt_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}

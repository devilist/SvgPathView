package com.svg.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.devilist.spv.SvgPathView;

public class MainActivity extends AppCompatActivity {


    private SvgPathView svgPathView0;
    private SvgPathView svgPathView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        Log.d("MainActivity", "asin45 " + Math.toDegrees(Math.asin(0.5)));
        Log.d("MainActivity", "sin30 " + Math.sin(3.14/6));
        svgPathView0 = (SvgPathView) findViewById(R.id.spv_view0);
        svgPathView0.startAnim();

        svgPathView1 = (SvgPathView) findViewById(R.id.spv_view1);
        svgPathView1.setSvgPathString(getString(R.string.poems_hhl))
                .setStrokeAnimDuration(500)
                .setStrokeColor(0xff757575)
                .setStrokeWidth(1.0f)
                .setAnimDelay(500)
                .setNeedFill(true)
                .setFillColor(0xff616161)
                .setFillAnimDuration(8000)
                .startAnim();

    }
}

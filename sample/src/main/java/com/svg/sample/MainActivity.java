package com.svg.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.devilist.spv.SvgPathView;
import com.devilist.spv.fillpath.FillingOrientation;

public class MainActivity extends AppCompatActivity {

    private SvgPathView svgPathView;
    private SvgPathView svgPathSample;
    private LinearLayout ll_svg;

    private EditText et_anim_delay, et_duration_stoke,
            et_width_path, et_color_path, et_duration_fill, et_color_fill;

    private RadioGroup rg_path, rg_fill_orien;

    private SwitchCompat sc_fill_anim;

    private Button btn_start;

    private int animDelay, stoke_duration, fill_duration;
    private float path_width;
    private String path_color, fill_color;
    private int fill_orientation;
    private String path_string;
    private boolean isNeedFill = true;

    private long currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        btn_start = (Button) findViewById(R.id.btn_start);
        et_anim_delay = (EditText) findViewById(R.id.et_anim_delay);
        et_duration_stoke = (EditText) findViewById(R.id.et_duration_stoke);
        et_width_path = (EditText) findViewById(R.id.et_width_path);
        et_color_path = (EditText) findViewById(R.id.et_color_path);
        et_duration_fill = (EditText) findViewById(R.id.et_duration_fill);
        et_color_fill = (EditText) findViewById(R.id.et_color_fill);
        sc_fill_anim = (SwitchCompat) findViewById(R.id.sc_fill_anim);

        ll_svg = (LinearLayout) findViewById(R.id.ll_svg);

        rg_path = (RadioGroup) findViewById(R.id.rg_path);
        rg_fill_orien = (RadioGroup) findViewById(R.id.rg_fill_orien);

        svgPathView = (SvgPathView) findViewById(R.id.spv_view);
        svgPathView.startAnim();

        fill_orientation = FillingOrientation.BOTTOM_TO_TOP;
        path_string = getResources().getString(R.string.path_world_map);

        rg_path.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_path_1:
                        path_string = getResources().getString(R.string.poems_hhl);
                        break;
                    case R.id.rb_path_2:
                        path_string = PathString.basketball_man;
                        break;
                    case R.id.rb_path_3:
                        path_string = getResources().getString(R.string.tower);
                        break;
                    case R.id.rb_path_4:
                        path_string = getResources().getString(R.string.path_world_map);
                        break;
                }

            }
        });
        rg_fill_orien.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_b2t:
                        fill_orientation = FillingOrientation.BOTTOM_TO_TOP;
                        break;
                    case R.id.rb_t2b:
                        fill_orientation = FillingOrientation.TOP_TO_BOTTOM;
                        break;
                    case R.id.rb_l2r:
                        fill_orientation = FillingOrientation.LEFT_TO_RIGHT;
                        break;
                    case R.id.rb_r2l:
                        fill_orientation = FillingOrientation.RIGHT_TO_LEFT;
                        break;
                    case R.id.rb_lt2rb:
                        fill_orientation = FillingOrientation.LEFT_TOP_TO_RIGHT_BOTTOM;
                        break;
                    case R.id.rb_rb2lt:
                        fill_orientation = FillingOrientation.RIGHT_BOTTOM_TO_LEFT_TOP;
                        break;
                    case R.id.rb_lbtrt:
                        fill_orientation = FillingOrientation.LEFT_BOTTOM_TO_RIGHT_TOP;
                        break;
                    case R.id.rb_rt2lb:
                        fill_orientation = FillingOrientation.RIGHT_TOP_TO_LEFT_BOTTOM;
                        break;
                    case R.id.rb_i20:
                        fill_orientation = FillingOrientation.INNER_TO_OUT;
                        break;
                    case R.id.rb_o2i:
                        fill_orientation = FillingOrientation.OUT_TO_INNER;
                        break;
                }
            }
        });

        svgPathSample = (SvgPathView) findViewById(R.id.spv_sample);

        currentTime = System.currentTimeMillis();
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long time = System.currentTimeMillis();
                if (time - currentTime < 1000) {
                    return;
                } else {
                    currentTime = time;

                    animDelay = TextUtils.isEmpty(et_anim_delay.getText().toString()) ?
                            0 : Integer.parseInt(et_anim_delay.getText().toString());
                    stoke_duration = TextUtils.isEmpty(et_duration_stoke.getText().toString()) ?
                            1000 : Integer.parseInt(et_duration_stoke.getText().toString());
                    path_width = TextUtils.isEmpty(et_width_path.getText().toString()) ?
                            1 : Float.parseFloat(et_width_path.getText().toString());
                    path_color = TextUtils.isEmpty(et_color_path.getText().toString()) ?
                            "000000" : et_color_path.getText().toString();
                    fill_duration = TextUtils.isEmpty(et_duration_fill.getText().toString()) ?
                            1000 : Integer.parseInt(et_duration_fill.getText().toString());
                    fill_color = TextUtils.isEmpty(et_color_fill.getText().toString()) ?
                            "ff5722" : et_color_fill.getText().toString();
                    isNeedFill = sc_fill_anim.isChecked();

                    SvgPathView svgPathView = new SvgPathView(MainActivity.this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                    params.gravity = Gravity.CENTER;
                    params.bottomMargin = 30;
                    params.leftMargin = 30;
                    params.topMargin = 30;
                    params.rightMargin = 30;
                    svgPathView.setLayoutParams(params);
                    svgPathView.setPadding(10, 10, 10, 10);

                    svgPathView.setSvgPathString(path_string)
                            .setAnimDelay(animDelay)
                            .setStrokeAnimDuration(stoke_duration)
                            .setStrokeWidth(path_width)
                            .setStrokeColor(Color.parseColor("#" + path_color))
                            .setNeedFill(isNeedFill)
                            .setFillAnimDuration(fill_duration)
                            .setFillColor(Color.parseColor("#" + fill_color))
                            .setFillingOrientation(fill_orientation)
                            .startAnim();

                    ll_svg.removeAllViews();
                    ll_svg.addView(svgPathView);
                }
            }
        });

        btn_start.postDelayed(new Runnable() {
            @Override
            public void run() {
                btn_start.performClick();
            }
        }, 1100);
    }

}

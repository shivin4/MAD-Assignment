package com.example.lottie;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;

import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {

    private LottieAnimationView lottieAnimationView;
    private ImageButton btnPlayPause;
    private SeekBar seekBarSpeed;
    private TextView tvSpeedLabel;
    private Spinner spAnimationSelector;

    private final String[] animations = {"animation.json", "animation1.json", "animation2.json"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lottieAnimationView = findViewById(R.id.lottieAnimationView);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        seekBarSpeed = findViewById(R.id.seekBarSpeed);
        tvSpeedLabel = findViewById(R.id.tvSpeedLabel);
        spAnimationSelector = findViewById(R.id.spAnimationSelector);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, animations);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAnimationSelector.setAdapter(spinnerAdapter);

        spAnimationSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedAnimation = animations[position];
                try {
                    lottieAnimationView.setAnimation(selectedAnimation);
                    lottieAnimationView.playAnimation();
                    btnPlayPause.setImageResource(R.drawable.ic_pause);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Error loading animation: " + selectedAnimation,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale);
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(scaleAnimation);
                if (lottieAnimationView.isAnimating()) {
                    lottieAnimationView.pauseAnimation();
                    btnPlayPause.setImageResource(R.drawable.ic_play);
                } else {
                    lottieAnimationView.playAnimation();
                    btnPlayPause.setImageResource(R.drawable.ic_pause);
                }
            }
        });

        btnPlayPause.setImageResource(R.drawable.ic_pause); // Initial state due to autoPlay

        seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float speed = progress / 100.0f;
                lottieAnimationView.setSpeed(speed);
                tvSpeedLabel.setText("Speed: " + speed + "x");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
}
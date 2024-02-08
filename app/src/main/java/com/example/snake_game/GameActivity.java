package com.example.snake_game;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class GameActivity extends AppCompatActivity implements SensorEventListener {

    SensorManager sensorManager;
    Sensor accelerometer;
    TextView xVal, yVal, zVal, direction;
    Button startButton;
    boolean collectValues = false;
    int counter; // X-axis
    float[] gravity;
    boolean initValueCheck;
    float initX, initY, initZ;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!collectValues) {
                    collectValues = true;
                    startButton.setText("Stop measuring");
                }
                else {
                    collectValues = false;
                    initValueCheck = true;
                    initX = 0;
                    initY = 0;
                    initZ = 0;
                    direction.setText("DIRECTION");
                    startButton.setText("Start measuring");
                }
            }
        });

        xVal = findViewById(R.id.xValueView);
        yVal = findViewById(R.id.yValueView);
        zVal = findViewById(R.id.zValueView);
        direction = findViewById(R.id.direction);

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);

        gravity = new float[3];
        initValueCheck = true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(collectValues) {
            if(initValueCheck) {
                initX = event.values[0];
                initY = event.values[1];
                initZ = event.values[2];

                initValueCheck = false;
            }

            float x, y, z;
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];

            xVal.setText(String.valueOf(x));
            yVal.setText(String.valueOf(y));
            zVal.setText(String.valueOf(z));

            if((x-initX) >= 4) {
                direction.setText("LEFT");
            }
            else if ((x-initX) <= (-4)) {
                direction.setText("RIGHT");
            }
            else if((y-initY) >= 3) {
                direction.setText("DOWN");
            }
            else if((y-initY) <= (-3)) {
                direction.setText("UP");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }
}

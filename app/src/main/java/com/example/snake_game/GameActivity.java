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
    float[] gravity;
    boolean init_value_check;
    boolean first_read;
    double init_time, new_time, prev_time; // Used to keep track of time when calculating derivative
    float[] new_vals, prev_vals; // Float arrays used for calculating derivative

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
                    init_value_check = true;
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
                SensorManager.SENSOR_DELAY_GAME);

        gravity = new float[3];
        init_value_check = true;
        first_read = true;
        init_time = 0; new_time = 0; prev_time = 0;
        prev_vals = new float[3]; new_vals = new float[3];
    }

    public float[] sensorDerivative(float[] sens_vals) {
        if(first_read) { // True if its the first read of the sensor values
            init_time = System.nanoTime() / 10e8; // Read initial time value
            new_time = init_time; // new_time becomes init_time
            new_vals[0] = sens_vals[0];
            new_vals[1] = sens_vals[1];
            new_vals[2] = sens_vals[2];
            first_read = false;
        }
        else {
            prev_time = new_time; // new_time becomes previous time value
            new_time = System.nanoTime() / 10e8; // Update new_time
            // new_vals becomes previous sensor values
            prev_vals[0] = new_vals[0];
            prev_vals[1] = new_vals[1];
            prev_vals[2] = new_vals[2];
            // Set up for new iteration
            new_vals[0] = sens_vals[0];
            new_vals[1] = sens_vals[1];
            new_vals[2] = sens_vals[2];
        }

        // Calculate difference in values and store them
        float[] delta_vals = new float[3];
        delta_vals[0] = new_vals[0]-prev_vals[0];
        delta_vals[1] = new_vals[0]-prev_vals[1];
        delta_vals[2] = new_vals[0]-prev_vals[2];
        // Calculate time difference between values
        double delta_time = new_time - prev_time;

        float[] sensval_derivative = new float[3];
        sensval_derivative[0] = (float) (delta_vals[0]/delta_time);
        sensval_derivative[1] = (float) (delta_vals[1]/delta_time);
        sensval_derivative[2] = (float) (delta_vals[2]/delta_time);

        return sensval_derivative;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(collectValues) {
            float[] sensor_values = new float[3];
            sensor_values[0] = event.values[0];
            sensor_values[1] = event.values[1];
            sensor_values[2] = event.values[2];

            xVal.setText(String.valueOf(sensor_values[0]));
            yVal.setText(String.valueOf(sensor_values[1]));
            zVal.setText(String.valueOf(sensor_values[2]));

            float[] sens_derivatives = sensorDerivative(sensor_values);
            if(sens_derivatives[0] >= 100) {
                direction.setText("LEFT");
            }
        }

        /*if(collectValues) {
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
        }*/
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
                SensorManager.SENSOR_DELAY_GAME);
    }
}

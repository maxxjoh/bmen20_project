package com.example.snake_game.game;
//imports for game
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.example.snake_game.R;
import com.example.snake_game.helper.SnakeDBOpenHelper;
import com.example.snake_game.intface.OnSnakeDeadListener;
import com.example.snake_game.intface.OnSnakeEatFoodListener;
import com.example.snake_game.widget.SnakeView;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

//imports for sensor
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


public class GameActivity extends AppCompatActivity implements View.OnClickListener,OnSnakeDeadListener,OnSnakeEatFoodListener,SensorEventListener {
    //variables for the game
    Button button_start;
    Button button_pause;
    private ProgressBar progress_horizontal;
    private ProgressBar progress_vertical;
    TextView textview_score;
    SnakeView snakeView;
    private int highscore = 0;
    boolean collectValues = false;
    private SnakeDBOpenHelper openHelper;
    private EditText input;//EditText object in Dialog

    //variables for the sensor
    SensorManager sensorManager;
    Sensor accelerometer;
    float[] gravity;
    boolean first_read;
    double init_time, new_time, prev_time; // Used to keep track of time when calculating derivative
    float[] new_vals, prev_vals; // Float arrays used for calculating derivative

    private static final String DataFile = "AccData_Game.txt";
    private static final String DataFile_2 = "De_AccData_Game.txt";
    private static final String DataFile_3 = "linear_AccData_Game.txt";
    //Name of the file to which the data is exported

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        //initialise the game

        button_start = (Button)this.findViewById(R.id.buttonStart);
        button_start.setOnClickListener(this);
        button_pause = (Button)this.findViewById(R.id.buttonRank);
        button_pause.setOnClickListener(this);
        progress_horizontal = (ProgressBar)this.findViewById(R.id.progressBar_horizontal);
        progress_vertical = (ProgressBar)this.findViewById(R.id.progressBar_vertical);

        textview_score = (TextView)this.findViewById(R.id.textView_Score);
        snakeView = (SnakeView)this.findViewById(R.id.myView);
        snakeView.setmOnSnakeDeadListener(this);
        snakeView.setmOnSnakeEatListener(this);

        //initialise the sensor
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME);

        gravity = new float[3];
        first_read = true;
        init_time = 0; new_time = 0; prev_time = 0;
        prev_vals = new float[3]; new_vals = new float[3];

        //Highest score
        openHelper = new SnakeDBOpenHelper(this,"table_score",null,1);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from table_score order by score desc limit 1",null);
        if(cursor != null && cursor.getCount() >= 1){
            cursor.moveToFirst();
            highscore = cursor.getInt(2);
        }
        textview_score.setText("Score：0"+ "    Highest Score：" + highscore);



    }

    public static void save(String FILE_NAME, float[] Data) {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return;
        }
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), FILE_NAME);
        for (int i = 0; i < Data.length; i++) {
            // Convert float to String
            String stringValue = Float.toString(Data[i]);
            // Write String to txt file
            try {
                FileWriter writer = new FileWriter(file, true);
                writer.write(stringValue);
                writer.write(System.lineSeparator());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        delta_vals[1] = new_vals[1]-prev_vals[1];
        delta_vals[2] = new_vals[2]-prev_vals[2];
        // Calculate time difference between values
        double delta_time = new_time - prev_time;

        float[] sensval_derivative = new float[3];
        sensval_derivative[0] = (float) (delta_vals[0]/delta_time);
        sensval_derivative[1] = (float) (delta_vals[1]/delta_time);
        sensval_derivative[2] = (float) (delta_vals[2]/delta_time);

        return sensval_derivative;
    }
    public float[] linearAcc(float[] sens_vals, float alpha) {
        float[] linear_acc = new float[3];

        gravity[0] = alpha * gravity[0] + (1- alpha) * sens_vals[0];
        gravity[1] = alpha * gravity[1] + (1- alpha) * sens_vals[1];
        gravity[2] = alpha * gravity[2] + (1- alpha) * sens_vals[2];

        linear_acc[0] = sens_vals[0]- gravity[0];
        linear_acc[1] = sens_vals[1]- gravity[1];
        linear_acc[2] = sens_vals[2]- gravity[2];

        return linear_acc;
    }
    private void startCountdown() {
        // Create a 3 second countdown
        new CountDownTimer(3000, 1) {
            public void onTick(long millisUntilFinished) {
                // Update the UI at each tick
                button_start.setText(String.valueOf(millisUntilFinished / 1000+1));
            }

            public void onFinish() {
                button_start.setText("Pause"); // restore button text
                // Countdown to end task execution
                snakeView.StartGame();
                button_start.setEnabled(true);
            }
        }.start();
    }
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.buttonStart) {
            if(!collectValues) {
                collectValues = true;
                button_start.setEnabled(false);
                startCountdown();
            } else{
                collectValues = false;

                button_start.setText("Start");
                snakeView.PauseGame();
            }
        }
        else if (id == R.id.buttonRank) {
            collectValues = false;
            button_start.setText("Start");
            snakeView.PauseGame();
            Intent intent = new Intent(GameActivity.this, ScoreActivity.class);
            startActivity(intent);
        }

    }

    @Override
    public void OnSnakeDead(int foodcnt) {
        collectValues = false;
        button_start.setText("Start");
        //snakeView.PauseGame();
        LayoutInflater inflater = LayoutInflater.from(this);
        View textEntryView = inflater.inflate(R.layout.dialoglayout,null);
        input = (EditText) textEntryView.findViewById(R.id.editText_Name);
        final int score = foodcnt;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game over! Please enter your name.");
        builder.setView(textEntryView);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name = input.getText().toString().trim();
                SQLiteDatabase db = openHelper.getWritableDatabase();
                Cursor cursor = db.rawQuery("select * from table_score order by score desc limit 10",null);
                if(cursor == null){
                    db.close();
                    return;
                }
                if(cursor.getCount() < 10){
                    db.execSQL("insert into table_score(name,score) values(?,?)",
                            new String[]{name,Integer.toString(score)});
                }else{
                    cursor.moveToLast();
                    String id = cursor.getString(0);
                    int oldscore = cursor.getInt(2);
                    if(score > oldscore){
                        db.execSQL("update table_score set name=?,score=? where id=?",
                                new String[]{name,Integer.toString(score),id});
                    }
                }
                db.close();
            }
        });
        builder.show();
        //Toast.makeText(this,"Game Over!",Toast.LENGTH_SHORT).show();
    }
    @Override
    public void OnSnakeEatFood(int foodcnt) {
        if(foodcnt > highscore){
            highscore = foodcnt;
        }
        textview_score.setText("Score：" + foodcnt + "    Highest Score：" + highscore);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {

        if(collectValues) {
            float[] sensor_values = new float[3];
            sensor_values[0] = event.values[0];
            sensor_values[1] = event.values[1];
            sensor_values[2] = event.values[2];

            float[] linear_sensor_values = linearAcc(sensor_values,0.9f);

            int progressStatus_horizontal;
            int progressStatus_vertical;
            progressStatus_horizontal = (int) (100-100/6*(linear_sensor_values[0]+3));
            progressStatus_vertical = (int) (100-100/6*(linear_sensor_values[1]+3));
            progress_horizontal.setProgress(progressStatus_horizontal);
            progress_vertical.setProgress(progressStatus_vertical);

            float[] sens_derivatives = sensorDerivative(sensor_values);
            if (sens_derivatives[0] > 100 && linear_sensor_values[0] > 3) {
                snakeView.ControlGame(SnakeView.DIR_LEFT);
            }
            else if (sens_derivatives[0] < -100 && linear_sensor_values[0] < -3){
                snakeView.ControlGame(SnakeView.DIR_RIGHT);
            }
            else if (sens_derivatives[1] > 50 && linear_sensor_values[1] > 2){
                snakeView.ControlGame(SnakeView.DIR_DOWN);
            }
            else if (sens_derivatives[1] < -50 && linear_sensor_values[1] < -3){
                snakeView.ControlGame(SnakeView.DIR_UP);
            }

            //save(DataFile, sensor_values);
            //save(DataFile_2, sens_derivatives);
            //save(DataFile_3, linear_sensor_values);
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
                SensorManager.SENSOR_DELAY_GAME);
    }


}

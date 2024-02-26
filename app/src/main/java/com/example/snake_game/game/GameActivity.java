package com.example.snake_game.game;
//imports for game
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
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
    Button button_up;
    Button button_down;
    Button button_left;
    Button button_right;
    TextView textview_score;
    SnakeView snakeView;
    private int highscore = 0;
    boolean collectValues = false;
    private SnakeDBOpenHelper openHelper;
    private EditText input;//EditText object in Dialog

    //variables for the sensor
    SensorManager sensorManager;
    Sensor accelerometer;
    int counter; // X-axis
    float[] gravity;
    boolean initValueCheck;
    float initX, initY, initZ;
    boolean first_read;
    double init_time, new_time, prev_time; // Used to keep track of time when calculating derivative
    float[] new_vals, prev_vals; // Float arrays used for calculating derivative

    private static final String DataFile = "AccData_Game.txt";
    private static final String DataFile_2 = "De_AccData_Game.txt";
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
        /*
        button_up = (Button)this.findViewById(R.id.buttonUp);
        button_up.setOnClickListener(this);
        button_down = (Button)this.findViewById(R.id.buttonDown);
        button_down.setOnClickListener(this);
        button_left = (Button)this.findViewById(R.id.buttonLeft);
        button_left.setOnClickListener(this);
        button_right = (Button)this.findViewById(R.id.buttonRight);
        button_right.setOnClickListener(this);
        */
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
        initValueCheck = true;
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

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.buttonStart) {
            if(!collectValues) {
                collectValues = true;
                button_start.setText("Pause");
               snakeView.StartGame();
            } else{
                collectValues = false;
                initValueCheck = true;
                initX = 0;
                initY = 0;
                initZ = 0;

                button_start.setText("Start");
                snakeView.PauseGame();
            }
        /*
        } else if (id == R.id.buttonUp) {
            snakeView.ControlGame(SnakeView.DIR_UP);
        } else if (id == R.id.buttonDown) {
            snakeView.ControlGame(SnakeView.DIR_DOWN);
        } else if (id == R.id.buttonLeft) {
            snakeView.ControlGame(SnakeView.DIR_LEFT);
        } else if (id == R.id.buttonRight) {
            snakeView.ControlGame(SnakeView.DIR_RIGHT);
        */
        } else if (id == R.id.buttonRank) {
            collectValues = false;
            button_start.setText("Start");
            snakeView.PauseGame();
            Intent intent = new Intent(GameActivity.this, ScoreActivity.class);
            startActivity(intent);
        }

    }

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.view_rank){
            Intent intent = new Intent(GameActivity.this,ScoreActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
*/
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

            if(initValueCheck) {
                /*initX = event.values[0];
                initY = event.values[1];
                initZ = event.values[2];*/
                initX = 0;
                initY = 0;
                initZ = 9.8f;
                initValueCheck = false;
            }

            float[] sensor_values = new float[4];
            sensor_values[0] = event.values[0];
            sensor_values[1] = event.values[1];
            sensor_values[2] = event.values[2];

            float[] sens_derivatives = sensorDerivative(sensor_values);
            if(sens_derivatives[0] > 100 && sensor_values[0]-initX > 2) {
                snakeView.ControlGame(SnakeView.DIR_LEFT);
                sensor_values[3] =1;
            }else if(sens_derivatives[0] < -100 && sensor_values[0]-initX < -2){
                snakeView.ControlGame(SnakeView.DIR_RIGHT);
                sensor_values[3] =-1;
            }else if(sens_derivatives[1] > 100 && sensor_values[1]-initY > 2){
                snakeView.ControlGame(SnakeView.DIR_DOWN);
                sensor_values[3] =2;
            }else if(sens_derivatives[1] < -100 && sensor_values[1]-initY < -2){
                snakeView.ControlGame(SnakeView.DIR_UP);
                sensor_values[3] =-2;
            }/*else {
                sensor_values[3] =0;
            }*/
            save(DataFile, sensor_values);
            save(DataFile_2, sens_derivatives);
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


  /* SensorManager sensorManager;
  Sensor accelerometer;

  protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}*/

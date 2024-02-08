package com.example.snake_game;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.example.snake_game.helper.SnakeDBOpenHelper;
import com.example.snake_game.intface.OnSnakeDeadListener;
import com.example.snake_game.intface.OnSnakeEatFoodListener;
import com.example.snake_game.widget.SnakeView;
public class GameActivity extends AppCompatActivity implements View.OnClickListener,OnSnakeDeadListener,OnSnakeEatFoodListener {
    Button button_start;
    Button button_pause;
    Button button_up;
    Button button_down;
    Button button_left;
    Button button_right;
    TextView textview_score;
    SnakeView snakeView;

    private int highscore = 0;
    private SnakeDBOpenHelper openHelper;
    private EditText input;//Dialog中EditText对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        button_start = (Button)this.findViewById(R.id.buttonStart);
        button_start.setOnClickListener(this);
        button_pause = (Button)this.findViewById(R.id.buttonPause);
        button_pause.setOnClickListener(this);
        button_up = (Button)this.findViewById(R.id.buttonUp);
        button_up.setOnClickListener(this);
        button_down = (Button)this.findViewById(R.id.buttonDown);
        button_down.setOnClickListener(this);
        button_left = (Button)this.findViewById(R.id.buttonLeft);
        button_left.setOnClickListener(this);
        button_right = (Button)this.findViewById(R.id.buttonRight);
        button_right.setOnClickListener(this);
        textview_score = (TextView)this.findViewById(R.id.textView_Score);
        snakeView = (SnakeView)this.findViewById(R.id.myView);
        snakeView.setmOnSnakeDeadListener(this);
        snakeView.setmOnSnakeEatListener(this);

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

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.buttonStart) {
            snakeView.StartGame();
        } else if (id == R.id.buttonPause) {
            snakeView.PauseGame();
        } else if (id == R.id.buttonUp) {
            snakeView.ControlGame(SnakeView.DIR_UP);
        } else if (id == R.id.buttonDown) {
            snakeView.ControlGame(SnakeView.DIR_DOWN);
        } else if (id == R.id.buttonLeft) {
            snakeView.ControlGame(SnakeView.DIR_LEFT);
        } else if (id == R.id.buttonRight) {
            snakeView.ControlGame(SnakeView.DIR_RIGHT);
        }
    }

    @Override
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

    @Override
    public void OnSnakeDead(int foodcnt) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View textEntryView = inflater.inflate(R.layout.dialoglayout,null);
        input = (EditText) textEntryView.findViewById(R.id.editText_Name);
        final int score = foodcnt - 4;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game over!");
        builder.setView(textEntryView);
        builder.setPositiveButton("Restart", new DialogInterface.OnClickListener() {
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

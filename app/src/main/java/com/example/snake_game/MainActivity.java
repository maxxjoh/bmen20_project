package com.example.snake_game;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.snake_game.game.GameActivity;
import com.example.snake_game.TestActivity;

public class MainActivity extends AppCompatActivity {
    private Button startButton;
    private Button testButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = findViewById(R.id.mStartButton);
        testButton = findViewById(R.id.mTestButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startStartGameIntent = new Intent(MainActivity.this,
                        GameActivity.class);
                startActivity(startStartGameIntent);
            }
        });
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startTestActivityIntent = new Intent(MainActivity.this, TestActivity.class);
                startActivity(startTestActivityIntent);
            }
        });
    }
}
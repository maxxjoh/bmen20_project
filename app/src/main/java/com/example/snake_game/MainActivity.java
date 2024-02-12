package com.example.snake_game;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.snake_game.game.GameActivity;

public class MainActivity extends AppCompatActivity {
    private Button playButton;
    private Button scoreButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playButton = findViewById(R.id.mPlayButton);
        scoreButton = findViewById(R.id.mScoreButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startStartGameIntent = new Intent(MainActivity.this,
                        GameActivity.class);
                startActivity(startStartGameIntent);
            }
        });
        scoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startTestActivityIntent = new Intent(MainActivity.this, TestActivity.class);
                startActivity(startTestActivityIntent);
            }
        });
    }
}
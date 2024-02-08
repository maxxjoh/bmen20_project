package com.example.snake_game;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private Button playButton;
    private Button scoreButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playButton = findViewById(R.id.mPlayButton);
        scoreButton = findViewById(R.id.mScoreButton);
        playButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startGameActivityIntent = new Intent(MainActivity.this, GameActivity.class);
                startActivity(startGameActivityIntent);
            }
        });
        scoreButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startScoreAvtivityIntent = new Intent(MainActivity.this, ScoreActivity.class);
                startActivity(startScoreAvtivityIntent);
            }
        });

    }
}
/* Copyright (C) 2015 by Alexandru Cojocaru */

/* This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package pw.xojoc.snake;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/* TODO:
        Vibratioin
        menu
        support keys
        optimize high score drawing
        delay after death
*/

public class SnakeActivity extends Activity {
    static TextView scoreText;
    static TextView highScoreText;
    static SnakeView snakeView;
    static TextView statusText;
    final static TextView[] speedsText = new TextView[3];
    static TextView speedLabelText;
    static ImageView muteImage;
    static TextView pauseText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snake);

        scoreText = (TextView) findViewById(R.id.score);
        highScoreText = (TextView) findViewById(R.id.highScore);
        snakeView = (SnakeView) findViewById(R.id.snake);
        statusText = (TextView) findViewById(R.id.status);
        speedsText[0] = (TextView) findViewById(R.id.speed1);
        speedsText[1] = (TextView) findViewById(R.id.speed2);
        speedsText[2] = (TextView) findViewById(R.id.speed3);
        speedLabelText = (TextView) findViewById(R.id.speedLabel);
        muteImage = (ImageView) findViewById(R.id.mute);
        pauseText = (TextView) findViewById(R.id.pause);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        snakeView.setPlaySounds(sharedPref.getBoolean("playSounds", true));
        snakeView.setSpeed(sharedPref.getInt("speed", 0));
        snakeView.setMode(SnakeView.Ready);
    }

    @Override
    protected void onPause() {
        super.onPause();
        snakeView.setMode(SnakeView.Pause);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("highScore0", SnakeView.highScore[0]);
        editor.putInt("highScore1", SnakeView.highScore[1]);
        editor.putInt("highScore2", SnakeView.highScore[2]);
        editor.putBoolean("playSounds", snakeView.playSounds);
        editor.putInt("speed", snakeView.speed);
        editor.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
        snakeView.destroySounds();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SnakeView.highScore[0] = sharedPref.getInt("highScore0", 0);
        SnakeView.highScore[1] = sharedPref.getInt("highScore1", 0);
        SnakeView.highScore[2] = sharedPref.getInt("highScore2", 0);
        snakeView.playSounds = sharedPref.getBoolean("playSounds", true);
        if (snakeView.playSounds && SnakeView.collectSound == null) {
            snakeView.createSounds();
        }
        snakeView.speed = sharedPref.getInt("speed", 0);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBundle("snakeView", snakeView.saveState());
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        snakeView.restoreState(savedInstanceState.getBundle("snakeView"));
    }

    public void moveSnakeUp(View v) {
        snakeView.moveSnakeUp();
    }

    public void moveSnakeRight(View v) {
        snakeView.moveSnakeRight();
    }

    public void moveSnakeDown(View v) {
        snakeView.moveSnakeDown();
    }

    public void moveSnakeLeft(View v) {
        snakeView.moveSnakeLeft();
    }

    public void mute(View v) {
        snakeView.setPlaySounds(!snakeView.playSounds);
    }

    public void pause(View v) {
        if (snakeView.mode == SnakeView.Pause) {
            snakeView.setMode(SnakeView.Running);
        } else if (snakeView.mode == SnakeView.Running) {
            snakeView.setMode(SnakeView.Pause);
        }
    }

    public void setSpeed(View v) {
        int vid = v.getId();
        if (vid == R.id.speed1) {
            snakeView.setSpeed(0);
        } else if (vid == R.id.speed2) {
            snakeView.setSpeed(1);
        } else if (vid == R.id.speed3) {
            snakeView.setSpeed(2);
        }
    }
}

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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class SnakeView extends View {
    public static final int Pause = 0;
    public static final int Ready = 1;
    public static final int Running = 2;
    public static final int Lose = 3;
    int mode = Ready;

    private static final int North = 1;
    private static final int South = 2;
    private static final int East = 3;
    private static final int West = 4;
    private int direction;

    private static int tileSize;
    private static int xTileCount;
    private static int yTileCount;
    final private static Paint tilePaint = new Paint();

    private static int score;
    static int highScore[] = new int[3];
    private static int appleX;
    private static int appleY;
    private static final Random rng = new Random();
    private ArrayList<Integer> tailX = new ArrayList<>();
    private ArrayList<Integer> tailY = new ArrayList<>();

    private long lastMove = 0;
    private long delayMove;
    int speed;

    boolean playSounds;
    // http://www.freesound.org/people/fins/sounds/133280/
    static MediaPlayer collectSound;

    final private RefreshHandler redrawHandler = new RefreshHandler();

    class RefreshHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            SnakeView.this.invalidate();
            SnakeView.this.update();
        }

        public void sleep(long delayMillis) {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    }


    public SnakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSnakeView();
    }

    public SnakeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initSnakeView();
    }

    private void initSnakeView() {
        Resources res = getResources();
        tileSize = (int) res.getDimension(R.dimen.tile);
        xTileCount = ((int) res.getDimension(R.dimen.gridWidth)) / tileSize;
        yTileCount = ((int) res.getDimension(R.dimen.gridHeight)) / tileSize;
        tilePaint.setColor(0xff000000);
        tilePaint.setStrokeWidth(tileSize);
        tilePaint.setStyle(Paint.Style.STROKE);
        initNewGame();
    }

    private void initNewGame() {
        tailX.clear();
        tailY.clear();
        tailX.addAll(Arrays.asList(7, 6, 5, 4, 3, 2));
        tailY.addAll(Arrays.asList(7, 7, 7, 7, 7, 7));
        direction = East;
        randomApple();
        score = 0;
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        SnakeActivity.scoreText.setText(String.format("SCORE: %d", score));
        drawHighScore(highScore[speed]);
        c.drawRect(0, 0, xTileCount * tileSize, yTileCount * tileSize, tilePaint);
        drawSnake(c);
        c.drawPoint(appleX * tileSize, appleY * tileSize, tilePaint);
    }

    private void drawSnake(Canvas c) {
        int i = 0;
        for (int x : tailX) {
            c.drawPoint(x * tileSize, tailY.get(i) * tileSize, tilePaint);
            ++i;
        }
    }

    private void drawHighScore(int hs) {
        SnakeActivity.highScoreText.setText(String.format("HIGH SCORE: %d", hs));
    }

    public Bundle saveState() {
        Bundle b = new Bundle();
        b.putInt("mode", mode);
        b.putInt("direction", direction);
        b.putInt("score", score);
        b.putInt("highScore0", highScore[0]);
        b.putInt("highScore1", highScore[1]);
        b.putInt("highScore2", highScore[2]);
        b.putInt("appleX", appleX);
        b.putInt("appleY", appleY);
        b.putIntegerArrayList("tailX", tailX);
        b.putIntegerArrayList("tailY", tailY);
        b.putInt("speed", speed);
        b.putBoolean("playSounds", playSounds);
        return b;
    }

    public void restoreState(Bundle b) {
        mode = b.getInt("mode");
        direction = b.getInt("direction");
        score = b.getInt("score");
        highScore[0] = b.getInt("highScore0");
        highScore[1] = b.getInt("highScore1");
        highScore[2] = b.getInt("highScore2");
        appleX = b.getInt("appleX");
        appleY = b.getInt("appleY");
        tailX = b.getIntegerArrayList("tailX");
        tailY = b.getIntegerArrayList("tailY");
        speed = b.getInt("speed");
        playSounds = b.getBoolean("playSounds");
    }


    void randomApple() {
        boolean collides;
        do {
            appleX = 1 + rng.nextInt(xTileCount - 1);
            appleY = 1 + rng.nextInt(yTileCount - 1);
            collides = false;
            int i = 0;
            for (int x : tailX) {
                if (x == appleX && tailY.get(i) == appleY) {
                    collides = true;
                    break;
                }
                ++i;
            }
        } while (collides);
    }

    void setMode(int newMode) {
        mode = newMode;

        if (newMode == Running) {
            SnakeActivity.statusText.setVisibility(View.INVISIBLE);
            SnakeActivity.pauseText.setVisibility(View.VISIBLE);
            SnakeActivity.pauseText.setText("PAUSE");
            SnakeActivity.speedLabelText.setVisibility(View.INVISIBLE);
            SnakeActivity.speedsText[0].setVisibility(View.INVISIBLE);
            SnakeActivity.speedsText[1].setVisibility(View.INVISIBLE);
            SnakeActivity.speedsText[2].setVisibility(View.INVISIBLE);
            return;
        }

        SnakeActivity.pauseText.setVisibility(View.INVISIBLE);
        SnakeActivity.statusText.setVisibility(View.VISIBLE);
        if (newMode == Ready || newMode == Lose) {
            SnakeActivity.speedLabelText.setVisibility(View.VISIBLE);
            SnakeActivity.speedsText[0].setVisibility(View.VISIBLE);
            SnakeActivity.speedsText[1].setVisibility(View.VISIBLE);
            SnakeActivity.speedsText[2].setVisibility(View.VISIBLE);
        }

        CharSequence str = "";
        if (newMode == Pause) {
            str = "Tap any arrow to RESUME";
        } else if (newMode == Ready) {
            str = "Tap any arrow to START :)";
        } else if (newMode == Lose) {
            str = "Game over! Tap any arrow to RESTART";
            if (score > highScore[speed]) {
                highScore[speed] = score;
            }
        }
        SnakeActivity.statusText.setText(str);
    }

    void update() {
        if (mode == Running) {
            long now = System.currentTimeMillis();
            if (now - lastMove >= delayMove) {
                if ((now - lastMove) - delayMove >= 10) {
                    Log.w("a", String.format("behind %d", (now - lastMove) - delayMove));
                }
                updateSnake();
                lastMove = now;
            }
            redrawHandler.sleep(delayMove);
        } else if (mode == Pause) {
            lastMove = System.currentTimeMillis();
        }
    }

    void awakeSnake() {
        if (mode == Ready || mode == Lose) {
            initNewGame();
            setMode(Running);
            update();
            return;
        }
        if (mode == Pause) {
            setMode(Running);
            lastMove = System.currentTimeMillis();
            update();
            return;
        }
    }

    void moveSnakeUp() {
        awakeSnake();
        if (direction != South) {
            direction = North;
        }
    }

    void moveSnakeRight() {
        awakeSnake();
        if (direction != West) {
            direction = East;
        }
    }

    void moveSnakeDown() {
        awakeSnake();
        if (direction != North) {
            direction = South;
        }
    }

    void moveSnakeLeft() {
        awakeSnake();
        if (direction != East) {
            direction = West;
        }
    }

    void updateSnake() {
        int newHeadX = tailX.get(0);
        int newHeadY = tailY.get(0);

        switch (direction) {
            case North:
                newHeadY -= 1;
                break;
            case East:
                newHeadX += 1;
                break;
            case South:
                newHeadY += 1;
                break;
            case West:
                newHeadX -= 1;
                break;
        }

        if (newHeadX < 1 || newHeadY < 1 || newHeadX > xTileCount - 1 || newHeadY > yTileCount - 1) {
            setMode(Lose);
            return;
        }


        int i = 0;
        for (int x : tailX) {
            if (x == newHeadX && tailY.get(i) == newHeadY) {
                setMode(Lose);
                return;
            }
            ++i;
        }

        tailX.add(0, newHeadX);
        tailY.add(0, newHeadY);

        if (newHeadX == appleX && newHeadY == appleY) {
            randomApple();
            ++score;
            if (collectSound != null) {
                collectSound.start();
            }
        } else {
            tailX.remove(tailX.size() - 1);
            tailY.remove(tailY.size() - 1);
        }

    }


    private class CreateMP extends AsyncTask<Integer, String, String> {
        @Override
        protected String doInBackground(Integer... params) {
            SnakeView.collectSound = MediaPlayer.create(getContext(), R.raw.collect);
            return null;
        }
    }

    void createSounds() {
        new CreateMP().execute();
    }

    void destroySounds() {
        collectSound.release();
        collectSound = null;
    }

    void setPlaySounds(boolean play) {
        if (play) {
            SnakeActivity.muteImage.setImageResource(R.drawable.mute);
            createSounds();
            playSounds = true;
        } else {
            SnakeActivity.muteImage.setImageResource(R.drawable.unmute);
            destroySounds();
            playSounds = false;
        }
    }

    void setSpeed(int s) {
        SnakeActivity.speedsText[speed].setBackgroundResource(0);
        SnakeActivity.speedsText[s].setBackgroundResource(R.drawable.highlight);
        speed = s;
        if (speed == 0) {
            delayMove = 200;
        } else if (speed == 1) {
            delayMove = 150;
        } else {
            delayMove = 70;
        }
        drawHighScore(highScore[speed]);
    }
}

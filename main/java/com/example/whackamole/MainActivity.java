package com.example.whackamole;

import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

/*
**  WhackAMole game, where user scores points by clicking/touching a Mole button.  Defaults to 10 second game.
 */
public class MainActivity extends AppCompatActivity {

    private int NUM_MOLES;
    private int PERIOD;
    private int GAME_LENGTH;
    private Button[] buttons;
    private int score = 0;
    private CountDownTimer timer;
    private long time_left;

    /*
     **  Setup the game based on user's choice of difficulty level
     */
    void game_setup () {
        score = 0;
        GAME_LENGTH = 10000;
        TextView tv = findViewById(R.id.score);
        tv.setText(getString(R.string.score, score));
        if (((RadioButton) findViewById(R.id.easy)).isChecked()) {
            PERIOD = 500;
            NUM_MOLES = 10;
        } else if (((RadioButton) findViewById(R.id.medium)).isChecked()) {
            PERIOD = 250;
            NUM_MOLES = 5;
        } else {  //hard is checked
            PERIOD = 100;
            NUM_MOLES = 3;
        }
        buttons = new Button[NUM_MOLES];
        createMoles(NUM_MOLES);
        RelativeLayout rl = findViewById(R.id.game_board);
        rl.setVisibility(View.VISIBLE);
    }

    /*
    **  Dynamically create Moles and add to game board, with random colors and starting positions, assign click listener
    **  @param n - number of Moles to create
     */
    private void createMoles(int n) {
        Random rnd = new Random();
        for (int i = 0; i < n; i++) {
            Button newBtn = new Button(this);
            newBtn.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            newBtn.setText(getString(R.string.mole, i));
            newBtn.setId(i);
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            newBtn.setBackgroundColor(color);

            //When clicked, score a point and move the Mole
            newBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View v) {
                    Animation aniFade = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_out);
                    v.startAnimation(aniFade);

                    v.setVisibility(View.INVISIBLE);
                    score += 1;
                    TextView tv = findViewById(R.id.score);
                    tv.setText(getString(R.string.score, score));
                    //if (aniFade.hasEnded())
                    move(v);
                    v.setVisibility(View.VISIBLE);
                }
            });
            buttons[i] = newBtn;

            RelativeLayout layout = findViewById(R.id.game_board);
            layout.addView(newBtn);
            move(newBtn);
        }
    }

    /*
    **  When player clicks Play button, launch game by starting the timer
    **  @param v - button that was clicked
     */
    void play_game(View v) {
        game_setup();
        v.setVisibility(View.INVISIBLE);
        RelativeLayout rl = findViewById(R.id.score_board);
        rl.setVisibility(View.INVISIBLE);

        create_timer(GAME_LENGTH, PERIOD);
    }

    /*
    **  Start game timer and move a Mole once every period
    **  @param time - how long in milliseconds to run game
    **  @param period - how often in milliseconds to move a Mole
     */
    private void create_timer (long time, long period) {
        timer = new CountDownTimer(time, period) {
            public void onFinish() {
                //Log.d("timer","done");
                stop_game();
            }

            //Each period, move a random Mole and keep track of time remaining
            public void onTick(long millisUntilFinished) {
                Random generator = new Random();
                int num = generator.nextInt(NUM_MOLES);
                move(buttons[num]);
                time_left = millisUntilFinished;
                TextView tv = findViewById(R.id.time);
                tv.setText(getString(R.string.time, time_left/1000));
            }
        }.start();
    }

    /*
    **  Randomly move Mole, making sure to keep within the game board
    **  @param mole - mole to move
     */
    private void move(View mole) {
        final RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mole.getLayoutParams();
        Random generator = new Random();
        int bound = ((RelativeLayout) mole.getParent()).getHeight() - Math.max(mole.getHeight(),126);
        int marginTop= generator.nextInt(bound); //possible values are 0 through bound-1

        bound = ((RelativeLayout) mole.getParent()).getWidth() - Math.max(mole.getWidth(),231);
        int marginLeft = generator.nextInt(bound);
        lp.setMargins(marginLeft, marginTop, 0, 0);
        mole.setLayoutParams(lp);
    }

    /*
     **  When timer runs out, clear board and show score
     */
    void stop_game() {
        time_left = 0;
        RelativeLayout rl = findViewById(R.id.game_board);
        rl.setVisibility(View.INVISIBLE);
        for (Button btn : buttons) {
            rl.removeView(btn);
        }

        Button btn = findViewById(R.id.play_again);
        btn.setVisibility(View.VISIBLE);
        TextView tv = findViewById(R.id.final_score_num);
        tv.setText(getString(R.string.score, score));
        rl = findViewById(R.id.score_board);
        rl.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if game was running when Paused, restart timer with appropriate amount of time left
        if (time_left > 0) create_timer(time_left,PERIOD);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }
}

/*        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("otter", "inside timer task");
            }
        }, 5000);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d("otter", "fixed rate");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        }, 5000,1000);
*/

package org.labex.controller;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ControllerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        initialize();
    }

    // region VARIABLES
    private byte curDir = -1;

    private Button buttonLeft;
    private Button buttonRight;
    private TextView textDistance;
    private ViewGroup btControlLayout, autopilotLayout;

    private AsyncTask<Integer, Void, Void> distanceCheckTask;
    private boolean stopMoving = false;
    // endregion

    private void initialize() {
        SeekBar seekbarHead = (SeekBar) findViewById(R.id.seekbar_head);

        seekbarHead.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // CONVERT INTO EYES LIKE DEGREE OF OBSERVATION (LEFT, CENTER, RIGHT)
                progress = 180 - progress * 5;

                LabexConnection.sendAction((byte) getResources().getInteger(R.integer.ACTION_LOOK), (byte) progress, false);
                //Log.e("SENT", String.format("%d %d", 1, progress));
            }
        });

        // region Button Touch listener
        View.OnTouchListener onActionButtonTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Resources res = getResources();
                byte action = (byte) res.getInteger(R.integer.ACTION_MOVE);
                byte data = (byte) res.getInteger(R.integer.BRAKE);
                boolean turnCondition = false;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    switch (v.getId()) {
                        case R.id.buttonLeft:
                            action = (byte) res.getInteger(R.integer.ACTION_MOVE);
                            data = (byte) res.getInteger(R.integer.LEFT);
                            turnCondition = true;
                            break;
                        case R.id.buttonUp:
                            action = (byte) res.getInteger(R.integer.ACTION_MOVE);
                            data = (byte) res.getInteger(R.integer.RIGHT_STAY);
                            break;
                        case R.id.buttonRight:
                            action = (byte) res.getInteger(R.integer.ACTION_MOVE);
                            data = (byte) res.getInteger(R.integer.RIGHT);
                            turnCondition = true;
                            break;
                        case R.id.buttonDown:
                            action = (byte) res.getInteger(R.integer.ACTION_MOVE);
                            data = (byte) res.getInteger(R.integer.LEFT_STAY);
                            break;
                        case R.id.buttonIks:
                            action = (byte) res.getInteger(R.integer.ACTION_MOVE);
                            data = (byte) res.getInteger(R.integer.FORWARD);
                            curDir = (byte) res.getInteger(R.integer.FORWARD);
                            toggleLeftRight(true);
                            break;
                        case R.id.buttonSquare:
                            action = (byte) res.getInteger(R.integer.ACTION_MOVE);
                            data = (byte) res.getInteger(R.integer.BRAKE);
                            curDir = (byte) res.getInteger(R.integer.BRAKE);
                            break;
                        case R.id.buttonTriangle:
                            action = (byte) res.getInteger(R.integer.ACTION_MOVE);
                            data = (byte) res.getInteger(R.integer.BACKWARD);
                            curDir = (byte) res.getInteger(R.integer.BACKWARD);
                            toggleLeftRight(true);
                            break;
                        case R.id.buttonCircle:
                            action = (byte) res.getInteger(R.integer.ACTION_MOVE);
                            data = (byte) res.getInteger(R.integer.BRAKE);
                            curDir = (byte) res.getInteger(R.integer.BRAKE);
                            break;
                        default:
                            // DFEAULT: STAY STILL
                            action = (byte) res.getInteger(R.integer.ACTION_MOVE);
                            data = (byte) res.getInteger(R.integer.BRAKE);
                            curDir = (byte) res.getInteger(R.integer.BRAKE);
                            break;
                    }

                    if (turnCondition && curDir == (byte) res.getInteger(R.integer.BRAKE)) {
                        action = (byte) res.getInteger(R.integer.ACTION_MOVE);
                        data = (byte) res.getInteger(R.integer.BRAKE);
                    }

                    LabexConnection.sendAction(action, data, false);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (v.getId() == R.id.buttonLeft || v.getId() == R.id.buttonRight) {
                        action = (byte) res.getInteger(R.integer.ACTION_MOVE);
                        data = curDir;
                    } else {
                        action = (byte) res.getInteger(R.integer.ACTION_MOVE);
                        data = (byte) res.getInteger(R.integer.BRAKE);
                        curDir = (byte) res.getInteger(R.integer.BRAKE);
                    }

                    if (v.getId() == R.id.buttonIks || v.getId() == R.id.buttonTriangle)
                        toggleLeftRight(false);

                    //Log.e("MSG", String.format("ACTION:%d DATA:%d SENT:%b", action, data, sent));
                    LabexConnection.sendAction(action, data, false);
                }

                return false;
            }
        };
        // endregion

        (buttonLeft = (Button) findViewById(R.id.buttonLeft)).setOnTouchListener(onActionButtonTouchListener);
        (buttonRight = (Button) findViewById(R.id.buttonRight)).setOnTouchListener(onActionButtonTouchListener);
        findViewById(R.id.buttonUp).setOnTouchListener(onActionButtonTouchListener);
        findViewById(R.id.buttonDown).setOnTouchListener(onActionButtonTouchListener);

        findViewById(R.id.buttonIks).setOnTouchListener(onActionButtonTouchListener);
        findViewById(R.id.buttonTriangle).setOnTouchListener(onActionButtonTouchListener);
        findViewById(R.id.buttonSquare).setOnTouchListener(onActionButtonTouchListener);
        findViewById(R.id.buttonCircle).setOnTouchListener(onActionButtonTouchListener);

        textDistance = (TextView) findViewById(R.id.text_obstacle_distance);

        btControlLayout = (ViewGroup) findViewById(R.id.layout_manual_control);
        autopilotLayout = (ViewGroup) findViewById(R.id.layout_autopilot_control);

        toggleLeftRight(false);
    }


    private void toggleLeftRight(boolean state) {
        buttonRight.setEnabled(state);
        buttonLeft.setEnabled(state);
    }

    public void toggleAutopilot(View view) {
        byte data;

        if (((ToggleButton) view).isChecked()) {
            // AUTOPILOT MODE
            data = (byte) getResources().getInteger(R.integer.MODE_AUTOPILOT);

            autopilotLayout.setVisibility(View.VISIBLE);
            btControlLayout.setVisibility(View.INVISIBLE);

            stopDistanceCheck();

            LabexConnection.sendAction((byte) getResources().getInteger(R.integer.ACTION_CHANGEMODE), data, true);
        } else {
            //HANDDRIVEN MODE
            data = (byte) getResources().getInteger(R.integer.MODE_HANDDRIVEN);

            btControlLayout.setVisibility(View.VISIBLE);
            autopilotLayout.setVisibility(View.INVISIBLE);

            startDistanceCheck();

            LabexConnection.sendAction((byte) getResources().getInteger(R.integer.ACTION_CHANGEMODE), data, true);
            LabexConnection.sendAction((byte) getResources().getInteger(R.integer.ACTION_MOVE), (byte) getResources().getInteger(R.integer.BRAKE), true);
        }
    }


    private void startDistanceCheck() {
        distanceCheckTask = new AsyncTask<Integer, Void, Void>() {
            @Override
            protected Void doInBackground(Integer... params) {
                while (true) {
                    try {
                        Thread.sleep((int) (params[0] * 0.7));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (LabexConnection.labexConnected()) {
                        if (isCancelled())
                            break;

                        LabexConnection.sendAction((byte) getResources().getInteger(R.integer.ACTION_DATAREQUEST), (byte) getResources().getInteger(R.integer.REQUEST_OBSTACLE_DISTANCE), false);
                        final int distance = LabexConnection.readByte(false) | (LabexConnection.readByte(false) << 8);

                        if (isCancelled())
                            break;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (noObstacle(distance))
                                    textDistance.setText("NO OBSTACLE");
                                else if (distance == -1)
                                    textDistance.setText("TRIGGER ERROR");
                                else if (distance == -2)
                                    textDistance.setText("ECHO ERROR");
                                else
                                    textDistance.setText(String.format("%s CMs", distance));

                                @SuppressWarnings("deprecation")
                                ObjectAnimator animator = ObjectAnimator.ofInt(textDistance, "textColor", getResources().getColor(R.color.textColor));
                                animator.setEvaluator(new ArgbEvaluator());
                                animator.setDuration(500);
                                animator.setIntValues(Color.RED, Color.GRAY);
                                animator.start();
                            }
                        });
                    }
                }
                return null;
            }
        };

        distanceCheckTask.execute(700);
    }

    private void stopDistanceCheck() {
        distanceCheckTask.cancel(true);
    }

    private boolean noObstacle(int value) {
        return (value != -1 && value != -2) && (value < 0 || value > 200);
    }


    @Override
    protected void onStop() {
        super.onStop();
        stopDistanceCheck();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startDistanceCheck();
    }
}

package com.sugarsnooper.workout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.emitters.StreamEmitter;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

import java.util.Locale;


public class RunExercise extends AppCompatActivity {
    TextView runningExerciseName;
    TextView runningExerciseState;
    ImageView runningExercisePauseButton;
    CircularProgressIndicator progressIndicator;
    WebView searchYoutube;

    Integer timerDuration;
    Integer exerciseIndexGoingOn;

    Boolean runningThread = true;
    Thread mainThread;
    Boolean mainThreadPaused = false;

    Boolean exerciseFinished = false;

    Handler handler = new Handler(Looper.getMainLooper());

    TextToSpeech textToSpeech;
    Boolean textToSpeechWorking = false;

    private Long goBackConfirmationToastTime = System.currentTimeMillis();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.run_exercise);

        runningExerciseName = findViewById(R.id.runningExerciseName);
        runningExerciseState = findViewById(R.id.runningExerciseState);
        runningExercisePauseButton = findViewById(R.id.runningExercisePauseButton);
        progressIndicator = findViewById(R.id.circular_progress);

        // initializing text to speech listener
        if (Globals.allowSound) {
            textToSpeech = new TextToSpeech(RunExercise.this, new OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int result = textToSpeech.setLanguage(Locale.ENGLISH);

                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        } else {
                            textToSpeechWorking = true;
                        }
                    }
                }
            });
            textToSpeech.setSpeechRate(1);
        }

        mainThread = new Thread(this::threadStarter);
        mainThread.start();
    }

    public void threadStarter(){
        for (int i=0; i<Globals.exerciseListToDisplay.size(); i++){

            exerciseIndexGoingOn = i;
            while (mainThreadPaused){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (runningThread) {
                handler.post(() -> runningExerciseState.setText(Globals.exerciseListToDisplay.get(exerciseIndexGoingOn)));
            }
            while (mainThreadPaused){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // rest period except for 1st exercise
            if (exerciseIndexGoingOn != 0 && runningThread) {
                handler.post(() -> runningExerciseName.setText(("Rest")));
                timer(Globals.restDuration-5);
            }
            while (mainThreadPaused){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // get ready period
            if (runningThread) {
                new Thread(() -> speak("Get ready")).start();
                handler.post(() -> runningExerciseName.setText(("Get ready")));
                timer(5);
            }
            while (mainThreadPaused){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // exercise period
            if (runningThread) {
                new Thread(() -> speak(("Start" + Globals.exerciseListToDisplay.get(exerciseIndexGoingOn)))).start();

                handler.post(() -> {
                    runningExerciseName.setText(Globals.exerciseListToDisplay.get(exerciseIndexGoingOn));

                    if (exerciseIndexGoingOn != (Globals.exerciseListToDisplay.size() - 1)) {
                        runningExerciseState.setText(("Next up: " + Globals.exerciseListToDisplay.get(exerciseIndexGoingOn + 1)));
                    } else {
                        runningExerciseState.setText(("It's the last exercise!!"));
                        exerciseFinished = true;
                    }
                });
                timer(Globals.exerciseDuration);

                if (runningThread){
                    new Thread(() -> speak("Stop")).start();
                }

                // exercise completed
                if (exerciseFinished && runningThread){
                    handler.post(() -> {
                        runningExerciseName.setText(("Exercise finished..!!"));
                        runningExerciseState.setText(("Good work..!!"));
                        progressIndicator.setProgress(0, 30);
                        runningExercisePauseButton.setEnabled(false);
                        runningExercisePauseButton.setAlpha(0.5f);
                        startParticles();
                    });
                }
            }
        }
    }

    public void timer(Integer seconds){
        timerDuration = seconds;
        while (mainThreadPaused){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (timerDuration > 0 && runningThread){
            handler.post(() -> progressIndicator.setProgress(timerDuration, seconds));

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (mainThreadPaused){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            timerDuration --;
        }
    }

    public void speak(String toSpeak){
        if (textToSpeechWorking && Globals.allowSound) {
            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void PlayPauseRunningExercise(View view) {
        if (mainThreadPaused){
            runningExercisePauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_24);
        }
        else{
            runningExercisePauseButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        }
        mainThreadPaused = !mainThreadPaused;
    }

    public void stopRunningExercise(View view) {
        exitRunExerciseConfirmation();
    }

    public void exitRunExerciseConfirmation(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(RunExercise.this);
        alertBuilder.setMessage("Go to home page? ")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        runningThread = false;
                        startActivity(new Intent(RunExercise.this, SetExercise.class));
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    private void startParticles(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        final KonfettiView konfettiView = findViewById(R.id.viewKonfetti);
        konfettiView.setVisibility(View.VISIBLE);
        konfettiView.build()
                .addColors(Color.WHITE, Color.RED, Color.GREEN)
                .setDirection(90.0, 90.0)
                .setSpeed(1f, 10f)
                .setTimeToLive(2000L)
                .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                .addSizes(new Size(10, 20f))
                .setPosition(0f, width + 0f, 0f, null)
                .streamFor(20, StreamEmitter.INDEFINITE);
    }

    @Override
    public void onBackPressed() {
        if (goBackConfirmationToastTime + 2000 > System.currentTimeMillis()) {
            startActivity(new Intent(RunExercise.this, SetExercise.class));
            finish();
            super.onBackPressed();
            return;
        }
        else{
            Toast.makeText(this, "Press again to go back", Toast.LENGTH_SHORT).show();
        }
        goBackConfirmationToastTime = System.currentTimeMillis();
    }

    public void fabSearchYouTube(View view) {
        if (GlobalMethods.isNetworkAvailable()) {
            searchYoutube = findViewById(R.id.searchYoutube);
            searchYoutube.loadUrl("https://www.youtube.com/results?search_query=exercise+" + Globals.exerciseListToDisplay.get(exerciseIndexGoingOn).replace(" ", "+"));
            PlayPauseRunningExercise(view);
        }
        else{
            Snackbar.make(view, "No internet connection", BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}

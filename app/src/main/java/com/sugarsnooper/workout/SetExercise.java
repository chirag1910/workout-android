package com.sugarsnooper.workout;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import java.util.*;

public class SetExercise extends AppCompatActivity {
    FloatingActionButton exerciseTime;
    FloatingActionButton restTime;
    FloatingActionButton add;
    FloatingActionButton expand;
    FloatingActionButton reset;
    FloatingActionButton mute;
    FloatingActionButton notification;

    TextView exerciseTimeLabel;
    TextView restTimeLabel;
    TextView addLabel;
    TextView expandLabel;
    TextView resetLabel;
    TextView muteLabel;
    TextView notificationLabel;

    ExtendedFloatingActionButton start;
    View darkenedBg;
    OnClickListener expandMenu;
    OnClickListener compressMenu;

    RecyclerView exerciseListView;

    AlertDialog.Builder alertDialogBuilder;
    AlertDialog alertDialog;

    ArrayList<String> defaultList = new ArrayList<>();

    String tempDeletedExercise = null;

    TinyDB tinyDB;

    int deviceOrientation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.set_exercise);

        // orientation
        setRequestedOrientation(getResources().getConfiguration().orientation);

        // expanding floating action buttons
        expandFABFunction();

        // setting global variables to default
        Globals.exerciseListToDisplay = new ArrayList<>();

        // database initialization
        tinyDB = new TinyDB(this);

        // recycler view (exercise list)
        getExerciseList();
        updateExerciseCount();

        // exercise list viewer
        exerciseListView = findViewById(R.id.exerciseList);

        //start button
        start = findViewById(R.id.floating_action_button_start);

        // mute unmute button
        getExerciseSoundStatus();
        updateExerciseSoundStatus();

        // notification
        getExerciseNotificationSettings();
        updateExerciseNotificationSettings();

        // notification main
        createNotificationChannel();
        setUpNotification();

        // exercise duration
        getExerciseDuration();
        updateExerciseDuration();

        // rest duration
        getRestDuration();
        updateRestDuration();

        exerciseListView.setLayoutManager(new LinearLayoutManager(this));
        exerciseListView.setAdapter(new ExerciseList(Globals.exerciseListToDisplay));

        // shrink on scroll
        exerciseListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 20){
                    start.shrink();
                }
                else if (dy < -20){
                    start.extend();
                }
            }
        });

        // re-order-able list instance
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(exerciseListView);
    }

    // expanding floating action buttons function
    public void expandFABFunction(){
        exerciseTime = findViewById(R.id.floating_action_button_exercise_time);
        restTime = findViewById(R.id.floating_action_button_rest_time);
        add = findViewById(R.id.floating_action_button_add);
        expand = findViewById(R.id.floating_action_button_expand);
        reset = findViewById(R.id.floating_action_button_reset_list);
        mute = findViewById(R.id.floating_action_button_mute);
        notification = findViewById(R.id.floating_action_button_notification);

        if (deviceOrientation == Configuration.ORIENTATION_PORTRAIT) {
            exerciseTimeLabel = findViewById(R.id.floating_action_button_exercise_time_label);
            restTimeLabel = findViewById(R.id.floating_action_button_rest_time_label);
            addLabel = findViewById(R.id.floating_action_button_add_label);
            expandLabel = findViewById(R.id.floating_action_button_expand_label);
            resetLabel = findViewById(R.id.floating_action_button_reset_list_label);
            muteLabel = findViewById(R.id.floating_action_button_mute_label);
            notificationLabel = findViewById(R.id.floating_action_button_notification_label);
        }

        darkenedBg = findViewById(R.id.darkenedBg);

        exerciseTime.setVisibility(View.GONE);
        restTime.setVisibility(View.GONE);
        add.setVisibility(View.GONE);
        reset.setVisibility(View.GONE);
        mute.setVisibility(View.GONE);
        notification.setVisibility(View.GONE);

        if (deviceOrientation == Configuration.ORIENTATION_PORTRAIT) {
            expandLabel.setVisibility(View.GONE);
            exerciseTimeLabel.setVisibility(View.GONE);
            restTimeLabel.setVisibility(View.GONE);
            addLabel.setVisibility(View.GONE);
            resetLabel.setVisibility(View.GONE);
            muteLabel.setVisibility(View.GONE);
            notificationLabel.setVisibility(View.GONE);
        }

        darkenedBg.setVisibility(View.GONE);

        expandMenu = new OnClickListener() {
            @Override
            public void onClick(View v) {
                expand.setImageResource(R.drawable.ic_baseline_close_24);
                expand.setOnClickListener(compressMenu);

                exerciseTime.show();
                restTime.show();
                add.show();
                reset.show();
                mute.show();
                notification.show();

                if (deviceOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    expandLabel.setVisibility(View.VISIBLE);
                    exerciseTimeLabel.setVisibility(View.VISIBLE);
                    restTimeLabel.setVisibility(View.VISIBLE);
                    addLabel.setVisibility(View.VISIBLE);
                    resetLabel.setVisibility(View.VISIBLE);
                    muteLabel.setVisibility(View.VISIBLE);
                    notificationLabel.setVisibility(View.VISIBLE);
                }

                darkenedBg.setAlpha(0);
                darkenedBg.setVisibility(View.VISIBLE);
                darkenedBg.animate().alpha(1f).setDuration(300).start();
                expand.animate().rotation(360).setDuration(1000).start();
            }
        };

        compressMenu = new OnClickListener() {
            @Override
            public void onClick(View v) {
                expand.setImageResource(R.drawable.ic_baseline_settings_24);
                expand.setOnClickListener(expandMenu);

                exerciseTime.hide();
                restTime.hide();
                add.hide();
                reset.hide();
                mute.hide();
                notification.hide();

                if (deviceOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    expandLabel.setVisibility(View.GONE);
                    exerciseTimeLabel.setVisibility(View.GONE);
                    restTimeLabel.setVisibility(View.GONE);
                    addLabel.setVisibility(View.GONE);
                    resetLabel.setVisibility(View.GONE);
                    muteLabel.setVisibility(View.GONE);
                    notificationLabel.setVisibility(View.GONE);
                }

                darkenedBg.setAlpha(1);
                darkenedBg.animate().alpha(0f).setDuration(300).withEndAction(() -> darkenedBg.setVisibility(View.GONE)).start();
                expand.animate().rotation(0).setDuration(1000).start();
            }
        };
        darkenedBg.setOnClickListener(compressMenu);
        expand.setOnClickListener(expandMenu);
    }

    // for exercise list
    public void fabAddFunction(View view){

        alertDialogBuilder = new AlertDialog.Builder(this);
        final View alertDialogueView = getLayoutInflater().inflate(R.layout.add_exercise_card, null);

        EditText addedExerciseName = alertDialogueView.findViewById(R.id.addedExerciesName);
        Button addExerciseButton = alertDialogueView.findViewById(R.id.addExerciseButton);

        alertDialogBuilder.setView(alertDialogueView);
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.getWindow().setLayout(900, 474);

        addExerciseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String addedExerciseNameString = addedExerciseName.getText().toString();
                if (addedExerciseNameString.contains(",")){
                    Toast.makeText(SetExercise.this, "Name can not contain ,", Toast.LENGTH_LONG).show();
                }
                else if(addedExerciseNameString.length() != 0){
                    Globals.exerciseListToDisplay.add(0, addedExerciseNameString);
                    exerciseListView.getAdapter().notifyItemInserted(0);
                    saveToLocal();
                    updateExerciseCount();
                    Snackbar.make(exerciseListView, "Exercise added", BaseTransientBottomBar.LENGTH_SHORT).show();

                    add.setImageResource(R.drawable.ic_baseline_done_24);
                    new Handler().postDelayed(() -> add.setImageResource(R.drawable.ic_baseline_add_24), 1000);

                    exerciseListView.smoothScrollToPosition(0);
                    alertDialog.dismiss();
                }
            }
        });

    }

    public void getExerciseList(){
        Globals.exerciseListToDisplay.addAll(tinyDB.getListString(Globals.sharedPreferencesSetName));
        if (Globals.exerciseListToDisplay.size() == 0){
            setDefaultList();
        }
    }

    public void updateExerciseCount(){
        String headingToDisplay = "Exercise List" + " - " + Globals.exerciseListToDisplay.size();
        ((TextView)findViewById(R.id.exerciseListHeading)).setText(headingToDisplay);
    }

    public void saveToLocal(){
        // save list to local storage
        tinyDB.putListString(Globals.sharedPreferencesSetName, Globals.exerciseListToDisplay);
    }

    // for exercise duration
    public void fabExerciseTimeFunction(View view){

        alertDialogBuilder = new AlertDialog.Builder(this);
        final View alertDialogueView = getLayoutInflater().inflate(R.layout.set_exercise_time_card, null);

        EditText setExerciseTimeDuration = alertDialogueView.findViewById(R.id.setExerciseTimeDuration);
        Button setExerciseTimeButton = alertDialogueView.findViewById(R.id.setNotificationTimeButton);

        alertDialogBuilder.setView(alertDialogueView);
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.getWindow().setLayout(900, 474);

        setExerciseTimeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setExerciseTimeDuration.getText().toString().length() == 0){
                    Snackbar.make(exerciseListView, "Field can not be empty", BaseTransientBottomBar.LENGTH_LONG).show();
                    alertDialog.dismiss();
                }
                else {
                    Integer setExerciseTimeDurationInteger = Integer.parseInt(setExerciseTimeDuration.getText().toString());
                    alertDialog.dismiss();

                    if (200 >= setExerciseTimeDurationInteger && setExerciseTimeDurationInteger >= 10) {
                        Globals.exerciseDuration = setExerciseTimeDurationInteger;

                        SharedPreferences sharedPreferences = getSharedPreferences("workout", MODE_PRIVATE);
                        Editor editor = sharedPreferences.edit();
                        editor.putInt("exerciseDuration", setExerciseTimeDurationInteger);
                        editor.apply();

                        updateExerciseDuration();
                        Snackbar.make(exerciseListView, "Exercise duration updated", BaseTransientBottomBar.LENGTH_SHORT).show();
                    } else if (setExerciseTimeDurationInteger < 10) {
                        Snackbar.make(exerciseListView, "Duration can not be less than 10 seconds", BaseTransientBottomBar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(exerciseListView, "Duration can not be more than 200 seconds", BaseTransientBottomBar.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    public void getExerciseDuration(){
        SharedPreferences sharedPreferences = getSharedPreferences("workout", MODE_PRIVATE);
        Globals.exerciseDuration = sharedPreferences.getInt("exerciseDuration", 30);
    }

    public void updateExerciseDuration(){
        String exerciseTimeToDisplay = "" + Globals.exerciseDuration;

        exerciseTime.setImageResource(R.drawable.ic_baseline_done_24);
        new Handler().postDelayed(() -> exerciseTime.setImageBitmap(GlobalMethods.textAsBitmap(exerciseTimeToDisplay, 40, Color.WHITE)), 500);
    }

    // for rest duration
    public void fabRestTimeFunction(View view){

        alertDialogBuilder = new AlertDialog.Builder(this);
        final View alertDialogueView = getLayoutInflater().inflate(R.layout.set_rest_time_card, null);

        EditText setRestTimeDuration = alertDialogueView.findViewById(R.id.setRestTimeDuration);
        Button setRestTimeButton = alertDialogueView.findViewById(R.id.setRestTimeButton);

        alertDialogBuilder.setView(alertDialogueView);
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.getWindow().setLayout(900, 474);

        setRestTimeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setRestTimeDuration.getText().toString().length() == 0){
                    Snackbar.make(exerciseListView, "Field can not be empty", BaseTransientBottomBar.LENGTH_LONG).show();
                    alertDialog.dismiss();
                }
                else {
                    Integer setRestTimeDurationInteger = Integer.parseInt(setRestTimeDuration.getText().toString());
                    alertDialog.dismiss();

                    if (200 >= setRestTimeDurationInteger && setRestTimeDurationInteger >= 10) {
                        Globals.restDuration = setRestTimeDurationInteger;

                        SharedPreferences sharedPreferences = getSharedPreferences("workout", MODE_PRIVATE);
                        Editor editor = sharedPreferences.edit();
                        editor.putInt("restDuration", setRestTimeDurationInteger);
                        editor.apply();

                        updateRestDuration();
                        Snackbar.make(exerciseListView, "Rest duration updated", BaseTransientBottomBar.LENGTH_SHORT).show();
                    } else if (setRestTimeDurationInteger < 10) {
                        Snackbar.make(exerciseListView, "Duration can not be less than 10 seconds", BaseTransientBottomBar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(exerciseListView, "Duration can not be more than 200 seconds", BaseTransientBottomBar.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    public void getRestDuration(){
        SharedPreferences sharedPreferences = getSharedPreferences("workout", MODE_PRIVATE);
        Globals.restDuration = sharedPreferences.getInt("restDuration", 30);
    }

    public void updateRestDuration(){
        String restTimeToDisplay = "" + Globals.restDuration;

        restTime.setImageResource(R.drawable.ic_baseline_done_24);
        new Handler().postDelayed(() -> restTime.setImageBitmap(GlobalMethods.textAsBitmap(restTimeToDisplay, 40, Color.WHITE)), 500);
    }

    // fab mute un-mute button
    public void fabMuteFunction(View view){
        Globals.allowSound = !Globals.allowSound;

        SharedPreferences sharedPreferences = getSharedPreferences("workout", MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putBoolean("muteStatus", Globals.allowSound);
        editor.apply();

        updateExerciseSoundStatus();
    }

    public void getExerciseSoundStatus(){
        SharedPreferences sharedPreferences = getSharedPreferences("workout", MODE_PRIVATE);
        Globals.allowSound = sharedPreferences.getBoolean("muteStatus", true);
    }

    public void updateExerciseSoundStatus(){
        if (Globals.allowSound){
            mute.setImageResource(R.drawable.ic_baseline_volume_up_24);
            mute.animate().rotation(360).setDuration(500).start();
        }
        else {
            mute.setImageResource(R.drawable.ic_baseline_volume_off_24);
            mute.animate().rotation(0).setDuration(500).start();
        }
    }

    // notification settings
    public void fabNotificationFunction(View view) {
        alertDialogBuilder = new AlertDialog.Builder(this);
        final View alertDialogueView = getLayoutInflater().inflate(R.layout.notification_settings_card, null);

        alertDialogBuilder.setView(alertDialogueView);
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.getWindow().setLayout(900, 630);
        alertDialog.setCancelable(false);

        Switch allowNotification = alertDialogueView.findViewById(R.id.allowNotification);
        Button setNotificationTimeButton = alertDialogueView.findViewById(R.id.setNotificationTimeButton);

        setNotificationTimeButton.setText(("Time: " + timeFormatter(Globals.notificationTimeHour) + ":" + timeFormatter(Globals.notificationTimeMinute)));
        setNotificationTimeButton.setEnabled(Globals.allowNotifications);

        if(!Globals.allowNotifications){
            setNotificationTimeButton.setAlpha(0.5f);
        }
        else{
            setNotificationTimeButton.setAlpha(1);
        }

        allowNotification.setChecked(Globals.allowNotifications);
        allowNotification.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Globals.allowNotifications = allowNotification.isChecked();
                setNotificationTimeButton.setEnabled(Globals.allowNotifications);

                if(!Globals.allowNotifications){
                    setNotificationTimeButton.setAlpha(0.5f);
                }
                else{
                    setNotificationTimeButton.setAlpha(1);
                }

                SharedPreferences sharedPreferences = getSharedPreferences("workout", MODE_PRIVATE);
                Editor editor = sharedPreferences.edit();
                editor.putBoolean("notificationSettings", Globals.allowNotifications);
                editor.apply();

                updateExerciseNotificationSettings();

                Snackbar.make(exerciseListView, "Notifications settings changed!", BaseTransientBottomBar.LENGTH_SHORT).show();
            }
        });

        setNotificationTimeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);

                TimePickerDialog timePicker;
                timePicker = new TimePickerDialog(SetExercise.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        setNotificationTimeButton.setText(("Time: " + timeFormatter(selectedHour) + ":" + timeFormatter(selectedMinute)));

                        Globals.notificationTimeHour = selectedHour;
                        Globals.notificationTimeMinute = selectedMinute;

                        SharedPreferences sharedPreferences = getSharedPreferences("workout", MODE_PRIVATE);
                        Editor editor = sharedPreferences.edit();
                        editor.putInt("notificationTimeHour", Globals.notificationTimeHour);
                        editor.putInt("notificationTimeMinute", Globals.notificationTimeMinute);
                        editor.apply();

                        Snackbar.make(exerciseListView, "Time set!", BaseTransientBottomBar.LENGTH_SHORT).show();

                        setUpNotification();
                    }
                }, hour, minute, true);
                timePicker.show();
                timePicker.getButton(TimePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#BBFFFFFF"));
                timePicker.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
            }
        });

        alertDialogueView.findViewById(R.id.closeNotificationCardButton).setOnClickListener(v -> alertDialog.dismiss());
    }

    public void getExerciseNotificationSettings(){
        SharedPreferences sharedPreferences = getSharedPreferences("workout", MODE_PRIVATE);
        Globals.allowNotifications = sharedPreferences.getBoolean("notificationSettings", true);
        Globals.notificationTimeHour = sharedPreferences.getInt("notificationTimeHour", 19);
        Globals.notificationTimeMinute = sharedPreferences.getInt("notificationTimeMinute", 0);
    }

    public void updateExerciseNotificationSettings(){
        if (Globals.allowNotifications){
            notification.setImageResource(R.drawable.ic_baseline_notifications_active_24);
        }
        else {
            notification.setImageResource(R.drawable.ic_baseline_notifications_off_24);
        }
    }

    // set list to default
    public void setDefaultList(){
        defaultList = new ArrayList<>();

        defaultList.add("Jumping jacks");
        defaultList.add("Push ups");
        defaultList.add("Forward lunges");
        defaultList.add("Hip raises");
        defaultList.add("Superman");
        defaultList.add("Jumping jacks");
        defaultList.add("Mountain climbers");
        defaultList.add("Close body squats");
        defaultList.add("Crunches");
        defaultList.add("Plank");
        defaultList.add("Burpees");
        defaultList.add("Toe touch");
        defaultList.add("Sit through");
        defaultList.add("D");
        defaultList.add("Elbow-knee touch");
        defaultList.add("Sitting toe touch");
        defaultList.add("Cycling");
        defaultList.add("Step up");
        defaultList.add("Body stretch");
        defaultList.add("Side bend");
        defaultList.add("Jogging");

        Globals.exerciseListToDisplay = defaultList;
    }

    // reset exercise list
    public void fabResetListFunction(View view) {
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Reset exercise list?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setDefaultList();
                        exerciseListView = findViewById(R.id.exerciseList);
                        exerciseListView.setLayoutManager(new LinearLayoutManager(SetExercise.this));
                        exerciseListView.setAdapter(new ExerciseList(Globals.exerciseListToDisplay));
                        updateExerciseCount();

                        // saving null to localStorage
                        saveToLocal();

                        Snackbar.make(view, "Exercise list has been reset", BaseTransientBottomBar.LENGTH_LONG).show();
                        reset.setRotation(0);
                        reset.animate().rotation(-360).setDuration(1000).start();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setCancelable(false);

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    // start exercise (change activity)
    public void fabStartFunction(View view){
        if (Globals.exerciseListToDisplay.size() > 0) {
            startActivity(new Intent(SetExercise.this, RunExercise.class));
            finish();
        }
        else{
            Snackbar.make(view, "No exercise added", BaseTransientBottomBar.LENGTH_LONG).show();
        }
    }


    // recycler view class to add item in recycler view
    class ExerciseList extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        ArrayList<String> exerciseArrayList;
//        Context context;

        public ExerciseList(ArrayList<String> exerciseArrayList) {
            this.exerciseArrayList = exerciseArrayList;
//            this.context = context;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_list_item_view, parent, false)){};
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            TextView exerciseName = findViewById(R.id.exerciseName);
            ((TextView)holder.itemView.findViewById(R.id.exerciseName)).setText(exerciseArrayList.get(position));
        }

        @Override
        public int getItemCount() {
            return exerciseArrayList.size();
        }
    }

    // for re-order-able recycler view
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP |
            ItemTouchHelper.DOWN |
            ItemTouchHelper.START |
            ItemTouchHelper.END, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            Collections.swap(Globals.exerciseListToDisplay, fromPosition, toPosition);
            exerciseListView.getAdapter().notifyItemMoved(fromPosition, toPosition);

            saveToLocal();
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int itemPosition = viewHolder.getAdapterPosition();

            tempDeletedExercise = Globals.exerciseListToDisplay.get(itemPosition);

            Globals.exerciseListToDisplay.remove(itemPosition);
            exerciseListView.getAdapter().notifyItemRemoved(itemPosition);

            Snackbar.make(exerciseListView, "Deleted "+tempDeletedExercise, BaseTransientBottomBar.LENGTH_LONG)
                    .setAction("Undo", new View.OnClickListener(){
                        @Override
                        public void onClick(View view){
                            Globals.exerciseListToDisplay.add(itemPosition, tempDeletedExercise);
                            exerciseListView.getAdapter().notifyItemInserted(itemPosition);

                            updateExerciseCount();
                            saveToLocal();
                        }
                    }).show();

            updateExerciseCount();
            saveToLocal();
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(SetExercise.this, R.color.delete_color))
                    .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24)
                    .setSwipeLeftActionIconTint(Color.WHITE)
                    .addSwipeLeftLabel("Remove")
                    .setSwipeLeftLabelColor(Color.WHITE)
                    .create()
                    .decorate();
        }
    };

    public String timeFormatter(int number){
        if (number < 10){
            return ("0"+number);
        }
        else{
            return (""+number);
        }
    }

    @Override
    public void onBackPressed() {
        finishAndRemoveTask();
        super.onBackPressed();
    }

    public void setUpNotification(){
        if (Globals.allowNotifications) {
            Calendar calendar = Calendar.getInstance();
            Calendar current = Calendar.getInstance();

            calendar.set(Calendar.HOUR_OF_DAY, Globals.notificationTimeHour);
            calendar.set(Calendar.MINUTE, Globals.notificationTimeMinute);
            calendar.set(Calendar.DAY_OF_MONTH, current.getTime().getDate());

            if (current.getTimeInMillis() >= calendar.getTimeInMillis()){
                calendar.set(Calendar.DAY_OF_MONTH, current.getTime().getDate()+1);
            }

            Intent intent = new Intent(getApplicationContext(), ReminderBroadcast.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 200, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("notifyUser", "General", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Channel for reminder");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
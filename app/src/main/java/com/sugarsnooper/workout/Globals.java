package com.sugarsnooper.workout;

import java.util.ArrayList;

public class Globals {
    public static ArrayList<String> exerciseListToDisplay = new ArrayList<>();
    public static Integer exerciseDuration;
    public static Integer restDuration;

    public static Boolean allowSound;
    public static Boolean allowNotifications;

    public static int notificationTimeHour;
    public static int notificationTimeMinute;

//    public static Time notificationTime;

    public static final String sharedPreferencesFileName = "workoutList";
    public static final String sharedPreferencesSetName = "workoutSet";
}

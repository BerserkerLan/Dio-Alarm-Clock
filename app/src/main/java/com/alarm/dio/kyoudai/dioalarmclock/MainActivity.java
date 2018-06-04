package com.alarm.dio.kyoudai.dioalarmclock;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;

import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    Button addNewAlarm;
    SQLiteDatabase alarmDB;
    SQLiteDatabase inactiveAlarmDB;
    public ListView alarmList;
    static ArrayList<Alarm> alarmsArrayList;
    ArrayList<String> alarmsDaysList;
    Switch activateSwitch;
    public static ActivateAlarmThread alarmThread;
    public static ArrayList<Boolean> activatedList;
    final int TIME_START_INDEX = 11;
    final int TIME_END_INDEX = 16;
    static Alarm CLOSEST_ALARM_HOPEFULLY;
    public static NotificationManager notificationManager;
    int closestDayIndex;
    Alarm closestAlarm;
    public static ArrayList<Alarm> unActiveAlarms = new ArrayList<>();
    public static boolean signalNewNotification;
    int backPress;
    @SuppressLint("StaticFieldLeak")
    public static AlarmListAdapter alarmListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_main);
        backPress = 0;
        MobileAds.initialize(this, "ca-app-pub-5483591282248570~7107432706");
        AdView adView = findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());

        signalNewNotification = false;
        addNewAlarm = findViewById(R.id.addAlarmButton);
        alarmList = findViewById(R.id.alarmsList);
        alarmsArrayList = new ArrayList<>();
        unActiveAlarms = new ArrayList<>();
        activateSwitch = findViewById(R.id.alarmSwitch);
        loadAlarmList();


        //TODO: Make Notifications show good icon


        if (!alarmsArrayList.isEmpty()) {
            alarmsDaysList = new ArrayList<>();

            for (Alarm a : alarmsArrayList) {
                StringBuilder tempdays = new StringBuilder();
                tempdays.append(a.getDays());

                String[] days = getDaysInArray(tempdays.toString());

                tempdays = new StringBuilder();
                for (int i = 0; i < days.length; i++) {
                    if (i == days.length - 1) {
                        tempdays.append(days[i]);
                    } else {
                        tempdays.append(days[i]).append(", ");
                    }
                }
                alarmsDaysList.add(tempdays.toString());
            }

            alarmListAdapter = new AlarmListAdapter(alarmsArrayList,activatedList,alarmsDaysList,this);

            alarmList.setAdapter(alarmListAdapter);

            //NOTE: str1.compareTo(str2) returns 1 when str1 is bigger, else -1
            //Date currentTime = Calendar.getInstance().getTime();

            setUpNotifications();

        }



        addNewAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),AddAlarmActivity.class);
                intent.putExtra("alarmsList",alarmsArrayList);
                finish();
                startActivity(new Intent(getApplicationContext(), AddAlarmActivity.class));

            }
        });
    }

    public void loadAlarmList() {
        activatedList = new ArrayList<>();
        alarmDB = openOrCreateDatabase("Active Alarms", SQLiteDatabase.OPEN_READWRITE, null);
        alarmDB.execSQL("CREATE TABLE IF NOT EXISTS Alarm(name VARCHAR, days VARCHAR, sound VARCHAR, time VARCHAR);");

        Cursor resultAlarm = alarmDB.rawQuery("SELECT * FROM Alarm", null);
        Alarm tempAlarm = new Alarm();
        resultAlarm.moveToFirst();
        long count = DatabaseUtils.longForQuery(alarmDB, "SELECT COUNT(*) FROM Alarm", null);
        if (count > 0) {
            tempAlarm.setName(resultAlarm.getString(resultAlarm.getColumnIndex("name")));
            tempAlarm.setDays(resultAlarm.getString(resultAlarm.getColumnIndex("days")));
            tempAlarm.setSound(resultAlarm.getString(resultAlarm.getColumnIndex("sound")));
            tempAlarm.setTime(resultAlarm.getString(resultAlarm.getColumnIndex("time")));
            activatedList.add(true);
            alarmsArrayList.add(tempAlarm);
        }

        while(resultAlarm.moveToNext()) {
            tempAlarm = new Alarm();
            tempAlarm.setName(resultAlarm.getString(resultAlarm.getColumnIndex("name")));
            tempAlarm.setDays(resultAlarm.getString(resultAlarm.getColumnIndex("days")));
            tempAlarm.setSound(resultAlarm.getString(resultAlarm.getColumnIndex("sound")));
            tempAlarm.setTime(resultAlarm.getString(resultAlarm.getColumnIndex("time")));
            activatedList.add(true);
            alarmsArrayList.add(tempAlarm);

        }

        inactiveAlarmDB = openOrCreateDatabase("Unactive Alarms",SQLiteDatabase.OPEN_READWRITE,null);
        inactiveAlarmDB.execSQL("CREATE TABLE IF NOT EXISTS Alarm(name VARCHAR, days VARCHAR, sound VARCHAR, time VARCHAR);");

        resultAlarm = inactiveAlarmDB.rawQuery("SELECT * FROM Alarm", null);
        Alarm tempAlarm2 = new Alarm();
        resultAlarm.moveToFirst();
        long count2 = DatabaseUtils.longForQuery(inactiveAlarmDB, "SELECT COUNT(*) FROM Alarm", null);
        if (count2 > 0) {
            tempAlarm2.setName(resultAlarm.getString(resultAlarm.getColumnIndex("name")));
            tempAlarm2.setDays(resultAlarm.getString(resultAlarm.getColumnIndex("days")));
            tempAlarm2.setSound(resultAlarm.getString(resultAlarm.getColumnIndex("sound")));
            tempAlarm2.setTime(resultAlarm.getString(resultAlarm.getColumnIndex("time")));
            activatedList.add(false);
            alarmsArrayList.add(tempAlarm2);
            unActiveAlarms.add(tempAlarm2);


        }

        while(resultAlarm.moveToNext()) {
            tempAlarm2 = new Alarm();
            tempAlarm2.setName(resultAlarm.getString(resultAlarm.getColumnIndex("name")));
            tempAlarm2.setDays(resultAlarm.getString(resultAlarm.getColumnIndex("days")));
            tempAlarm2.setSound(resultAlarm.getString(resultAlarm.getColumnIndex("sound")));
            tempAlarm2.setTime(resultAlarm.getString(resultAlarm.getColumnIndex("time")));
            activatedList.add(false);
            alarmsArrayList.add(tempAlarm2);
            unActiveAlarms.add(tempAlarm2);


        }
        resultAlarm.close();
        alarmDB.close();
    }

    //Time Format:SAT MAY 26 18:45:33 GMT +05:00 2018



    public  void setUpNotifications() {
        //BEGINNING OF SMALLEST ALARM CALCULATION

        String currentDate = Calendar.getInstance().getTime().toString();
        String currentTime = transformTime(currentDate.substring(TIME_START_INDEX,TIME_END_INDEX));
        closestAlarm = new Alarm();

        //Toast.makeText(this, "Setting up notification..........", Toast.LENGTH_SHORT).show();

            int closestDayDistance = 9;
            closestDayIndex = -1;

            if (!alarmsArrayList.isEmpty()) {
                if (!(alarmsArrayList.get(0).getName() == null)) {
                    String initialTimeDifference = getTimeDifference(transformTime(currentTime), transformTime(alarmsArrayList.get(0).getTime()));
                    Log.d("Day", alarmsArrayList.get(0).getDays().toLowerCase());
                    String[] splitter = alarmsArrayList.get(0).getDays().split("\\|");
                    StringBuilder temp = new StringBuilder();
                    for (String aSplitter : splitter) {
                        temp.append(aSplitter).append(" , ");
                    }
                    Log.d("temp", temp.toString());
                    for (int i = 0; i < alarmsArrayList.size(); i++) {
                        if (!unActiveAlarms.contains(alarmsArrayList.get(i))) {
                            int tempClosestDayIndex = getClosestDayIndex(getDaysInArray(alarmsArrayList.get(i).getDays().toLowerCase()));
                            int tempClosestDayDistance = getClosestDayDifference(getDaysInArray(alarmsArrayList.get(i).getDays().toLowerCase()));
                            String tempTime = transformTime(alarmsArrayList.get(i).getTime());
                            if ((tempClosestDayDistance == 0) && (transformTime(tempTime).compareTo(transformTime(currentTime)) > 0) && (transformTime(getTimeDifference(transformTime(tempTime), currentTime)).compareTo(initialTimeDifference) <= 0)) {
                                initialTimeDifference = transformTime(getTimeDifference(tempTime, currentTime));
                                closestDayDistance = tempClosestDayDistance;
                                closestAlarm = alarmsArrayList.get(i);
                                closestDayIndex = getClosestDayIndex(getDaysInArray(alarmsArrayList.get(i).getDays().toLowerCase()));
                            } else if (tempClosestDayDistance < closestDayDistance && tempClosestDayDistance != 0) {
                                closestDayDistance = tempClosestDayDistance;
                                closestDayIndex = tempClosestDayIndex;
                                closestAlarm = alarmsArrayList.get(i);

                            }
                        }
                    }
                }


                if (closestAlarm.getDays() != null && closestDayIndex >= 0) {

                    String nextDay = getDaysInArray(closestAlarm.getDays())[closestDayIndex];

                    if (!unActiveAlarms.contains(closestAlarm)) {


                        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

                        CLOSEST_ALARM_HOPEFULLY = closestAlarm;
                        String alarmName = CLOSEST_ALARM_HOPEFULLY.getName();
                        String alarmTime = CLOSEST_ALARM_HOPEFULLY.getTime() + " on " + nextDay;
                        NotificationCompat.Builder builder =
                                new NotificationCompat.Builder(this,"1")
                                        .setAutoCancel(true)
                                        .setContentTitle(alarmName)
                                        .setContentText(alarmTime)
                                        .setBadgeIconType(R.drawable.ic_alarm_status_icon)
                                        .setLargeIcon(largeIcon)
                                        .setSmallIcon(R.drawable.ic_alarm_status_icon);



                        Notification notification = builder.build();
                        notification.flags |= Notification.FLAG_ONGOING_EVENT;
                        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        if (notificationManager != null) {
                            notificationManager.cancelAll();
                        }
                        if (notificationManager != null) {
                            notificationManager.notify(0, notification);
                        }

                        createNotificationChannel(alarmName, alarmTime);

                    }


                    alarmThread = new ActivateAlarmThread();
                    alarmThread.execute();
                }
            }
        //END OF SMALLEST ALARM CALCULATION
    }

    private void createNotificationChannel(String alarmName, String alarmTime) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1", alarmName, importance);
            channel.setDescription(alarmTime);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }


    @Override
    public void onBackPressed() {
        backPress++;
        if (backPress == 1) {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        else {
            this.moveTaskToBack(true);
        }

    }


    public String getTimeDifference(String time1, String time2) {
        int time1Hour = Integer.parseInt(time1.substring(0,2));
        int time1Minutes = Integer.parseInt(time1.substring(3,time1.length()));
        int time2Hour = Integer.parseInt(time2.substring(0,2));
        int time2Minutes = Integer.parseInt(time2.substring(3,time2.length()));

        int hourDifference;
        if (time1Hour > time2Hour) {
            hourDifference = time1Hour-time2Hour;
        }
        else {
            hourDifference = time2Hour - time1Hour;
        }
        int minuteDifference;
        if (time1Minutes > time2Minutes) {
            minuteDifference = time1Minutes - time2Minutes;
        }
        else {
            minuteDifference = time2Minutes - time1Minutes;
        }

        return hourDifference  + ":" + minuteDifference;

    }

    public String[] getDaysInArray(String d) {
        String[] days = d.split("\\|");
        String[] daysMinusOne = new String[days.length - 1];
        System.arraycopy(days, 1, daysMinusOne, 0, days.length - 1);
        return daysMinusOne;
    }

    public int getClosestDayIndex(String[] days) {
        String currentDate = Calendar.getInstance().getTime().toString();
        String currentDay = currentDate.substring(0,3).toLowerCase();

        int closestDayIndex = 0;
        int smallestDayDifference = getDayDifference(currentDay, days[0]);

        for (int i = 0; i < days.length; i++) {
            if (days[i] != null) {
                if (smallestDayDifference > getDayDifference(currentDay, days[i])) {
                    closestDayIndex = i;
                    smallestDayDifference = getDayDifference(currentDay, days[i]);
                }
            }
        }
        return closestDayIndex;


    }
    public int getClosestDayDifference(String[] days) {
        String currentDate = Calendar.getInstance().getTime().toString();
        String currentDay = currentDate.substring(0,3).toLowerCase();

        int smallestDayDifference = getDayDifference(currentDay, days[0]);

        for (String day : days) {
            if (day != null) {
                if (smallestDayDifference > getDayDifference(currentDay, day)) {
                    smallestDayDifference = getDayDifference(currentDay, day);
                }
            }
        }
        return smallestDayDifference;


    }



    public String getNextDay(String day) {
        String[] days = {"mon","tue","wed","thu","fri","sat","sun"};

        if (day.equals("sun")) {
            return "mon";
        }
        for (int i = 0; i < days.length; i++) {
            if (days[i].equals(day)) {
                return days[i+1];
            }
        }
        return null;
    }

    public String transformTime(String time) {
        String[] timeArray = time.split("\\:");

        int hour = Integer.parseInt(timeArray[0]);

        int minute = Integer.parseInt(timeArray[1]);

        String hourString = hour + "";
        String minuteString = minute + "";

        if (hour <= 9) {
            hourString = "0" + hour;
        }
        if (minute <= 9) {
            minuteString = "0" + minute;
        }

        return hourString + ":" +  minuteString;
    }

    public int getDayDifference(String day1, String day2) {
        if (day1.equals(day2)) {
            return 0;
        }
        Log.d("Day 1", "Day1: " + day1);
        Log.d("Day 2", "Day2: " + day2);
        String[] days = {"mon","tue","wed","thu","fri","sat","sun"};

        int day1Index = 0;
        int day2Index = 0;

       for (int i = 0; i < days.length; i++) {
           if (day1.equals(days[i])) {
               day1Index = i;
           }
           if (day2.equals(days[i])) {
               day2Index = i;
           }
       }

       String biggerDay;
       String smallerDay;

       if (day1Index > day2Index) {
           biggerDay = day1.toLowerCase();
           smallerDay = day2.toLowerCase();
       }
       else {
           biggerDay = day2.toLowerCase();
           smallerDay = day1.toLowerCase();
       }

       int dayDifference1 = 0;


       while (!smallerDay.equals(biggerDay)) {
           smallerDay = getNextDay(smallerDay);
           Log.d("smaller day now", "message : " + smallerDay);
           dayDifference1++;
       }

       int dayDifference2 = 0;

       while (!biggerDay.equals(smallerDay)) {
           biggerDay = getNextDay(biggerDay);
           dayDifference2++;
       }

       return Math.max(dayDifference1,dayDifference2);


    }


    class ActivateAlarmThread extends AsyncTask<Alarm,Alarm,Alarm> {

        boolean itsTime;

        @Override
        protected Alarm doInBackground(Alarm... alarms) {
            itsTime = false;
            if (CLOSEST_ALARM_HOPEFULLY != null && !activatedList.isEmpty()) {


                int index = alarmsArrayList.indexOf(CLOSEST_ALARM_HOPEFULLY);


                //Log.d("In THREAD", "ENTERED THE THREAD");


                boolean dayIsToday = false;

                while (!itsTime) {
                    if (!activatedList.isEmpty()) {
                        if (!activatedList.get(index)) {
                            break;
                        }
                    }
                    else {
                         break;
                    }
                    String currentDate = Calendar.getInstance().getTime().toString();

                    String currentDay = currentDate.substring(0, 3).toLowerCase();

                    //Log.d("Tag","Looping......");

                    if (CLOSEST_ALARM_HOPEFULLY != null) {
                        String[] days = getDaysInArray(CLOSEST_ALARM_HOPEFULLY.getDays());

                        if (!dayIsToday) {
                            for (String day : days) {
                                if (day.toLowerCase().equals(currentDay)) {
                                    dayIsToday = true;
                                    Log.d("day", "Day is today!");
                                    break;
                                }
                            }
                        }


                        String currentTime = currentDate.substring(TIME_START_INDEX, TIME_END_INDEX);
                        //Log.d("time", currentTime);


                        if (transformTime(CLOSEST_ALARM_HOPEFULLY.getTime()).equals(transformTime(currentTime)) && dayIsToday && activatedList.get(index)) {
                            itsTime = true;
                            break;
                        }
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(Alarm alarm) {
            if (CLOSEST_ALARM_HOPEFULLY != null && itsTime) {
                Log.d("tried", "Tried to run alarm");
                Intent playAlarmIntent = new Intent(getApplicationContext(), AlarmPlayingActivity.class);
                playAlarmIntent.putExtra("alarmName", CLOSEST_ALARM_HOPEFULLY.getName());
                playAlarmIntent.putExtra("alarmTime", CLOSEST_ALARM_HOPEFULLY.getTime());
                playAlarmIntent.putExtra("alarmSound", CLOSEST_ALARM_HOPEFULLY.getSound());

                if (notificationManager != null) {
                    notificationManager.cancelAll();
                }
                finish();

                startActivity(playAlarmIntent);
            }
        }
    }
}


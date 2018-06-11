package com.alarm.dio.kyoudai.dioalarmclock;

import android.app.DialogFragment;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.HashMap;

public class AddAlarmActivity extends AppCompatActivity {

    static TextView timeSetText;
    LinearLayoutCompat daysLayout;
    EditText alarmName;
    TextView monday;
    TextView tuesday;
    TextView wednesday;
    TextView thursday;
    TextView friday;
    TextView saturday;
    TextView sunday;
    HashMap<TextView, Boolean> daysSelected;
    Spinner songSpinner;
    String currentSong;
    FloatingActionButton addAlarmButton;
    SQLiteDatabase alarmDB;
    SQLiteDatabase inactiveAlarmDB;
    ArrayList<String> alarmsTitlesArrayList;
    ArrayList<String> alarmsInActive;
    ArrayList<String> alarmsNotActive;
    private AdView adView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);
        //Setup UI elements
        daysLayout = findViewById(R.id.linearLayoutCompat);
        songSpinner = findViewById(R.id.soundSpinner);
        addAlarmButton = findViewById(R.id.addAlarm);
        alarmName = findViewById(R.id.AlarmTitle);
        timeSetText = findViewById(R.id.setTime);
        monday = daysLayout.findViewById(R.id.monday);
        tuesday = daysLayout.findViewById(R.id.tuesday);
        wednesday = daysLayout.findViewById(R.id.wednesday);
        thursday = daysLayout.findViewById(R.id.thursday);
        friday = daysLayout.findViewById(R.id.friday);
        saturday = daysLayout.findViewById(R.id.saturday);
        sunday = daysLayout.findViewById(R.id.sunday);

        alarmsTitlesArrayList = new ArrayList<>();
        alarmsInActive = new ArrayList<>();
        alarmsNotActive = new ArrayList<>();
        MobileAds.initialize(this, "ca-app-pub-5483591282248570~7107432706");
//        adView = findViewById(R.id.adView2);
//        adView.loadAd(new AdRequest.Builder().build());


        //Load Both Databases, and store the titles so that user cannot create an alarm if title already exists
        alarmDB = openOrCreateDatabase("Active Alarms", SQLiteDatabase.OPEN_READWRITE, null);
        alarmDB.execSQL("CREATE TABLE IF NOT EXISTS Alarm(name VARCHAR, days VARCHAR, sound VARCHAR, time VARCHAR);");
        Cursor resultAlarm = alarmDB.rawQuery("SELECT * FROM Alarm", null);
        resultAlarm.moveToFirst();
        long count = DatabaseUtils.longForQuery(alarmDB, "SELECT COUNT(*) FROM Alarm", null);
        if (count > 0) {
            alarmsTitlesArrayList.add(resultAlarm.getString(resultAlarm.getColumnIndex("name")));
            alarmsInActive.add(resultAlarm.getString(resultAlarm.getColumnIndex("name")));
        }
        while(resultAlarm.moveToNext()) {
            alarmsTitlesArrayList.add(resultAlarm.getString(resultAlarm.getColumnIndex("name")));
            alarmsInActive.add(resultAlarm.getString(resultAlarm.getColumnIndex("name")));
        }
        inactiveAlarmDB = openOrCreateDatabase("Unactive Alarms",SQLiteDatabase.OPEN_READWRITE,null);
        inactiveAlarmDB.execSQL("CREATE TABLE IF NOT EXISTS Alarm(name VARCHAR, days VARCHAR, sound VARCHAR, time VARCHAR);");
        resultAlarm = inactiveAlarmDB.rawQuery("SELECT * FROM Alarm", null);
        resultAlarm.moveToFirst();
        long count2 = DatabaseUtils.longForQuery(inactiveAlarmDB, "SELECT COUNT(*) FROM Alarm", null);
        if (count2 > 0) {
            alarmsTitlesArrayList.add(resultAlarm.getString(resultAlarm.getColumnIndex("name")));
            alarmsNotActive.add(resultAlarm.getString(resultAlarm.getColumnIndex("name")));
        }
        while(resultAlarm.moveToNext()) {
            alarmsTitlesArrayList.add(resultAlarm.getString(resultAlarm.getColumnIndex("name")));
            alarmsNotActive.add(resultAlarm.getString(resultAlarm.getColumnIndex("name")));
        }
        resultAlarm.close();


        final ArrayList<String> soundList = new ArrayList<>();
        soundList.add("ZA WARUDO");
        soundList.add("IT WAS ME, DIO!");
        soundList.add("MUDA MUDA");
        soundList.add("ROAD ROLLER");
        soundList.add("WRRRYYY");

        //Setup Spinner

        final ArrayAdapter<String> soundAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_list, soundList);


        songSpinner.setAdapter(soundAdapter);
        songSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSong = soundList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentSong = "";
            }
        });




        //Loads days Listview for selection
        daysSelected = new HashMap<>();

        daysSelected.put(monday, false);
        daysSelected.put(tuesday, false);
        daysSelected.put(wednesday, false);
        daysSelected.put(thursday, false);
        daysSelected.put(friday, false);
        daysSelected.put(saturday, false);
        daysSelected.put(sunday, false);


        //Change days color when clicked, and update value of selection based on if its selected
        monday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!daysSelected.get(monday)) {
                    monday.setBackgroundColor(Color.rgb(255, 153, 51));
                    daysSelected.put(monday,true);
                }
                else {
                    monday.setBackgroundColor(Color.WHITE);
                    daysSelected.put(monday,false);
                }
            }
        });
        tuesday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!daysSelected.get(tuesday)) {
                    tuesday.setBackgroundColor(Color.rgb(255, 153, 51));
                    daysSelected.put(tuesday,true);
                }
                else {
                    tuesday.setBackgroundColor(Color.WHITE);
                    daysSelected.put(tuesday,false);
                }
            }
        });
        wednesday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!daysSelected.get(wednesday)) {
                    wednesday.setBackgroundColor(Color.rgb(255, 153, 51));
                    daysSelected.put(wednesday,true);
                }
                else {
                    wednesday.setBackgroundColor(Color.WHITE);
                    daysSelected.put(wednesday,false);
                }
            }
        });
        thursday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!daysSelected.get(thursday)) {
                    thursday.setBackgroundColor(Color.rgb(255, 153, 51));
                    daysSelected.put(thursday,true);
                }
                else {
                    thursday.setBackgroundColor(Color.WHITE);
                    daysSelected.put(thursday,false);
                }
            }
        });
        friday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!daysSelected.get(friday)) {
                    friday.setBackgroundColor(Color.rgb(255, 153, 51));
                    daysSelected.put(friday,true);
                }
                else {
                    friday.setBackgroundColor(Color.WHITE);
                    daysSelected.put(friday,false);
                }
            }
        });
        saturday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!daysSelected.get(saturday)) {
                    saturday.setBackgroundColor(Color.rgb(255, 153, 51));
                    daysSelected.put(saturday,true);
                }
                else {
                    saturday.setBackgroundColor(Color.WHITE);
                    daysSelected.put(saturday,false);
                }
            }
        });
        sunday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!daysSelected.get(sunday)) {
                    sunday.setBackgroundColor(Color.rgb(255, 153, 51));
                    daysSelected.put(sunday,true);
                }
                else {
                    sunday.setBackgroundColor(Color.WHITE);
                    daysSelected.put(sunday,false);
                }
            }
        });




        addAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Store Days as "|Mon|..."
                String days = "|";
                if (daysSelected.get(monday)) {
                    days += "Mon|";
                }
                if (daysSelected.get(tuesday)) {
                    days += "Tue|";
                }
                if (daysSelected.get(wednesday)) {
                    days += "Wed|";
                }
                if (daysSelected.get(thursday)) {
                    days += "Thu|";
                }
                if (daysSelected.get(friday)) {
                    days += "Fri|";
                }
                if (daysSelected.get(saturday)) {
                    days += "Sat|";
                }
                if (daysSelected.get(sunday)) {
                    days += "Sun|";
                }
                if (days.equals("|")) {
                    //If no day selected, prompt user to select one
                    Toast.makeText(getApplicationContext(),"Please select a day",Toast.LENGTH_LONG).show();
                }
                else {
                    //Change all titles to lowercase for easier comparison
                    changeArrayListToLowerCase(alarmsTitlesArrayList);
                    if (allWhitespaces(alarmName.getText().toString()) || alarmName.getText().toString().equals("")) {
                        //If name isn't there, or is invalid
                        Toast.makeText(getApplicationContext(),"Please set a valid name", Toast.LENGTH_LONG).show();
                    }
                    else if (alarmsTitlesArrayList.contains(alarmName.getText().toString())) {
                        //Prompt user that alarm is taken
                        if (alarmsInActive.contains(alarmName.getText().toString()) && alarmsNotActive.contains(alarmName.getText().toString())) {
                            Toast.makeText(getApplicationContext(), "Alarm name is already taken", Toast.LENGTH_LONG).show();
                        }
                        else if (alarmsInActive.contains(alarmName.getText().toString())) {
                            Toast.makeText(getApplicationContext(),"Alarm name is already taken", Toast.LENGTH_LONG).show();
                        }
                        if (alarmsNotActive.contains(alarmName.getText().toString())) {
                            Toast.makeText(getApplicationContext(),"Alarm name is already taken", Toast.LENGTH_LONG).show();
                        }
                    }

                    else {
                        //Insert the alarm into the active alarm DB, and go back to MainActivity
                        alarmDB.execSQL("INSERT INTO Alarm (name, days, sound, time) VALUES('" + alarmName.getText() + "', '" + days + "', '" + currentSong + "', '" + timeSetText.getText() + "');");
                        Toast.makeText(getApplicationContext(), "Alarm Added", Toast.LENGTH_LONG).show();
                        finish();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                }
            }
        });

    }

    //Method to check if all the characters in a title are whitespaces
    public boolean allWhitespaces(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != ' ') {
                return false;
            }
        }
        return true;
    }

    //Method to change all Strings in a list to lowercase
    public void changeArrayListToLowerCase(ArrayList<String> list) {
        for (String s : list) {
            s = s.toLowerCase();
        }
    }
    //Add Customized Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_alarm_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Action to close activity when 'X' is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        startActivity(new Intent(this,MainActivity.class));
        return true;
    }

    //Finish if back button is pressed
    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }

    //Method for handling the settings of time, OnClick method for Time Textview
    public void setTime(View v) {
        DialogFragment fragment = new TimeClockFragment();
        fragment.show(getFragmentManager(),"showTimer");
    }
}

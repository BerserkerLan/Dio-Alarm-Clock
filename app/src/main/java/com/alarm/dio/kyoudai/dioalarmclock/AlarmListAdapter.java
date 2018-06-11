package com.alarm.dio.kyoudai.dioalarmclock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class AlarmListAdapter extends BaseAdapter implements ListAdapter {

    private ArrayList<Alarm> alarms;
    private ArrayList<Boolean> activatedList;
    private ArrayList<String> days;
    private Context context;

    AlarmListAdapter(ArrayList<Alarm> alarms, ArrayList<Boolean> activatedList, ArrayList<String> days, Context context) {
        this.alarms = alarms;
        this.activatedList = activatedList;
        this.days = days;
        this.context = context;
    }
    @Override
    public int getCount() {
        return alarms.size();
    }

    @Override
    public Object getItem(int position) {
        return alarms.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view  = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {
                view = inflater.inflate(R.layout.alarm_list, null);
            }
        }

        final View tempView = view;

        TextView alarmName = view.findViewById(R.id.alarmTitle);
        final Switch alarmActive = view.findViewById(R.id.alarmSwitch);
        TextView timeText = view.findViewById(R.id.alarmTimeList);
        TextView daysText = view.findViewById(R.id.daysTextView);
        ImageView deleteButton = view.findViewById(R.id.deleteButton);

        alarmName.setText(alarms.get(position).getName());
        timeText.setText(alarms.get(position).getTime());

        daysText.setText(days.get(position));

        if (activatedList.get(position)) {
            alarmActive.setChecked(true);
        }
        else {
            alarmActive.setChecked(false);
        }

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("clicked", "It was clicked");
                final SQLiteDatabase alarmDB = tempView.getContext().openOrCreateDatabase("Active Alarms",SQLiteDatabase.OPEN_READWRITE,null);
                alarmDB.execSQL("CREATE TABLE IF NOT EXISTS Alarm(name VARCHAR, days VARCHAR, sound VARCHAR, time VARCHAR);");


                final SQLiteDatabase unActiveAlarmDB = tempView.getContext().openOrCreateDatabase("Unactive Alarms",SQLiteDatabase.OPEN_READWRITE,null);
                unActiveAlarmDB.execSQL("CREATE TABLE IF NOT EXISTS Alarm(name VARCHAR, days VARCHAR, sound VARCHAR, time VARCHAR);");

                alarmDB.delete("Alarm","name=?",new String[]{alarms.get(position).getName()});
                unActiveAlarmDB.delete("Alarm","name=?",new String[]{alarms.get(position).getName()});

                Log.d("Deleted", "got to this");
                if (!alarms.isEmpty()) {
                    MainActivity.alarmsArrayList.remove(alarms.get(position));
                    if (!MainActivity.unActiveAlarms.isEmpty() && !alarms.isEmpty()) {
                        try {
                            MainActivity.unActiveAlarms.remove(alarms.get(position));
                        }
                        catch (Exception e) {
                            Toast.makeText(context,"Deleted", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                if (!MainActivity.activatedList.isEmpty()) {
                    MainActivity.activatedList.remove(position);
                }
                if (MainActivity.notificationManager != null) {
                    MainActivity.notificationManager.cancelAll();
                }
                Log.d("Delted", "Deleted Alarm");
                MainActivity.alarmListAdapter.notifyDataSetChanged();
                Snackbar snackbar = Snackbar.make(tempView, R.string.alarm_delete_snack,Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        });


        alarmActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tempView != null) {
                    final SQLiteDatabase alarmDB = tempView.getContext().openOrCreateDatabase("Active Alarms", SQLiteDatabase.OPEN_READWRITE, null);
                    alarmDB.execSQL("CREATE TABLE IF NOT EXISTS Alarm(name VARCHAR, days VARCHAR, sound VARCHAR, time VARCHAR);");


                    final SQLiteDatabase unActiveAlarmDB = tempView.getContext().openOrCreateDatabase("Unactive Alarms", SQLiteDatabase.OPEN_READWRITE, null);
                    unActiveAlarmDB.execSQL("CREATE TABLE IF NOT EXISTS Alarm(name VARCHAR, days VARCHAR, sound VARCHAR, time VARCHAR);");
                    if (!alarmActive.isChecked()) {
                        Cursor activeCursor = alarmDB.rawQuery("SELECT * FROM Alarm WHERE name=?", new String[]{alarms.get(position).getName()});
                        activeCursor.moveToFirst();
                        String tempName = activeCursor.getString(activeCursor.getColumnIndex("name"));
                        String tempDays = activeCursor.getString(activeCursor.getColumnIndex("days"));
                        String tempSound = activeCursor.getString(activeCursor.getColumnIndex("sound"));
                        String tempTime = activeCursor.getString(activeCursor.getColumnIndex("time"));
                        unActiveAlarmDB.execSQL("INSERT INTO Alarm (name, days, sound, time) VALUES('" + tempName + "', '" + tempDays + "', '" + tempSound + "', '" + tempTime + "');");
                        alarmDB.delete("Alarm", "name=?", new String[]{tempName});
                        MainActivity.unActiveAlarms.add(alarms.get(position));
                        MainActivity.activatedList.set(position, true);
                        if (MainActivity.notificationManager != null) {
                            MainActivity.notificationManager.cancelAll();
                            ((MainActivity) tempView.getContext()).setUpNotifications();
                        }

                        activeCursor.close();

                        //MainActivity.alarmListAdapter.notifyDataSetChanged();
                        Toast.makeText(tempView.getContext(), "Made alarm inactive", Toast.LENGTH_SHORT).show();

                    } else {
                        Cursor activeCursor = unActiveAlarmDB.rawQuery("SELECT * FROM Alarm WHERE name=?", new String[]{alarms.get(position).getName()});
                        long count = DatabaseUtils.longForQuery(unActiveAlarmDB, "SELECT COUNT(*) FROM Alarm", null);
                        if (count > 0 && activeCursor != null && activeCursor.moveToFirst()) {
                            String tempName = activeCursor.getString(activeCursor.getColumnIndex("name"));
                            String tempDays = activeCursor.getString(activeCursor.getColumnIndex("days"));
                            String tempSound = activeCursor.getString(activeCursor.getColumnIndex("sound"));
                            String tempTime = activeCursor.getString(activeCursor.getColumnIndex("time"));
                            alarmDB.execSQL("INSERT INTO Alarm (name, days, sound, time) VALUES('" + tempName + "', '" + tempDays + "', '" + tempSound + "', '" + tempTime + "');");
                            unActiveAlarmDB.delete("Alarm", "name=?", new String[]{tempName});
                            MainActivity.unActiveAlarms.remove(alarms.get(position));
                            MainActivity.activatedList.set(position, false);
                            if (MainActivity.notificationManager != null) {
                                MainActivity.notificationManager.cancelAll();
                                ((MainActivity) tempView.getContext()).setUpNotifications();
                            }

                            activeCursor.close();

                            //MainActivity.alarmListAdapter.notifyDataSetChanged();
                            Toast.makeText(tempView.getContext(), "Made alarm Active", Toast.LENGTH_SHORT).show();

                        }
                    }
                }
            }
        });



        return view;
    }
}

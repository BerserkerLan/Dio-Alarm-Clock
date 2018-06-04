package com.alarm.dio.kyoudai.dioalarmclock;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

public class TimeClockFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);



        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));


    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        AddAlarmActivity.timeSetText.setText(addZeroToTime(hourOfDay) + ":" + addZeroToTime(minute));

    }

    //Method adds a 0 in front of an int if its less than a two digit int
    public String addZeroToTime(int time) {
        if (0 <= time && time <= 9) {
            return "0" + time;
        }
            return time + "";

    }
}

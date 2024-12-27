package com.example.lab371;

import android.os.Parcel;
import android.os.Parcelable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Task implements Parcelable {
    private String taskText;
    private int year, month, day, hour, minute;

    public Task(String taskText, int year, int month, int day, int hour, int minute) {
        this.taskText = taskText;
        this.year = year;
        this.month = month;  // Месяц в DatePicker начинается с 0, если это важно
        this.day = day;
        this.hour = hour;
        this.minute = minute;
    }

    protected Task(Parcel in) {
        taskText = in.readString();
        year = in.readInt();
        month = in.readInt();
        day = in.readInt();
        hour = in.readInt();
        minute = in.readInt();
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(taskText);
        dest.writeInt(year);
        dest.writeInt(month);
        dest.writeInt(day);
        dest.writeInt(hour);
        dest.writeInt(minute);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getTaskText() {
        return taskText;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public long getTimeInMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);
        return calendar.getTimeInMillis();
    }

    // Метод для форматирования времени задачи
    public String getFormattedTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }
}

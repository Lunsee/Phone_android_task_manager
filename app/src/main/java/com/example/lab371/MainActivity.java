package com.example.lab371;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.provider.Settings;
import android.app.AlarmManager;
import android.content.Context;
import android.widget.Toast;
import com.google.gson.reflect.TypeToken;
//import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Type;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;



public class MainActivity extends AppCompatActivity {

    private EditText taskInput;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private ListView taskListView;
    private ArrayList<Task> taskList = new ArrayList<>();
    private TaskAdapter taskAdapter;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int ALARM_PERMISSION_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadTasks();

        taskInput = findViewById(R.id.task_input);
        datePicker = findViewById(R.id.date_picker);
        timePicker = findViewById(R.id.time_picker);
        taskListView = findViewById(R.id.task_list);

        timePicker.setIs24HourView(true);

        // Передаем метод удаления в адаптер
        taskAdapter = new TaskAdapter(this, taskList, this::deleteTask);
        taskListView.setAdapter(taskAdapter);

        Button addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> addTask());

        Button activeTasksButton = findViewById(R.id.active_tasks_button);
        activeTasksButton.setOnClickListener(v -> openActiveTasks());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivityForResult(intent, ALARM_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void openActiveTasks() {
        removeExpiredTasks();
        Intent intent = new Intent(this, ActiveTasksActivity.class);
        intent.putParcelableArrayListExtra("task_list", taskList);
        startActivityForResult(intent, 1); // Указываем requestCode = 1
    }

    private void addTask() {
        String taskText = taskInput.getText().toString();

        if (taskText.isEmpty()) {
            Toast.makeText(this, "Введите текст задачи", Toast.LENGTH_SHORT).show();
            return;
        }

        int year = datePicker.getYear();
        int month = datePicker.getMonth();
        int day = datePicker.getDayOfMonth();
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        Task task = new Task(taskText, year, month, day, hour, minute);
        taskList.add(task);

        taskList.sort((t1, t2) -> Long.compare(t1.getTimeInMillis(), t2.getTimeInMillis()));
        taskAdapter.notifyDataSetChanged();

        saveTasks();
        setReminder(task);
        taskInput.setText("");
        Toast.makeText(this, "Задача добавлена!", Toast.LENGTH_SHORT).show();
    }

    private void setReminder(Task task) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(task.getYear(), task.getMonth(), task.getDay(), task.getHour(), task.getMinute(), 0);

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("task", task.getTaskText());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) task.getTimeInMillis(),
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void saveTasks() {
        SharedPreferences sharedPreferences = getSharedPreferences("task_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(taskList);
        editor.putString("task_list", json);
        editor.apply();
    }

    private void loadTasks() {
        SharedPreferences sharedPreferences = getSharedPreferences("task_prefs", MODE_PRIVATE);
        String json = sharedPreferences.getString("task_list", "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Task>>() {}.getType();
            taskList = gson.fromJson(json, type);
        } else {
            taskList = new ArrayList<>();
        }
    }

    // Метод для удаления задачи
    private void deleteTask(Task task) {
        taskList.remove(task);  // Удаляем задачу из списка
        saveTasks();            // Сохраняем изменения
        taskAdapter.notifyDataSetChanged(); // Обновляем адаптер

        // Показываем сообщение
        Toast.makeText(this, "Задача удалена", Toast.LENGTH_SHORT).show();
    }

    // Вложенный класс ReminderReceiver
    public static class ReminderReceiver extends BroadcastReceiver {
        private static final String CHANNEL_ID = "task_notifications";

        @Override
        public void onReceive(Context context, Intent intent) {
            String taskText = intent.getStringExtra("task");

            createNotificationChannel(context);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Task Reminder")
                    .setContentText(taskText)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify((int) System.currentTimeMillis(), builder.build());
            }
        }

        private void createNotificationChannel(Context context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "Task Notifications";
                String description = "Channel for task reminders";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            ArrayList<Task> updatedTaskList = data.getParcelableArrayListExtra("task_list");
            if (updatedTaskList != null) {
                taskList.clear();
                taskList.addAll(updatedTaskList);
                taskAdapter.notifyDataSetChanged();
                saveTasks(); // Сохраняем изменения
            }
        }
    }
    private void removeExpiredTasks(){
        long currentTime = System.currentTimeMillis();  // Текущее время в миллисекундах
        ArrayList<Task> expiredTasks = new ArrayList<>();

        // Ищем задачи, которые уже просрочены
        for (Task task : taskList) {
            if (task.getTimeInMillis() < currentTime) {
                expiredTasks.add(task);
            }
        }

        // Удаляем просроченные задачи из списка
        taskList.removeAll(expiredTasks);

        // Сохраняем изменения
        saveTasks();
        taskAdapter.notifyDataSetChanged();

    }
}

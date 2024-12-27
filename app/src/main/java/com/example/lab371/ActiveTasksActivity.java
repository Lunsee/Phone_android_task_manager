package com.example.lab371;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.ArrayList;

public class ActiveTasksActivity extends AppCompatActivity {

    private ArrayList<Task> taskList;
    private TaskAdapter taskAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_tasks);

        listView = findViewById(R.id.task_list_view);

        // Получаем переданный список задач из Intent
        taskList = getIntent().getParcelableArrayListExtra("task_list");

        // Если список задач пуст, создаём новый
        if (taskList == null) {
            taskList = new ArrayList<>();
        }

        // Устанавливаем адаптер для отображения списка задач
        taskAdapter = new TaskAdapter(this, taskList, this::deleteTask);
        listView.setAdapter(taskAdapter);

        // Обработчик кнопки возврата на главную
        findViewById(R.id.back_button).setOnClickListener(v -> {
            // Возвращаем обновленный список задач в MainActivity
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra("task_list", taskList);
            setResult(RESULT_OK, intent);
            finish(); // Закрываем текущую активность
        });
    }

    // Метод для удаления задачи
    private void deleteTask(Task task) {
        taskList.remove(task);  // Удаляем задачу из списка
        saveTasks();            // Сохраняем изменения в SharedPreferences
        taskAdapter.notifyDataSetChanged(); // Обновляем адаптер

        // Показываем сообщение
        Toast.makeText(this, "Задача удалена", Toast.LENGTH_SHORT).show();
    }

    // Метод для сохранения задач в SharedPreferences
    private void saveTasks() {
        SharedPreferences sharedPreferences = getSharedPreferences("task_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(taskList);
        editor.putString("task_list", json);
        editor.apply();
    }
}

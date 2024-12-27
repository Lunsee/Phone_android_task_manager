package com.example.lab371;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Button;

import java.util.ArrayList;

public class TaskAdapter extends ArrayAdapter<Task> {

    private Context context;
    private ArrayList<Task> tasks;
    private OnDeleteClickListener onDeleteClickListener; // Метод для удаления

    public TaskAdapter(Context context, ArrayList<Task> tasks, OnDeleteClickListener onDeleteClickListener) {
        super(context, 0, tasks);
        this.context = context;
        this.tasks = tasks;
        this.onDeleteClickListener = onDeleteClickListener; // Инициализируем метод удаления
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false);
        }

        Task task = tasks.get(position);

        // Отображение текста задачи
        TextView taskTextView = convertView.findViewById(R.id.task_text);
        taskTextView.setText(task.getTaskText());

        // Отображение времени задачи
        TextView taskTimeView = convertView.findViewById(R.id.task_time);
        String taskTimeText = String.format("%02d:%02d, %02d/%02d/%04d",
                task.getHour(), task.getMinute(), task.getDay(), task.getMonth() + 1, task.getYear());
        taskTimeView.setText(taskTimeText);

        // Кнопка для удаления задачи
        Button deleteButton = convertView.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(task); // Вызываем метод для удаления
            }
        });

        return convertView;
    }

    // Интерфейс для удаления задачи
    public interface OnDeleteClickListener {
        void onDeleteClick(Task task);
    }
}

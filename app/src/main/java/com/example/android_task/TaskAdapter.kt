package com.example.android_task

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android_task.database.TaskEntity


class TaskAdapter : ListAdapter<TaskEntity, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskView: TextView = view.findViewById(R.id.task)
        val titleView: TextView = view.findViewById(R.id.title)
        val descriptionView: TextView = view.findViewById(R.id.description)
        val colorView: View = view.findViewById(R.id.colorCode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)

        val color = try {
            if (task.colorCode.isNotEmpty()) {
                Color.parseColor(task.colorCode)
            } else {
                Color.LTGRAY
            }
        } catch (e: IllegalArgumentException) {
            Color.LTGRAY
        }

        holder.itemView.findViewById<View>(R.id.colorCode).setBackgroundColor(color)
        holder.taskView.text = task.task
        holder.titleView.text = task.title
        holder.descriptionView.text = task.description
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<TaskEntity>() {
        override fun areItemsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean {
            return oldItem == newItem
        }
    }
}

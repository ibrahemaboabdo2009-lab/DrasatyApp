package com.drasaty.app.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.drasaty.app.R
import com.drasaty.app.db.TaskEntry
import kotlinx.android.synthetic.main.item_task.view.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private var items: List<TaskEntry>,
    private val onToggleComplete: (TaskEntry, Boolean) -> Unit,
    private val onDelete: (TaskEntry) -> Unit
) : RecyclerView.Adapter<TaskAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.itemView.apply {
            tvTitle.text = item.title
            tvSubject.text = "📘 ${item.subject}"
            cbComplete.setOnCheckedChangeListener(null)
            cbComplete.isChecked = item.isCompleted
            toggleStrikeThrough(tvTitle, item.isCompleted)

            // الأولوية
            val (priorityText, priorityBg) = when (item.priority) {
                1 -> "⚠️ عالية" to R.drawable.bg_priority_high
                2 -> "متوسطة" to R.drawable.bg_priority_medium
                else -> "منخفضة" to R.drawable.bg_priority_low
            }
            tvPriority.text = priorityText
            tvPriority.setBackgroundResource(priorityBg)

            // التاريخ
            val dateFmt = SimpleDateFormat("dd MMM", Locale("ar"))
            tvDueDate.text = "📅 ${dateFmt.format(Date(item.dueDateMillis))}"

            cbComplete.setOnCheckedChangeListener { _, isChecked ->
                toggleStrikeThrough(tvTitle, isChecked)
                onToggleComplete(item, isChecked)
            }

            btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    private fun toggleStrikeThrough(view: View, strike: Boolean) {
        if (strike) {
            (view as android.widget.TextView).paintFlags =
                view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            view.alpha = 0.5f
        } else {
            (view as android.widget.TextView).paintFlags =
                view.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            view.alpha = 1f
        }
    }

    fun update(newItems: List<TaskEntry>) {
        items = newItems
        notifyDataSetChanged()
    }
}
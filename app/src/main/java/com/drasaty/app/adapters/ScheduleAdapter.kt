package com.drasaty.app.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.drasaty.app.R
import com.drasaty.app.db.ScheduleEntry
import kotlinx.android.synthetic.main.item_schedule.view.*

class ScheduleAdapter(
    private var items: List<ScheduleEntry>,
    private val onDelete: (ScheduleEntry) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.itemView.apply {
            tvSubject.text = item.subjectName
            tvTime.text = "${item.startTime} - ${item.endTime}"
            tvRoom.text = if (item.room.isBlank()) "" else "📍 ${item.room}"

            // تغيير اللون بناءً على اليوم
            val colorRes = when (item.dayOfWeek) {
                0 -> R.color.saturday
                1 -> R.color.sunday
                2 -> R.color.monday
                3 -> R.color.tuesday
                4 -> R.color.wednesday
                5 -> R.color.thursday
                else -> R.color.friday
            }
            vColorBar.setBackgroundColor(ContextColorUtil.getColor(context, colorRes))

            btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    fun update(newItems: List<ScheduleEntry>) {
        items = newItems
        notifyDataSetChanged()
    }
}

object ContextColorUtil {
    fun getColor(context: android.content.Context, resId: Int): Int {
        return context.getColor(resId)
    }
}
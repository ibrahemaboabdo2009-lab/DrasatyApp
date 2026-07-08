package com.drasaty.app.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.drasaty.app.Utils
import com.drasaty.app.adapters.TaskAdapter
import com.drasaty.app.databinding.DialogAddTaskBinding
import com.drasaty.app.databinding.FragmentTasksBinding
import com.drasaty.app.db.DrasatyDatabase
import com.drasaty.app.db.TaskEntry
import com.drasaty.app.notifications.NotificationsHelper
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TaskAdapter
    private var currentTab = 0 // 0=today, 1=upcoming, 2=completed
    private var selectedDueDate: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupTabs() {
        binding.tabTasks.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTab = tab.position
                loadData()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupUI() {
        adapter = TaskAdapter(
            items = emptyList(),
            onToggleComplete = { task, isChecked ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        val updated = task.copy(isCompleted = isChecked)
                        DrasatyDatabase.getDatabase(requireContext()).taskDao().update(updated)
                        if (isChecked) {
                            NotificationsHelper.cancelTaskReminder(requireContext(), task.id)
                        } else {
                            NotificationsHelper.scheduleTaskReminder(requireContext(), updated)
                        }
                    }
                    loadData()
                }
            },
            onDelete = { task ->
                AlertDialog.Builder(requireContext())
                    .setTitle("حذف المهمة")
                    .setMessage("هل تريد حذف \"${task.title}\"؟")
                    .setPositiveButton("حذف") { _, _ ->
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                DrasatyDatabase.getDatabase(requireContext()).taskDao().delete(task.id)
                                NotificationsHelper.cancelTaskReminder(requireContext(), task.id)
                            }
                            loadData()
                        }
                    }
                    .setNegativeButton("إلغاء", null)
                    .show()
            }
        )
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = adapter

        binding.fabAddTask.setOnClickListener { showAddDialog() }
    }

    private fun loadData() {
        val db = DrasatyDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                when (currentTab) {
                    0 -> db.taskDao().getTodayPending(Utils.startOfDay(), Utils.endOfDay())
                    1 -> db.taskDao().getUpcoming(System.currentTimeMillis())
                    else -> db.taskDao().getCompleted()
                }
            }
            adapter.update(items)
            binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.rvTasks.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun showAddDialog() {
        val dialogBinding = DialogAddTaskBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        val dateFmt = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("ar"))
        dialogBinding.btnPickDate.text = dateFmt.format(java.util.Date(selectedDueDate))

        dialogBinding.btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedDueDate }
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val newCal = Calendar.getInstance().apply {
                        set(year, month, day, 23, 59, 0)
                    }
                    selectedDueDate = newCal.timeInMillis
                    dialogBinding.btnPickDate.text = dateFmt.format(java.util.Date(selectedDueDate))
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.etTaskTitle.text.toString().trim()
            val subject = dialogBinding.etTaskSubject.text.toString().trim()

            if (title.isEmpty() || subject.isEmpty()) {
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    "يرجى إدخال العنوان والمادة",
                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val priority = when (dialogBinding.rgPriority.checkedRadioButtonId) {
                com.drasaty.app.R.id.rbHigh -> 1
                com.drasaty.app.R.id.rbLow -> 3
                else -> 2
            }

            val task = TaskEntry(
                title = title,
                subject = subject,
                dueDateMillis = selectedDueDate,
                priority = priority
            )

            lifecycleScope.launch {
                val newId = withContext(Dispatchers.IO) {
                    DrasatyDatabase.getDatabase(requireContext()).taskDao().insert(task)
                }
                NotificationsHelper.scheduleTaskReminder(
                    requireContext(), task.copy(id = newId)
                )
                loadData()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
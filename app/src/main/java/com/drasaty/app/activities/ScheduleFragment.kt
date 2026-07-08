package com.drasaty.app.activities

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.drasaty.app.Utils
import com.drasaty.app.adapters.ScheduleAdapter
import com.drasaty.app.databinding.DialogAddSubjectBinding
import com.drasaty.app.databinding.FragmentScheduleBinding
import com.drasaty.app.db.DrasatyDatabase
import com.drasaty.app.db.ScheduleEntry
import com.drasaty.app.notifications.NotificationsHelper
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ScheduleAdapter
    private var selectedDay: Int = Utils.getTodayAppDay()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
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
        Utils.ARABIC_DAYS_SHORT.forEachIndexed { index, day ->
            binding.tabDays.addTab(binding.tabDays.newTab().setText(day))
        }
        // تحديد اليوم الحالي
        val today = Utils.getTodayAppDay()
        binding.tabDays.getTabAt(today)?.select()
        selectedDay = today

        binding.tabDays.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedDay = tab.position
                loadData()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupUI() {
        adapter = ScheduleAdapter(emptyList()) { entry ->
            AlertDialog.Builder(requireContext())
                .setTitle("حذف الحصة")
                .setMessage("هل تريد حذف ${entry.subjectName}؟")
                .setPositiveButton("حذف") { _, _ ->
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            DrasatyDatabase.getDatabase(requireContext()).scheduleDao().delete(entry.id)
                        }
                        loadData()
                    }
                }
                .setNegativeButton("إلغاء", null)
                .show()
        }
        binding.rvSchedule.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSchedule.adapter = adapter

        binding.fabAddClass.setOnClickListener { showAddDialog() }
    }

    private fun loadData() {
        val db = DrasatyDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                db.scheduleDao().getByDay(selectedDay)
            }
            adapter.update(items)
            binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.rvSchedule.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun showAddDialog() {
        val dialogBinding = DialogAddSubjectBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etSubjectName.text.toString().trim()
            val start = dialogBinding.etStartTime.text.toString().trim()
            val end = dialogBinding.etEndTime.text.toString().trim()
            val room = dialogBinding.etRoom.text.toString().trim()

            if (name.isEmpty() || start.isEmpty() || end.isEmpty()) {
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    "يرجى إدخال جميع الحقول المطلوبة",
                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val entry = ScheduleEntry(
                subjectName = name,
                dayOfWeek = selectedDay,
                startTime = start,
                endTime = end,
                room = room
            )

            lifecycleScope.launch {
                val newId = withContext(Dispatchers.IO) {
                    DrasatyDatabase.getDatabase(requireContext()).scheduleDao().insert(entry)
                }
                NotificationsHelper.scheduleClassReminder(
                    requireContext(), selectedDay, start, name, newId
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
package com.drasaty.app.activities

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.drasaty.app.Utils
import com.drasaty.app.databinding.FragmentSettingsBinding
import com.drasaty.app.db.DrasatyDatabase
import com.drasaty.app.notifications.NotificationsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        loadStats()
    }

    override fun onResume() {
        super.onResume()
        loadStats()
    }

    private fun setupUI() {
        val prefs = requireContext().getSharedPreferences("drasaty_prefs", Context.MODE_PRIVATE)
        binding.switchNotifications.isChecked = prefs.getBoolean("notifications_enabled", true)
        binding.switchNotifications.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("notifications_enabled", checked).apply()
        }

        binding.btnClearCompleted.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("حذف المهام المكتملة")
                .setMessage("هل تريد حذف كل المهام المكتملة؟")
                .setPositiveButton("حذف") { _, _ ->
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            DrasatyDatabase.getDatabase(requireContext()).taskDao().deleteCompleted()
                        }
                        loadStats()
                    }
                }
                .setNegativeButton("إلغاء", null)
                .show()
        }

        binding.btnResetAll.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("⚠️ تحذير")
                .setMessage("سيتم حذف كل البيانات (الجدول، المهام، الإحصائيات). هل أنت متأكد؟")
                .setPositiveButton("احذف الكل") { _, _ ->
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            val db = DrasatyDatabase.getDatabase(requireContext())
                            db.scheduleDao().deleteAll()
                            db.taskDao().deleteAll()
                            db.pomodoroDao().deleteAll()
                        }
                        loadStats()
                    }
                }
                .setNegativeButton("إلغاء", null)
                .show()
        }
    }

    private fun loadStats() {
        val db = DrasatyDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            val (total, completed, pomodoroAll) = withContext(Dispatchers.IO) {
                Triple(
                    db.taskDao().countAll(),
                    db.taskDao().countCompleted(),
                    db.pomodoroDao().countAllSessions()
                )
            }
            binding.tvTotalTasks.text = total.toString()
            binding.tvCompletedTasks.text = completed.toString()
            binding.tvPomodoroSessions.text = pomodoroAll.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
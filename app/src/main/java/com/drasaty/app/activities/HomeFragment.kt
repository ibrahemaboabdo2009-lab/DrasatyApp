package com.drasaty.app.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.drasaty.app.Utils
import com.drasaty.app.adapters.ScheduleAdapter
import com.drasaty.app.adapters.TaskAdapter
import com.drasaty.app.databinding.FragmentHomeBinding
import com.drasaty.app.db.DrasatyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupUI() {
        binding.tvDate.text = Utils.formatArabicDate()

        scheduleAdapter = ScheduleAdapter(emptyList()) { /* no-op on home */ }
        binding.rvTodaySchedule.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTodaySchedule.adapter = scheduleAdapter

        taskAdapter = TaskAdapter(emptyList(), { _, _ -> }, { /* no-op on home */ })
        binding.rvTodayTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTodayTasks.adapter = taskAdapter

        binding.btnViewAllTasks.setOnClickListener {
            (requireActivity() as MainActivity).findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(com.drasaty.app.R.id.bottomNav).selectedItemId = com.drasaty.app.R.id.nav_tasks
        }
    }

    private fun loadData() {
        val db = DrasatyDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            val today = Utils.getTodayAppDay()
            val (todaySchedule, todayTasks, pendingCount) = withContext(Dispatchers.IO) {
                Triple(
                    db.scheduleDao().getByDay(today),
                    db.taskDao().getTodayPending(Utils.startOfDay(), Utils.endOfDay()),
                    db.taskDao().countPending()
                )
            }

            binding.tvClassesCount.text = todaySchedule.size.toString()
            binding.tvTasksCount.text = pendingCount.toString()

            if (todaySchedule.isEmpty()) {
                binding.tvNoClasses.visibility = View.VISIBLE
                binding.rvTodaySchedule.visibility = View.GONE
            } else {
                binding.tvNoClasses.visibility = View.GONE
                binding.rvTodaySchedule.visibility = View.VISIBLE
                scheduleAdapter.update(todaySchedule)
            }

            if (todayTasks.isEmpty()) {
                binding.tvNoTasks.visibility = View.VISIBLE
                binding.rvTodayTasks.visibility = View.GONE
            } else {
                binding.tvNoTasks.visibility = View.GONE
                binding.rvTodayTasks.visibility = View.VISIBLE
                taskAdapter.update(todayTasks)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
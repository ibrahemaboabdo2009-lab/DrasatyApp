package com.drasaty.app.activities

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.drasaty.app.Utils
import com.drasaty.app.databinding.FragmentPomodoroBinding
import com.drasaty.app.db.DrasatyDatabase
import com.drasaty.app.db.PomodoroSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class PomodoroFragment : Fragment() {

    private var _binding: FragmentPomodoroBinding? = null
    private val binding get() = _binding!!

    private var timer: CountDownTimer? = null
    private var isRunning = false
    private var isWorkMode = true
    private var totalMillis = WORK_DURATION
    private var remainingMillis = WORK_DURATION
    private var todaySessionCount = 0

    companion object {
        const val WORK_DURATION = 25 * 60 * 1000L // 25 دقيقة
        const val BREAK_DURATION = 5 * 60 * 1000L // 5 دقايق راحة
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPomodoroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUI()
        binding.btnStartPause.setOnClickListener { toggleTimer() }
        binding.btnReset.setOnClickListener { resetTimer() }
        loadTodayCount()
    }

    private fun loadTodayCount() {
        lifecycleScope.launch {
            todaySessionCount = withContext(Dispatchers.IO) {
                DrasatyDatabase.getDatabase(requireContext()).pomodoroDao()
                    .countTodaySessions(Utils.todayString())
            }
            updateSessionCount()
        }
    }

    private fun toggleTimer() {
        if (isRunning) {
            timer?.cancel()
            isRunning = false
            binding.btnStartPause.text = "استئناف"
        } else {
            isRunning = true
            binding.btnStartPause.text = "إيقاف"
            timer = object : CountDownTimer(remainingMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    remainingMillis = millisUntilFinished
                    updateTimerDisplay()
                    val progress = ((totalMillis - millisUntilFinished).toFloat() / totalMillis * 100).toInt()
                    binding.progressBar.progress = progress
                }

                override fun onFinish() {
                    isRunning = false
                    binding.btnStartPause.text = "ابدأ"
                    binding.progressBar.progress = 100
                    onSessionComplete()
                }
            }.start()
        }
    }

    private fun resetTimer() {
        timer?.cancel()
        isRunning = false
        totalMillis = if (isWorkMode) WORK_DURATION else BREAK_DURATION
        remainingMillis = totalMillis
        binding.btnStartPause.text = "ابدأ"
        binding.progressBar.progress = 0
        updateTimerDisplay()
        updateUI()
    }

    private fun onSessionComplete() {
        val type = if (isWorkMode) "work" else "break"
        val durationMin = if (isWorkMode) 25 else 5

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                DrasatyDatabase.getDatabase(requireContext()).pomodoroDao().insert(
                    PomodoroSession(
                        date = Utils.todayString(),
                        durationMinutes = durationMin,
                        type = type
                    )
                )
            }
            if (isWorkMode) {
                todaySessionCount++
                updateSessionCount()
                playNotificationSound()
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    "🎉 أحسنت! خذ 5 دقايق راحة",
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                ).show()
            } else {
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    "💪 وقت الراحة انتهى، يلا نشتغل!",
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                ).show()
            }
            // تبديل الوضع
            isWorkMode = !isWorkMode
            totalMillis = if (isWorkMode) WORK_DURATION else BREAK_DURATION
            remainingMillis = totalMillis
            binding.progressBar.progress = 0
            updateUI()
        }
    }

    private fun playNotificationSound() {
        try {
            val tone = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            val mp = MediaPlayer.create(requireContext(), tone)
            mp?.setOnCompletionListener { it.release() }
            mp?.start()
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun updateTimerDisplay() {
        val minutes = (remainingMillis / 1000 / 60).toInt()
        val seconds = (remainingMillis / 1000 % 60).toInt()
        binding.tvTimer.text = String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    private fun updateUI() {
        binding.tvMode.text = if (isWorkMode) "وقت العمل 🎯" else "وقت الراحة ☕"
        updateTimerDisplay()
    }

    private fun updateSessionCount() {
        binding.tvSessionCount.text = "الجلسات المنجزة اليوم: $todaySessionCount"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
    }
}
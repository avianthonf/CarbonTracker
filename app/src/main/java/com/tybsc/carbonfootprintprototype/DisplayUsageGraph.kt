package com.tybsc.carbonfootprintprototype

import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.*
import androidx.core.content.edit

class DisplayUsageGraph : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_display_usage_graph)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupGoalSection()

        val barChart = findViewById<BarChart>(R.id.barChart)

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startTime = calendar.timeInMillis
        updateBarGraph(barChart, startTime)
    }


    // Simple SUMMARY BUILDER

    private fun updateSummary(textView: TextView, data: List<Float>) {
        val avg = data.average().toFloat()
        val total = data.sum()
        val max = data.maxOrNull() ?: 0f
        val min = data.minOrNull() ?: 0f

        val keyword = when {
            avg < 100 -> "Excellent"
            avg < 300 -> "Good"
            avg < 600 -> "Moderate"
            avg < 1000 -> "High"
            avg < 2000 -> "Very High"
            total > 10000 -> "Excessive"
            max > 5000 -> "Spike"
            min == 0f -> "Zero Day"
            avg.isNaN() -> "No Data"
            else -> "Unusual"
        }

        val summary = when (keyword) {
            "Excellent" -> "Your footprint is very low. Keep it up."
            "Good" -> "Your footprint is good. Slight improvements possible."
            "Moderate" -> "Your footprint is moderate. Watch high-usage days."
            "High" -> "Your footprint is high. Consider efficiency changes."
            "Very High" -> "Your footprint is very high. Strong action needed."
            "Excessive" -> "Weekly total is excessive. Reduce usage drastically."
            "Spike" -> "One day had a spike in emissions. Check that activity."
            "Zero Day" -> "You had a zero-usage day. Good efficiency."
            "No Data" -> "No usage data available for this period."
            else -> "Footprint pattern is unusual. Monitor closely."
        }

        textView.text =
            "Summary: $summary (Total: ${"%.1f".format(total)} gCO₂e, Avg/day: ${"%.1f".format(avg)} gCO₂e)"
    }


    // GRAPH & PROGRESS

    private fun updateBarGraph(barChart: BarChart, startTimeLong: Long) {
        val ue = UsageEngine(applicationContext)
        val calendar = Calendar.getInstance()
        var startTime = startTimeLong
        var endTime: Long
        val entries = ArrayList<BarEntry>()

        var weeklyProgress = 0f // compute weekly progress from scratch

        for (day in 1..7) {
            calendar.timeInMillis = startTime
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            endTime = calendar.timeInMillis

            val footprint = ue.getCarbonFootprint(startTime, endTime).toFloat()
            entries.add(BarEntry(day.toFloat(), footprint))
            weeklyProgress += footprint

            startTime = endTime
        }

        // Save weekly progress
        val prefs = getSharedPreferences("CarbonPrefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("weeklyProgress", weeklyProgress.toInt()).apply()

        val dataSet = BarDataSet(entries, "Carbon Footprint (in gCO₂e)")
        dataSet.color = ContextCompat.getColor(applicationContext, R.color.colourChartBar)

        val data = BarData(dataSet)
        barChart.data = data

        val days = listOf("0", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(days)
        xAxis.granularity = 1f
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        barChart.invalidate()

        val values = entries.map { it.y }
        updateSummary(findViewById(R.id.summaryText), values)

        updateProgressUI()
    }

    private fun updateProgressUI() {
        val prefs = getSharedPreferences("CarbonPrefs", Context.MODE_PRIVATE)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        val progressText: TextView = findViewById(R.id.progressText)
        val currentGoalText: TextView = findViewById(R.id.currentGoalText)

        val goal = prefs.getInt("weeklyGoal", 0)
        val progress = prefs.getInt("weeklyProgress", 0)

        currentGoalText.text =
            if (goal > 0) "Current Goal: $goal gCO₂e" else "No goal set"

        if (goal > 0) {
            val percent = ((progress.toFloat() / goal) * 100).toInt().coerceIn(0, 100)
            progressBar.progress = percent
            progressText.text = "Progress: $percent% ($progress / $goal gCO₂e)"
        } else {
            progressBar.progress = 0
            progressText.text = "Progress: 0%"
        }
    }

    // GOALS

    private fun setupGoalSection() {
        val prefs = getSharedPreferences("CarbonPrefs", Context.MODE_PRIVATE)

        val goalInput: EditText = findViewById(R.id.goalInput)
        val saveButton: Button = findViewById(R.id.saveButton)

        saveButton.setOnClickListener {
            val goalValue = goalInput.text.toString().toIntOrNull()
            if (goalValue != null && goalValue > 0) {
                prefs.edit { putInt("weeklyGoal", goalValue) }
                Toast.makeText(this, "Goal saved!", Toast.LENGTH_SHORT).show()
                updateProgressUI()
            } else {
                Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show()
            }
        }

        updateProgressUI()
    }
}

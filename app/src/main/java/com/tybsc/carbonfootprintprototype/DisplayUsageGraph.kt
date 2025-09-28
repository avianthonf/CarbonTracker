package com.tybsc.carbonfootprintprototype

import android.os.Bundle
import android.widget.TextView
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
import java.util.Calendar

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

        val barChart = findViewById<BarChart>(R.id.barChart)

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startTime = calendar.timeInMillis
        updateBarGraph(barChart, startTime)
    }

    fun updateSummary(textView: TextView, data: List<Float>) {
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

        textView.text = "Summary: $summary (Total: ${"%.1f".format(total)} gCO₂e, Avg/day: ${"%.1f".format(avg)} gCO₂e)"
    }


    fun updateBarGraph(barChart: BarChart, startTimeLong: Long)
    {
        val ue = UsageEngine(applicationContext)

        var carbonFootprint = DoubleArray(7)

        val calendar = Calendar.getInstance()
        var startTime = startTimeLong
        var endTime = startTime

        val entries = ArrayList<BarEntry>()


        for (day in 1..7)
        {
            startTime = endTime
            calendar.timeInMillis = startTime
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            endTime = calendar.timeInMillis

            val footprint = ue.getCarbonFootprint(startTime, endTime).toFloat()
            entries.add(BarEntry(day.toFloat(), footprint))
        }

        val dataSet = BarDataSet(entries, "Carbon Footprint (in gCO2e)")
        dataSet.color = ContextCompat.getColor(applicationContext, R.color.colourChartBar)

        // Create BarData
        val data = BarData(dataSet)
        barChart.data = data


        // X-axis labels
        val days = listOf("0","Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(days)
        xAxis.granularity = 1f
        xAxis.position = XAxis.XAxisPosition.BOTTOM

    // Refresh chart
        barChart.invalidate()

    // UpdaTE SUMMARY
        val values = entries.map { it.y }
        updateSummary(findViewById(R.id.summaryText), values)


    }
}
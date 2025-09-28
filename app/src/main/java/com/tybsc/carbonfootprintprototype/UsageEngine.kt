package com.tybsc.carbonfootprintprototype

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat.getSystemService
import android.app.usage.UsageStatsManager
import android.app.usage.UsageStats
import android.app.usage.UsageEvents
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import androidx.core.content.ContextCompat
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.random.nextUInt

// Module 2, Abb battery usage, Single app data struct
data class AppStat(var appName: String, var appActivity: String, var appMahConsumed: Double, var appIcon: Drawable, var appHrs: Int, var appMins : Int)
data class CallStat(var phoneNumber: String, var callerName: String = "UNKNOWN", var callMins: Int, var callMahConsumed: Double)


class UsageEngine(val context: Context) {


    // Module 2 tools
    fun getAppStats(startTime: Long, endTime: Long) : List<AppStat> // Requires UsageAccess Permission
    {
         val toReturn = mutableListOf<AppStat>()

        // Get App Data

        val usageStatsManager: UsageStatsManager? =
            getSystemService(context, UsageStatsManager::class.java)
            val usageStatsList: List<UsageStats> = usageStatsManager?.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,startTime, endTime)?: emptyList()


            val pm = context.packageManager //For app icons

        // Estimate Battery Percentage
        fun getConsumedMah(uStat: UsageStats): Double
        {
            // Get total battery capacity in mAh
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val batteryCapacityUah =
                batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)

            // Fallback capacity (mAh) if unavailable
            val batteryCapacityMah = if (batteryCapacityUah > 0) {
                batteryCapacityUah / 1000.0
            } else {
                4000.0 // assume 3000mAh if unknown
            }

            val fgTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(uStat.totalTimeInForeground)

            if (fgTimeMinutes > 0) {
                // Estimated drain in mAh (very rough!)
                val estimatedMah = fgTimeMinutes * 0.5  // assume ~0.5mAh per minute

                return estimatedMah
            }
            else
            {
                return 0.1
            }
        }

            for (i in usageStatsList)
            {
                val icon = try {
                    pm.getApplicationIcon(i.packageName)
                } catch (e: PackageManager.NameNotFoundException) {
                    ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground) // fallback icon
                }

                var mins : Int = (i.totalTimeInForeground / (1000 * 60)).toInt()
                val hrs : Int = (mins / 60).toInt()
                mins %= 60

                toReturn.add(
                    AppStat(
                        i.packageName.toString(),
                        i.packageName.toString(),
                        getConsumedMah(i),
                        icon,
                        hrs,
                        mins
                    )
                )
            }
        return  toReturn.sortedByDescending { it.appMins + (it.appHrs * 60) }
    }

    fun getAppStats(fromNDaysAgo: Int = -1) : List<AppStat>
    {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, fromNDaysAgo)
        val startTime = calendar.timeInMillis

        return getAppStats(startTime, endTime)
    }

    fun mahToCarbonFootprint(
        mAh: Double,
        voltage: Double = 3.7,                // default: phone battery nominal voltage
        carbonIntensity: Double = 700.0       // gCO2e per kWh (default: India avg.)
    ): Double {
        // Step 1: Convert mAh → Wh
        val wh = (mAh / 1000.0) * voltage

        // Step 2: Wh → kWh
        val kWh = wh / 1000.0

        // Step 3: kWh → gCO2e
        return kWh * carbonIntensity
    }

    fun getCarbonFootprint(startTime: Long, endTime: Long) : Double
    {
        val usage = getAppStats(startTime, endTime)

        var usedMah = 0.0

        for (app in usage)
        {
            usedMah += app.appMahConsumed
        }

        return mahToCarbonFootprint(usedMah)
    }
}
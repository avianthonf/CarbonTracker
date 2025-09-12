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
import androidx.core.content.ContextCompat
import java.util.Calendar

// Module 2, Abb battery usage, Single app data struct
data class AppStat(var appName: String, var appActivity: String, var appIcon: Drawable, var appHrs: Int, var appMins : Int, var appBatteryUsed: Double)

class UsageEngine(val context: Context) {


    // Module 2 tools
    fun getAppStats(fromNDaysAgo: Int = -1) : MutableList<AppStat> // Requires UsageAccess Permission
    {
        var toReturn = mutableListOf<AppStat>()

        // Get App Data

        val usageStatsManager: UsageStatsManager? =
            getSystemService(context, UsageStatsManager::class.java)
            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, fromNDaysAgo)
            val startTime = calendar.timeInMillis

            val usageStatsList: List<UsageStats> = usageStatsManager?.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,startTime, endTime)?: emptyList()


            val pm = context.packageManager //Fort app icons

            for (i in usageStatsList)
            {
                val icon = try {
                    pm.getApplicationIcon(i.packageName)
                } catch (e: PackageManager.NameNotFoundException) {
                    ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground) // fallback icon
                }

                toReturn.add(
                    AppStat(
                        i.packageName.toString(),
                        i.packageName.toString(),
                        icon,
                        1,
                         1,
                        20.5
                    )
                )
            }




        return  toReturn
    }
}
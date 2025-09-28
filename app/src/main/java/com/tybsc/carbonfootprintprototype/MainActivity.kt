package com.tybsc.carbonfootprintprototype


import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process   // <-- this one
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Ensure usage access - only requests if not granted
        requestUsageAccessIfNeeded()

        val intent = Intent(this, DisplayUsageGraph::class.java)
        startActivity(intent)



        val  output = findViewById<TextView>(R.id.output)
        val nextButton = findViewById<Button>(R.id.next)
        val iconIV = findViewById<ImageView>(R.id.imageView)

        val engine = UsageEngine(this)

        val data = engine.getAppStats(-1) // -1 -> From yesterday till today

        var count = 0
        val limit = data.size

        fun updateViews()
        {
            if (count<limit) {
                output.text = data[count].appMahConsumed.toString()
                iconIV.setImageDrawable(data[count].appIcon)

                count += 1
            }
        }

        updateViews()

        nextButton.setOnClickListener {
            updateViews()
        }



    }

    fun requestUsageStatsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        context.startActivity(intent)
    }



    override fun onResume() {
        super.onResume()
        // When user returns from settings re-check and act accordingly
        if (hasUsageStatsPermission(this)) {
            Toast.makeText(this, "Usage access granted", Toast.LENGTH_SHORT).show()
            // proceed with functionality that needs usage stats
        } else {
            // still not granted (user cancelled or didn't enable). handle as needed
        }
    }

    private fun requestUsageAccessIfNeeded() {
        if (!hasUsageStatsPermission(this)) {
            // Open the Usage access settings page so the user can grant permission
            // Try to open the package-specific page first (better UX)
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                // direct user to your app's page if supported
                data = Uri.parse("package:$packageName")
            }
            // startActivityForResult is optional — you can just startActivity(intent)
            startActivity(intent)
            // If you prefer, use startActivityForResult(intent, REQUEST_USAGE_ACCESS)
        } else {
            // already granted — continue
        }
    }

    private fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // checkOpNoThrow is still fine for most versions; newer APIs also exist
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
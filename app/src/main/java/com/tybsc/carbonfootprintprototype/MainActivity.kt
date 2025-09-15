package com.tybsc.carbonfootprintprototype

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.inc
import kotlin.text.compareTo
import kotlin.text.get


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




}
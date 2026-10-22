package com.equifa

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(36, 48, 36, 36)
        }

        val title = TextView(this).apply {
            text = "EquiFa\nمترجم کمکی Equilab"
            textSize = 25f
            gravity = Gravity.CENTER
        }
        val info = TextView(this).apply {
            text = "\nاین نسخهٔ آزمایشی، واژه‌ها و عبارت‌های رایج را به‌صورت آفلاین ترجمه می‌کند.\n\nترجمهٔ کامل و هوشمند همهٔ صفحه‌ها هنوز در این نسخه وجود ندارد. فقط متنی قابل ترجمه است که اندروید در دسترس‌پذیری نشان دهد."
            textSize = 17f
            setPadding(0, 24, 0, 24)
        }
        val button = Button(this).apply {
            text = "فعال‌کردن سرویس EquiFa"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
        layout.addView(title)
        layout.addView(info)
        layout.addView(button)
        setContentView(layout)
    }
}

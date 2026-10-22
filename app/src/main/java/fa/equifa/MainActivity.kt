package fa.equifa

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

        val title = TextView(this).apply {
            text = "EquiFa — ترجمهٔ کمکی Equilab"
            textSize = 22f
            gravity = Gravity.CENTER
            setPadding(24, 28, 24, 16)
        }
        val info = TextView(this).apply {
            text = "این نسخهٔ آزمایشی متن‌های قابل‌دسترسی را شناسایی می‌کند و ترجمه را در پنجره‌ای شناور نشان می‌دهد. ترجمهٔ همهٔ بخش‌ها تضمین‌شده نیست."
            textSize = 16f
            setPadding(24, 16, 24, 16)
        }
        val accessibility = Button(this).apply {
            text = "فعال‌کردن سرویس دسترس‌پذیری"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(20, 20, 20, 20)
            addView(title)
            addView(info)
            addView(accessibility)
        }
        setContentView(layout)
    }
}

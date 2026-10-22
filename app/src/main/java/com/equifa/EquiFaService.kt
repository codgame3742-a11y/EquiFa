package com.equifa

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView

class EquiFaService : AccessibilityService() {
    private lateinit var windowManager: WindowManager
    private var overlay: TextView? = null
    private var lastSource = ""

    private val dictionary = linkedMapOf(
        "Start" to "شروع", "Home" to "خانه", "Profile" to "نمایه",
        "Settings" to "تنظیمات", "History" to "تاریخچه", "Activity" to "فعالیت",
        "Training" to "تمرین", "Ride" to "سواری", "Rides" to "سواری‌ها",
        "Horse" to "اسب", "Horses" to "اسب‌ها", "Distance" to "مسافت",
        "Duration" to "مدت", "Speed" to "سرعت", "Average speed" to "سرعت متوسط",
        "Map" to "نقشه", "Save" to "ذخیره", "Cancel" to "لغو",
        "Search" to "جست‌وجو", "Share" to "اشتراک‌گذاری", "Continue" to "ادامه",
        "Next" to "بعدی", "Back" to "بازگشت", "Done" to "تمام",
        "Time" to "زمان", "Today" to "امروز", "Week" to "هفته",
        "Month" to "ماه", "Calendar" to "تقویم", "Statistics" to "آمار",
        "Settings and privacy" to "تنظیمات و حریم خصوصی",
        "Outdoor" to "فضای باز", "Indoor" to "فضای بسته",
        "Walk" to "قدم", "Trot" to "یورتمه", "Canter" to "چهارنعل",
        "Gallop" to "تاخت", "Pause" to "مکث", "Resume" to "ادامه",
        "End ride" to "پایان سواری", "New ride" to "سواری جدید",
        "No data" to "داده‌ای وجود ندارد", "Loading" to "در حال بارگذاری"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val view = TextView(this).apply {
            text = "EquiFa آماده است"
            textSize = 15f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xDD244A38.toInt())
            setPadding(22, 14, 22, 14)
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP }
        windowManager.addView(view, params)
        overlay = view
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString().orEmpty()
        // Equilab package names can vary; hide the overlay when another app is in front.
        if (!packageName.contains("equilab", ignoreCase = true)) {
            overlay?.visibility = android.view.View.GONE
            return
        }
        overlay?.visibility = android.view.View.VISIBLE

        val root = rootInActiveWindow ?: return
        val texts = mutableListOf<String>()
        collectText(root, texts)
        root.recycle()

        val source = texts.map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(45)
            .joinToString(" · ")
        if (source.isBlank() || source == lastSource) return
        lastSource = source

        val translated = dictionary.entries.fold(source) { text, entry ->
            text.replace(entry.key, entry.value, ignoreCase = true)
        }
        overlay?.text = if (translated == source) {
            "EquiFa: واژهٔ شناخته‌شده‌ای در این صفحه پیدا نشد."
        } else {
            translated.take(1800)
        }
    }

    private fun collectText(node: AccessibilityNodeInfo, output: MutableList<String>) {
        node.text?.toString()?.let { if (it.isNotBlank()) output.add(it) }
        node.contentDescription?.toString()?.let { if (it.isNotBlank()) output.add(it) }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectText(child, output)
            child.recycle()
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        overlay?.let {
            if (it.isAttachedToWindow) windowManager.removeView(it)
        }
        overlay = null
        super.onDestroy()
    }
}

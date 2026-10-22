package fa.equifa

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.concurrent.atomic.AtomicBoolean

class EquiFaService : AccessibilityService() {
    private var translator: Translator? = null
    private var windowManager: WindowManager? = null
    private var overlay: TextView? = null
    private val translating = AtomicBoolean(false)
    private var lastSource = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(com.google.mlkit.nl.translate.TranslateLanguage.ENGLISH)
            .setTargetLanguage(com.google.mlkit.nl.translate.TranslateLanguage.PERSIAN)
            .build()
        translator = Translation.getClient(options)
        translator?.downloadModelIfNeeded()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || translating.get()) return
        val packageName = event.packageName?.toString() ?: return
        if (packageName == this.packageName) return
        val root = rootInActiveWindow ?: return
        val collected = StringBuilder()
        collectText(root, collected)
        val source = collected.toString().trim().take(2500)
        if (source.isBlank() || source == lastSource) return
        lastSource = source
        val client = translator ?: return
        translating.set(true)
        client.translate(source)
            .addOnSuccessListener { translated -> showOverlay(translated) }
            .addOnFailureListener { }
            .addOnCompleteListener { translating.set(false) }
    }

    private fun collectText(node: AccessibilityNodeInfo?, out: StringBuilder) {
        if (node == null || out.length >= 2500) return
        val text = node.text?.toString()?.trim().orEmpty()
        if (text.isNotEmpty() && out.length + text.length < 2500) {
            if (out.isNotEmpty()) out.append('\n')
            out.append(text)
        }
        for (i in 0 until node.childCount) collectText(node.getChild(i), out)
    }

    private fun showOverlay(text: String) {
        if (text.isBlank()) return
        runOnUiThread {
            try {
                val view = overlay ?: TextView(this).apply {
                    textSize = 14f
                    setTextColor(0xFFFFFFFF.toInt())
                    setBackgroundColor(0xDD202020.toInt())
                    setPadding(20, 16, 20, 16)
                    textDirection = TextView.TEXT_DIRECTION_FIRST_STRONG_RTL
                    overlay = this
                    val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                    else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
                    val params = WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        type,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        PixelFormat.TRANSLUCENT
                    ).apply {
                        gravity = Gravity.BOTTOM
                    }
                    windowManager?.addView(this, params)
                }
                view.text = "EquiFa • ترجمهٔ کمکی\n$text"
            } catch (_: Exception) { }
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        try { overlay?.let { windowManager?.removeView(it) } } catch (_: Exception) { }
        translator?.close()
        super.onDestroy()
    }
}

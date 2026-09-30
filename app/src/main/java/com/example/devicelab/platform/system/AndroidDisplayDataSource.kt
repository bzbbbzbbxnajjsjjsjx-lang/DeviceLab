package com.example.devicelab.platform.system

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import com.example.devicelab.domain.model.DisplayBasicSpecs
import com.example.devicelab.domain.repository.DisplayDataSource

class AndroidDisplayDataSource(
    private val context: Context
) : DisplayDataSource {

    @Suppress("DEPRECATION")
    override fun getDisplaySpecs(): DisplayBasicSpecs {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                context.display
            } catch (_: Exception) {
                windowManager?.defaultDisplay
            }
        } else {
            windowManager?.defaultDisplay
        }

        val metrics = context.resources.displayMetrics

        val width: Int
        val height: Int
        val refreshRate: Float

        if (display != null) {
            val mode = display.mode
            width = mode.physicalWidth
            height = mode.physicalHeight
            refreshRate = mode.refreshRate
        } else {
            width = metrics.widthPixels
            height = metrics.heightPixels
            refreshRate = 60.0f
        }

        return DisplayBasicSpecs(
            widthPixels = width,
            heightPixels = height,
            densityDpi = metrics.densityDpi,
            densityScale = metrics.density,
            refreshRateHz = refreshRate
        )
    }
}

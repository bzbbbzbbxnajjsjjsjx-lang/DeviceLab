package com.example.devicelab.platform.system

import android.content.Context
import android.os.Build
import android.view.Display
import android.view.WindowManager
import com.example.devicelab.domain.model.DisplayBasicSpecs
import com.example.devicelab.domain.model.DisplaySnapshot
import com.example.devicelab.domain.repository.DisplayDataSource

class AndroidDisplayDataSource(
    private val context: Context
) : DisplayDataSource {

    override fun getDisplaySpecs(): DisplayBasicSpecs {
        val snapshot = getDisplaySnapshot()
        return DisplayBasicSpecs(
            widthPixels = snapshot.widthPixels,
            heightPixels = snapshot.heightPixels,
            densityDpi = snapshot.densityDpi,
            densityScale = snapshot.densityScale,
            refreshRateHz = snapshot.currentRefreshRateHz
        )
    }

    @Suppress("DEPRECATION")
    override fun getDisplaySnapshot(): DisplaySnapshot {
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
        val supportedRates: List<Float>
        val isHdr: Boolean
        val isWideColor: Boolean

        if (display != null) {
            val mode = display.mode
            width = mode.physicalWidth
            height = mode.physicalHeight
            refreshRate = mode.refreshRate
            supportedRates = display.supportedModes
                .map { it.refreshRate }
                .distinct()
                .sorted()

            isHdr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                display.isHdr
            } else {
                false
            }

            isWideColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                display.isWideColorGamut
            } else {
                false
            }
        } else {
            width = metrics.widthPixels
            height = metrics.heightPixels
            refreshRate = 60.0f
            supportedRates = listOf(60.0f)
            isHdr = false
            isWideColor = false
        }

        return DisplaySnapshot(
            widthPixels = width,
            heightPixels = height,
            densityDpi = metrics.densityDpi,
            densityScale = metrics.density,
            currentRefreshRateHz = refreshRate,
            supportedRefreshRates = supportedRates,
            isHdr = isHdr,
            isWideColorGamut = isWideColor
        )
    }
}

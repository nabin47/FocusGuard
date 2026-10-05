package com.focusguard.presentation.util

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val ICON_SIZE_PX = 96
private val iconCache = LruCache<String, ImageBitmap>(120)

/** Loads an app's launcher icon off the main thread, cached across screens. */
@Composable
fun rememberAppIcon(packageName: String): State<ImageBitmap?> {
    val context = LocalContext.current
    return produceState(initialValue = iconCache.get(packageName), packageName) {
        if (value == null) {
            value = withContext(Dispatchers.IO) { loadIcon(context, packageName) }
        }
    }
}

private fun loadIcon(context: Context, packageName: String): ImageBitmap? = try {
    val drawable = context.packageManager.getApplicationIcon(packageName)
    val bitmap = Bitmap.createBitmap(ICON_SIZE_PX, ICON_SIZE_PX, Bitmap.Config.ARGB_8888)
    drawable.setBounds(0, 0, ICON_SIZE_PX, ICON_SIZE_PX)
    drawable.draw(Canvas(bitmap))
    bitmap.asImageBitmap().also { iconCache.put(packageName, it) }
} catch (e: PackageManager.NameNotFoundException) {
    null
}

/** Human readable label for an installed package, falling back to the package name. */
fun appLabel(context: Context, packageName: String): String = try {
    val pm = context.packageManager
    pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
} catch (e: PackageManager.NameNotFoundException) {
    packageName
}

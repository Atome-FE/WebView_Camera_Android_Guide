package com.pintar.pintar_andorid_guide

import android.content.res.Resources


fun Int.toDP(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()


fun Float.toDP(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
fun Float.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()


fun Double.toDP(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
fun Double.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
package com.pintar.pintar_andorid_guide

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import com.journeyapps.barcodescanner.BarcodeView
import com.pintar.pintar_andorid_guide.ViewfinderViewEx

class CustomBarCodeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : BarcodeView(context, attrs) {

    override fun getPreviewFramingRect(): Rect {
        return ViewfinderViewEx.getFramingRect();
    }
}
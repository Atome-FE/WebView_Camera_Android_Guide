package com.pintar.pintar_andorid_guide

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory

class QrScanActivity : AppCompatActivity(), BarcodeCallback {

    private var barcodeView: BarcodeView? = null
    private var viewfinderView: ViewfinderViewEx? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scan)

        barcodeView = findViewById(R.id.barcode_view)
        viewfinderView = findViewById(R.id.viewfinder_view)

        val formats: Collection<BarcodeFormat> = listOf(BarcodeFormat.QR_CODE)
        barcodeView?.decoderFactory = DefaultDecoderFactory(formats)
        barcodeView?.decodeSingle(this)

    }


    override fun barcodeResult(result: BarcodeResult?) {
        val intent = Intent(this@QrScanActivity, WebActivity::class.java)
        intent.putExtra("url", result?.text ?: "")
        startActivity(intent)
        finish()
    }


    override fun onResume() {
        super.onResume()
        barcodeView?.resume()
        viewfinderView?.drawViewfinder()
    }

    override fun onPause() {
        super.onPause()
        barcodeView?.pause()
        viewfinderView?.onPause()
    }

    override fun onDestroy() {
        barcodeView?.stopDecoding()
        super.onDestroy()
    }
}
package com.pintar.pintar_andorid_guide

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissionLaunch =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    startActivity(Intent(this, QrScanActivity::class.java))
                } else {
                    Toast.makeText(
                        this, "open failed, pls open camera permission", Toast.LENGTH_SHORT
                    ).show()
                }
            }

        val btnOpenWeb = findViewById<Button>(R.id.btn_open_web)
        val btnOpenQr = findViewById<Button>(R.id.btn_open_qr)
        val et = findViewById<EditText>(R.id.et)


        btnOpenWeb.setOnClickListener {
            val intent = Intent(this, WebActivity::class.java)
            intent.putExtra("url", et.text.toString().trim())
            startActivity(intent)
        }

        btnOpenQr.setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                startActivity(Intent(this, QrScanActivity::class.java))
            } else {
                permissionLaunch.launch(android.Manifest.permission.CAMERA)
            }
        }


    }
}
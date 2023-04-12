package com.pintar.pintar_andorid_guide

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File


class WebActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var permissionLaunch: ActivityResultLauncher<String>? = null
    private var takePictureLaunch: ActivityResultLauncher<Intent>? = null
    private var takePictureCallback: ValueCallback<Array<Uri>>? = null

    private var permissionRequest: PermissionRequest? = null
    private var takeCameraPermissionLaunch: ActivityResultLauncher<String>? = null
    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        webView = findViewById(R.id.web)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        initWebView()

        val url = intent.getStringExtra("url") ?: ""
        webView.loadUrl(url)

        permissionLaunch =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                permissionRequest?.let {
                    if (granted) {
                        it.grant(it.resources)
                    } else {
                        it.deny()
                    }
                }
            }

        takeCameraPermissionLaunch =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    openCamera()
                } else {
                    Toast.makeText(
                        this, "open failed, pls open camera permission", Toast.LENGTH_SHORT
                    ).show()
                }
            }


        takePictureLaunch =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                imageUri?.let { uri ->
                    takePictureCallback?.onReceiveValue(arrayOf(uri))
                }
            }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })
    }

    private fun initWebView() {
        webView.webChromeClient = mWebChromeClient
        webView.webViewClient = mWebViewClient
        webView.settings.useWideViewPort = false
        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.domStorageEnabled = true
        webView.settings.textZoom = 100

        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
    }

    private var mWebViewClient: WebViewClient = object : WebViewClient() {

    }

    private var mWebChromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest?) {
            //for getUserMedia
            // IMPORTANT: Check host and adapt filter to allow camera access
            // e.g. if (request?.origin?.host == "xxx") {...}
            permissionRequest = request
            if (ContextCompat.checkSelfPermission(
                    this@WebActivity, android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                request?.grant(request.resources)
            } else {
                permissionLaunch?.launch(android.Manifest.permission.CAMERA)
            }
        }

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            //for input file
            takePictureCallback = filePathCallback
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                // IMPORTANT: Check host and adapt filter to allow camera access
                openCamera()
            } else {
                takeCameraPermissionLaunch?.launch(android.Manifest.permission.CAMERA)
            }

            return true
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val outputImage = File(cacheDir, "TEST_${System.currentTimeMillis()}.jpg");
        imageUri = FileProvider.getUriForFile(
            this@WebActivity, "${application.packageName}.fileProvider", outputImage
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        takePictureLaunch?.launch(intent)
    }

}
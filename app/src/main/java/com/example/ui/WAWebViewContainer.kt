package com.example.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.util.WebViewUtils

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WAWebViewContainer(
    onWebViewCreated: (WebView) -> Unit,
    onProgressChanged: (Float) -> Unit,
    onLoadingStateChanged: (Boolean) -> Unit,
    onErrorOccurred: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // Setup persistent cookies & third-party cookies
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(this, true)
            }

            // WebSettings configuration
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT

                // Desktop User-Agent spoofing to bypass WhatsApp mobile redirect
                userAgentString = WebViewUtils.DESKTOP_CHROME_USER_AGENT

                // Viewport & Zoom settings for desktop page scaling
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false

                // Permissions & Storage
                allowFileAccess = true
                allowContentAccess = true
                mediaPlaybackRequiresUserGesture = false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                }
            }

            // WebChromeClient for page loading progress & media permissions
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    onProgressChanged(newProgress / 100f)
                }

                override fun onPermissionRequest(request: PermissionRequest?) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        try {
                            request?.grant(request.resources)
                        } catch (_: Exception) {
                            super.onPermissionRequest(request)
                        }
                    } else {
                        super.onPermissionRequest(request)
                    }
                }
            }

            // WebViewClient for URL routing, errors, and session cookie flushing
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    onLoadingStateChanged(true)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    onLoadingStateChanged(false)
                    // Persist session cookies to disk immediately
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        CookieManager.getInstance().flush()
                    }
                }

                // API 24+ URL override
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString() ?: return false
                    return handleUrl(view, url)
                }

                // API 21-23 URL override
                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url == null) return false
                    return handleUrl(view, url)
                }

                private fun handleUrl(view: WebView?, url: String): Boolean {
                    // WhatsApp Web domains should stay inside WebView
                    if (url.startsWith("https://web.whatsapp.com") ||
                        url.startsWith("https://whatsapp.com") ||
                        url.startsWith("https://www.whatsapp.com")
                    ) {
                        return false
                    }

                    // External URLs (mailto:, tel:, intent:) handled by system
                    return try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        true
                    } catch (_: Exception) {
                        false
                    }
                }

                // API 23+ Error Handling
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (request?.isForMainFrame == true) {
                            val description = error?.description?.toString() ?: "Connection error"
                            onLoadingStateChanged(false)
                            onErrorOccurred(description)
                        }
                    }
                }

                // API 21-22 Error Handling
                @Deprecated("Deprecated in Java")
                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    onLoadingStateChanged(false)
                    onErrorOccurred(description ?: "Network connection failed ($errorCode)")
                }
            }

            // Initial load of WhatsApp Web
            loadUrl(WebViewUtils.WHATSAPP_WEB_URL)
        }
    }

    DisposableEffect(webView) {
        onWebViewCreated(webView)
        onDispose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().flush()
            }
            webView.destroy()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize()
    )
}

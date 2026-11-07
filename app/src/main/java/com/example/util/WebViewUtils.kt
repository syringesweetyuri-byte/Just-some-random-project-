package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView

object WebViewUtils {
    const val WHATSAPP_WEB_URL = "https://web.whatsapp.com/"

    // Modern Desktop Chrome User-Agent on Windows 10
    // WhatsApp Web strictly verifies the User-Agent to avoid serving the mobile redirect page.
    const val DESKTOP_CHROME_USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"

    data class WebViewDiagnosticInfo(
        val androidVersion: String,
        val apiLevel: Int,
        val deviceModel: String,
        val webViewPackageName: String,
        val webViewVersionName: String,
        val isModernWebView: Boolean,
        val compatibilityNote: String
    )

    fun getDiagnostics(context: Context): WebViewDiagnosticInfo {
        val androidVersion = Build.VERSION.RELEASE ?: "5.0"
        val apiLevel = Build.VERSION.SDK_INT
        val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

        var pkgName = "com.google.android.webview"
        var versionName = "Unknown"

        try {
            val pm = context.packageManager
            val packages = listOf(
                "com.google.android.webview",
                "com.android.webview",
                "com.android.chrome",
                "org.bromite.webview"
            )
            for (pkg in packages) {
                try {
                    val info: PackageInfo = pm.getPackageInfo(pkg, 0)
                    pkgName = pkg
                    versionName = info.versionName ?: "Unknown"
                    break
                } catch (_: PackageManager.NameNotFoundException) {
                    // Try next package
                }
            }
        } catch (_: Exception) {
            versionName = "System Default"
        }

        // Parse major version number if available
        val majorVersion = versionName.split(".").firstOrNull()?.toIntOrNull() ?: 0
        val isModern = majorVersion >= 70 || (majorVersion == 0 && apiLevel >= 26)

        val note = if (isModern) {
            "Your WebView appears to be up-to-date and supports modern web standards."
        } else {
            "Android 5.0 (Lollipop) shipped with Chromium 37. WhatsApp Web requires modern JavaScript (ES6+), WebCrypto, and IndexedDB. If the page does not render, please update 'Android System WebView' via Google Play."
        }

        return WebViewDiagnosticInfo(
            androidVersion = androidVersion,
            apiLevel = apiLevel,
            deviceModel = deviceModel,
            webViewPackageName = pkgName,
            webViewVersionName = versionName,
            isModernWebView = isModern,
            compatibilityNote = note
        )
    }

    fun openPlayStoreForWebView(context: Context) {
        val webViewPackage = "com.google.android.webview"
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$webViewPackage"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$webViewPackage")
            )
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)
        }
    }

    fun clearAllWebData(webView: WebView?, onCompleted: () -> Unit) {
        try {
            // 1. Clear Cookies
            val cookieManager = CookieManager.getInstance()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.removeAllCookies {
                    cookieManager.flush()
                }
                cookieManager.removeSessionCookies {
                    cookieManager.flush()
                }
            } else {
                @Suppress("DEPRECATION")
                cookieManager.removeAllCookie()
            }

            // 2. Clear HTML5 Web Storage (localStorage, sessionStorage)
            WebStorage.getInstance().deleteAllData()

            // 3. Clear WebView internal cache and history
            webView?.clearCache(true)
            webView?.clearHistory()
            webView?.clearFormData()
            webView?.clearSslPreferences()
        } catch (_: Exception) {
            // Ignore clearing failures
        } finally {
            onCompleted()
        }
    }
}

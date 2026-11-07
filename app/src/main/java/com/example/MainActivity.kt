package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ui.WAWebViewContainer
import com.example.ui.components.ClearDataDialog
import com.example.ui.components.DiagnosticsDialog
import com.example.ui.components.ErrorView
import com.example.ui.components.WATopBar
import com.example.ui.theme.WAClassicTheme
import com.example.ui.theme.WaGreen
import com.example.util.WebViewUtils
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var activeWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WAClassicTheme {
                WAClassicApp(
                    onRegisterWebView = { webView ->
                        activeWebView = webView
                    },
                    onExitApp = {
                        finish()
                    }
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().flush()
        }
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().flush()
        }
        activeWebView = null
        super.onDestroy()
    }
}

@Composable
fun WAClassicApp(
    onRegisterWebView: (WebView) -> Unit,
    onExitApp: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0f) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isFullscreen by remember { mutableStateOf(false) }

    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    var lastBackPressTimestamp by remember { mutableLongStateOf(0L) }

    // Request audio permission for voice messaging if needed
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Microphone permission granted for voice notes")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Hardware Back Button Handling
    BackHandler {
        val currentWebView = webViewInstance
        if (isFullscreen) {
            // Exit fullscreen on first back press if fullscreen is active
            isFullscreen = false
        } else if (currentWebView != null && currentWebView.canGoBack()) {
            currentWebView.goBack()
        } else {
            val now = System.currentTimeMillis()
            if (now - lastBackPressTimestamp < 2000L) {
                onExitApp()
            } else {
                lastBackPressTimestamp = now
                Toast.makeText(context, "Press back again to exit WA Classic", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            if (!isFullscreen) {
                WATopBar(
                    isLoading = isLoading,
                    progress = progress,
                    isFullscreen = false,
                    onRefresh = {
                        hasError = false
                        webViewInstance?.reload()
                    },
                    onToggleFullscreen = {
                        isFullscreen = true
                    },
                    onOpenDiagnostics = {
                        showDiagnosticsDialog = true
                    },
                    onClearDataClick = {
                        showClearDataDialog = true
                    },
                    onUpdateWebViewClick = {
                        WebViewUtils.openPlayStoreForWebView(context)
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (hasError) {
                ErrorView(
                    errorMessage = errorMessage,
                    onRetry = {
                        hasError = false
                        isLoading = true
                        webViewInstance?.loadUrl(WebViewUtils.WHATSAPP_WEB_URL)
                    },
                    onOpenDiagnostics = {
                        showDiagnosticsDialog = true
                    }
                )
            } else {
                WAWebViewContainer(
                    onWebViewCreated = { webView ->
                        webViewInstance = webView
                        onRegisterWebView(webView)
                    },
                    onProgressChanged = { newProgress ->
                        progress = newProgress
                    },
                    onLoadingStateChanged = { loading ->
                        isLoading = loading
                        if (!loading) {
                            hasError = false
                        }
                    },
                    onErrorOccurred = { desc ->
                        hasError = true
                        errorMessage = desc
                    }
                )
            }

            // Floating control to exit fullscreen mode
            AnimatedVisibility(
                visible = isFullscreen,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                FloatingActionButton(
                    onClick = { isFullscreen = false },
                    containerColor = WaGreen,
                    contentColor = androidx.compose.ui.graphics.Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("exit_fullscreen_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.FullscreenExit,
                        contentDescription = "Exit Fullscreen",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }

    // Dialogs
    if (showClearDataDialog) {
        ClearDataDialog(
            onDismiss = { showClearDataDialog = false },
            onConfirmClear = {
                showClearDataDialog = false
                WebViewUtils.clearAllWebData(webViewInstance) {
                    hasError = false
                    isLoading = true
                    webViewInstance?.loadUrl(WebViewUtils.WHATSAPP_WEB_URL)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Website data and session cookies cleared")
                    }
                }
            }
        )
    }

    if (showDiagnosticsDialog) {
        val diagnostics = remember { WebViewUtils.getDiagnostics(context) }
        DiagnosticsDialog(
            diagnostics = diagnostics,
            onDismiss = { showDiagnosticsDialog = false },
            onOpenPlayStore = {
                showDiagnosticsDialog = false
                WebViewUtils.openPlayStoreForWebView(context)
            }
        )
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WaGreen
import com.example.ui.theme.WaTealDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WATopBar(
    isLoading: Boolean,
    progress: Float,
    isFullscreen: Boolean,
    onRefresh: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onClearDataClick: () -> Unit,
    onUpdateWebViewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Secure Connection",
                        tint = WaGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "WA Classic",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "web.whatsapp.com",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurface
            ),
            actions = {
                // Refresh Button
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.testTag("refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Page",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Fullscreen Toggle
                IconButton(
                    onClick = onToggleFullscreen,
                    modifier = Modifier.testTag("fullscreen_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = if (isFullscreen) "Exit Fullscreen" else "Enter Fullscreen",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Overflow Menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Device & Compatibility Info") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = WaGreen
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onOpenDiagnostics()
                            },
                            modifier = Modifier.testTag("menu_diagnostics")
                        )

                        DropdownMenuItem(
                            text = { Text("Update Android System WebView") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.OpenInBrowser,
                                    contentDescription = null,
                                    tint = WaGreen
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onUpdateWebViewClick()
                            },
                            modifier = Modifier.testTag("menu_update_webview")
                        )

                        DropdownMenuItem(
                            text = { Text("Clear Website Data (Logout)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onClearDataClick()
                            },
                            modifier = Modifier.testTag("menu_clear_data")
                        )
                    }
                }
            }
        )

        // Web loading progress indicator
        if (isLoading && progress < 1f) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = WaGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        } else {
            Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)))
        }
    }
}

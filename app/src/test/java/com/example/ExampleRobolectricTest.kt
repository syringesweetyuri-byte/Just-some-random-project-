package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.WebViewUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("WA Classic", appName)
  }

  @Test
  fun `verify desktop user agent and url`() {
    assertEquals("https://web.whatsapp.com/", WebViewUtils.WHATSAPP_WEB_URL)
    assertTrue(WebViewUtils.DESKTOP_CHROME_USER_AGENT.contains("Windows NT 10.0"))
    assertTrue(WebViewUtils.DESKTOP_CHROME_USER_AGENT.contains("Chrome/"))
  }

  @Test
  fun `verify diagnostics report generation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val diag = WebViewUtils.getDiagnostics(context)
    assertTrue(diag.androidVersion.isNotEmpty())
    assertTrue(diag.deviceModel.isNotEmpty())
    assertTrue(diag.compatibilityNote.isNotEmpty())
  }
}

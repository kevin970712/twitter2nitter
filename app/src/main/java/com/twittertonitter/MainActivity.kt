package com.twittertonitter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.twittertonitter.ui.theme.Twitter2NitterTheme

private const val PREF_NAME = "app_prefs"
private const val KEY_INSTANCE = "nitter_instance"
private const val DEFAULT_INSTANCE = "nitter.net"
private val TWITTER_REGEX = Regex("https?://(www\\.)?(twitter\\.com|x\\.com)/\\S+")

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            transformToNitterUrl(intent.getStringExtra(Intent.EXTRA_TEXT))?.let { url ->
                launchCustomTab(url)
                finish()
                return
            }
        }

        setContent {
            Twitter2NitterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ConfigScreen()
                }
            }
        }
    }

    private fun transformToNitterUrl(text: String?): String? {
        val domain = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_INSTANCE, DEFAULT_INSTANCE)
            .let { if (it.isNullOrBlank()) DEFAULT_INSTANCE else it }

        return text?.let { TWITTER_REGEX.find(it)?.value }?.let { url ->
            val cleanUrl = if (url.contains("?")) url.substringBefore("?") else url
            cleanUrl.replace(Regex("twitter\\.com|x\\.com"), domain)
        }
    }

    private fun launchCustomTab(url: String) {
        CustomTabsIntent.Builder().build().launchUrl(this, Uri.parse(url))
    }
}

@Composable
fun ConfigScreen() {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val prefs = remember { context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) }

    var instanceDomain by remember {
        mutableStateOf(prefs.getString(KEY_INSTANCE, DEFAULT_INSTANCE) ?: DEFAULT_INSTANCE)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Twitter to Nitter", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Redirects X/Twitter links to:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = instanceDomain,
            onValueChange = { input ->
                instanceDomain = input
                val clean = input.replace(Regex("https?://|/"), "").trim()
                prefs.edit().putString(KEY_INSTANCE, clean).apply()
            },
            label = { Text("Nitter Instance") },
            placeholder = { Text(DEFAULT_INSTANCE) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
    }
}
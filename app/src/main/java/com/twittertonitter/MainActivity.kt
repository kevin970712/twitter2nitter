package com.twittertonitter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.browser.customtabs.CustomTabsIntent

private const val PREF_NAME = "app_prefs"
private const val KEY_INSTANCE = "nitter_instance"
private const val DEFAULT_INSTANCE = "nitter.net"
private val TWITTER_REGEX = Regex("https?://(www\\.)?(twitter\\.com|x\\.com)/\\S+")

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle Share Intent
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            transformUrl(intent.getStringExtra(Intent.EXTRA_TEXT))?.let { url ->
                CustomTabsIntent.Builder().build().launchUrl(this, Uri.parse(url))
                finish()
                return
            }
        }

        // Setup UI
        setContentView(R.layout.activity_main)
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val input = findViewById<EditText>(R.id.instanceInput)

        val currentInstance = prefs.getString(KEY_INSTANCE, DEFAULT_INSTANCE) ?: DEFAULT_INSTANCE
        input.setText(currentInstance)

        input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val clean = s.toString().replace(Regex("https?://|/"), "").trim()
                prefs.edit().putString(KEY_INSTANCE, clean).apply()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun transformUrl(text: String?): String? {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val domain = prefs.getString(KEY_INSTANCE, DEFAULT_INSTANCE).let {
            if (it.isNullOrBlank()) DEFAULT_INSTANCE else it
        }

        return text?.let { TWITTER_REGEX.find(it)?.value }?.let { url ->
            val cleanUrl = if (url.contains("?")) url.substringBefore("?") else url
            cleanUrl.replace(Regex("twitter\\.com|x\\.com"), domain)
        }
    }
}
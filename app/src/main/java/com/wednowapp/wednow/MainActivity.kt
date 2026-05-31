package com.wednowapp.wednow

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.wednowapp.wednow.core.navigation.WedNowNavGraph
import com.wednowapp.wednow.ui.theme.WedNowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // dismiss the system splash immediately; our Compose splash takes over
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val deepLinkWeddingId = extractDeepLinkWeddingId(intent)
        setContent {
            WedNowTheme {
                val navController = rememberNavController()
                WedNowNavGraph(
                    navController = navController,
                    deepLinkWeddingId = deepLinkWeddingId,
                )
            }
        }
    }

    /**
     * Called when a new deep-link intent arrives while the app is already running
     * (possible because the activity is declared singleTop in the manifest).
     * We recreate so that [onCreate] processes the new URI cleanly.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (extractDeepLinkWeddingId(intent) != null) {
            recreate()
        }
    }

    /**
     * Extracts the wedding ID from a deep-link URI, or returns null if the intent
     * does not carry a recognised deep link.
     *
     * Supported formats:
     *   • wednow://join/{weddingId}          – custom-scheme link
     *   • https://wednow.app/join/{weddingId} – verified App Link
     */
    private fun extractDeepLinkWeddingId(intent: Intent?): String? {
        val uri: Uri = intent?.data ?: return null
        return when {
            // wednow://join/{weddingId}
            uri.scheme == "wednow" && uri.host == "join" ->
                uri.lastPathSegment?.takeIf { it.isNotBlank() }

            // https://wednow.app/join/{weddingId}
            uri.scheme == "https" &&
                    (uri.host == "wednow.app" || uri.host == "www.wednow.app") -> {
                val segments = uri.pathSegments
                if (segments.size >= 2 && segments[0] == "join") segments[1] else null
            }

            else -> null
        }
    }
}

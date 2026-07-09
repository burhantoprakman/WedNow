package com.maritsa.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.maritsa.app.core.navigation.WedNowNavGraph
import com.maritsa.app.ui.theme.WedNowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
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
     * Extracts the join code (weddingId, short code, or per-group invite token — all
     * handled generically by JoinWeddingUseCase) from a deep-link URI, or returns null
     * if the intent does not carry a recognised deep link.
     *
     * Supported formats:
     *   • maritsa://join/{weddingId}            – custom-scheme link
     *   • maritsa://invite/{inviteToken}         – custom-scheme link
     *   • https://maritsa.app/join/{weddingId}   – verified App Link
     *   • https://maritsa.app/invite/{inviteToken} – verified App Link
     */
    private fun extractDeepLinkWeddingId(intent: Intent?): String? {
        val uri: Uri = intent?.data ?: return null
        return when {
            // maritsa://join/{weddingId} or maritsa://invite/{inviteToken}
            uri.scheme == "maritsa" && (uri.host == "join" || uri.host == "invite") ->
                uri.lastPathSegment?.takeIf { it.isNotBlank() }

            // https://maritsa.app/join/{weddingId} or /invite/{inviteToken}
            uri.scheme == "https" &&
                    (uri.host == "maritsa.app" || uri.host == "www.maritsa.app") -> {
                val segments = uri.pathSegments
                if (segments.size >= 2 && (segments[0] == "join" || segments[0] == "invite")) {
                    segments[1]
                } else null
            }

            else -> null
        }
    }
}

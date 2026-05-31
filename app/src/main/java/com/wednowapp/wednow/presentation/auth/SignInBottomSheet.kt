package com.wednowapp.wednow.presentation.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wednowapp.wednow.R
import com.wednowapp.wednow.ui.theme.Gold
import com.wednowapp.wednow.ui.theme.Ivory
import com.wednowapp.wednow.ui.theme.Spacing
import com.wednowapp.wednow.ui.theme.WarmGray200
import com.wednowapp.wednow.ui.theme.WarmGray400
import com.wednowapp.wednow.ui.theme.WarmGray500
import com.wednowapp.wednow.ui.theme.WarmGray800
import com.wednowapp.wednow.ui.theme.WarmWhite

private val _gfProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)
private val DancingScript = FontFamily(
    Font(GoogleFont("Dancing Script"), _gfProvider, FontWeight.Normal),
    Font(GoogleFont("Dancing Script"), _gfProvider, FontWeight.SemiBold),
)

/**
 * Elegant sign-in bottom sheet shared across all auth-gated actions.
 *
 * @param reason        Context-specific message shown under the title.
 *                      e.g. "Sign in to share photos with everyone."
 * @param onDismiss     Called when user drags the sheet down or taps the scrim.
 * @param onSuccess     Called once the auth state changes to signed-in.
 *                      The caller executes its pending action here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInBottomSheet(
    authViewModel: AuthViewModel,
    reason: String = "Sign in to share photos, leave messages, or help manage this wedding.",
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
) {
    val authState by authViewModel.authState.collectAsState()
    val isLoading by authViewModel.signInLoading.collectAsState()
    val error by authViewModel.signInError.collectAsState()
    val activity = LocalContext.current as? Activity
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Auth succeeded while the sheet was open → notify the caller
    LaunchedEffect(authState) {
        if (authState != null) onSuccess()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Ivory,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(WarmGray200)
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(bottom = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(Spacing.md))

            // ── Gold ornament ─────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(Modifier
                    .size(20.dp, 0.5.dp)
                    .background(Gold.copy(alpha = 0.4f)))
                Text("✦", fontSize = 8.sp, color = Gold.copy(alpha = 0.6f))
                Text("♡", fontSize = 12.sp, color = Gold.copy(alpha = 0.70f))
                Text("✦", fontSize = 8.sp, color = Gold.copy(alpha = 0.6f))
                Box(Modifier
                    .size(20.dp, 0.5.dp)
                    .background(Gold.copy(alpha = 0.4f)))
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Welcome",
                style = TextStyle(
                    fontFamily = DancingScript,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 34.sp,
                    color = WarmGray800,
                ),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = FontStyle.Italic,
                ),
                color = WarmGray500,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(Spacing.lg))

            if (isLoading) {
                CircularProgressIndicator(
                    color = Gold,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(Modifier.height(Spacing.lg))
            } else {
                // ── Google button ─────────────────────────────────────────────
                SignInButton(
                    label = "Continue with Google",
                    logoText = "G",
                    logoBackground = Color(0xFF4285F4),
                    buttonBackground = WarmWhite,
                    borderColor = WarmGray200,
                    textColor = WarmGray800,
                    onClick = { activity?.let { authViewModel.signInWithGoogle(it) } },
                )

                Spacer(Modifier.height(Spacing.sm))

                // ── Apple button ──────────────────────────────────────────────
                SignInButton(
                    label = "Continue with Apple",
                    logoText = "",
                    logoBackground = Color.Black,
                    buttonBackground = Color(0xFF1C1C1E),
                    borderColor = Color.Transparent,
                    textColor = Color.White,
                    onClick = { activity?.let { authViewModel.signInWithApple(it) } },
                )

                Spacer(Modifier.height(Spacing.lg))
            }

            // ── Error ─────────────────────────────────────────────────────────
            if (!error.isNullOrBlank()) {
                Text(
                    text = error!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = Spacing.sm),
                )
            }

            // ── Privacy note ──────────────────────────────────────────────────
            Text(
                text = "Your information is only used within this wedding.",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                ),
                color = WarmGray400,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Shared sign-in button ─────────────────────────────────────────────────────

@Composable
private fun SignInButton(
    label: String,
    logoText: String,
    logoBackground: Color,
    buttonBackground: Color,
    borderColor: Color,
    textColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(buttonBackground)
            .border(0.8.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Logo circle
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(logoBackground),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = logoText,
                    style = TextStyle(
                        fontSize = if (logoText == "G") 14.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    ),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 0.3.sp,
                ),
                color = textColor,
            )
        }
    }
}


package com.wednowapp.wednow.presentation.auth

import androidx.activity.compose.LocalActivity
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wednowapp.wednow.R
import com.wednowapp.wednow.ui.theme.Gold
import com.wednowapp.wednow.ui.theme.GoldDeep
import com.wednowapp.wednow.ui.theme.Ivory
import com.wednowapp.wednow.ui.theme.Spacing
import com.wednowapp.wednow.ui.theme.WarmGray100
import com.wednowapp.wednow.ui.theme.WarmGray200
import com.wednowapp.wednow.ui.theme.WarmGray400
import com.wednowapp.wednow.ui.theme.WarmGray500
import com.wednowapp.wednow.ui.theme.WarmGray800
import com.wednowapp.wednow.ui.theme.WarmWhite

private val _acctGfProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)
private val _DancingScriptAcct = FontFamily(
    Font(GoogleFont("Dancing Script"), _acctGfProvider, FontWeight.SemiBold),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountMenuSheet(
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit,
) {
    val authState by authViewModel.authState.collectAsState()
    val isLoading by authViewModel.signInLoading.collectAsState()
    val error by authViewModel.signInError.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val activity = LocalActivity.current

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

            if (authState != null) {
                // ── Authenticated state ───────────────────────────────────────
                // Capture as local val so Kotlin can smart-cast and we avoid repeated
                // !! on a mutable compose state that can't be smart-cast directly.
                val state = authState

                // Avatar circle with initial
                val initial = state?.displayName?.firstOrNull()?.uppercaseChar() ?: '?'
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Gold.copy(alpha = 0.15f))
                        .border(1.5.dp, Gold.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initial.toString(),
                        style = TextStyle(
                            fontFamily = _DancingScriptAcct,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 32.sp,
                            color = GoldDeep,
                        )
                    )
                }

                Spacer(Modifier.height(Spacing.sm))

                Text(
                    text = state?.displayName ?: "Guest",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = WarmGray800,
                    textAlign = TextAlign.Center,
                )

                if (state?.email.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = state?.email.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmGray400,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(Modifier.height(Spacing.lg))

                // Sign Out button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(WarmGray100)
                        .border(0.8.dp, WarmGray200, RoundedCornerShape(14.dp))
                        .clickable {
                            authViewModel.signOut()
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Sign Out",
                        style = MaterialTheme.typography.labelLarge,
                        color = WarmGray800,
                    )
                }
            } else {
                // ── Guest state ───────────────────────────────────────────────

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
                    text = "Sign In",
                    style = TextStyle(
                        fontFamily = _DancingScriptAcct,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 34.sp,
                        color = WarmGray800,
                    ),
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Sign in to upload photos, leave messages, and more.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmGray500,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(Spacing.lg))

                if (isLoading) {
                    CircularProgressIndicator(
                        color = Gold,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(Spacing.lg))
                } else {
                    // Google button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(WarmWhite)
                            .border(0.8.dp, WarmGray200, RoundedCornerShape(14.dp))
                            .clickable { activity?.let { authViewModel.signInWithGoogle(it) } },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4285F4)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "G",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                            Text(
                                "Continue with Google",
                                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.3.sp),
                                color = WarmGray800
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.sm))

                    // Apple button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1C1C1E))
                            .clickable { activity?.let { authViewModel.signInWithApple(it) } },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                            Text(
                                "Continue with Apple",
                                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.3.sp),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.lg))
                }

                if (!error.isNullOrBlank()) {
                    Text(
                        text = error.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = Spacing.sm),
                    )
                }

                Text(
                    text = "Your information is only used within this wedding.",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = WarmGray400,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

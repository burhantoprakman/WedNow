package com.maritsa.app.presentation.auth

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal that makes the Activity-scoped [AuthViewModel] available to
 * any composable in the tree without prop-drilling.
 *
 * Provided in [WedNowNavGraph] via [CompositionLocalProvider].
 */
val LocalAuthViewModel = staticCompositionLocalOf<AuthViewModel> {
    error("LocalAuthViewModel not provided. Wrap your content with WedNowNavGraph.")
}

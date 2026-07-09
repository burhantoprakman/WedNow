package com.maritsa.app.presentation.identity

import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal that provides [IdentityViewModel] to the entire navigation
 * graph without requiring every composable to declare it as a parameter.
 *
 * Provided in  WedNowNavGraph  alongside [LocalAuthViewModel]:
 *
 *   CompositionLocalProvider(
 *       LocalAuthViewModel     provides authViewModel,
 *       LocalIdentityViewModel provides identityViewModel,
 *   ) { NavHost(...) }
 *
 * Usage in any composable:
 *   val identityViewModel = LocalIdentityViewModel.current
 */
val LocalIdentityViewModel = compositionLocalOf<IdentityViewModel> {
    error("LocalIdentityViewModel was accessed outside of WedNowNavGraph")
}

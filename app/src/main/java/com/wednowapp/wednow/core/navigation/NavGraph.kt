package com.wednowapp.wednow.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.wednowapp.wednow.core.session.WeddingSessionManager
import com.wednowapp.wednow.presentation.auth.AuthViewModel
import com.wednowapp.wednow.presentation.auth.LocalAuthViewModel
import com.wednowapp.wednow.presentation.auth.SignInBottomSheet
import com.wednowapp.wednow.presentation.identity.CreateWeddingNavViewModel
import com.wednowapp.wednow.presentation.identity.IdentityViewModel
import com.wednowapp.wednow.presentation.identity.LocalIdentityViewModel
import com.wednowapp.wednow.presentation.broadcast.BroadcastScreen
import com.wednowapp.wednow.presentation.chat.ChatScreen
import com.wednowapp.wednow.presentation.chat.DirectMessageScreen
import com.wednowapp.wednow.presentation.guestbook.GuestbookScreen
import com.wednowapp.wednow.presentation.guestmanagement.GuestManagementScreen
import com.wednowapp.wednow.presentation.guests.GuestListScreen
import com.wednowapp.wednow.presentation.home.HomeScreen
import com.wednowapp.wednow.presentation.notifications.NotificationsScreen
import com.wednowapp.wednow.presentation.onboarding.CreateWeddingScreen
import com.wednowapp.wednow.presentation.onboarding.JoinWeddingScreen
import com.wednowapp.wednow.presentation.onboarding.OnboardingScreen
import com.wednowapp.wednow.presentation.photos.PhotosScreen
import com.wednowapp.wednow.presentation.rsvp.RSVPScreen
import com.wednowapp.wednow.presentation.share.ShareInvitationScreen
import com.wednowapp.wednow.presentation.splash.SplashScreen
import com.wednowapp.wednow.presentation.timeline.WeddingTimelineScreen
import com.wednowapp.wednow.presentation.weddinginfo.WeddingInfoScreen

@Composable
fun WedNowNavGraph(
    navController: NavHostController,
    /**
     * Non-null when the app was launched (or resumed) via a QR-code deep link.
     * The NavGraph uses this value to:
     *  • skip the Splash screen entirely, and
     *  • jump straight to WeddingHome (already a member) or the JoinWedding screen
     *    with the code pre-filled (new guest).
     */
    deepLinkWeddingId: String? = null,
) {
    val context = LocalContext.current

    // One AuthViewModel + IdentityViewModel for the entire app lifetime.
    val authViewModel: AuthViewModel = hiltViewModel()
    val identityViewModel: IdentityViewModel = hiltViewModel()

    // Compute the start destination once. When a deep link is present we skip
    // the Splash screen and route the user directly to the right place.
    val startDestination = remember(deepLinkWeddingId) {
        if (deepLinkWeddingId != null) {
            val savedId = WeddingSessionManager.getWeddingId(context)
            if (savedId == deepLinkWeddingId) {
                Screen.WeddingHome.createRoute(deepLinkWeddingId)
            } else {
                Screen.JoinWedding.createDeepLinkRoute(deepLinkWeddingId)
            }
        } else {
            Screen.Splash.route
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalAuthViewModel provides authViewModel,
        LocalIdentityViewModel provides identityViewModel,
    ) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {

        // ── Splash ────────────────────────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigate = { destination ->
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Onboarding ────────────────────────────────────────────────────────
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onCompleted = {
                    navController.navigate(Screen.CreateWedding.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CreateWedding.route) {
            val auth = LocalAuthViewModel.current
            val authState by auth.authState.collectAsState()
            var showSignIn by remember { mutableStateOf(false) }
            // Track previous sign-in state so we only react to sign-in transitions
            var wasSignedIn by remember { mutableStateOf(auth.isSignedIn) }

            // Scoped to this back-stack entry — injected use case for cross-device restore
            val createWeddingNavVm: CreateWeddingNavViewModel =
                androidx.hilt.navigation.compose.hiltViewModel()

            // When the user signs in from this screen, navigate to their last wedding.
            // SyncLastActiveWeddingUseCase checks local first, then Firestore (new device).
            LaunchedEffect(authState) {
                val isNowSignedIn = authState != null
                if (!wasSignedIn && isNowSignedIn) {
                    val weddingId = createWeddingNavVm.resolveLastWedding()
                    if (weddingId != null) {
                        navController.navigate(Screen.WeddingHome.createRoute(weddingId)) {
                            popUpTo(Screen.CreateWedding.route) { inclusive = true }
                        }
                    }
                }
                wasSignedIn = isNowSignedIn
            }

            CreateWeddingScreen(
                onWeddingCreated = { weddingId ->
                    // After creation, show Guest Management before ShareInvitation
                    navController.navigate(
                        Screen.GuestManagement.createRoute(weddingId, fromOnboarding = true)
                    ) {
                        popUpTo(Screen.CreateWedding.route) { inclusive = true }
                    }
                },
                onJoinWeddingClick = {
                    navController.navigate(Screen.JoinWedding.route)
                },
                onSignInClick = { showSignIn = true },
            )

            if (showSignIn) {
                SignInBottomSheet(
                    authViewModel = auth,
                    reason = "Sign in to access your previous wedding.",
                    onDismiss = { showSignIn = false; auth.clearError() },
                    onSuccess = { showSignIn = false },
                )
            }
        }

        // ── Join Wedding (manual entry — no pre-filled code) ──────────────────
        composable(Screen.JoinWedding.route) {
            JoinWeddingScreen(
                onWeddingJoined = { weddingId ->
                    navController.navigate(Screen.WeddingHome.createRoute(weddingId)) {
                        popUpTo(Screen.CreateWedding.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Join Wedding via deep link (code pre-filled from QR scan) ─────────
        //
        // This route is the landing point when the app is opened by scanning
        // or tapping a WedNow QR / invitation link.  The wedding code is embedded
        // in the route path so the ViewModel can read it from SavedStateHandle.
        composable(
            route = Screen.JoinWedding.deepLinkRoute,
            arguments = listOf(
                navArgument(Screen.JoinWedding.CODE_ARG) { type = NavType.StringType }
            ),
        ) {
            JoinWeddingScreen(
                onWeddingJoined = { weddingId ->
                    navController.navigate(Screen.WeddingHome.createRoute(weddingId)) {
                        // Clear the deep-link join screen from the back stack so
                        // pressing Back from WeddingHome exits the app cleanly.
                        popUpTo(Screen.JoinWedding.deepLinkRoute) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Notifications ─────────────────────────────────────────────────────
        composable(
            route = Screen.Notifications.route,
            arguments = listOf(navArgument(Screen.Notifications.ARG) { type = NavType.StringType })
        ) {
            NotificationsScreen(
                onBack = { navController.popBackStack() },
                navController = navController,
            )
        }

        // ── Broadcasts ───────────────────────────────────────────────────────
        composable(
            route = Screen.Broadcasts.route,
            arguments = listOf(navArgument(Screen.Broadcasts.ARG) { type = NavType.StringType })
        ) {
            BroadcastScreen(onBack = { navController.popBackStack() })
        }

        // ── Chat ─────────────────────────────────────────────────────────────
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument(Screen.Chat.ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val weddingId = backStackEntry.arguments?.getString(Screen.Chat.ARG) ?: return@composable
            ChatScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDm = { otherGuestId ->
                    navController.navigate(Screen.DirectMessage.createRoute(weddingId, otherGuestId))
                }
            )
        }

        // ── Direct Message ────────────────────────────────────────────────────
        composable(
            route = Screen.DirectMessage.route,
            arguments = listOf(
                navArgument(Screen.DirectMessage.WEDDING_ARG) { type = NavType.StringType },
                navArgument(Screen.DirectMessage.OTHER_GUEST_ARG) { type = NavType.StringType }
            )
        ) {
            DirectMessageScreen(onBack = { navController.popBackStack() })
        }

        // ── Wedding Home ──────────────────────────────────────────────────────
        composable(
            route = Screen.WeddingHome.route,
            arguments = listOf(navArgument(Screen.WeddingHome.ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val weddingId = backStackEntry.arguments?.getString(Screen.WeddingHome.ARG)
                ?: return@composable
            HomeScreen(
                onNavigateToRSVP = { navController.navigate(Screen.RSVP.createRoute(weddingId)) },
                onNavigateToGuestbook = { navController.navigate(Screen.Guestbook.createRoute(weddingId)) },
                onNavigateToPhotos = { navController.navigate(Screen.Photos.createRoute(weddingId)) },
                onNavigateToWeddingInfo = { navController.navigate(Screen.WeddingInfo.createRoute(weddingId)) },
                onNavigateToGuests = {
                    navController.navigate(
                        Screen.GuestManagement.createRoute(
                            weddingId
                        )
                    )
                },
                onNavigateToChat = { navController.navigate(Screen.Chat.createRoute(weddingId)) },
                onNavigateToBroadcasts = { navController.navigate(Screen.Broadcasts.createRoute(weddingId)) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.createRoute(weddingId)) },
                onNavigateToTimeline = { navController.navigate(Screen.Timeline.createRoute(weddingId)) },
                onNavigateToShareInvitation = {
                    navController.navigate(
                        Screen.ShareInvitation.createRoute(
                            weddingId
                        )
                    )
                },
            )
        }

        // ── Guest List ────────────────────────────────────────────────────────
        composable(
            route = Screen.GuestList.route,
            arguments = listOf(navArgument(Screen.GuestList.ARG) { type = NavType.StringType })
        ) {
            GuestListScreen(onBack = { navController.popBackStack() })
        }

        // ── Guest Management ──────────────────────────────────────────────────
        composable(
            route = Screen.GuestManagement.route,
            arguments = listOf(
                navArgument(Screen.GuestManagement.ARG) { type = NavType.StringType },
                navArgument(Screen.GuestManagement.FROM_ONBOARDING) { type = NavType.BoolType },
            ),
        ) { backStackEntry ->
            val weddingId =
                backStackEntry.arguments?.getString(Screen.GuestManagement.ARG) ?: return@composable
            val fromOnboarding =
                backStackEntry.arguments?.getBoolean(Screen.GuestManagement.FROM_ONBOARDING)
                    ?: false
            GuestManagementScreen(
                onBack = { navController.popBackStack() },
                onContinue = if (fromOnboarding) {
                    {
                        navController.navigate(Screen.ShareInvitation.createRoute(weddingId)) {
                            popUpTo(Screen.GuestManagement.route) { inclusive = true }
                        }
                    }
                } else null,
            )
        }

        // ── Sub-screens ───────────────────────────────────────────────────────
        composable(
            route = Screen.RSVP.route,
            arguments = listOf(navArgument(Screen.RSVP.ARG) { type = NavType.StringType })
        ) {
            RSVPScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Guestbook.route,
            arguments = listOf(navArgument(Screen.Guestbook.ARG) { type = NavType.StringType })
        ) {
            GuestbookScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Photos.route,
            arguments = listOf(navArgument(Screen.Photos.ARG) { type = NavType.StringType })
        ) {
            PhotosScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.WeddingInfo.route,
            arguments = listOf(navArgument(Screen.WeddingInfo.ARG) { type = NavType.StringType })
        ) {
            WeddingInfoScreen(onBack = { navController.popBackStack() })
        }

        // ── Timeline ──────────────────────────────────────────────────────────
        composable(
            route = Screen.Timeline.route,
            arguments = listOf(navArgument(Screen.Timeline.ARG) { type = NavType.StringType })
        ) {
            WeddingTimelineScreen(onBack = { navController.popBackStack() })
        }

        // ── Share Invitation ──────────────────────────────────────────────────
        composable(
            route = Screen.ShareInvitation.route,
            arguments = listOf(navArgument(Screen.ShareInvitation.ARG) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val weddingId = backStackEntry.arguments?.getString(Screen.ShareInvitation.ARG)
                ?: return@composable
            ShareInvitationScreen(
                onEnterWedding = {
                    navController.navigate(Screen.WeddingHome.createRoute(weddingId)) {
                        popUpTo(Screen.ShareInvitation.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
    } // NavHost
    } // CompositionLocalProvider
}

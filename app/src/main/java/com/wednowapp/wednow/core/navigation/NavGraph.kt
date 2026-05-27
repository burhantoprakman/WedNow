package com.wednowapp.wednow.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.wednowapp.wednow.presentation.broadcast.BroadcastScreen
import com.wednowapp.wednow.presentation.chat.ChatScreen
import com.wednowapp.wednow.presentation.chat.DirectMessageScreen
import com.wednowapp.wednow.presentation.guestbook.GuestbookScreen
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
fun WedNowNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
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
            CreateWeddingScreen(
                onWeddingCreated = { weddingId ->
                    navController.navigate(Screen.ShareInvitation.createRoute(weddingId)) {
                        popUpTo(Screen.CreateWedding.route) { inclusive = true }
                    }
                },
                onJoinWeddingClick = {
                    navController.navigate(Screen.JoinWedding.route)
                }
            )
        }

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

        // ── Notifications ─────────────────────────────────────────────────────
        composable(
            route = Screen.Notifications.route,
            arguments = listOf(navArgument(Screen.Notifications.ARG) { type = NavType.StringType })
        ) {
            NotificationsScreen(onBack = { navController.popBackStack() })
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
                onNavigateToGuests = { navController.navigate(Screen.GuestList.createRoute(weddingId)) },
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
    }
}

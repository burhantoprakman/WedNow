package com.maritsa.app.core.navigation

sealed class Screen(val route: String) {

    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object CreateWedding : Screen("create_wedding")
    object JoinWedding : Screen("join_wedding") {
        /**
         * Route variant that carries a pre-filled wedding code — used exclusively
         * when the app is opened via a deep link so the code arrives already typed in.
         */
        const val CODE_ARG = "code"
        const val deepLinkRoute = "join_wedding_deep/{$CODE_ARG}"
        fun createDeepLinkRoute(code: String) = "join_wedding_deep/$code"
    }

    object WeddingHome : Screen("wedding_home/{weddingId}") {
        const val ARG = "weddingId"
        fun createRoute(weddingId: String) = "wedding_home/$weddingId"
    }

    object GuestList : Screen("guest_list/{weddingId}") {
        const val ARG = "weddingId"
        fun createRoute(weddingId: String) = "guest_list/$weddingId"
    }

    object GuestManagement : Screen("guest_management/{weddingId}/{fromOnboarding}") {
        const val ARG = "weddingId"
        const val FROM_ONBOARDING = "fromOnboarding"
        fun createRoute(weddingId: String, fromOnboarding: Boolean = false) =
            "guest_management/$weddingId/$fromOnboarding"
    }

    object RSVP : Screen("rsvp/{weddingId}") {
        const val ARG = "weddingId"
        fun createRoute(weddingId: String) = "rsvp/$weddingId"
    }

    object Guestbook : Screen("guestbook/{weddingId}") {
        const val ARG = "weddingId"
        fun createRoute(weddingId: String) = "guestbook/$weddingId"
    }

    object Photos : Screen("photos/{weddingId}") {
        const val ARG = "weddingId"
        fun createRoute(weddingId: String) = "photos/$weddingId"
    }

    object WeddingInfo : Screen("wedding_info/{weddingId}") {
        const val ARG = "weddingId"
        fun createRoute(weddingId: String) = "wedding_info/$weddingId"
    }

    object Chat : Screen("chat/{weddingId}") {
        const val ARG = "weddingId"
        fun createRoute(weddingId: String) = "chat/$weddingId"
    }

    object DirectMessage : Screen("dm/{weddingId}/{otherGuestId}") {
        const val WEDDING_ARG = "weddingId"
        const val OTHER_GUEST_ARG = "otherGuestId"
        fun createRoute(weddingId: String, otherGuestId: String) = "dm/$weddingId/$otherGuestId"
    }

    object Broadcasts : Screen("broadcasts/{weddingId}") {
        const val ARG = "weddingId"
        fun createRoute(weddingId: String) = "broadcasts/$weddingId"
    }

    object Notifications : Screen("notifications/{weddingId}") {
        const val ARG = "weddingId"
        fun createRoute(weddingId: String) = "notifications/$weddingId"
    }

    object Timeline : Screen("timeline/{weddingId}") {
        const val ARG = "weddingId"
        fun createRoute(weddingId: String) = "timeline/$weddingId"
    }

    object ShareInvitation : Screen("share_invitation/{weddingId}") {
        const val ARG = "weddingId"
        fun createRoute(weddingId: String) = "share_invitation/$weddingId"
    }
}

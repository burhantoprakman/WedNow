package com.wednowapp.wednow.core.navigation

sealed class Screen(val route: String) {

    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object CreateWedding : Screen("create_wedding")
    object JoinWedding : Screen("join_wedding")

    object WeddingHome : Screen("wedding_home/{weddingId}") {
        const val ARG = "weddingId"
        fun createRoute(weddingId: String) = "wedding_home/$weddingId"
    }

    object GuestList : Screen("guest_list/{weddingId}") {
        const val ARG = "weddingId"
        fun createRoute(weddingId: String) = "guest_list/$weddingId"
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
}

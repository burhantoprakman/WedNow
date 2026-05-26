package com.wednowapp.wednow.core.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.core.session.WeddingSessionManager
import com.wednowapp.wednow.data.remote.FcmTokenService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WedNowMessagingService : FirebaseMessagingService() {

    @Inject lateinit var fcmTokenService: FcmTokenService

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val weddingId = WeddingSessionManager.getWeddingId(applicationContext) ?: return
        val guestId = GuestSessionManager.getGuestId(applicationContext)
        serviceScope.launch {
            fcmTokenService.saveToken(weddingId, guestId, token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // TODO: handle incoming push notification
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

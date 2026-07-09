package com.maritsa.app.presentation.identity

import androidx.lifecycle.ViewModel
import com.maritsa.app.domain.usecase.SyncLastActiveWeddingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Minimal back-stack-scoped ViewModel used exclusively by the CreateWedding
 * composable destination in [WedNowNavGraph].
 *
 * Its sole responsibility is to call [SyncLastActiveWeddingUseCase] so that
 * after sign-in the user is routed to their last active wedding — even on a
 * brand-new device where the preference must be fetched from Firestore.
 */
@HiltViewModel
class CreateWeddingNavViewModel @Inject constructor(
    private val syncLastActiveWeddingUseCase: SyncLastActiveWeddingUseCase,
) : ViewModel() {

    /** Returns the weddingId to open, or null if no wedding is known. */
    suspend fun resolveLastWedding(): String? = syncLastActiveWeddingUseCase()
}

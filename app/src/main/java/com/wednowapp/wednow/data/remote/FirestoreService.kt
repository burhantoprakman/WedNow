package com.wednowapp.wednow.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.wednowapp.wednow.domain.model.Wedding
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val weddingsCollection = firestore.collection("weddings")

    fun getWeddings(): Flow<List<Wedding>> = callbackFlow {
        val listener = weddingsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val weddings = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Wedding::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(weddings)
        }
        awaitClose { listener.remove() }
    }

    suspend fun getWeddingById(weddingId: String): Result<Wedding?> = runCatching {
        val doc = weddingsCollection.document(weddingId).get().await()
        if (doc.exists()) doc.toObject(Wedding::class.java)?.copy(id = doc.id) else null
    }

    suspend fun createWedding(wedding: Wedding): Result<String> = runCatching {
        val data = hashMapOf(
            "name" to wedding.name,
            "date" to wedding.date,
            "location" to wedding.location,
            "adminGuestId" to wedding.adminGuestId,
            "createdAt" to wedding.createdAt
        )
        weddingsCollection.add(data).await().id
    }
}

package com.wednowapp.wednow.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.wednowapp.wednow.domain.model.DressCodeData
import com.wednowapp.wednow.domain.model.MenuCourseData
import com.wednowapp.wednow.domain.model.TimelineEventData
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
                documentToWedding(doc)
            } ?: emptyList()
            trySend(weddings)
        }
        awaitClose { listener.remove() }
    }

    suspend fun getWeddingById(weddingId: String): Result<Wedding?> = runCatching {
        val doc = weddingsCollection.document(weddingId).get().await()
        documentToWedding(doc)
    }

    suspend fun createWedding(wedding: Wedding): Result<String> = runCatching {
        val menuData = wedding.menu.map { c ->
            mapOf("courseName" to c.courseName, "emoji" to c.emoji, "items" to c.items)
        }
        val dressCodeData = mapOf(
            "style" to wedding.dressCode.style,
            "colorHexes" to wedding.dressCode.colorHexes,
            "colorLabels" to wedding.dressCode.colorLabels,
            "suggested" to wedding.dressCode.suggested,
            "avoid" to wedding.dressCode.avoid,
        )
        val timelineData = wedding.timeline.map { e ->
            mapOf(
                "time" to e.time,
                "title" to e.title,
                "description" to e.description,
                "iconName" to e.iconName,
                "status" to e.status,
            )
        }
        val data = hashMapOf(
            "name" to wedding.name,
            "date" to wedding.date,
            "location" to wedding.location,
            "adminGuestId" to wedding.adminGuestId,
            "createdAt" to wedding.createdAt,
            "coverImageUrl" to wedding.coverImageUrl,
            "menu" to menuData,
            "dressCode" to dressCodeData,
            "timeline" to timelineData,
        )
        weddingsCollection.add(data).await().id
    }

    suspend fun updateWedding(wedding: Wedding): Result<Unit> = runCatching {
        val menuData = wedding.menu.map { c ->
            mapOf("courseName" to c.courseName, "emoji" to c.emoji, "items" to c.items)
        }
        val dressCodeData = mapOf(
            "style" to wedding.dressCode.style,
            "colorHexes" to wedding.dressCode.colorHexes,
            "colorLabels" to wedding.dressCode.colorLabels,
            "suggested" to wedding.dressCode.suggested,
            "avoid" to wedding.dressCode.avoid,
        )
        val timelineData = wedding.timeline.map { e ->
            mapOf(
                "time" to e.time,
                "title" to e.title,
                "description" to e.description,
                "iconName" to e.iconName,
                "status" to e.status,
            )
        }
        val data = mapOf<String, Any>(
            "name" to wedding.name,
            "date" to wedding.date,
            "location" to wedding.location,
            "coverImageUrl" to wedding.coverImageUrl,
            "menu" to menuData,
            "dressCode" to dressCodeData,
            "timeline" to timelineData,
        )
        weddingsCollection.document(wedding.id).update(data).await()
    }

    @Suppress("UNCHECKED_CAST")
    private fun documentToWedding(doc: DocumentSnapshot): Wedding? {
        if (!doc.exists()) return null

        val menuRaw = doc.get("menu") as? List<Map<String, Any?>> ?: emptyList()
        val menu = menuRaw.map { m ->
            MenuCourseData(
                courseName = m["courseName"] as? String ?: "",
                emoji = m["emoji"] as? String ?: "",
                items = (m["items"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            )
        }

        val dcRaw = doc.get("dressCode") as? Map<String, Any?> ?: emptyMap()
        val dressCode = DressCodeData(
            style = dcRaw["style"] as? String ?: "",
            colorHexes = (dcRaw["colorHexes"] as? List<*>)?.filterIsInstance<String>()
                ?: emptyList(),
            colorLabels = (dcRaw["colorLabels"] as? List<*>)?.filterIsInstance<String>()
                ?: emptyList(),
            suggested = (dcRaw["suggested"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            avoid = (dcRaw["avoid"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        )

        val tlRaw = doc.get("timeline") as? List<Map<String, Any?>> ?: emptyList()
        val timeline = tlRaw.map { m ->
            TimelineEventData(
                time = m["time"] as? String ?: "",
                title = m["title"] as? String ?: "",
                description = m["description"] as? String ?: "",
                iconName = m["iconName"] as? String ?: "",
                status = m["status"] as? String ?: "upcoming",
            )
        }

        return Wedding(
            id = doc.id,
            name = doc.getString("name") ?: "",
            date = doc.getString("date") ?: "",
            location = doc.getString("location") ?: "",
            adminGuestId = doc.getString("adminGuestId") ?: "",
            createdAt = doc.getLong("createdAt") ?: 0L,
            coverImageUrl = doc.getString("coverImageUrl") ?: "",
            menu = menu,
            dressCode = dressCode,
            timeline = timeline,
        )
    }
}

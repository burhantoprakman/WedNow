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

    // shortCodes/{CODE} → { weddingId: "..." }  — enables direct-doc lookup, no query needed
    private val shortCodesCollection = firestore.collection("shortCodes")

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

    /**
     * Resolves a 6-character short code to the full wedding document.
     *
     * Strategy:
     *  1. Fast path  — read shortCodes/{CODE} (a single doc get, always O(1)).
     *  2. Fallback   — query weddings where shortCode == CODE.
     *                  Handles weddings created before the shortCodes collection
     *                  existed (i.e. legacy data without a reverse-lookup entry).
     *  3. Backfill   — after a successful fallback, write the missing shortCodes
     *                  entry so the next lookup hits the fast path.
     */
    suspend fun getWeddingByShortCode(shortCode: String): Result<Wedding?> = runCatching {
        val code = shortCode.uppercase()

        // ── Fast path: reverse-lookup doc ─────────────────────────────────────
        val codeDoc = shortCodesCollection.document(code).get().await()
        if (codeDoc.exists()) {
            val weddingId = codeDoc.getString("weddingId") ?: return@runCatching null
            val weddingDoc = weddingsCollection.document(weddingId).get().await()
            return@runCatching documentToWedding(weddingDoc)
        }

        // ── Fallback: query weddings collection by shortCode field ─────────────
        // Works because rules have `allow read: if true` on /weddings/{weddingId}
        // which grants both `get` (document) and `list` (query) permissions.
        val querySnap = weddingsCollection
            .whereEqualTo("shortCode", code)
            .limit(1)
            .get()
            .await()

        val weddingDoc = querySnap.documents.firstOrNull() ?: return@runCatching null
        val wedding = documentToWedding(weddingDoc) ?: return@runCatching null

        // ── Backfill: write the missing shortCodes entry ──────────────────────
        if (wedding.shortCode.isNotBlank()) {
            runCatching {
                shortCodesCollection
                    .document(wedding.shortCode)
                    .set(mapOf("weddingId" to wedding.id))
                    .await()
            } // best-effort — don't fail the join if backfill write is denied
        }

        wedding
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
            "shortCode" to wedding.shortCode,
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
        val weddingId = weddingsCollection.add(data).await().id
        // Write the reverse-lookup entry so guests can join by short code
        if (wedding.shortCode.isNotBlank()) {
            shortCodesCollection
                .document(wedding.shortCode)
                .set(mapOf("weddingId" to weddingId))
                .await()
        }
        weddingId
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
            shortCode = doc.getString("shortCode") ?: "",
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

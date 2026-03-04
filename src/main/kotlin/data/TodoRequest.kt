package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.Todo
import org.delcom.tables.UrgencyLevel
import java.util.UUID

@Serializable
data class TodoRequest(
    var userId: String = "",
    var title: String = "",
    var description: String = "",
    var cover: String? = null,
    var isDone: Boolean = false,
    var urgency: String = "LOW"   // ✅ Tambahan baru
) {

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "title" to title,
            "description" to description,
            "cover" to cover,
            "isDone" to isDone,
            "urgency" to urgency
        )
    }

    fun toEntity(): Todo {
        return Todo(
            userId = userId,
            title = title,
            description = description,
            cover = cover,
            isDone = isDone,
            urgency = parseUrgency(),   // ✅ convert ke enum
            updatedAt = Clock.System.now()
        )
    }

    private fun parseUrgency(): UrgencyLevel {
        return try {
            UrgencyLevel.valueOf(urgency.uppercase())
        } catch (e: Exception) {
            UrgencyLevel.LOW
        }
    }
}
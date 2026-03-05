package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.delcom.tables.UrgencyLevel
import java.util.UUID

@Serializable
data class Todo(
    var id: String = UUID.randomUUID().toString(),

    var userId: String,
    var title: String,
    var description: String,
    var cover: String? = null,

    val urgency: String? = null,  // ← ganti default dari "LOW" ke null
    var isDone: Boolean = false,

    @Contextual
    val createdAt: Instant = Clock.System.now(),

    @Contextual
    var updatedAt: Instant = Clock.System.now(),
)
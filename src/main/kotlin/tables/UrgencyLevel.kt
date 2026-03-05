package org.delcom.tables

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

// pastikan package sesuai

@Serializable
enum class UrgencyLevel {
    @SerialName("LOW") LOW,
    @SerialName("MEDIUM") MEDIUM,
    @SerialName("HIGH") HIGH
}
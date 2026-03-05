package org.delcom.tables

import kotlinx.serialization.Serializable

// pastikan package sesuai

@Serializable
enum class UrgencyLevel {
    LOW,
    MEDIUM,
    HIGH
}
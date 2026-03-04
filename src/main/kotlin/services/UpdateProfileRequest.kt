package org.delcom.services

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val name: String,
    val username: String,
    val about: String? = null
)
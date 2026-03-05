package org.delcom.data

import kotlinx.serialization.Serializable
import org.delcom.entities.Todo

@Serializable
data class TodosResponse(
    val todos: List<Todo>,
    val page: Int,
    val perPage: Int,
    val totalPages: Int,
    val totalCount: Long
)
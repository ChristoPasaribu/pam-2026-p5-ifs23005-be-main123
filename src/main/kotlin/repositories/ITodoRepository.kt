package org.delcom.repositories

import org.delcom.entities.Todo

interface ITodoRepository {

    suspend fun getAll(
        userId: String,
        search: String,
        urgency: Int?,      // filter urgency (nullable)
        sortBy: String?,    // title, createdAt, urgency
        order: String?      // asc / desc
    ): List<Todo>

    suspend fun getById(todoId: String): Todo?

    suspend fun create(todo: Todo): String

    suspend fun update(userId: String, todoId: String, newTodo: Todo): Boolean

    suspend fun delete(userId: String, todoId: String): Boolean

    suspend fun getStatistics(userId: String): TodoStatistics

    data class TodoStatistics(
        val total: Long,
        val completed: Long,
        val incomplete: Long
    )
}
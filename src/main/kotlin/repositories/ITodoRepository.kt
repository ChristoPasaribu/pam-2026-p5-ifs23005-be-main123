package org.delcom.repositories

import org.delcom.entities.Todo

interface ITodoRepository {
    suspend fun getAll(
        userId: String,
        search: String,
        page: Int = 1,
        perPage: Int = 10,
        isDone: Boolean? = null,
        urgency: String? = null,   // ← TAMBAH
        sortBy: String? = null,    // ← TAMBAH
        order: String? = null      // ← TAMBAH
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
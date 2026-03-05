package org.delcom.repositories

import org.delcom.dao.TodoDAO
import org.delcom.entities.Todo
import org.delcom.helpers.suspendTransaction
import org.delcom.helpers.todoDAOToModel
import org.delcom.tables.TodoTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import java.util.*
import org.delcom.repositories.ITodoRepository.TodoStatistics
import org.delcom.tables.UrgencyLevel

class TodoRepository : ITodoRepository {

    override suspend fun getAll(
        userId: String,
        search: String,
        page: Int,
        perPage: Int,
        isDone: Boolean?,
        urgency: String?,
        sortBy: String?,
        order: String?
    ): List<Todo> = suspendTransaction {
        val offset = ((page - 1) * perPage).toLong()
        val keyword = if (search.isBlank()) null else "%${search.lowercase()}%"

        val sortColumn = when (sortBy) {
            "title" -> TodoTable.title
            "urgency" -> TodoTable.urgency
            else -> TodoTable.createdAt
        }
        val sortOrder = if (order == "asc") SortOrder.ASC else SortOrder.DESC

        TodoDAO.find {
            var condition = TodoTable.userId eq UUID.fromString(userId)
            if (keyword != null) condition = condition and (TodoTable.title.lowerCase() like keyword)
            if (isDone != null) condition = condition and (TodoTable.isDone eq isDone)
            if (urgency != null) {
                val urgencyLevel = UrgencyLevel.entries.find { it.name.equals(urgency, ignoreCase = true) }
                if (urgencyLevel != null) condition = condition and (TodoTable.urgency eq urgencyLevel)
            }
            condition
        }
            .orderBy(sortColumn to sortOrder)
            .limit(perPage)
            .offset(offset)
            .map(::todoDAOToModel)
    }   // ← tutup getAll di sini — BUKAN setelah ini

    override suspend fun getById(todoId: String): Todo? = suspendTransaction {
        TodoDAO
            .find { TodoTable.id eq UUID.fromString(todoId) }
            .limit(1)
            .map(::todoDAOToModel)
            .firstOrNull()
    }

    override suspend fun create(todo: Todo): String = suspendTransaction {
        val todoDAO = TodoDAO.new {
            userId = UUID.fromString(todo.userId)
            title = todo.title
            description = todo.description
            cover = todo.cover
            isDone = todo.isDone
            urgency = todo.urgency ?: UrgencyLevel.LOW   // ← TAMBAH
            createdAt = todo.createdAt
            updatedAt = todo.updatedAt
        }
        todoDAO.id.value.toString()
    }

    override suspend fun update(userId: String, todoId: String, newTodo: Todo): Boolean = suspendTransaction {
        val todoDAO = TodoDAO
            .find {
                (TodoTable.id eq UUID.fromString(todoId)) and
                        (TodoTable.userId eq UUID.fromString(userId))
            }
            .limit(1)
            .firstOrNull()

        if (todoDAO != null) {
            todoDAO.title = newTodo.title
            todoDAO.description = newTodo.description
            todoDAO.cover = newTodo.cover
            todoDAO.isDone = newTodo.isDone
            todoDAO.urgency = newTodo.urgency ?: UrgencyLevel.LOW   // ← TAMBAH
            todoDAO.updatedAt = newTodo.updatedAt
            true
        } else {
            false
        }
    }

    override suspend fun delete(userId: String, todoId: String): Boolean = suspendTransaction {
        val rowsDeleted = TodoTable.deleteWhere {
            (TodoTable.id eq UUID.fromString(todoId)) and
                    (TodoTable.userId eq UUID.fromString(userId))
        }
        rowsDeleted >= 1
    }

    override suspend fun getStatistics(userId: String): TodoStatistics = suspendTransaction {
        val total = TodoDAO.find { TodoTable.userId eq UUID.fromString(userId) }.count()

        val completed = TodoDAO.find {
            (TodoTable.userId eq UUID.fromString(userId)) and (TodoTable.isDone eq true)
        }.count()

        val incomplete = TodoDAO.find {
            (TodoTable.userId eq UUID.fromString(userId)) and (TodoTable.isDone eq false)
        }.count()

        TodoStatistics(total = total, completed = completed, incomplete = incomplete)
    }

    override suspend fun countAll(
        userId: String,
        search: String,
        isDone: Boolean?,
        urgency: String?
    ): Long = suspendTransaction {
        val keyword = if (search.isBlank()) null else "%${search.lowercase()}%"
        TodoDAO.find {
            var condition = TodoTable.userId eq UUID.fromString(userId)
            if (keyword != null) condition = condition and (TodoTable.title.lowerCase() like keyword)
            if (isDone != null) condition = condition and (TodoTable.isDone eq isDone)
            if (urgency != null) {
                val urgencyLevel = UrgencyLevel.entries.find { it.name.equals(urgency, ignoreCase = true) }
                if (urgencyLevel != null) condition = condition and (TodoTable.urgency eq urgencyLevel)
            }
            condition
        }.count()
    }

}  // ← tutup class di sini
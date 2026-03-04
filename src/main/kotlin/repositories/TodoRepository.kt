package org.delcom.repositories

import org.delcom.dao.TodoDAO
import org.delcom.entities.Todo
import org.delcom.tables.UrgencyLevel
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

class TodoRepository : ITodoRepository {

    override suspend fun getAll(
        userId: String,
        search: String?,
        urgency: String?,
        isDone: Boolean?,
        sortBy: String?,
        order: String?
    ): List<Todo> = suspendTransaction {

        val userUUID = UUID.fromString(userId)

        val query = TodoDAO.find {
            var condition = (TodoTable.userId eq userUUID)

            if (!search.isNullOrBlank()) {
                condition = condition and
                        (TodoTable.title.lowerCase() like "%${search.lowercase()}%")
            }

            if (!urgency.isNullOrBlank()) {
                val urgencyEnum = try {
                    UrgencyLevel.valueOf(urgency.uppercase())
                } catch (e: Exception) {
                    null
                }

                if (urgencyEnum != null) {
                    condition = condition and
                            (TodoTable.urgency eq urgencyEnum)
                }
            }

            if (isDone != null) {
                condition = condition and
                        (TodoTable.isDone eq isDone)
            }

            condition
        }

        val sortColumn = when (sortBy) {
            "title" -> TodoTable.title
            "urgency" -> TodoTable.urgency
            "isDone" -> TodoTable.isDone
            "createdAt" -> TodoTable.createdAt
            else -> TodoTable.createdAt
        }

        val sortOrder = if (order.equals("asc", true))
            SortOrder.ASC
        else
            SortOrder.DESC

        query
            .orderBy(sortColumn to sortOrder)
            .map(::todoDAOToModel)
    }

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
            urgency = todo.urgency
            isDone = todo.isDone
            createdAt = todo.createdAt
            updatedAt = todo.updatedAt
        }
        todoDAO.id.value.toString()
    }

    override suspend fun update(
        userId: String,
        todoId: String,
        newTodo: Todo
    ): Boolean = suspendTransaction {

        val todoDAO = TodoDAO.find {
            (TodoTable.id eq UUID.fromString(todoId)) and
                    (TodoTable.userId eq UUID.fromString(userId))
        }.limit(1).firstOrNull()

        if (todoDAO != null) {
            todoDAO.title = newTodo.title
            todoDAO.description = newTodo.description
            todoDAO.cover = newTodo.cover
            todoDAO.urgency = newTodo.urgency
            todoDAO.isDone = newTodo.isDone
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

        val userUUID = UUID.fromString(userId)

        val total = TodoDAO.find {
            TodoTable.userId eq userUUID
        }.count()

        val completed = TodoDAO.find {
            (TodoTable.userId eq userUUID) and
                    (TodoTable.isDone eq true)
        }.count()

        val incomplete = TodoDAO.find {
            (TodoTable.userId eq userUUID) and
                    (TodoTable.isDone eq false)
        }.count()

        TodoStatistics(
            total = total,
            completed = completed,
            incomplete = incomplete
        )
    }
}
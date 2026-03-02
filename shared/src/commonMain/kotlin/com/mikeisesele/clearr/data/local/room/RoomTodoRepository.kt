package com.mikeisesele.clearr.data.local.room

import com.mikeisesele.clearr.data.model.TodoItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTodoRepository(
    private val todoDao: TodoRoomDao
) {
    fun getTodosForTracker(trackerId: Long): Flow<List<TodoItem>> =
        todoDao.getTodosForTracker(trackerId).map { todos -> todos.map(TodoRoomEntity::toDomain) }

    suspend fun getTodoById(id: String): TodoItem? = todoDao.getTodoById(id)?.toDomain()

    suspend fun insertTodo(todo: TodoItem) {
        todoDao.upsertTodo(todo.toRoomEntity())
    }

    suspend fun updateTodo(todo: TodoItem) {
        todoDao.updateTodo(todo.toRoomEntity())
    }

    suspend fun markTodoDone(id: String, completedAt: Long) {
        todoDao.markTodoDone(id, completedAt)
    }

    suspend fun deleteTodo(id: String) {
        todoDao.deleteTodo(id)
    }

    suspend fun deleteTodosForTracker(trackerId: Long) {
        todoDao.deleteTodosForTracker(trackerId)
    }

    suspend fun isEmpty(): Boolean = todoDao.countTodos() == 0

    suspend fun seedTodos(todos: List<TodoItem>) {
        if (todos.isNotEmpty()) {
            todoDao.upsertTodos(todos.map(TodoItem::toRoomEntity))
        }
    }
}

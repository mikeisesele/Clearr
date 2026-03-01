package com.mikeisesele.clearr.domain.repository

import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.Tracker
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun getTrackerByIdFlow(id: Long): Flow<Tracker?>
    fun getTodosForTracker(trackerId: Long): Flow<List<TodoItem>>
    suspend fun getTodoById(id: String): TodoItem?
    suspend fun insertTodo(todo: TodoItem)
    suspend fun updateTodo(todo: TodoItem)
    suspend fun markTodoDone(id: String, completedAt: Long)
    suspend fun deleteTodo(id: String)
}

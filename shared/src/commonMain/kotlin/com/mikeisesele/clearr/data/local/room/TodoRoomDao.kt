package com.mikeisesele.clearr.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoRoomDao {
    @Query("SELECT * FROM todos WHERE trackerId = :trackerId ORDER BY createdAt DESC")
    fun getTodosForTracker(trackerId: Long): Flow<List<TodoRoomEntity>>

    @Query("SELECT * FROM todos WHERE id = :id LIMIT 1")
    suspend fun getTodoById(id: String): TodoRoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTodo(todo: TodoRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTodos(todos: List<TodoRoomEntity>)

    @Update
    suspend fun updateTodo(todo: TodoRoomEntity)

    @Query("UPDATE todos SET status = 'DONE', completedAt = :completedAt WHERE id = :id")
    suspend fun markTodoDone(id: String, completedAt: Long)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteTodo(id: String)

    @Query("DELETE FROM todos WHERE trackerId = :trackerId")
    suspend fun deleteTodosForTracker(trackerId: Long)

    @Query("SELECT COUNT(*) FROM todos")
    suspend fun countTodos(): Int
}

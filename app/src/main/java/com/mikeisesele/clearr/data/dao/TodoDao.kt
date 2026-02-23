package com.mikeisesele.clearr.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mikeisesele.clearr.data.model.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Query("SELECT * FROM todos WHERE trackerId = :trackerId ORDER BY createdAt DESC")
    fun getTodos(trackerId: Long): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE id = :id LIMIT 1")
    suspend fun getTodoById(id: String): TodoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoEntity)

    @Query("UPDATE todos SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun markDone(id: String, status: String = "DONE", completedAt: Long)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM todos WHERE trackerId = :trackerId")
    suspend fun deleteForTracker(trackerId: Long)
}

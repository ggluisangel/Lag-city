package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {
    @Query("SELECT * FROM saved_cities ORDER BY updatedAt DESC")
    fun getAllCities(): Flow<List<CityEntity>>

    @Query("SELECT * FROM saved_cities WHERE id = :id LIMIT 1")
    suspend fun getCityById(id: Long): CityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: CityEntity): Long

    @Update
    suspend fun updateCity(city: CityEntity)

    @Delete
    suspend fun deleteCity(city: CityEntity)

    @Query("DELETE FROM saved_cities WHERE id = :id")
    suspend fun deleteById(id: Long)
}

package ca.gbc.g85.personalrestaurantguide.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {

    @Query("SELECT * FROM restaurants ORDER BY name")
    fun getAll(): Flow<List<Restaurant>>

    @Query("SELECT * FROM restaurants WHERE id = :id")
    suspend fun getById(id: Long): Restaurant?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(restaurant: Restaurant)

    @Delete
    suspend fun delete(restaurant: Restaurant)
}

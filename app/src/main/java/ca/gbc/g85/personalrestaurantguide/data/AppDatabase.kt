package ca.gbc.g85.personalrestaurantguide.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Restaurant::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao
}

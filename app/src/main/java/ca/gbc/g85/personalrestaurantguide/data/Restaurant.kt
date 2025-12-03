package ca.gbc.g85.personalrestaurantguide.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class Restaurant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val address: String,
    val phone: String,
    val description: String,
    val tags: String,
    val rating: Int
)

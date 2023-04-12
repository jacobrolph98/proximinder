package com.jrolph.proximityreminder.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty

@Entity(tableName="reminders")
data class Reminder(
    @JsonProperty("id") @PrimaryKey(autoGenerate = true) var id: Int? = null,
    @JsonProperty("note") @ColumnInfo(name="note") val note: String,
    @JsonProperty("radius") @ColumnInfo(name="radius") val radius: Float,
    @JsonProperty("longitude") @ColumnInfo(name="longitude") val longitude: Float,
    @JsonProperty("latitude") @ColumnInfo(name="latitude") val latitude: Float,
    @JsonProperty("name") @ColumnInfo(name="name") val name: String?
)
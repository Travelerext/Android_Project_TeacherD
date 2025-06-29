package com.example.teacherd.model

import androidx.room.TypeConverter
import java.time.Instant

class InstantConverter {
    @TypeConverter
    fun fromTimeStamp(value: Long): Instant {
        return Instant.ofEpochMilli(value)
    }

    @TypeConverter
    fun instantToTimestamp(instant: Instant): Long {
        return instant.toEpochMilli()
    }
}
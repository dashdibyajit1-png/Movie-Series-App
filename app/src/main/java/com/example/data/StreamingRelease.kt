package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tracked_releases")
data class TrackedRelease(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val platform: String, // "Netflix" or "Prime Video"
    val releaseDate: String,
    val imageUrl: String? = null,
    val genre: String,
    val synopsis: String,
    val rating: Float? = null, // Custom user rating (e.g. 1.0 to 5.0)
    val userNotes: String? = null, // Custom user notes
    val status: String = "To Watch", // "To Watch", "Watching", "Completed"
    val dateTracked: Long = System.currentTimeMillis(),
    val isCustom: Boolean = false // Created manually by user vs tracked from curated feed
)

@Dao
interface TrackedReleaseDao {
    @Query("SELECT * FROM tracked_releases ORDER BY dateTracked DESC")
    fun getAllTrackedReleases(): Flow<List<TrackedRelease>>

    @Query("SELECT * FROM tracked_releases WHERE title = :title LIMIT 1")
    suspend fun getByTitle(title: String): TrackedRelease?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(release: TrackedRelease)

    @Update
    suspend fun update(release: TrackedRelease)

    @Delete
    suspend fun delete(release: TrackedRelease)

    @Query("DELETE FROM tracked_releases WHERE title = :title")
    suspend fun deleteByTitle(title: String)
}

@Database(entities = [TrackedRelease::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val trackedReleaseDao: TrackedReleaseDao
}

class TrackedReleaseRepository(private val dao: TrackedReleaseDao) {
    val allTrackedReleases: Flow<List<TrackedRelease>> = dao.getAllTrackedReleases()

    suspend fun getByTitle(title: String): TrackedRelease? = dao.getByTitle(title)
    suspend fun insert(release: TrackedRelease) = dao.insert(release)
    suspend fun update(release: TrackedRelease) = dao.update(release)
    suspend fun delete(release: TrackedRelease) = dao.delete(release)
    suspend fun deleteByTitle(title: String) = dao.deleteByTitle(title)
}

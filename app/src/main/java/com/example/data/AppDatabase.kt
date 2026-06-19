package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. Entity for Custom Characters
@Entity(tableName = "custom_characters")
data class CustomCharacter(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val age: String,
    val personality: String,
    val appearance: String,
    val timestamp: Long = System.currentTimeMillis()
)

// 2. Entity for Saved Story Script Projects
@Entity(tableName = "story_projects")
data class StoryProject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val topic: String,
    val characterName: String,
    val characterAge: String,
    val characterPersonality: String,
    val characterAppearance: String,
    val title: String,
    val story: String,
    val sceneBreakdown: String,
    val dialogues: String,
    val imagePrompt: String,
    val videoPrompt: String,
    val voicePrompt: String,
    val episodeNumber: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

// 3. DAOs
@Dao
interface CustomCharacterDao {
    @Query("SELECT * FROM custom_characters ORDER BY timestamp DESC")
    fun getAllCharacters(): Flow<List<CustomCharacter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CustomCharacter)

    @Delete
    suspend fun deleteCharacter(character: CustomCharacter)
}

@Dao
interface StoryProjectDao {
    @Query("SELECT * FROM story_projects ORDER BY timestamp DESC")
    fun getAllProjects(): Flow<List<StoryProject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: StoryProject)

    @Delete
    suspend fun deleteProject(project: StoryProject)
}

// 4. Room Database
@Database(entities = [CustomCharacter::class, StoryProject::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CustomCharacterDao
    abstract fun projectDao(): StoryProjectDao
}

// 5. Repository Layer
class VideoFactoryRepository(
    private val characterDao: CustomCharacterDao,
    private val projectDao: StoryProjectDao
) {
    val allCharacters: Flow<List<CustomCharacter>> = characterDao.getAllCharacters()
    val allProjects: Flow<List<StoryProject>> = projectDao.getAllProjects()

    suspend fun insertCharacter(character: CustomCharacter) {
        characterDao.insertCharacter(character)
    }

    suspend fun deleteCharacter(character: CustomCharacter) {
        characterDao.deleteCharacter(character)
    }

    suspend fun insertProject(project: StoryProject) {
        projectDao.insertProject(project)
    }

    suspend fun deleteProject(project: StoryProject) {
        projectDao.deleteProject(project)
    }
}

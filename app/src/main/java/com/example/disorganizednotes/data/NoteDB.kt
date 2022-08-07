package com.example.disorganizednotes.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import java.net.CacheRequest

@Entity(tableName = "note",
    foreignKeys = [ForeignKey(
        entity = Idea::class,
        parentColumns = ["id"],
        childColumns = ["idea_id"],
        onDelete = ForeignKey.CASCADE)])
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo val title: String,
    @ColumnInfo val description: String?,
    @ColumnInfo(name = "idea_id", index = true) val ideaId: Long,
    @ColumnInfo(name = "prev_id") val prevId: Long?,
    @ColumnInfo(name = "next_id") val nextId: Long?
)

@Entity(tableName = "idea")
data class Idea(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo val name: String
)

@Entity(tableName = "archived_note",
    foreignKeys = [ForeignKey(
        entity = Idea::class,
        parentColumns = ["id"],
        childColumns = ["idea_id"],
        onDelete = ForeignKey.CASCADE)])
data class ArchivedNote(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo val title: String,
    @ColumnInfo val description: String?,
    @ColumnInfo(name = "idea_id", index = true) val ideaId: Long,
    @ColumnInfo(name = "completion_time") val completionTime: Long
)

data class NoteWithIdea(
    @ColumnInfo(name = "note_id")
    val noteId: Long,
    @ColumnInfo(name = "idea_id")
    val ideaId: Long,
    val title: String,
    val description: String,
    @ColumnInfo(name = "idea_name")
    val ideaName: String,
    @ColumnInfo(name = "prev_id")
    val prevNoteId: Long?,
    @ColumnInfo(name = "next_id")
    val nextNoteId: Long?
)

data class ArchivedNoteWithIdea(
    @ColumnInfo(name = "note_id")
    val noteId: Long,
    @ColumnInfo(name = "idea_id")
    val ideaId: Long,
    val title: String,
    val description: String,
    @ColumnInfo(name = "idea_name")
    val ideaName: String,
    @ColumnInfo(name = "completion_time")
    val completionTime: Long
)

@Dao
interface NoteDao {
    @Query("SELECT note.id AS note_id, " +
            "idea.id AS idea_id, " +
            "note.title, " +
            "note.description, " +
            "idea.name AS idea_name, " +
            "note.next_id AS next_id, " +
            "note.prev_id AS prev_id " +
            "FROM note INNER JOIN idea ON note.idea_id = idea.id")
    fun getAll(): LiveData<List<NoteWithIdea>>

    @Query("SELECT note.id AS note_id, " +
            "idea.id AS idea_id, " +
            "note.title, " +
            "note.description, " +
            "idea.name AS idea_name, " +
            "note.next_id AS next_id, " +
            "note.prev_id AS prev_id " +
            "FROM note INNER JOIN idea ON note.idea_id = idea.id " +
            "WHERE note.id = :id")
    fun get(id: Int): LiveData<NoteWithIdea>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note) : Long

    @Update
    suspend fun update(note: Note)

    @Update
    suspend fun update(notes: List<Note>)

    @Delete
    suspend fun delete(note: Note)
}

@Dao
interface IdeaDao {
    @Query("SELECT * FROM idea ORDER BY name ASC")
    fun getAll(): LiveData<List<Idea>>

    @Query("SELECT * FROM idea WHERE id = :id")
    fun get(id: Int): LiveData<Idea>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(idea: Idea) : Long
}

@Dao
interface ArchivedNoteDao {
    @Query("SELECT archived_note.id AS note_id, " +
            "idea.id AS idea_id, " +
            "archived_note.title, " +
            "archived_note.description, " +
            "idea.name AS idea_name, " +
            "archived_note.completion_time AS completion_time " +
            "FROM archived_note INNER JOIN idea ON archived_note.idea_id = idea.id " +
            "ORDER BY completion_time DESC")
    fun getAll(): LiveData<List<ArchivedNoteWithIdea>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: ArchivedNote)

    @Delete
    suspend fun delete(note: ArchivedNote)
}

@Database(entities = [Note::class, Idea::class, ArchivedNote::class],
    version = 6)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun ideaDao(): IdeaDao
    abstract fun archivedNoteDao(): ArchivedNoteDao

    companion object{
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase{
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
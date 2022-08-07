package com.example.disorganizednotes.data

import androidx.lifecycle.LiveData

class NoteRepository(private val noteDao: NoteDao, private val ideaDao: IdeaDao, private val archivedNoteDao: ArchivedNoteDao) {
    val getAllNotes: LiveData<List<NoteWithIdea>> = noteDao.getAll()

    suspend fun addNote(note: Note): Long {
        return noteDao.insert(note)
    }

    suspend fun updateNote(note: Note) {
        noteDao.update(note)
    }

    suspend fun updateNotes(notes: List<Note>) {
        noteDao.update(notes)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.delete(note)
    }

    val getAllIdeas: LiveData<List<Idea>> = ideaDao.getAll()

    suspend fun addIdea(idea: Idea): Long {
        return ideaDao.insert(idea)
    }

    val getAllArchivedNotes: LiveData<List<ArchivedNoteWithIdea>> = archivedNoteDao.getAll()

    suspend fun addArchivedNote(note: ArchivedNote) {
        archivedNoteDao.insert(note)
    }

    suspend fun deleteArchivedNote(note: ArchivedNote) {
        archivedNoteDao.delete(note)
    }

}
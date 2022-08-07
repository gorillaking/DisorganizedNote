package com.example.disorganizednotes.data

import android.app.Application
import android.os.Build.VERSION_CODES.M
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.temporal.ChronoField

class NoteViewModel(application: Application): AndroidViewModel(application) {
    private val notes: MutableLiveData<List<NoteWithIdea>>
    private var notesMap: Map<Long, NoteWithIdea>
    private val ideas: LiveData<List<Idea>>
    private val archivedNotes: LiveData<List<ArchivedNoteWithIdea>>
    private val repository: NoteRepository


    init {
        val database = NoteDatabase.getDatabase(application)
        val noteDao = database.noteDao()
        val ideaDao = database.ideaDao()
        val archivedNoteDao = database.archivedNoteDao()
        repository = NoteRepository(noteDao, ideaDao, archivedNoteDao)
        notes = MutableLiveData<List<NoteWithIdea>>(listOf())
        notesMap = mapOf()
        repository.getAllNotes.observeForever() {
            notes.value = orderNotes(it)
        }
        ideas = repository.getAllIdeas
        archivedNotes = repository.getAllArchivedNotes
    }

    private fun orderNotes(notes: List<NoteWithIdea>): List<NoteWithIdea> {
        notesMap = notes.associateBy { it.noteId }
        var currentNote = notes.firstOrNull { it.prevNoteId == null } ?: return listOf()

        val orderedNotes = mutableListOf(currentNote)
        while(currentNote.nextNoteId != null){
            currentNote = notesMap[currentNote.nextNoteId]!!
            orderedNotes.add(currentNote)
        }

        return orderedNotes
    }

    fun addNote(title: String, description: String, ideaName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val idea = ideas.value?.firstOrNull { it.name.lowercase() == ideaName.lowercase()}
            var ideaId = idea?.id ?: repository.addIdea(Idea(0, ideaName))

            addAndLinkNote(title, description, ideaId)
        }
    }

    private suspend fun addAndLinkNote(title: String, description: String, ideaId: Long) {
        val lastNote = notes.value?.lastOrNull()
        val newNote = Note(0, title, description, ideaId, lastNote?.noteId, null)
        val newNoteId = repository.addNote(newNote)
        if(lastNote != null) {
            val noteToUpdate =
                Note(lastNote.noteId,
                    lastNote.title,
                    lastNote.description,
                    lastNote.ideaId,
                    lastNote.prevNoteId,
                    newNoteId)

            repository.updateNote(noteToUpdate)
        }
    }

    fun updateNote(id: Long,
                   title: String,
                   description: String,
                   ideaName: String,
                   prevId: Long?,
                   nextId: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            val idea = ideas.value?.find { it.name.lowercase() == ideaName.lowercase()}
            var ideaId = idea?.id ?: repository.addIdea(Idea(0, ideaName))

            val noteToUpdate = Note(id, title, description, ideaId, prevId, nextId)
            repository.updateNote(noteToUpdate)
        }
    }

    fun deleteNote(id: Long) {
        val noteToDelete = notesMap[id]
        val prevNote = if(noteToDelete?.prevNoteId != null) notesMap[noteToDelete.prevNoteId] else null
        val nextNote = if(noteToDelete?.nextNoteId != null) notesMap[noteToDelete.nextNoteId] else null

        var updateList = mutableListOf<Note>()
        if(prevNote != null) {
            updateList.add(Note(prevNote.noteId, prevNote.title, prevNote.description, prevNote.ideaId, prevNote.prevNoteId, nextNote?.noteId))
        }

        if(nextNote != null) {
            updateList.add(Note(nextNote.noteId, nextNote.title, nextNote.description, nextNote.ideaId, prevNote?.noteId, nextNote.nextNoteId))
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNotes(updateList)
            if(noteToDelete != null) {
                repository.deleteNote(
                    Note(
                        noteToDelete.noteId,
                        noteToDelete.title,
                        noteToDelete.description,
                        noteToDelete.ideaId,
                        noteToDelete.prevNoteId,
                        noteToDelete.nextNoteId))
            }
        }
    }

    fun updateNoteOrders(notes: List<NoteWithIdea>) {
        var updateList = mutableListOf<Note>()
        for(index in notes.indices) {
            val note = notes[index]
            val prevId = if(index == 0) null else notes[index - 1].noteId
            val nextId = if(index == notes.size - 1) null else notes[index + 1].noteId

            updateList.add(Note(note.noteId, note.title, note.description, note.ideaId, prevId, nextId))
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNotes(updateList)
        }
    }

    fun moveNoteToArchive(note: NoteWithIdea) {
        val prevNote = if(note.prevNoteId == null) null else notesMap[note.prevNoteId]
        val nextNote = if(note.nextNoteId == null) null else notesMap[note.nextNoteId]

        var updateList = mutableListOf<Note>()
        if(prevNote != null) {
            updateList.add(Note(prevNote.noteId, prevNote.title, prevNote.description, prevNote.ideaId, prevNote.prevNoteId, nextNote?.noteId))
        }

        if(nextNote != null) {
            updateList.add(Note(nextNote.noteId, nextNote.title, nextNote.description, nextNote.ideaId, prevNote?.noteId, nextNote.nextNoteId))
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.addArchivedNote(
                ArchivedNote(
                0, note.title, note.description, note.ideaId, System.currentTimeMillis())
            )

            repository.updateNotes(updateList)

            repository.deleteNote(
                Note(note.noteId, note.title, note.description, note.ideaId, note.prevNoteId, note.nextNoteId)
            )
        }
    }

    fun moveNoteOutOfArchive(note: ArchivedNoteWithIdea) {
        viewModelScope.launch(Dispatchers.IO) {
            addAndLinkNote(note.title, note.description, note.ideaId)

            repository.deleteArchivedNote(
                ArchivedNote(note.noteId, note.title, note.description, note.ideaId, note.completionTime)
            )
        }
    }

    fun getAllNotes(): LiveData<List<NoteWithIdea>> {
        return notes
    }

    fun getAllIdeas(): LiveData<List<Idea>> {
        return ideas
    }

    fun getAllArchivedNote(): LiveData<List<ArchivedNoteWithIdea>> {
        return archivedNotes
    }
}
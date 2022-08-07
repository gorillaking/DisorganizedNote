package com.example.disorganizednotes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.disorganizednotes.data.NoteWithIdea

class NoteRecyclerAdapter(context: Context)
    : RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

    var notes = listOf<NoteWithIdea>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = layoutInflater.inflate(R.layout.item, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount() = notes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        holder.noteTitle.text = note.title
        holder.noteIdea.text = note.ideaName
        holder.notePosition = position
        holder.id = note.noteId
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var id: Long = -1
        val noteTitle: TextView = view.findViewById(R.id.textItem)
        val noteIdea: TextView = view.findViewById(R.id.textIdea)
        var notePosition = -1

        init {
            view.setOnClickListener {
                val action = NotesFragmentDirections.actionNotesFragmentToEditFragment(notePosition)
                view.findNavController().navigate(action)
            }
        }
    }


}
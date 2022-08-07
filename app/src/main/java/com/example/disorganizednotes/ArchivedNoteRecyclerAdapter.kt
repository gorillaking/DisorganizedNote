package com.example.disorganizednotes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.disorganizednotes.data.ArchivedNoteWithIdea
import kotlinx.coroutines.NonDisposableHandle
import kotlinx.coroutines.NonDisposableHandle.parent

class ArchivedNoteRecyclerAdapter(context: Context) :
    RecyclerView.Adapter<ArchivedNoteRecyclerAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

    var notes = listOf<ArchivedNoteWithIdea>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = layoutInflater.inflate(R.layout.item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        holder.noteTitle.text = note.title
        holder.noteIdea.text = note.ideaName
        holder.notePosition = position
    }

    override fun getItemCount() = notes.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var noteTitle: TextView = view.findViewById(R.id.textItem)
        var noteIdea: TextView = view.findViewById(R.id.textIdea)
        var notePosition = -1

        init {
            view.setOnClickListener {
                val action = ArchivedNotesFragmentDirections.actionArchivedNotesFragmentToArchivedNoteViewFragment(notePosition)
                view.findNavController().navigate(action)
            }
        }
    }
}
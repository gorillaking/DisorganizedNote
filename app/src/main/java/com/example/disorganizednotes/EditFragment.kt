package com.example.disorganizednotes

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.disorganizednotes.data.*
import com.example.disorganizednotes.databinding.FragmentEditBinding
import com.google.android.material.snackbar.Snackbar

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class EditFragment : Fragment() {

    private var _binding: FragmentEditBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val args: EditFragmentArgs by navArgs()
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var dialog: AlertDialog.Builder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        binding.checkBox.visibility = if(args.notePosition == -1) CheckBox.INVISIBLE else CheckBox.VISIBLE

        noteViewModel = ViewModelProvider(requireActivity())[NoteViewModel::class.java]

        noteViewModel.getAllIdeas().observe(viewLifecycleOwner) {
            val acAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, it.map { idea -> idea.name })
            binding.acTextIdea.setAdapter(acAdapter)
            acAdapter.notifyDataSetChanged()
        }

        dialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete note?")
            .setMessage("Are you sure you want to delete this note?")
            .setNegativeButton("no"){ _, _ -> run {} }
            .setPositiveButton("yes"){ _, _ ->
                noteViewModel.deleteNote(noteViewModel.getAllNotes().value?.get(args.notePosition)?.noteId!!)
                findNavController().navigate((R.id.action_EditFragment_to_NotesFragment))
            }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val notePosition = args.notePosition

        if(notePosition != -1) {
            (activity as MainActivity).supportActionBar?.title = "Edit note"
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_edit, menu)

                val deleteItem = menu.findItem(R.id.action_edit_delete)
                if(notePosition == -1) {
                    deleteItem.isVisible = false
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
               return when(menuItem.itemId) {
                    R.id.action_edit_delete -> {
                        dialog.show()
                        return true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        if (notePosition != -1) {
            var note = noteViewModel.getAllNotes().value?.get(notePosition)
            binding.editTextTitle.setText(note?.title)
            binding.editTextDescription.setText(note?.description)
            binding.acTextIdea.setText(note?.ideaName)
        }

        binding.buttonSave.setOnClickListener { onClickView ->
            save(onClickView)
            findNavController().navigate(R.id.action_EditFragment_to_NotesFragment)
        }

        binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                val note = noteViewModel.getAllNotes().value?.get(notePosition)
                noteViewModel.moveNoteToArchive(note!!)
                findNavController().navigate(R.id.action_EditFragment_to_NotesFragment)
            }
        }
    }

    private fun save(view: View) {
        if(!validateTitle()) {
            Snackbar.make(view, "Title cannot be empty.", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            return
        }

        saveNote()
    }

    private fun validateTitle(): Boolean {
        val title = (_binding?.editTextTitle?.text) ?: ""
        return title.isNotEmpty()
    }

    private fun saveNote() {
        val notePosition = args.notePosition
        if(notePosition == -1) {
            addNote()
            return
        }

        updateNote()
    }

    private fun addNote() {
        var titleText = _binding?.editTextTitle?.text?.toString() ?: ""
        var descriptionText = _binding?.editTextDescription?.text?.toString() ?: ""
        var ideaText = _binding?.acTextIdea?.text?.toString() ?: ""

        noteViewModel.addNote(titleText, descriptionText, ideaText)
    }

    private fun updateNote() {
        var note: NoteWithIdea? = noteViewModel.getAllNotes().value?.get(args.notePosition)
            ?: return

        var titleText = _binding?.editTextTitle?.text?.toString() ?: ""
        var descriptionText = _binding?.editTextDescription?.text?.toString() ?: ""
        var ideaText = _binding?.acTextIdea?.text?.toString() ?: ""

        if (note != null) {
            noteViewModel.updateNote(note.noteId,
                titleText,
                descriptionText,
                ideaText,
                note.prevNoteId,
                note.nextNoteId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
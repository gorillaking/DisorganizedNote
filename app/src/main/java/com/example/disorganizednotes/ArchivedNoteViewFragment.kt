package com.example.disorganizednotes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.disorganizednotes.data.NoteViewModel
import com.example.disorganizednotes.databinding.FragmentArchivedNoteViewBinding

/**
 * A simple [Fragment] subclass.
 * Use the [ArchivedNoteViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ArchivedNoteViewFragment : Fragment() {

    private var _binding: FragmentArchivedNoteViewBinding? = null
    private val binding get() = _binding!!
    private val args: ArchivedNoteViewFragmentArgs by navArgs()
    private lateinit var noteViewModel: NoteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentArchivedNoteViewBinding.inflate(inflater, container, false)

        noteViewModel = ViewModelProvider(requireActivity())[NoteViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val notePosition = args.notePosition
        var note = noteViewModel.getAllArchivedNote().value?.get(notePosition)
        binding.textNoteTitle.text = note?.title
        binding.textNoteDescription.text = note?.description
        binding.textNoteIdea.text = note?.ideaName

        binding.checkBox2.setOnCheckedChangeListener { _, isChecked ->
            if(!isChecked) {
                noteViewModel.moveNoteOutOfArchive(note!!)
                val navController = findNavController()
                navController.navigate(R.id.action_archivedNoteViewFragment_to_archivedNotesFragment)
            }
        }
    }
}
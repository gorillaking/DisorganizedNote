package com.example.disorganizednotes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.disorganizednotes.data.ArchivedNote
import com.example.disorganizednotes.data.NoteViewModel
import com.example.disorganizednotes.databinding.FragmentArchivedNotesBinding

/**
 * A simple [Fragment] subclass.
 * Use the [ArchivedNotesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ArchivedNotesFragment : Fragment() {

    private var _binding: FragmentArchivedNotesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: NoteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentArchivedNotesBinding.inflate(inflater, container, false)

        val navController = findNavController()
        navController.popBackStack(R.id.archivedNoteViewFragment, true)

        var adapter = ArchivedNoteRecyclerAdapter(requireContext())
        viewModel = ViewModelProvider(requireActivity())[NoteViewModel::class.java]
        viewModel.getAllArchivedNote().observe(viewLifecycleOwner) {
            adapter.notes = it
            adapter.notifyDataSetChanged()
        }

        val listItems = binding.archivedListItems
        listItems.layoutManager = LinearLayoutManager(context)
        listItems.adapter = adapter

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
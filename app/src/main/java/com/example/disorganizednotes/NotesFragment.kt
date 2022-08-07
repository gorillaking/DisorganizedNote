package com.example.disorganizednotes

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.disorganizednotes.data.NoteViewModel
import com.example.disorganizednotes.databinding.FragmentNotesBinding
import java.util.*
import kotlin.math.sign

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher?.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentNotesBinding.inflate(inflater, container, false)

        var adapter = NoteRecyclerAdapter(requireContext())
        viewModel = ViewModelProvider(requireActivity())[NoteViewModel::class.java]
        viewModel.getAllNotes().observe(viewLifecycleOwner) {
            adapter.notes = it
            adapter.notifyDataSetChanged()
        }

        val listItems = binding.listItems
        listItems.layoutManager = LinearLayoutManager(context)
        listItems.adapter = adapter
        itemTouchHelper.attachToRecyclerView(listItems)

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_NotesFragment_to_EditFragment)
        }

        return binding.root
    }

    private val itemTouchHelper = ItemTouchHelper(
        object: ItemTouchHelper.Callback(){
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return makeMovementFlags(ItemTouchHelper.DOWN or ItemTouchHelper.UP or ItemTouchHelper.START or ItemTouchHelper.END, 0)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition

                val adapter = (recyclerView.adapter as NoteRecyclerAdapter)
                Collections.swap(adapter.notes, fromPosition, toPosition)
                adapter.notifyItemMoved(fromPosition, toPosition)

                return false
            }

            override fun interpolateOutOfBoundsScroll(
                recyclerView: RecyclerView,
                viewSize: Int,
                viewSizeOutOfBounds: Int,
                totalSize: Int,
                msSinceStartScroll: Long
            ): Int {
                val direction = viewSizeOutOfBounds.sign
                return 20 * direction
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)

                val adapter = (recyclerView.adapter as NoteRecyclerAdapter)
                adapter.notes
                viewModel.updateNoteOrders(adapter.notes)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

        })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_notes, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId) {
                    R.id.action_go_to_archive -> {
                        findNavController().navigate(R.id.action_NotesFragment_to_archivedNotesFragment)
                        return true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
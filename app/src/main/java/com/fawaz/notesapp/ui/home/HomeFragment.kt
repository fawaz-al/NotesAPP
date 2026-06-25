package com.fawaz.notesapp.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fawaz.notesapp.NotesViewModel
import com.fawaz.notesapp.R
import com.fawaz.notesapp.data.entity.Notes
import com.fawaz.notesapp.databinding.FragmentHomeBinding
import com.fawaz.notesapp.utils.ExtensionFunction.setActionBar
import com.fawaz.notesapp.utils.HelperFunction
import com.fawaz.notesapp.utils.HelperFunction.checkDataIsEmpty
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment(), SearchView.OnQueryTextListener {

    private var _binding: FragmentHomeBinding?=null
    private val binding get() = _binding as FragmentHomeBinding

    private  val homeViewModel by viewModels<NotesViewModel> ()
    private val  homeAdapter by lazy { HomeAdapter() }

    private var _currentData: List<Notes>? = null
    private val currentData get() = _currentData as List<Notes>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mHelperFunctions = HelperFunction

        setHasOptionsMenu(true)

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbarHome.setActionBar(requireActivity())

        binding.fabAdd.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_addFragment)
        }

        setupRecycleView()
    }

    private fun setupRecycleView() {
        binding.rvHome.apply {
            homeViewModel.getAllData().observe(viewLifecycleOwner) {
                checkDataIsEmpty(it)
                showEmptyDataLayout(it)
                homeAdapter.setData(it)
            }
            adapter = homeAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    private fun showEmptyDataLayout(data: List<Notes>) {
        if (data.isEmpty()) {
            // Data KOSONG: Munculkan gambar & tombol, Sembunyikan RecyclerView
            binding.rvHome.visibility = View.GONE
            binding.imgNoNotes.visibility = View.VISIBLE
            binding.btnToDetail.visibility = View.VISIBLE
        } else {
            // Data ADA: Tampilkan RecyclerView, Sembunyikan gambar & tombol
            binding.rvHome.visibility = View.VISIBLE
            binding.imgNoNotes.visibility = View.GONE
            binding.btnToDetail.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
        super.onCreateOptionsMenu(menu, inflater)

        val search = menu.findItem(R.id.menu_search)
        val searchAction = search.actionView as? SearchView
        searchAction?.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_priority_high -> homeViewModel.sortByHighPriority().observe(this){
                homeAdapter.setData(it)
            }
            R.id.menu_priority_low -> homeViewModel.sortByLowPriority().observe(this){
                homeAdapter.setData(it)
            }
            R.id.menu_delete_all -> confirmDeleteAll()
        }
        return super.onOptionsItemSelected(item)
    }
    private fun confirmDeleteAll() {
        AlertDialog.Builder(requireContext())
    }

    // akan dijalankan ketika memasukkan search
    override fun onQueryTextSubmit(query: String?): Boolean {
        val querySearch = "%$query%"
        query?.let {
            homeViewModel.searchByQuery(querySearch).observe(this) {
                homeAdapter.setData(it)
            }
        }
        return true
    }

    // akan dijalankan setiap ada perubahan yang ada di table
    override fun onQueryTextChange(newText: String?): Boolean {
        val querySearch = "%$newText%"
        newText?.let {
            homeViewModel.searchByQuery(querySearch).observe(this) {
                homeAdapter.setData(it)
            }
        }
        return true
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDelete = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItem = homeAdapter.listNotes[viewHolder.adapterPosition]
                homeViewModel.deleteNotes(deletedItem)
                restoredData(viewHolder.itemView, deletedItem)
            }

        }
    }

    private fun restoredData(view: View, deletedItem: Notes) {
        val snackBar = Snackbar.make(
            view, "Deleted: '${deletedItem.title}'", Snackbar.LENGTH_LONG
        )
        snackBar.setTextColor(ContextCompat.getColor(view.context, R.color.black))
        snackBar.setAction("Undo") {
            homeViewModel.insertData(deletedItem)
        }
        snackBar.setActionTextColor(ContextCompat.getColor(view.context, R.color.black))
        snackBar.show()
    }

    // akan menghancurkan fragmentnya jika tugasnya sudah selesai
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
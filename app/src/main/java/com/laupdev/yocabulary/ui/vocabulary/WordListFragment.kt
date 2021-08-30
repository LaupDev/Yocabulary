package com.laupdev.yocabulary.ui.vocabulary

import android.os.Bundle
import android.view.*
import android.view.View.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.adapters.WordAdapter
import com.laupdev.yocabulary.application.DictionaryApplication
import com.laupdev.yocabulary.database.Word
import com.laupdev.yocabulary.databinding.FragmentWordListBinding
import com.laupdev.yocabulary.model.VocabularyViewModel
import com.laupdev.yocabulary.model.VocabularyViewModelFactory
import timber.log.Timber
import java.util.*

class WordListFragment : Fragment() {

    companion object {
//        const val LETTER = "letter"
    }

    private var _binding: FragmentWordListBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WordAdapter

    private var sortMode = SortModes.BY_DATE_MODIFIED

    private val viewModel: VocabularyViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(
            this,
            VocabularyViewModelFactory((activity.application as DictionaryApplication).repository)
        )
            .get(VocabularyViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("onCreate()")
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordListBinding.inflate(inflater, container, false)
//        (activity as AppCompatActivity).setSupportActionBar(binding.topAppBar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.i("onViewCreated()")
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = WordAdapter(viewModel)
        recyclerView.adapter = adapter


        setObservers()

        setListeners()
    }

    private fun setListeners() {
        binding.searchInDictionary.setOnClickListener {
            val action =
                VocabularyHomeFragmentDirections.showWordDetails(
                    word = binding.wordSearch.query.toString(),
                    isInVocabulary = false
                )
            requireView().findNavController().navigate(action)
        }

        binding.wordSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(searchQuery: String?): Boolean {
                binding.searchInDictionary.visibility = if (searchQuery?.isNotEmpty() == true) {
                    VISIBLE
                } else {
                    GONE
                }
                adapter.filter.filter(searchQuery)
                return false
            }

        })
    }

    private fun setObservers() {
        viewModel.allWords.observe(viewLifecycleOwner) { words ->
            words?.let {
                sortWords(it, sortMode)
            }
        }

        viewModel.status.observe(viewLifecycleOwner, {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sort_words_menu, menu)
        menu.getItem(0).subMenu.getItem(sortMode.menuPosition).isChecked = true
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sort_by_date -> {
                item.isChecked = true
                sortWords(viewModel.allWords.value ?: listOf(), SortModes.BY_DATE_MODIFIED)
                return true
            }
            R.id.sort_by_word -> {
                item.isChecked = true
                sortWords(viewModel.allWords.value ?: listOf(), SortModes.BY_NAME)
                return true
            }
            R.id.sort_by_favorite -> {
                item.isChecked = true
                sortWords(viewModel.allWords.value ?: listOf(), SortModes.BY_IS_FAVORITE)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun sortWords(words: List<Word>, sortMode: SortModes) {
        if (words.isNotEmpty()) {
            this.sortMode = sortMode
            when (sortMode) {
                SortModes.BY_DATE_MODIFIED -> {
                    adapter.submitList(words.sortedByDescending { word -> word.dateAdded } as MutableList<Word>)
                }
                SortModes.BY_NAME -> {
                    adapter.submitList(words.sortedBy { word -> word.word.lowercase() } as MutableList<Word>)
                }
                SortModes.BY_IS_FAVORITE -> {
                    adapter.submitList(words.filter { word -> word.isFavourite == 1 } as MutableList<Word>)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}

enum class SortModes(val menuPosition: Int) {
    BY_DATE_MODIFIED(0),
    BY_NAME(1),
    BY_IS_FAVORITE(2)

}
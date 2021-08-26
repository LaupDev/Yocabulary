package com.laupdev.yocabulary.ui

import android.os.Bundle
import android.view.*
import android.view.View.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.adapters.WordAdapter
import com.laupdev.yocabulary.application.DictionaryApplication
import com.laupdev.yocabulary.database.Word
import com.laupdev.yocabulary.databinding.FragmentWordListBinding
import com.laupdev.yocabulary.model.VocabularyViewModel
import com.laupdev.yocabulary.model.VocabularyViewModelFactory
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

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = WordAdapter(viewModel)
        recyclerView.adapter = adapter

//        TabLayoutMediator(binding.vocabularyTabs, viewPager) { tab, position ->
//            Timber.i(position.toString())
//            when(position) {
//                0 -> {
//
//                }
//            }
//        }.attach()

//        binding.addNewWordBtn.setOnClickListener {
//            val action = WordListFragmentDirections.actionWordListFragmentToAddNewWordFragment("")
//            view.findNavController().navigate(action)
//        }

        binding.searchInDictionary.setOnClickListener {
            val action =
                VocabularyHomeFragmentDirections.showWordDetails(
                    word = binding.wordSearch.query.toString(),
                    isInVocabulary = false
                )
            view.findNavController().navigate(action)
        }

        viewModel.allWords.observe(viewLifecycleOwner) { words ->
            words?.let {
                sortWords(it, sortMode)
            }
        }

        viewModel.status.observe(viewLifecycleOwner, {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        })
        binding.wordSearch.apply {
            queryHint = resources.getString(R.string.search_words_hint)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(searchQuery: String?): Boolean {
//                    println("----------------SEARCH----------------: " + searchQuery + " -- " + adapter.currentList.size)
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
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sort_words_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.sort_by_date -> {
                item.isChecked = true
                sortWords(viewModel.allWords.value?: listOf(), SortModes.BY_DATE_MODIFIED)
                return true
            }
            R.id.sort_by_word -> {
                item.isChecked = true
                sortWords(viewModel.allWords.value?: listOf(), SortModes.BY_NAME)
                return true
            }
            R.id.sort_by_favorite -> {
                item.isChecked = true
                sortWords(viewModel.allWords.value?: listOf(), SortModes.BY_IS_FAVORITE)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun sortWords(words: List<Word>, sortMode: SortModes) {
        if (words.isNotEmpty()) {
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

enum class SortModes {
    BY_DATE_MODIFIED,
    BY_NAME,
    BY_IS_FAVORITE

}
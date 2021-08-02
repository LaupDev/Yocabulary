package com.laupdev.yocabulary.ui

import android.os.Bundle
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.application.DictionaryApplication
import com.laupdev.yocabulary.database.Word
import com.laupdev.yocabulary.databinding.FragmentWordListBinding
import com.laupdev.yocabulary.model.VocabularyViewModel
import com.laupdev.yocabulary.model.VocabularyViewModelFactory

class WordListFragment : Fragment() {

    companion object {
        const val LETTER = "letter"
    }

    private var _binding: FragmentWordListBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var letterId: String
    private lateinit var adapter: WordAdapter

    private var sortMode = 0 // 0 -> Date added; 1 -> Word; 2 -> Favorite

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
        arguments?.let {
            letterId = it.getString(LETTER).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordListBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).setSupportActionBar(binding.topAppBar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = WordAdapter(viewModel)
        recyclerView.adapter = adapter

//        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
//        binding.wordSearch.apply {
//            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
//            setIconifiedByDefault(false)
//        }

        binding.addNewWordBtn.setOnClickListener {
            val action = WordListFragmentDirections.actionWordListFragmentToAddNewWordFragment()
            view.findNavController().navigate(action)
        }

        binding.searchInDictionary.setOnClickListener {
            val action =
                WordListFragmentDirections.actionWordListFragmentToWordDetailsFragment(
                    wordName = binding.wordSearch.query.toString()
                )
            view.findNavController().navigate(action)
        }

        // TODO: 02.08.2021 Fix bug. Recreation: 1. Enter some word in search 2. Move to another fragment 3. Go back

//        if (letterId == "recent") {
        viewModel.allWords.observe(viewLifecycleOwner) { words ->
            words?.let {
                println("--------------submitList-----------: " + it.size)
                sortWords(it, sortMode)
//                adapter.submitList(it.sortedByDescending { word -> word.wordId } as MutableList<Word>)
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
                    binding.searchInDictionary.visibility = if (searchQuery?.isNotEmpty() == true) {
                        VISIBLE
                    } else {
                        INVISIBLE
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
                sortWords(viewModel.allWords.value?: listOf(), 0)
                return true
            }
            R.id.sort_by_word -> {
                item.isChecked = true
                sortWords(viewModel.allWords.value?: listOf(), 1)
                return true
            }
            R.id.sort_by_favorite -> {
                item.isChecked = true
                sortWords(viewModel.allWords.value?: listOf(), 2)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun sortWords(words: List<Word>, sortMode: Int) {
        when(sortMode) {
            0 -> {
                adapter.submitList(words.sortedByDescending { word -> word.wordId } as MutableList<Word>)
            }
            1 -> {
                adapter.submitList(words.sortedBy { word -> word.word.lowercase() } as MutableList<Word>)
            }
            2 -> {
                adapter.submitList(words.filter { word -> word.isFavourite == 1 } as MutableList<Word>)
            }
        }
    }

}
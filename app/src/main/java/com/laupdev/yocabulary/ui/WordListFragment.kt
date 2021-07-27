package com.laupdev.yocabulary.ui

import android.os.Bundle
import android.view.*
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
        val adapter = WordAdapter(viewModel)
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

//        if (letterId == "recent") {
        viewModel.allWords.observe(viewLifecycleOwner) { words ->
            words?.let {
                println("--------------submitList-----------: " + it.size)
                adapter.submitList(it.sortedByDescending { word -> word.wordId } as MutableList<Word>)
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

                override fun onQueryTextChange(p0: String?): Boolean {
                    adapter.filter.filter(p0)
                    return false
                }

            })
        }


        // TODO: 27.07.2021 Filter words
//        } else {
//            viewModel.allWords.observe(viewLifecycleOwner, { words ->
//                words?.let {
//                    adapter.submitList(it.filter { word ->
//                        word.word.first().toString().equals(letterId, true)
//                    }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { word -> word.word }))
//                }
//            })
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sort_words_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

}
package com.laupdev.yocabulary.adapters

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.database.Word
import com.laupdev.yocabulary.model.VocabularyViewModel
import com.laupdev.yocabulary.ui.VocabularyHomeFragmentDirections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class WordAdapter(private val viewModel: VocabularyViewModel) : ListAdapter<Word, WordAdapter.WordViewHolder>(
    DiffCallback
), Filterable {
    class WordViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        lateinit var word: Word
        val wordTextView: TextView? = view.findViewById(R.id.word)
        val transTextView: TextView? = view.findViewById(R.id.translation)
        val addWordToFavorite: ImageButton? = view.findViewById(R.id.add_to_favorite)
        val pronounceWordBtn: ImageButton? = view.findViewById(R.id.pronounce_word)

        init {
            view.findViewById<RelativeLayout>(R.id.word_container).setOnClickListener {
                val action = VocabularyHomeFragmentDirections.showWordDetails(word = word.word)
                view.findNavController().navigate(action)
            }
        }
    }

    private var pronounceWordMediaPlayer: MediaPlayer? = null
    private var initialList = currentList
    private var filteredList = currentList
    private var lastQuery = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val layout = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_view, parent, false)
        return WordViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.word = filteredList[position]
        holder.wordTextView?.text = filteredList[position].word
        holder.transTextView?.text = filteredList[position].translations.split("|")[0]
        holder.addWordToFavorite?.isSelected = filteredList[position].isFavourite == 1
        if (filteredList[position].audioUrl.isNotEmpty()) {
            holder.pronounceWordBtn?.isEnabled = true
            holder.pronounceWordBtn?.setOnClickListener {
                playWordPronunciation(holder, filteredList[position].audioUrl)
            }
        } else {
            pronounceWordMediaPlayer = null
            holder.pronounceWordBtn?.isEnabled = false
        }
        holder.addWordToFavorite?.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                if (viewModel.updateWordIsFavorite(filteredList[position].word, it.isSelected)) {
                    it.isSelected = !it.isSelected
                }
            }
        }
    }

    override fun submitList(list: MutableList<Word>?) {
//        println(Calendar.getInstance().time.time.toString() + "-------------------SUBMIT_LIST------------: " + list?.size)
        super.submitList(list)
        if (list != null) {
            initialList = list
            filteredList = list
        } else {
            initialList = mutableListOf()
            filteredList = mutableListOf()
        }
            filter.filter(lastQuery)
    }

    private fun playWordPronunciation(holder: WordViewHolder, audioUrl: String) {
        pronounceWordMediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(audioUrl)
            isLooping = false
            setOnPreparedListener {
                it.start()
            }
            setOnCompletionListener {
                if (holder.view.isEnabled) {
                    holder.pronounceWordBtn?.isSelected = false
                }
                it.release()
            }
            prepareAsync()
            holder.pronounceWordBtn?.isSelected = true
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Word>() {
        override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem.word == newItem.word
        }

        override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem == newItem
        }

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
//                println(Calendar.getInstance().time.time.toString() + "--------------FILTER---------------" + initialList.size)
                lastQuery = constraint.toString()
                var resultList = initialList
                if (lastQuery.isNotEmpty()) {
                    resultList = resultList.filter {
                        it.word.lowercase().contains(lastQuery.lowercase())
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = resultList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
//                println("--------------PUBLISH---------------")
                filteredList = results?.values as List<Word>
                notifyDataSetChanged()
            }

        }
    }

}
package com.laupdev.yocabulary.ui

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
                val action = WordListFragmentDirections.actionWordListFragmentToWordDetailsFragment(wordId = word.wordId)
                view.findNavController().navigate(action)
            }
        }
    }

    private var pronounceWordMediaPlayer: MediaPlayer? = null
    private var initialList = currentList
    private var filteredList = currentList

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
            holder.pronounceWordBtn?.setOnClickListener {
                playWordPronunciation(holder, filteredList[position].audioUrl)
            }
        } else {
            holder.pronounceWordBtn?.isEnabled = false
        }
        holder.addWordToFavorite?.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                if (viewModel.updateWordIsFavorite(filteredList[position].wordId, it.isSelected)) {
                    it.isSelected = !it.isSelected
                }
            }
        }
    }

    override fun submitList(list: MutableList<Word>?) {
        super.submitList(list)
        if (!list.isNullOrEmpty()) {
            filteredList = list
            initialList = list
            filter.filter("")
        }
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
            return oldItem.wordId == newItem.wordId
        }

        override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem == newItem
        }

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                var resultList = initialList
                if (charSearch.isNotEmpty()) {
                    resultList = resultList.filter {
                        it.word.lowercase().contains(charSearch.lowercase())
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = resultList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as List<Word>
                notifyDataSetChanged()
            }

        }
    }

}
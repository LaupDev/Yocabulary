package com.laupdev.yocabulary.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.database.Word

class WordAdapter() : ListAdapter<Word, WordAdapter.WordViewHolder>(
    DiffCallback
) {

    class WordViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val wordContainer: View? = view.findViewById(R.id.word_container)
        val wordTextView: TextView? = view.findViewById(R.id.word)
        val transTextView: TextView? = view.findViewById(R.id.translation)
        val addWordToFavorite: ImageView? = view.findViewById(R.id.add_to_favorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val layout = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_view, parent, false)
        return WordViewHolder(layout)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.wordTextView?.text = currentList[position].word
//        holder.transTextView?.text = currentList[position].translation
//        holder.addWordToFavorite?.contentDescription = holder.view.context.getString(R.string.add_word_to_favorite, currentList[position].word) // Check how it works
        holder.wordContainer?.setOnClickListener {
            val action = WordListFragmentDirections.actionWordListFragmentToWordDetailsFragment(wordId = currentList[position].wordId)
            holder.view.findNavController().navigate(action)
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

}
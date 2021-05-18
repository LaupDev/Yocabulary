package com.laupdev.yourdictionary.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.laupdev.yourdictionary.R
import com.laupdev.yourdictionary.database.Word

class WordAdapter() : ListAdapter<Word, WordAdapter.WordViewHolder>(
    DiffCallback
) {

    class WordViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val button: Button? = view.findViewById(R.id.button_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val layout = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_view, parent, false)
        return WordViewHolder(layout)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.button?.text = currentList[position].word
        holder.button?.setOnClickListener {
            val action = WordListFragmentDirections.actionWordListFragmentToWordDetailsFragment(wordId = currentList[position].id)
            holder.view.findNavController().navigate(action)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Word>() {
        override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem == newItem
        }

    }

}
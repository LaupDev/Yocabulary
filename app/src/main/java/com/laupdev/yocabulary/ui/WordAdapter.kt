package com.laupdev.yocabulary.ui

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.database.Word

class WordAdapter : ListAdapter<Word, WordAdapter.WordViewHolder>(
    DiffCallback
) {
    // TODO: 08.07.2021 When word is too long -> add three dots
    class WordViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        lateinit var word: Word
//        val wordContainer: View? = view.findViewById(R.id.word_container)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val layout = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_view, parent, false)
        return WordViewHolder(layout)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.word = currentList[position]
        holder.wordTextView?.text = currentList[position].word
        holder.transTextView?.text = currentList[position].translations.split("|")[0]
        holder.addWordToFavorite?.isSelected = currentList[position].isFavourite == 1
        if (currentList[position].audioUrl.isNotEmpty()) {
            holder.pronounceWordBtn?.setOnClickListener {
                playWordPronunciation(holder, currentList[position].audioUrl)
            }
        }
//        holder.transTextView?.text = currentList[position].translation
//        holder.addWordToFavorite?.contentDescription = holder.view.context.getString(R.string.add_word_to_favorite, currentList[position].word) // Check how it works

//        println("----WORD: " + currentList[position].word + " -- Pos: " + position + " ------------")
//        holder.wordContainer?.setOnClickListener {
//            val action = WordListFragmentDirections.actionWordListFragmentToWordDetailsFragment(wordId = currentList[position].wordId)
//            holder.view.findNavController().navigate(action)
//        }
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

}
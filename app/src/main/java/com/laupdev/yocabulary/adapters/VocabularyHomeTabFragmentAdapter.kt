package com.laupdev.yocabulary.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.laupdev.yocabulary.ui.vocabulary.WordListFragment
import com.laupdev.yocabulary.ui.vocabulary.WordSetsListFragment

class VocabularyHomeTabFragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            WordListFragment()
        } else {
            WordSetsListFragment()
        }
    }
}
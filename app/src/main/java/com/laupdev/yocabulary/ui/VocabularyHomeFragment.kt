package com.laupdev.yocabulary.ui

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.adapters.VocabularyHomeTabFragmentAdapter
import com.laupdev.yocabulary.databinding.FragmentVocabularyHomeBinding

class VocabularyHomeFragment : Fragment() {

    private var _binding: FragmentVocabularyHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVocabularyHomeBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).setSupportActionBar(binding.topAppBar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = binding.pager
        viewPager.isUserInputEnabled = false
        viewPager.adapter = VocabularyHomeTabFragmentAdapter(this)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 0) {
                    binding.addNewWordBtn.visibility = VISIBLE
                } else {
                    binding.addNewWordBtn.visibility = GONE
                }
            }
        })

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            viewPager.layoutParams.height =
//                requireActivity().windowManager.currentWindowMetrics.bounds.height() - binding.topAppBarLayout.height
//        } else {
//            val size = Point()
//            @Suppress("DEPRECATION")
//            (requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getSize(size)
//            viewPager.layoutParams.height = size.y
//        }

        binding.addNewWordBtn.setOnClickListener {
            val action = VocabularyHomeFragmentDirections.addNewWord("")
            view.findNavController().navigate(action)
        }

        TabLayoutMediator(binding.vocabularyTabs, viewPager) { tab, position ->
            if (position == 0) {
                tab.text = "MY WORDS"
                tab.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_order_alphabetical_24)
            } else {
                tab.text = "MY SETS"
                tab.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_sets_24)
            }
        }.attach()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

}
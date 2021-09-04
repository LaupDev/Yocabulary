package com.laupdev.yocabulary.ui.practice

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.laupdev.yocabulary.R
import com.laupdev.yocabulary.adapters.PracticeQuestionAdapter
import com.laupdev.yocabulary.model.practice.BasePracticeViewModel
import com.laupdev.yocabulary.ui.MainActivity

open class BasePracticeFragment : Fragment() {

    private val viewModel: BasePracticeViewModel by viewModels()
    private lateinit var viewPager2: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            (requireActivity() as MainActivity).showBottomNav()
            findNavController().popBackStack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager2 = view.findViewById(R.id.pager)
        viewPager2.adapter = PracticeQuestionAdapter(viewModel)
//        viewPager2.canScrollHorizontally()
//        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//            override fun onPageScrolled(
//                position: Int,
//                positionOffset: Float,
//                positionOffsetPixels: Int
//            ) {
//                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
//            }
//
//            override fun onPageScrollStateChanged(state: Int) {
//                super.onPageScrollStateChanged(state)
//
//                viewPager2.isUserInputEnabled = !(state == SCROLL_STATE_DRAGGING && viewPager2.currentItem == 0)
//            }
//        })

        (requireActivity() as MainActivity).hideBottomNav()

        view.findViewById<MaterialToolbar>(R.id.top_app_bar).setNavigationOnClickListener {
            (requireActivity() as MainActivity).showBottomNav()
            findNavController().popBackStack()
        }

        viewModel.practiceProgress.observe(viewLifecycleOwner) {
            view.findViewById<LinearProgressIndicator>(R.id.practice_progress_bar).setProgressCompat(it, true)
        }
    }

    fun nextPage() {
        // TODO: 04.09.2021 Go to next page
        viewModel.increaseProgress()
    }
}
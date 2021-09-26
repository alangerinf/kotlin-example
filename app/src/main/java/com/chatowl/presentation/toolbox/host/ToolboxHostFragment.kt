package com.chatowl.presentation.toolbox.host

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.chatowl.R
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.journal.JournalFragment
import com.chatowl.presentation.messages.MessagesFragment
import com.chatowl.presentation.moodmeter.MoodMeterFragment
import com.chatowl.presentation.tabs.TabFragment
import com.chatowl.presentation.tabs.TabViewModel
import com.chatowl.presentation.toolbox.home.ToolboxHomeFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_toolbox_host.view.*

@ExperimentalStdlibApi
class ToolboxHostFragment :
    ViewModelFragment<ToolboxHostViewModel>(ToolboxHostViewModel::class.java),
    ViewPagerFragmentInteraction {

    companion object {
        private const val NUM_PAGES = 3
        const val PAGE_HOME = 0 //home is 0, journal is 1 and so on...
        const val PAGE_JOURNAL = 1 //home is 0, journal is 1 and so on...
        const val PAGE_MOOD = 2


        private const val PAGE_HOME_INDEX = 0 //
        private const val PAGE_JOURNAL_INDEX = 1 //
        private const val PAGE_MOOD_INDEX = 2 //

        //TODO add the other page's numbers, as they be used
    }

    private lateinit var viewPager: ViewPager2
    private lateinit var tabViewModel: TabViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tabFragment = requireParentFragment().requireParentFragment() as TabFragment
        tabViewModel = ViewModelProvider(tabFragment).get(TabViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_toolbox_host, container, false)

        viewPager = v.fragment_toolbox_host_view_pager
        viewPager.isUserInputEnabled = false
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = pagerAdapter
        viewPager.setOnClickListener { Log.e("ToolBox", "pagerClick") }
        viewPager.setOnTouchListener(object : View.OnTouchListener {
            val len = 10
            var deltaY: Float = 0F
            override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
               // Toast.makeText(p0?.context, "pager", Toast.LENGTH_SHORT).show()
                Log.e("ToolBox", "pagerTouch")
                /*when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        p0?.performClick()
                    }
                    MotionEvent.ACTION_UP -> {

                        //p0?.performClick()
                    }
                }*/
                return false
            }
        })

        val tabLayout = v.fragment_toolbox_host_tab_layout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.icon =
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_exercises_tab)
                }
                1 -> {
                    tab.icon =
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_journal_tab)
                }
                2 -> {
                    tab.icon =
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_mood_meter_tab)
                }
                3 -> {
                    tab.icon =
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_envelope_tab)
                }
            }
        }.attach()

        return v
    }

    override fun addObservers() {
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
    }

    override fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
    }

    private inner class ScreenSlidePagerAdapter(fa: Fragment) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = NUM_PAGES

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    ToolboxHomeFragment(this@ToolboxHostFragment)
                }
                1 -> {
                    JournalFragment(this@ToolboxHostFragment)
                }
                2 -> {
                    MoodMeterFragment(this@ToolboxHostFragment)
                }
                else -> {
                    MessagesFragment(this@ToolboxHostFragment)
                }
            }
        }
    }

    override fun onPageFragmentAction(navDirections: NavDirections) {
        findNavController().navigate(navDirections)
    }

    override fun onResume() {
        super.onResume()

        viewPager.post {
            val toolId = tabViewModel.getNavigationParam()
            when (toolId) {
                PAGE_JOURNAL -> {
                    viewPager.setCurrentItem(PAGE_JOURNAL_INDEX)
                }
                PAGE_HOME -> {
                    viewPager.setCurrentItem(PAGE_HOME_INDEX)
                }
                PAGE_MOOD -> {
                    viewPager.setCurrentItem(PAGE_MOOD_INDEX)
                }
            }
        }

    }
}
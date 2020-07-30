package com.manu.connect.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.manu.connect.view.ui.fragments.ChatFragment
import com.manu.connect.view.ui.fragments.SearchFragment
import com.manu.connect.view.ui.fragments.SettingsFragment

class ViewPagerAdapter(fragmentActivity : FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    companion object {
        internal const val TRANSACTION_SCREENS_NUMBER = 3
        internal const val CHATS_SCREEN_POSITION = 0
        internal const val SEARCH_SCREEN_POSITION = 1
        internal const val SETTINGS_SCREEN_POSITION = 2
    }

    override fun getItemCount(): Int {
        return TRANSACTION_SCREENS_NUMBER
    }

    override fun createFragment(position: Int): Fragment = when (position) {
        CHATS_SCREEN_POSITION -> ChatFragment()
        SEARCH_SCREEN_POSITION -> SearchFragment()
        SETTINGS_SCREEN_POSITION -> SettingsFragment()
        else -> throw IllegalStateException("Invalid adapter position")
    }
}

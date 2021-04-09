package studio.ultoolapp.rvadapterlab

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import studio.ultoolapp.rvadapterlab.databinding.ActivityMainBinding
import studio.ultoolapp.rvadapterlab.view.HeaderListFragment
import studio.ultoolapp.rvadapterlab.view.SimpleListFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        ViewPagerAdapter(supportFragmentManager).run {
            addFragment(SimpleListFragment(), getString(R.string.tab_simple_list))
            addFragment(HeaderListFragment(), getString(R.string.tab_header_list))
            binding.viewPager.adapter = this
        }
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    internal class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val mFragmentList: MutableList<Fragment> = ArrayList()
        private val mFragmentTitleList: MutableList<String> = ArrayList()
        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList[position]
        }
    }
}
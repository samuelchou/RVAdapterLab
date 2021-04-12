package studio.ultoolapp.rvadapterlab

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import studio.ultoolapp.rvadapterlab.databinding.ActivityMainBinding
import studio.ultoolapp.rvadapterlab.view.HeaderListFragment
import studio.ultoolapp.rvadapterlab.view.SimpleListFragment
import studio.ultoolapp.rvadapterlab.view.component.ViewPagerAdapter

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
}
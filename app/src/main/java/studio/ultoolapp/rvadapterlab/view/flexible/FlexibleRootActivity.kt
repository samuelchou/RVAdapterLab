package studio.ultoolapp.rvadapterlab.view.flexible

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import studio.ultoolapp.rvadapterlab.R
import studio.ultoolapp.rvadapterlab.databinding.ActivityFlexibleRootBinding
import studio.ultoolapp.rvadapterlab.view.HeaderListFragment
import studio.ultoolapp.rvadapterlab.view.SimpleListFragment
import studio.ultoolapp.rvadapterlab.view.component.ViewPagerAdapter

class FlexibleRootActivity : AppCompatActivity() {
    companion object {
        fun getInitializeIntent(context: Context): Intent =
            Intent(context, FlexibleRootActivity::class.java)
    }

    private lateinit var binding: ActivityFlexibleRootBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_flexible_root)

        ViewPagerAdapter(supportFragmentManager).run {
            addFragment(SimpleListFragment(), getString(R.string.tab_simple_list))
            addFragment(HeaderListFragment(), getString(R.string.tab_header_list))
            binding.viewPager.adapter = this
        }
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }
}
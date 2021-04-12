package studio.ultoolapp.rvadapterlab.view.epoxy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import studio.ultoolapp.rvadapterlab.R
import studio.ultoolapp.rvadapterlab.databinding.ActivityEpoxyRootBinding
import studio.ultoolapp.rvadapterlab.view.component.ViewPagerAdapter

class EpoxyRootActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEpoxyRootBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_epoxy_root)

        ViewPagerAdapter(supportFragmentManager).run {
            addFragment(SimpleEpoxyFragment(), getString(R.string.tab_simple_list))
            addFragment(HeaderEpoxyFragment(), getString(R.string.tab_header_list))
            binding.viewPager.adapter = this
        }
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }
}
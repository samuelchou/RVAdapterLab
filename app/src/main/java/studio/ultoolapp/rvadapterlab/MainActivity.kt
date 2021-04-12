package studio.ultoolapp.rvadapterlab

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import studio.ultoolapp.rvadapterlab.databinding.ActivityMainBinding
import studio.ultoolapp.rvadapterlab.view.epoxy.EpoxyRootActivity
import studio.ultoolapp.rvadapterlab.view.flexible.FlexibleRootActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.btnOpenFlexible.setOnClickListener {
            FlexibleRootActivity.getInitializeIntent(this).also { startActivity(it) }
        }

        binding.btnOpenEpoxy.setOnClickListener {
            Intent(this, EpoxyRootActivity::class.java).apply {
                startActivity(this)
            }
        }
    }
}
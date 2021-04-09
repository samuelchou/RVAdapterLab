package studio.ultoolapp.rvadapterlab.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import studio.ultoolapp.rvadapterlab.R
import studio.ultoolapp.rvadapterlab.databinding.FragmentSimpleListBinding

class SimpleListFragment : Fragment() {

    private lateinit var binding: FragmentSimpleListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSimpleListBinding.inflate(inflater, container, false)

        binding.textView.text = "Hello Data-Binding Text."
        return binding.root
    }

}
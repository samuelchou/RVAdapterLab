package studio.ultoolapp.rvadapterlab.view.epoxy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import studio.ultoolapp.rvadapterlab.databinding.FragmentHeaderEpoxyBinding

class HeaderEpoxyFragment : Fragment() {
    private lateinit var binding: FragmentHeaderEpoxyBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHeaderEpoxyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textView.text = "Hello Epoxy Header"
    }
}
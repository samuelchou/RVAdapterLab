package studio.ultoolapp.rvadapterlab.view.epoxy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.airbnb.epoxy.stickyheader.StickyHeaderLinearLayoutManager
import studio.ultoolapp.rvadapterlab.databinding.FragmentHeaderEpoxyBinding
import studio.ultoolapp.rvadapterlab.metadata.SimpleItem

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
        val itemLists = getDummyLists(100)

        StickyHeaderEpoxyItemAdapter().apply {
            binding.itemListRecycler.layoutManager = StickyHeaderLinearLayoutManager(requireContext())
            binding.itemListRecycler.adapter = this
            updateList(itemLists)
        }
    }

    fun getDummyLists(amount: Int): List<SimpleItem> {
        return mutableListOf<SimpleItem>().apply {
            for (i in 0 until amount) {
                add(SimpleItem("Title $i", "Subtitle $i"))
            }
        }
    }
}
package studio.ultoolapp.rvadapterlab.view.flexible

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import eu.davidea.flexibleadapter.FlexibleAdapter
import studio.ultoolapp.rvadapterlab.databinding.FragmentHeaderListBinding
import studio.ultoolapp.rvadapterlab.metadata.SimpleItem

class HeaderFlexibleFragment : Fragment() {

    private lateinit var binding: FragmentHeaderListBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHeaderListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mDataList = getDummyItemList(30)
        val mAdapter = FlexibleAdapter(mDataList.mapIndexed { index, simpleItem ->
            if (index % 5 == 1) {
                SimpleFlexibleHeader(simpleItem)
            } else {
                SimpleFlexibleItem(simpleItem)
            }
        })

        mAdapter.setDisplayHeadersAtStartUp(true)
                .setStickyHeaders(true) // BOTH has to be set true.

        binding.itemListRecycler.apply {
            layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = mAdapter
        }
    }

    private fun getDummyItemList(amount: Int): List<SimpleItem> =
            mutableListOf<SimpleItem>().apply {
                for (i in 0..amount) add(SimpleItem("Title $i", "Subtitle $i"))
            }
}
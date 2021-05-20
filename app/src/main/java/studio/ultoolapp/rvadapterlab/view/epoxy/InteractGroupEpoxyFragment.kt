package studio.ultoolapp.rvadapterlab.view.epoxy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.airbnb.epoxy.stickyheader.StickyHeaderLinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import studio.ultoolapp.rvadapterlab.databinding.FragmentInteractEpoxyBinding
import studio.ultoolapp.rvadapterlab.metadata.DateAmountItem
import studio.ultoolapp.rvadapterlab.metadata.toCurrencyFormat
import studio.ultoolapp.rvadapterlab.metadata.toYMDPlainString
import java.util.*

class InteractGroupEpoxyFragment : Fragment() {
    private lateinit var binding: FragmentInteractEpoxyBinding

    private val itemLists = mutableListOf<DateAmountItem>()
    private var listAdapter: InteractEpoxyItemAdapter? = null
    private var secondsReversed = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentInteractEpoxyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemLists.addAll(getDummyLists(100))

        binding.itemListRecycler.layoutManager = StickyHeaderLinearLayoutManager(requireContext())
        listAdapter = InteractEpoxyItemAdapter().apply {
            itemClickListener = object : InteractEpoxyItemAdapter.OnItemClickListener {
                override fun onItemClick(rootView: View, item: DateAmountItem, index: Int) {
                    clickItem(rootView, item, index)
                }
            }

            binding.itemListRecycler.adapter = this
            updateList(itemLists)
        }

        binding.btnAdd.setOnClickListener {
            addItem().also {
                Snackbar.make(
                    binding.root,
                    "Added item at ${it.date.toYMDPlainString()}",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun addItem(): DateAmountItem {
        secondsReversed += 27351
        return Calendar.getInstance().run {
            add(Calendar.SECOND, -secondsReversed)
            time
        }.let { date ->
            DateAmountItem(date, (secondsReversed.toDouble() / 2.12) % 324)
        }.also {
            itemLists.add(0, it)
            listAdapter?.updateList(itemLists)
        }
    }

    private fun clickItem(holderView: View, item: DateAmountItem, index: Int) {
        Toast.makeText(
            context,
            "Removed item $index: ${item.amount.toCurrencyFormat()} at ${item.date.toYMDPlainString()}",
            Toast.LENGTH_SHORT
        ).show()
        itemLists.remove(item)
        listAdapter?.updateList(itemLists)
    }

    private fun getDummyLists(amount: Int): List<DateAmountItem> {
        return mutableListOf<DateAmountItem>().apply {
            Calendar.getInstance().let { cal ->
                for (i in 0 until amount) {
                    cal.add(Calendar.SECOND, 27351)
                    add(DateAmountItem(cal.time, (i + 1) * 485.2 % 324))
                }
            }
        }
    }
}
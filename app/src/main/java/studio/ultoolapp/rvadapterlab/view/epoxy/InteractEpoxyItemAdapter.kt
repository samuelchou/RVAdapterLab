package studio.ultoolapp.rvadapterlab.view.epoxy

import android.util.Log
import android.view.View
import android.widget.Toast
import com.airbnb.epoxy.DataBindingEpoxyModel
import com.airbnb.epoxy.EpoxyAdapter
import com.airbnb.epoxy.stickyheader.StickyHeaderCallbacks
import studio.ultoolapp.rvadapterlab.SimpleHeaderBindingModel_
import studio.ultoolapp.rvadapterlab.SimpleStyleBindingModel_
import studio.ultoolapp.rvadapterlab.metadata.*
import java.util.*

class InteractEpoxyItemAdapter : EpoxyAdapter(), StickyHeaderCallbacks {
    companion object {
        private const val TAG = "InteractEpoxyItemAdapter"
    }

    init {
        enableDiffing()
    }

    fun updateList(list: List<DateAmountItem>) {
        removeAllModels()
        var index = 0
        list.groupBy {
            it.date.toYMDPlainString()
        }.forEach { (dateString, itemList) ->
            if (itemList.isEmpty()) {
                Log.e(TAG, "updateList: found a empty list of dateString $dateString. IGNORED.")
                return@forEach
            }
            val groupDate: Date = dateString.fromYMDToDate() ?: itemList[0].date
            addModel(generateHeaderView(groupDate, itemList.sumByDouble { it.amount }))
            itemList.forEach { item ->
                addModel(item.dataToView(index))
                index++
            }
        }
        notifyModelsChanged()
    }

    private fun generateHeaderView(date: Date, totalAmount: Double): DataBindingEpoxyModel {
        return SimpleHeaderBindingModel_().apply {
            titleText(totalAmount.toCurrencyFormat())
            subtitleText(date.toDayTitleString())
        }
    }

    private fun DateAmountItem.dataToView(index: Int) = let { item ->
        SimpleStyleBindingModel_().apply {
            // only id and xml-defined properties can be called/set here.
            id("view holder $index")
            titleText(item.amount.toCurrencyFormat())
            subtitleText(item.date.toDetailedTimeString())
            clickListener(View.OnClickListener {
                Toast.makeText(
                    it.context,
                    "click item ${item.amount.toCurrencyFormat()} / ${item.date.toDayTitleString()}",
                    Toast.LENGTH_SHORT
                ).show()
            })
        }
    }

    override fun isStickyHeader(position: Int) = models[position] is SimpleHeaderBindingModel_
}
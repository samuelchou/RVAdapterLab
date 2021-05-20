package studio.ultoolapp.rvadapterlab.view.epoxy

import android.util.Log
import android.view.View
import com.airbnb.epoxy.DataBindingEpoxyModel
import com.airbnb.epoxy.EpoxyAdapter
import com.airbnb.epoxy.GroupModel_
import com.airbnb.epoxy.stickyheader.StickyHeaderCallbacks
import studio.ultoolapp.rvadapterlab.R
import studio.ultoolapp.rvadapterlab.SimpleHeaderBindingModel_
import studio.ultoolapp.rvadapterlab.SimpleStyleBindingModel_
import studio.ultoolapp.rvadapterlab.metadata.*
import java.util.*

class InteractGroupEpoxyItemAdapter : EpoxyAdapter(), StickyHeaderCallbacks {
    companion object {
        private const val TAG = "InteractEpoxyItemAdapter"
    }

    init {
        enableDiffing()
    }

    interface OnItemClickListener {
        fun onItemClick(rootView: View, item: DateAmountItem, index: Int)
    }

    var itemClickListener: OnItemClickListener? = null

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

            GroupModel_().apply {
                id("Group $dateString / ${itemList.size}")
                layout(R.layout.viewgroup_linear)
                itemList.forEach { item ->
                    add(item.dataToView(index))
                    index++
                }
            }.let { addModel(it) }
        }
        notifyModelsChanged()
    }

    private fun generateHeaderView(date: Date, totalAmount: Double): DataBindingEpoxyModel {
        return SimpleHeaderBindingModel_().apply {
            titleText(totalAmount.toCurrencyFormat())
            subtitleText(date.toDayTitleString())
        }
    }

    private fun DateAmountItem.dataToView(index: Int): DataBindingEpoxyModel = let { item ->
        SimpleStyleBindingModel_().apply {
            // only id and xml-defined properties can be called/set here.
            id("view holder $index")
            titleText(item.amount.toCurrencyFormat())
            subtitleText(item.date.toDetailedTimeString())
            clickListener(View.OnClickListener {
                itemClickListener?.onItemClick(it, item, index)
            })
        }
    }

    override fun isStickyHeader(position: Int) = models[position] is SimpleHeaderBindingModel_
}
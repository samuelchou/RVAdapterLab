package studio.ultoolapp.rvadapterlab.view.epoxy

import android.view.View
import com.airbnb.epoxy.DataBindingEpoxyModel
import com.airbnb.epoxy.TypedEpoxyController
import studio.ultoolapp.rvadapterlab.SimpleHeaderBindingModel_
import studio.ultoolapp.rvadapterlab.SimpleStyleBindingModel_
import studio.ultoolapp.rvadapterlab.metadata.*
import java.util.*

class InteractController : TypedEpoxyController<List<DateAmountItem>>() {
    interface OnItemClickListener {
        fun onItemClick(rootView: View, item: DateAmountItem, index: Int)
    }

    var itemClickListener: OnItemClickListener? = null

    override fun buildModels(items: List<DateAmountItem>) {
        var index = 0

        items.groupBy {
            it.date.toYMDPlainString()
        }.forEach { (dateString, itemList) ->
            if (itemList.isEmpty()) {
                return@forEach
            }
            val groupDate: Date = dateString.fromYMDToDate() ?: itemList[0].date
            add(generateHeaderView(groupDate, itemList.sumByDouble { it.amount }))
            itemList.forEach { item ->
                add(item.dataToView(index))
                index++
            }
        }
    }

    private fun generateHeaderView(date: Date, totalAmount: Double): DataBindingEpoxyModel {
        return SimpleHeaderBindingModel_().apply {
            id("Header of ${date.toDayTitleString()}")
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

}
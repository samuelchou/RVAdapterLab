package studio.ultoolapp.rvadapterlab.view.epoxy

import com.airbnb.epoxy.DataBindingEpoxyModel
import com.airbnb.epoxy.EpoxyAdapter
import com.airbnb.epoxy.stickyheader.StickyHeaderCallbacks
import studio.ultoolapp.rvadapterlab.SimpleHeaderBindingModel_
import studio.ultoolapp.rvadapterlab.SimpleStyleBindingModel_
import studio.ultoolapp.rvadapterlab.metadata.DateAmountItem
import studio.ultoolapp.rvadapterlab.metadata.toCurrencyFormat
import studio.ultoolapp.rvadapterlab.metadata.toDayTitleString
import studio.ultoolapp.rvadapterlab.metadata.toDetailedTimeString

class InteractEpoxyItemAdapter : EpoxyAdapter(), StickyHeaderCallbacks {
    init {
        enableDiffing()
    }

    fun updateList(list: List<DateAmountItem>) {
        removeAllModels()
        list.forEachIndexed { index, dateAmountItem ->
            // TODO: 2021/5/19 do a header condition check by day change
            // TODO: 2021/5/19 pass total amount to [generateHeaderView] instead of index
            if (index % 5 == 0) {
                addModel(generateHeaderView(dateAmountItem, index))
            }
            addModel(dateAmountItem.dataToView(index))
        }
        notifyModelsChanged()
    }

    private fun generateHeaderView(item: DateAmountItem, index: Int): DataBindingEpoxyModel {
        return SimpleHeaderBindingModel_().apply {
            titleText(item.date.toDayTitleString())
            subtitleText("original index: $index")
        }
    }

    private fun DateAmountItem.dataToView(index: Int) = let { item ->
        SimpleStyleBindingModel_().apply {
            // only id and xml-defined properties can be called/set here.
            id("view holder $index")
            titleText(item.amount.toCurrencyFormat())
            subtitleText(item.date.toDetailedTimeString())
        }
    }

    override fun isStickyHeader(position: Int) = models[position] is SimpleHeaderBindingModel_
}
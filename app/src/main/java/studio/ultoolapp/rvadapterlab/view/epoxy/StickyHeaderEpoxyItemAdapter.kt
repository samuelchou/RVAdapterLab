package studio.ultoolapp.rvadapterlab.view.epoxy

import com.airbnb.epoxy.DataBindingEpoxyModel
import com.airbnb.epoxy.EpoxyAdapter
import com.airbnb.epoxy.stickyheader.StickyHeaderCallbacks
import studio.ultoolapp.rvadapterlab.SimpleHeaderBindingModel_
import studio.ultoolapp.rvadapterlab.SimpleStyleBindingModel_
import studio.ultoolapp.rvadapterlab.metadata.SimpleItem

class StickyHeaderEpoxyItemAdapter : EpoxyAdapter(), StickyHeaderCallbacks {
    init {
        enableDiffing()
    }

    fun updateList(list: List<SimpleItem>) {
        removeAllModels()
        addModels(list.mapIndexed { index, simpleItem ->
            if (index % 5 == 0) {
                generateHeaderView(simpleItem, index)
            } else {
                simpleItem.dataToView(index)
            }
        })
        notifyModelsChanged()
    }

    private fun generateHeaderView(item: SimpleItem, index: Int): DataBindingEpoxyModel {
        return SimpleHeaderBindingModel_().apply {
            titleText("Sticky header at $index")
            subtitleText("original title: ${item.title}")
        }
    }

    private fun SimpleItem.dataToView(index: Int) = let { item ->
        SimpleStyleBindingModel_().apply {
            // only id and xml-defined properties can be called/set here.
            id("view holder $index")
            titleText(item.title)
            subtitleText(item.subtitle)
        }
    }

    override fun isStickyHeader(position: Int) = models[position] is SimpleHeaderBindingModel_
}
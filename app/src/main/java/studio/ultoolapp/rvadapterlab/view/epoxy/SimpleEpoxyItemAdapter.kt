package studio.ultoolapp.rvadapterlab.view.epoxy

import com.airbnb.epoxy.EpoxyAdapter
import studio.ultoolapp.rvadapterlab.SimpleStyleBindingModel_
import studio.ultoolapp.rvadapterlab.metadata.SimpleItem

class SimpleEpoxyItemAdapter : EpoxyAdapter() {
    init {
        enableDiffing()
    }

    fun updateList(list: List<SimpleItem>) {
        removeAllModels()
        addModels(list.mapIndexed { index, simpleItem -> simpleItem.dataToView(index) })
        notifyModelsChanged()
    }

    private fun SimpleItem.dataToView(index: Int) = let { item ->
        SimpleStyleBindingModel_().apply {
            // only id and xml-defined properties can be called/set here.
            id("view holder $index")
            titleText(item.title)
            subtitleText(item.subtitle)
        }
    }
}
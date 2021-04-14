package studio.ultoolapp.rvadapterlab.view.flexible

import android.view.View
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractHeaderItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import studio.ultoolapp.rvadapterlab.R
import studio.ultoolapp.rvadapterlab.databinding.ItemSimpleHeaderBinding
import studio.ultoolapp.rvadapterlab.metadata.SimpleItem


class SimpleFlexibleHeader(private val item: SimpleItem) :
        AbstractHeaderItem<SimpleFlexibleHeader.SimpleHeaderViewHolder>() {

    /**
     * The ViewHolder used by this item.
     * Extending from FlexibleViewHolder is recommended especially when you will use
     * more advanced features.
     */
    inner class SimpleHeaderViewHolder(view: View, adapter: FlexibleAdapter<*>?) :
            FlexibleViewHolder(view, adapter, true) { // important constructor set up!!!
        val binding: ItemSimpleHeaderBinding = ItemSimpleHeaderBinding.bind(view)
    }

    /**
     * When an item is equals to another?
     * Write your own concept of equals, mandatory to implement or use
     * default java implementation (return this == o;) if you don't have unique IDs!
     * This will be explained in the "Item interfaces" Wiki page.
     */
    override fun equals(other: Any?): Boolean {
        if (other is SimpleFlexibleHeader) {
            return item == other.item
        }
        return false
    }

    /**
     * You should implement also this method if equals() is implemented.
     * This method, if implemented, has several implications that Adapter handles better:
     * - The Hash, increases performance in big list during Update & Filter operations.
     * - You might want to activate stable ids via Constructor for RV, if your id
     * is unique (read more in the wiki page: "Setting Up Advanced") you will benefit
     * of the animations also if notifyDataSetChanged() is invoked.
     */
    override fun hashCode(): Int {
        return item.hashCode()
    }

    /**
     * For the item type we need an int value: the layoutResID is sufficient.
     */
    override fun getLayoutRes(): Int {
        return R.layout.item_simple_header
    }

    /**
     * Delegates the creation of the ViewHolder to the user (AutoMap).
     * The inflated view is already provided as well as the Adapter.
     */
    override fun createViewHolder(
            view: View,
            adapter: FlexibleAdapter<IFlexible<*>?>?
    ): SimpleHeaderViewHolder {
        return SimpleHeaderViewHolder(view, adapter)
    }

    /**
     * The Adapter and the Payload are provided to perform and get more specific
     * information.
     */
    override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<*>?>?, holder: SimpleHeaderViewHolder,
            position: Int,
            payloads: List<Any>
    ) {
        holder.binding.titleText = item.title
        holder.binding.subtitleText = item.subtitle
    }
}
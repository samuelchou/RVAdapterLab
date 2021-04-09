package studio.ultoolapp.rvadapterlab.view.component

import android.view.View
import android.widget.TextView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import studio.ultoolapp.rvadapterlab.R
import studio.ultoolapp.rvadapterlab.databinding.ItemSimpleStyleBinding


class SimpleListItem(private val id: String, private val title: String) :
    AbstractFlexibleItem<SimpleListItem.SimpleItemViewHolder>() {

    /**
     * When an item is equals to another?
     * Write your own concept of equals, mandatory to implement or use
     * default java implementation (return this == o;) if you don't have unique IDs!
     * This will be explained in the "Item interfaces" Wiki page.
     */
    override fun equals(inObject: Any?): Boolean {
        if (inObject is SimpleListItem) {
            return id == inObject.id
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
        return id.hashCode()
    }

    /**
     * For the item type we need an int value: the layoutResID is sufficient.
     */
    override fun getLayoutRes(): Int {
        return R.layout.item_simple_style
    }

    /**
     * Delegates the creation of the ViewHolder to the user (AutoMap).
     * The inflated view is already provided as well as the Adapter.
     */
    override fun createViewHolder(
        view: View,
        adapter: FlexibleAdapter<IFlexible<*>?>?
    ): SimpleItemViewHolder {
        return SimpleItemViewHolder(view, adapter)
    }

    /**
     * The Adapter and the Payload are provided to perform and get more specific
     * information.
     */
    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<*>?>?, holder: SimpleItemViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        holder.titleText.text = title
        // Title appears disabled if item is disabled
        holder.titleText.isEnabled = isEnabled
    }

    /**
     * The ViewHolder used by this item.
     * Extending from FlexibleViewHolder is recommended especially when you will use
     * more advanced features.
     */
    inner class SimpleItemViewHolder(view: View, adapter: FlexibleAdapter<*>?) :
        FlexibleViewHolder(view, adapter) {
        // TODO: 2021/4/9 improve: change to data binding.
        val titleText = view.findViewById<TextView>(R.id.itemTitle)!!
        val subtitleText = view.findViewById<TextView>(R.id.itemSubTitle)!!
    }
}
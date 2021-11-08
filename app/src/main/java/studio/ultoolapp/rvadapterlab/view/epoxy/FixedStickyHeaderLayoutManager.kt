package studio.ultoolapp.rvadapterlab.view.epoxy

import android.content.Context
import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.stickyheader.StickyHeaderCallbacks

/**
 * Code borrowed from Epoxy optimized by someone:
 * https://github.com/airbnb/epoxy/issues/1211
 * https://gist.github.com/cbeyls/db4351870e493a817bf0f4fd84e2b598
 *
 * Adds sticky headers capabilities to your [RecyclerView.Adapter].
 * The provided callbacks must implement [StickyHeaderCallbacks.isStickyHeader] to
 * indicate which items are sticky.
 */
class FixedStickyHeaderLayoutManager @JvmOverloads constructor(
    context: Context,
    // Pass the adapter here before assigning it to the RecyclerView,
    // to ensure this LayoutManager will be notified of changes before the RecyclerView
    orientation: Int = RecyclerView.VERTICAL,
    reverseLayout: Boolean = false,
    private val adapter: RecyclerView.Adapter<*>,
    private val callbacks: StickyHeaderCallbacks,
) : LinearLayoutManager(context, orientation, reverseLayout) {

    init {
        val adapterObserver = HeaderPositionsAdapterDataObserver()
        adapter.registerAdapterDataObserver(adapterObserver)
    }

    // Translation for header
    private var translationX: Float = 0f
    private var translationY: Float = 0f

    // Header positions for the currently displayed list and their observer.
    private var headerPositions = IntArray(8)
    private var headerPositionsSize = 0

    private var headerPositionsUpdateStart: Int = HEADER_POSITIONS_UPDATE_FULL
    private var headerPositionsUpdateCount: Int = 0

    // Sticky header's ViewHolder and dirty state.
    private var stickyHeader: View? = null
    private var stickyHeaderPosition = RecyclerView.NO_POSITION

    // Save / Restore scroll state
    private var scrollPosition = RecyclerView.NO_POSITION
    private var scrollOffset = 0

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(
            superState = super.onSaveInstanceState(),
            scrollPosition = scrollPosition,
            scrollOffset = scrollOffset
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        (state as? SavedState)?.let {
            scrollPosition = it.scrollPosition
            scrollOffset = it.scrollOffset
            super.onRestoreInstanceState(it.superState)
        }
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State?): Int {
        val scrolled = restoreView { super.scrollVerticallyBy(dy, recycler, state) }
        if (scrolled != 0) {
            updateStickyHeader(recycler, false)
        }
        return scrolled
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State?): Int {
        val scrolled = restoreView { super.scrollHorizontallyBy(dx, recycler, state) }
        if (scrolled != 0) {
            updateStickyHeader(recycler, false)
        }
        return scrolled
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        restoreView { super.onLayoutChildren(recycler, state) }
        if (!state.isPreLayout) {
            updateStickyHeader(recycler, true)
        }
    }

    override fun scrollToPosition(position: Int) = scrollToPositionWithOffset(position, INVALID_OFFSET)

    override fun scrollToPositionWithOffset(position: Int, offset: Int) =
        scrollToPositionWithOffset(position, offset, true)

    private fun scrollToPositionWithOffset(position: Int, offset: Int, adjustForStickyHeader: Boolean) {
        // Reset pending scroll.
        setScrollState(RecyclerView.NO_POSITION, INVALID_OFFSET)

        // Adjusting is disabled.
        if (!adjustForStickyHeader) {
            super.scrollToPositionWithOffset(position, offset)
            return
        }

        updateHeaderPositionsIfNeeded()

        // There is no header above or the position is a header.
        val headerIndex = findHeaderIndexOrBefore(position)
        if (headerIndex == -1 || findHeaderIndex(position) != -1) {
            super.scrollToPositionWithOffset(position, offset)
            return
        }

        // The position is right below a header, scroll to the header.
        if (findHeaderIndex(position - 1) != -1) {
            super.scrollToPositionWithOffset(position - 1, offset)
            return
        }

        // Current sticky header is the same as at the position. Adjust the scroll offset and reset pending scroll.
        val header = stickyHeader
        if (header != null && headerIndex == findHeaderIndex(stickyHeaderPosition)) {
            val adjustedOffset = (if (offset != INVALID_OFFSET) offset else 0) + header.height
            super.scrollToPositionWithOffset(position, adjustedOffset)
            return
        }

        // Remember this position and offset and scroll to it to trigger creating the sticky header.
        setScrollState(position, offset)
        super.scrollToPositionWithOffset(position, offset)
    }

    //region Computation
    // Mainly [RecyclerView] functionality by removing sticky header from calculations

    override fun computeVerticalScrollExtent(state: RecyclerView.State): Int =
        restoreView { super.computeVerticalScrollExtent(state) }

    override fun computeVerticalScrollOffset(state: RecyclerView.State): Int =
        restoreView { super.computeVerticalScrollOffset(state) }

    override fun computeVerticalScrollRange(state: RecyclerView.State): Int =
        restoreView { super.computeVerticalScrollRange(state) }

    override fun computeHorizontalScrollExtent(state: RecyclerView.State): Int =
        restoreView { super.computeHorizontalScrollExtent(state) }

    override fun computeHorizontalScrollOffset(state: RecyclerView.State): Int =
        restoreView { super.computeHorizontalScrollOffset(state) }

    override fun computeHorizontalScrollRange(state: RecyclerView.State): Int =
        restoreView { super.computeHorizontalScrollRange(state) }

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? =
        restoreView { super.computeScrollVectorForPosition(targetPosition) }

    override fun onFocusSearchFailed(
        focused: View,
        focusDirection: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): View? = restoreView { super.onFocusSearchFailed(focused, focusDirection, recycler, state) }

    /**
     * Perform the [operation] without the sticky header view by
     * detaching the view -> performing operation -> attaching the view.
     */
    private inline fun <T> restoreView(operation: () -> T): T {
        stickyHeader?.let(this::detachView)
        val result = operation()
        stickyHeader?.let(this::attachView)
        return result
    }

    //endregion

    /**
     * Offsets the vertical location of the sticky header relative to the its default position.
     */
    fun setStickyHeaderTranslationY(translationY: Float) {
        this.translationY = translationY
        requestLayout()
    }

    /**
     * Offsets the horizontal location of the sticky header relative to the its default position.
     */
    fun setStickyHeaderTranslationX(translationX: Float) {
        this.translationX = translationX
        requestLayout()
    }

    /**
     * Returns true if `view` is the current sticky header.
     */
    fun isStickyHeader(view: View): Boolean = view === stickyHeader

    /**
     * Updates the sticky header state (creation, binding, display), to be called whenever there's a layout or scroll
     */
    private fun updateStickyHeader(recycler: RecyclerView.Recycler, layout: Boolean) {
        updateHeaderPositionsIfNeeded()

        val headerCount = headerPositionsSize
        val childCount = childCount
        if (headerCount > 0 && childCount > 0) {
            // Find first valid child.
            var anchorView: View? = null
            var anchorIndex = -1
            var anchorPos = -1
            for (i in 0 until childCount) {
                val child = getChildAt(i)!!
                val params = child.layoutParams as RecyclerView.LayoutParams
                if (isViewValidAnchor(child, params)) {
                    anchorView = child
                    anchorIndex = i
                    anchorPos = params.absoluteAdapterPosition
                    break
                }
            }
            if (anchorView != null && anchorPos != -1) {
                val headerIndex = findHeaderIndexOrBefore(anchorPos)
                val headerPos = if (headerIndex != -1) headerPositions[headerIndex] else -1
                val nextHeaderPos = if (headerCount > headerIndex + 1) headerPositions[headerIndex + 1] else -1

                // Show sticky header if:
                // - There's one to show;
                // - It's on the edge or it's not the anchor view;
                // - Isn't followed by another sticky header;
                if (headerPos != -1 &&
                    (headerPos != anchorPos || isViewOnBoundary(anchorView)) &&
                    nextHeaderPos != headerPos + 1
                ) {
                    // 1. Ensure existing sticky header, if any, is of correct type.
                    var header = stickyHeader
                    if (header != null && getItemViewType(header) != adapter.getItemViewType(headerPos)) {
                        // A sticky header was shown before but is not of the correct type. Scrap it.
                        scrapStickyHeader(recycler)
                        header = null
                    }

                    // 2. Ensure sticky header is created, if absent, or bound, if being laid out or the position changed.
                    if (header == null) {
                        header = createStickyHeader(recycler, headerPos)
                    }
                    // 3. Bind the sticky header
                    if (layout || getPosition(header) != headerPos) {
                        bindStickyHeader(recycler, header, headerPos)
                    }

                    // 4. Draw the sticky header using translation values which depend on orientation, direction and
                    // position of the next header view.
                    val nextHeaderView: View? = if (nextHeaderPos != -1) {
                        val nextHeaderView = getChildAt(anchorIndex + (nextHeaderPos - anchorPos))
                        // The header view itself is added to the RecyclerView. Discard it if it comes up.
                        if (nextHeaderView === stickyHeader) null else nextHeaderView
                    } else null
                    header.translationX = getX(header, nextHeaderView)
                    header.translationY = getY(header, nextHeaderView)
                    return
                }
            }
        }

        if (stickyHeader != null) {
            scrapStickyHeader(recycler)
        }
    }

    /**
     * Creates [RecyclerView.ViewHolder] for [position], including measure / layout, and assigns it to
     * [stickyHeader].
     */
    private fun createStickyHeader(recycler: RecyclerView.Recycler, position: Int): View {
        val stickyHeader = recycler.getViewForPosition(position)

        // Setup sticky header if the adapter requires it.
        callbacks.setupStickyHeaderView(stickyHeader)

        // Add sticky header as a child view, to be detached / reattached whenever LinearLayoutManager#fill() is called,
        // which happens on layout and scroll (see overrides).
        addView(stickyHeader)
        measureAndLayout(stickyHeader)

        // Ignore sticky header, as it's fully managed by this LayoutManager.
        ignoreView(stickyHeader)

        this.stickyHeader = stickyHeader
        this.stickyHeaderPosition = position
        return stickyHeader
    }

    /**
     * Binds the [stickyHeader] for the given [position].
     */
    private fun bindStickyHeader(recycler: RecyclerView.Recycler, stickyHeader: View, position: Int) {
        // Bind the sticky header.
        recycler.bindViewToPosition(stickyHeader, position)
        stickyHeaderPosition = position
        measureAndLayout(stickyHeader)

        // If we have a pending scroll wait until the end of layout and scroll again.
        if (scrollPosition != RecyclerView.NO_POSITION) {
            stickyHeader.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    stickyHeader.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    if (scrollPosition != RecyclerView.NO_POSITION) {
                        scrollToPositionWithOffset(scrollPosition, scrollOffset)
                        setScrollState(RecyclerView.NO_POSITION, INVALID_OFFSET)
                    }
                }
            })
        }
    }

    /**
     * Measures and lays out [stickyHeader].
     */
    private fun measureAndLayout(stickyHeader: View) {
        measureChildWithMargins(stickyHeader, 0, 0)
        when (orientation) {
            VERTICAL -> stickyHeader.layout(paddingLeft, 0, width - paddingRight, stickyHeader.measuredHeight)
            else -> stickyHeader.layout(0, paddingTop, stickyHeader.measuredWidth, height - paddingBottom)
        }
    }

    /**
     * Returns [stickyHeader] to the [RecyclerView]'s [RecyclerView.RecycledViewPool], assigning it
     * to `null`.
     *
     * @param recycler If passed, the sticky header will be returned to the recycled view pool.
     */
    private fun scrapStickyHeader(recycler: RecyclerView.Recycler?) {
        val stickyHeader = stickyHeader ?: return
        this.stickyHeader = null
        this.stickyHeaderPosition = RecyclerView.NO_POSITION

        // Revert translation values.
        stickyHeader.translationX = 0f
        stickyHeader.translationY = 0f

        // Teardown holder if the adapter requires it.
        callbacks.teardownStickyHeaderView(stickyHeader)

        // Stop ignoring sticky header so that it can be recycled.
        stopIgnoringView(stickyHeader)

        // Remove and recycle sticky header.
        removeView(stickyHeader)
        recycler?.recycleView(stickyHeader)
    }

    /**
     * Returns true when `view` is a valid anchor, ie. the first view to be valid and visible.
     */
    private fun isViewValidAnchor(view: View, params: RecyclerView.LayoutParams): Boolean {
        return when {
            !params.isItemRemoved && !params.isViewInvalid -> when (orientation) {
                VERTICAL -> when {
                    reverseLayout -> view.top + view.translationY <= height + translationY
                    else -> view.bottom - view.translationY >= translationY
                }
                else -> when {
                    reverseLayout -> view.left + view.translationX <= width + translationX
                    else -> view.right - view.translationX >= translationX
                }
            }
            else -> false
        }
    }

    /**
     * Returns true when the `view` is at the edge of the parent [RecyclerView].
     */
    private fun isViewOnBoundary(view: View): Boolean {
        return when (orientation) {
            VERTICAL -> when {
                reverseLayout -> view.bottom - view.translationY > height + translationY
                else -> view.top + view.translationY < translationY
            }
            else -> when {
                reverseLayout -> view.right - view.translationX > width + translationX
                else -> view.left + view.translationX < translationX
            }
        }
    }

    /**
     * Returns the position in the Y axis to position the header appropriately, depending on orientation, direction and
     * [android.R.attr.clipToPadding].
     */
    private fun getY(headerView: View, nextHeaderView: View?): Float {
        when (orientation) {
            VERTICAL -> {
                var y = translationY
                if (reverseLayout) {
                    y += (height - headerView.height).toFloat()
                }
                if (nextHeaderView != null) {
                    val bottomMargin = (nextHeaderView.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
                    val topMargin = (nextHeaderView.layoutParams as ViewGroup.MarginLayoutParams).topMargin
                    y = when {
                        reverseLayout -> (nextHeaderView.bottom + bottomMargin).toFloat().coerceAtLeast(y)
                        else -> (nextHeaderView.top - topMargin - headerView.height).toFloat().coerceAtMost(y)
                    }
                }
                return y
            }
            else -> return translationY
        }
    }

    /**
     * Returns the position in the X axis to position the header appropriately, depending on orientation, direction and
     * [android.R.attr.clipToPadding].
     */
    private fun getX(headerView: View, nextHeaderView: View?): Float {
        when (orientation) {
            HORIZONTAL -> {
                var x = translationX
                if (reverseLayout) {
                    x += (width - headerView.width).toFloat()
                }
                if (nextHeaderView != null) {
                    val leftMargin = (nextHeaderView.layoutParams as ViewGroup.MarginLayoutParams).leftMargin
                    val rightMargin = (nextHeaderView.layoutParams as ViewGroup.MarginLayoutParams).rightMargin
                    x = when {
                        reverseLayout -> (nextHeaderView.right + rightMargin).toFloat().coerceAtLeast(x)
                        else -> (nextHeaderView.left - leftMargin - headerView.width).toFloat().coerceAtMost(x)
                    }
                }
                return x
            }
            else -> return translationX
        }
    }

    private fun updateHeaderPositionsIfNeeded() {
        val updateStart = headerPositionsUpdateStart
        if (updateStart == HEADER_POSITIONS_UPDATE_NONE) {
            return
        }

        if (updateStart == HEADER_POSITIONS_UPDATE_FULL) {
            // Full header scan
            headerPositionsSize = 0
            for (i in 0 until adapter.itemCount) {
                if (callbacks.isStickyHeader(i)) {
                    appendHeaderPosition(i)
                }
            }

            // Remove sticky header immediately if the entry it represents has been removed. A layout will follow.
            if (stickyHeader != null && findHeaderIndex(stickyHeaderPosition) == -1) {
                scrapStickyHeader(null)
            }
        } else {
            // Partial header scan, grow the existing list
            for (i in updateStart until updateStart + headerPositionsUpdateCount) {
                if (callbacks.isStickyHeader(i)) {
                    insertHeaderPositionSorted(i)
                }
            }
        }
        headerPositionsUpdateStart = HEADER_POSITIONS_UPDATE_NONE
    }

    /**
     * Finds the header index of `position` in `headerPositions`.
     */
    private fun findHeaderIndex(position: Int): Int {
        val index = headerPositions.binarySearch(position, 0, headerPositionsSize)
        return if (index >= 0) index else -1
    }

    /**
     * Finds the header index of `position` or the one before it in `headerPositions`.
     */
    private fun findHeaderIndexOrBefore(position: Int): Int {
        val index = headerPositions.binarySearch(position, 0, headerPositionsSize)
        return if (index >= 0) index else index.inv() - 1
    }

    /**
     * Finds the header index of `position` or the one next to it in `headerPositions`.
     * Returns headerPositionsSize if no next element is found.
     */
    private fun findHeaderIndexOrNext(position: Int): Int {
        val index = headerPositions.binarySearch(position, 0, headerPositionsSize)
        return if (index >= 0) index else index.inv()
    }

    private fun insertHeaderPositionSorted(headerPos: Int) {
        insertHeaderPosition(findHeaderIndexOrNext(headerPos), headerPos)
    }

    private fun appendHeaderPosition(element: Int) {
        val array = headerPositions
        val currentSize = headerPositionsSize
        if (currentSize + 1 > array.size) {
            val newArray = array.copyOf(currentSize * 2)
            newArray[currentSize] = element
            headerPositions = newArray
        } else {
            array[currentSize] = element
        }
        headerPositionsSize = currentSize + 1
    }

    private fun insertHeaderPosition(index: Int, element: Int) {
        val array = headerPositions
        val currentSize = headerPositionsSize
        if (currentSize + 1 <= array.size) {
            array.copyInto(array, index + 1, index, currentSize)
            array[index] = element
        } else {
            val newArray = IntArray(currentSize * 2)
            array.copyInto(newArray, 0, 0, index)
            newArray[index] = element
            array.copyInto(newArray, index + 1, index, array.size)
            headerPositions = newArray
        }
        headerPositionsSize = currentSize + 1
    }

    private fun removeHeaderPositionAt(index: Int) {
        val array = headerPositions
        array.copyInto(array, index, index + 1, headerPositionsSize--)
    }

    private fun setScrollState(position: Int, offset: Int) {
        scrollPosition = position
        scrollOffset = offset
    }

    /**
     * Save / restore existing [RecyclerView] state and
     * scrolling position and offset.
     */
    class SavedState(
        val superState: Parcelable?,
        val scrollPosition: Int,
        val scrollOffset: Int
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readParcelable(SavedState::class.java.classLoader),
            parcel.readInt(),
            parcel.readInt()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeParcelable(superState, flags)
            parcel.writeInt(scrollPosition)
            parcel.writeInt(scrollOffset)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    /**
     * Handles header positions while adapter changes occur.
     *
     * This is used in detriment of [RecyclerView.LayoutManager]'s callbacks to control when they're received.
     */
    private inner class HeaderPositionsAdapterDataObserver : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            // There's no hint at what changed, so request a full update.
            headerPositionsUpdateStart = HEADER_POSITIONS_UPDATE_FULL
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            if (headerPositionsUpdateStart != HEADER_POSITIONS_UPDATE_NONE) {
                // If a partial update was pending, cancel it and request a full update.
                headerPositionsUpdateStart = HEADER_POSITIONS_UPDATE_FULL
                return
            }

            // Shift headers below down.
            val headerCount = headerPositionsSize
            if (headerCount > 0) {
                var i = findHeaderIndexOrNext(positionStart)
                while (i < headerCount) {
                    headerPositions[i] = headerPositions[i] + itemCount
                    i++
                }
            }

            // Request adding new headers through a partial update.
            headerPositionsUpdateStart = positionStart
            headerPositionsUpdateCount = itemCount
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            val updateStart = headerPositionsUpdateStart
            if (positionStart == HEADER_POSITIONS_UPDATE_FULL) {
                return
            }
            if (updateStart != HEADER_POSITIONS_UPDATE_NONE) {
                // A partial update is pending
                if (positionStart + itemCount <= updateStart) {
                    // The removed range is before the pending update range, shift update range down and continue.
                    headerPositionsUpdateStart -= itemCount
                } else if (positionStart < updateStart + headerPositionsUpdateCount) {
                    // The removed range starts before the end of the pending update range and conflicts with it.
                    headerPositionsUpdateStart = HEADER_POSITIONS_UPDATE_FULL
                    return
                }
            }

            var headerCount = headerPositionsSize
            if (headerCount > 0) {
                // Remove headers.
                for (i in positionStart + itemCount - 1 downTo positionStart) {
                    val index = findHeaderIndex(i)
                    if (index != -1) {
                        removeHeaderPositionAt(index)
                        headerCount--
                    }
                }

                // Remove sticky header immediately if the entry it represents has been removed. A layout will follow.
                if (stickyHeader != null && findHeaderIndex(stickyHeaderPosition) == -1) {
                    scrapStickyHeader(null)
                }

                // Shift headers below up.
                var i = findHeaderIndexOrNext(positionStart + itemCount)
                while (i < headerCount) {
                    headerPositions[i] = headerPositions[i] - itemCount
                    i++
                }
            }
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            if (headerPositionsUpdateStart != HEADER_POSITIONS_UPDATE_NONE) {
                // If a partial update was pending, cancel it and request a full update.
                headerPositionsUpdateStart = HEADER_POSITIONS_UPDATE_FULL
                return
            }

            // Shift moved headers by toPosition - fromPosition.
            // Shift headers in-between by -itemCount (reverse if upwards).
            val headerCount = headerPositionsSize
            if (headerCount > 0) {
                if (fromPosition < toPosition) {
                    var i = findHeaderIndexOrNext(fromPosition)
                    while (i < headerCount) {
                        val headerPos = headerPositions[i]
                        if (headerPos >= fromPosition && headerPos < fromPosition + itemCount) {
                            removeHeaderPositionAt(i)
                            insertHeaderPositionSorted(headerPos - toPosition + fromPosition)
                        } else if (headerPos >= fromPosition + itemCount && headerPos <= toPosition) {
                            removeHeaderPositionAt(i)
                            insertHeaderPositionSorted(headerPos - itemCount)
                        } else {
                            break
                        }
                        i++
                    }
                } else {
                    var i = findHeaderIndexOrNext(toPosition)
                    loop@ while (i < headerCount) {
                        val headerPos = headerPositions[i]
                        when {
                            headerPos >= fromPosition && headerPos < fromPosition + itemCount -> {
                                removeHeaderPositionAt(i)
                                insertHeaderPositionSorted(headerPos + toPosition - fromPosition)
                            }
                            headerPos in toPosition..fromPosition -> {
                                removeHeaderPositionAt(i)
                                insertHeaderPositionSorted(headerPos + itemCount)
                            }
                            else -> break@loop
                        }
                        i++
                    }
                }
            }
        }
    }

    companion object {
        private const val HEADER_POSITIONS_UPDATE_NONE = -1
        private const val HEADER_POSITIONS_UPDATE_FULL = -2
    }
}
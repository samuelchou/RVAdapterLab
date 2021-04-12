package studio.ultoolapp.rvadapterlab.view.epoxy

import com.airbnb.epoxy.EpoxyDataBindingPattern
import studio.ultoolapp.rvadapterlab.R

/**
 * Patterns annotation are important: it makes Epoxy to generate class like LayoutBindingModel_ class.
 */
@EpoxyDataBindingPattern(rClass = R::class, layoutPrefix = "item")
object EpoxyDataBindingPatterns

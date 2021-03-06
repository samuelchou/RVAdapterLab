package studio.ultoolapp.rvadapterlab.metadata

import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

data class DateAmountItem(
    val date: Date,
    val amount: Double
)

fun Double.toCurrencyFormat(): String = DecimalFormat("$#.###").format(this)

fun Date.toDetailedTimeString(): String =
    SimpleDateFormat("yyyy/MM/dd-HH:mm:ss", Locale.getDefault()).format(this)

fun Date.toDayTitleString(): String = SimpleDateFormat("MM/dd EEE", Locale.getDefault()).format(this)

fun Date.toYMDPlainString(): String = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(this)

fun String.fromYMDToDate(): Date? = try {
    SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).parse(this)
} catch (e: ParseException) { null }

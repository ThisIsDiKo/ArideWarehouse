package ru.dikoresearch.aridewarehouse.presentation.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import ru.dikoresearch.aridewarehouse.App
import ru.dikoresearch.aridewarehouse.MainActivity
import ru.dikoresearch.aridewarehouse.di.AppComponent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun Fragment.getAppComponent() = requireContext().applicationContext.appComponent

fun Activity.hideKeyboard() {
    val imm = (this as MainActivity).getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    val view = this.currentFocus

    view?.let{ v ->
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }

}

fun View.gone(){
    this.visibility = View.GONE
}

fun View.visible(){
    this.visibility = View.VISIBLE
}

fun SharedPreferences.token(): String {
    return this.getString(WAREHOUSE_TOKEN, "") ?: ""
}

fun SharedPreferences.setToken(token: String = "", username: String = ""){
    this.edit()
        .putString(WAREHOUSE_TOKEN, token)
        .putString(WAREHOUSE_USERNAME, username)
        .apply()
}

fun String.getFormattedDateFromDataBaseDate(): String {
    val dateFormat = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
    val incomingDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    return LocalDateTime.parse(this.split(".").first(), incomingDateFormat)
        .format(dateFormat)
}

val Context.appComponent: AppComponent
    get() = when(this){
        is App -> appComponent
        else -> this.applicationContext.appComponent
    }
package me.heftymouse.timetable.utils

import kotlinx.coroutines.CompletableDeferred
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL

val URL_BASE = "https://timetable-meta.amrita.town"

fun getRegistry() {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url(URL_BASE + "/index.json")
        .build()

//    val deferred = CompletableDeferred()
}
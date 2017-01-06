package nl.infi.profitmonitor

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class WebService private constructor() {

    companion object {
        val BASE_URL = "https://urenapi.infi.nl/"
        val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)

        val instance by lazy { WebService() }
    }

    private val httpClient by lazy { OkHttpClient() }

    private fun createUrlBuilder(): HttpUrl.Builder {
        return HttpUrl.parse(BASE_URL).newBuilder()
    }

    private fun createUrlBuilder(startDate: Date, endDate: Date): HttpUrl.Builder {
        return createUrlBuilder()
                .addQueryParameter("startdate", DATE_FORMAT.format(startDate))
                .addQueryParameter("enddate", DATE_FORMAT.format(endDate))
    }

    private fun get(url: HttpUrl): Response {
        val request = Request.Builder().url(url).build()
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) throw IOException("Unable to load " + url.toString() + ": " + response.code())
        return response
    }

    private fun responseToFloat(response: Response): Float? {
        val body = response.body().string()
        if (body.isNullOrBlank()) return null
        return body.replace(",", ".").toFloat()
    }

    fun getRevenue(startDate: Date, endDate: Date): Float? {
        val url = createUrlBuilder(startDate, endDate)
                .addPathSegment("kpi_omzet_afgerond.php")
                .build()

        return responseToFloat(get(url))
    }

    fun getProfit(startDate: Date, endDate: Date): Float? {
        val url = createUrlBuilder(startDate, endDate)
                .addPathSegment("kpi_afrondingswinst.php")
                .build()

        return responseToFloat(get(url))
    }
}
## Step 2: Model

Supercool dat we nu een paar knopjes en labels hebben, maar laten we ons nu eens richten op het ophalen van wat data die we erin kwijt kunnen. Daarvoor gaan we de [uren-api](https://git.infi.nl/infi/uren-api) gebruiken. De uren-api is te vinden op [https://urenapi.infi.nl/](https://urenapi.infi.nl/). We maken een nieuwe service waar we onze request-logica in kwijt kunnen. We gebruiken hier een singleton-patroon voor.

```Kotlin
    class WebService private constructor() {

        companion object {
            val BASE_URL = "https://urenapi.infi.nl/"
            val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)

            val instance by lazy { WebService() }
        }
    }
```

Als je een constructor definieert in Kotlin volgt die direct na de class definitie. We willen een private constructor gebruiken, en standaard is _alles_ in Kotlin public, dus dat moeten we expliciet definiëren. Later gaan we wat dieper in op constructors.

:heavy_exclamation_mark: Kotlin heeft, in tegenstelling tot bijvoorbeeld Java en C#, geen static eigenschappen. Om die functionaliteit te verkrijgen gebruik je een `companion object`. Je kan de properties of methods in het companion object statisch gebruiken. Als je ze vanuit Java wil aanspreken doe je dat als volgt: `WebService.Companion.getInstance()`. Je kan het companion object eventueel een andere naam geven, maar omdat je er maar 1 mag hebben per class schept dat in mijn ogen meestal alleen maar verwarring.

De `instance` value heeft een bijzondere definitie: hier wordt een `delegate` gebruikt. Een delegate heeft een `getValue()` en een `setValue()` die worden gebruikt als de `get()` en `set()` van een property worden gebruikt (een _value_ heeft natuurlijk geen setters). De `lazy` delegate wordt standaard met Kotlin meegeleverd, en neemt een lambda aan. De eerste keer dat de property wordt geaccessed wordt deze lambda uitgevoerd en het resultaat opgeslagen en geretourneerd. De volgende keer wordt de opgeslagen waarde gebruikt. De `lazy` delegate is standaard gesynchroniseerd (thread-safe), maar je kan dat overriden. Hierdoor hoeven we dus niet zelf een instance variable bij te houden, toe te wijzen et cetera.

De `DATE_FORMAT` hebben we straks nodig bij het opbouwen van de URLs (net zoals de `BASE_URL`, maar dat had je waarschijnlijk al verwacht).

Eerst nemen we even een kijkje naar de requests die we willen gaan uitvoeren. Voor de week van 5 december 2016 zien die er zo uit (de einddatum is _exclusive_):
* https://uren_api.infi.nl/kpi_omzet_afgerond.php?startdate=2016-12-05&enddate=2016-12-10
* https://uren_api.infi.nl/kpi_afrondingswinst.php?startdate=2016-12-05&enddate=2016-12-10

We geven dus een **startdatum** en een **einddatum** op (in het formaat `yyyy-MM-dd`, als je de Java formatter gebruikt), en krijgen een **float met een komma in plaats van een punt** (deze api is eigenlijk voor een Nederlandse versie van Excel gemaakt) terug. Soms is er geen data en is de body leeg. Onthoud dit.

We gebruiken [OkHttp](http://square.github.io/okhttp/) om onze requests te doen. Daarvoor moeten we een `OkHttpClient` maken, daar gebruiken we ook de `lazy` delegate voor. Om een request op te bouwen gebruik je de `RequestBuilder`. Voor al onze requests hebben we een start- en een einddatum nodig en omdat we één keer code typen wel genoeg vinden maken we daar aparte methods voor. Het maken van een request doen we ook een paar keer, evenals het parsen van de response (een `String`) naar een `Float`.
* Dat ziet er zo uit:
```Kotlin
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
```

We hebben nu de grote brokken gehad, dus wat overblijft zijn de individuele requests.
* Nu we het voorgaan de hebben is dat geen kunst meer:
```Kotlin
    fun getRevenue(startDate: Date, endDate: Date): Float? {
        val url = createUrlBuilder(startDate, endDate)
                .addPathSegment("kpi_omzet_afgerond.php")
                .build()

        return responseToFloat(get(url))
    }
```
:warning: Vergeet `getProfit` niet te implementeren.

Zo, dat is onze webservice! Daar willen we ook nog wat mee doen natuurlijk. Laten we even snel wat in elkaar knallen, zodat je ziet dat het werkt.
* Voeg tijdelijk de webservice toe aan je `MainActivity`:
```Kotlin
    private val webService = WebService.instance
```
* En gebruik hem in de volgende subscription (later gaan we hier verder op in)
```Kotlin
    sub.add(RxView.clicks(button_start_date)
            .observeOn(Schedulers.io())
            .map { webService.getRevenue(Date(116, 11, 5), Date(116, 11, 9)) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                textview_revenue.text = it.toString()
    })
```

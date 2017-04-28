## Bonus

Nu we de omzet van heel Infi op kunnen halen is het ook wel leuk om eens te kijken hoeveel je collega's daar nu aan bij hebben gedragen. Met een call naar `kpi_uren_facturabel.php` kan je bijvoorbeeld kijken hoeveel facturabele uren iemand heeft gemaakt in de opgegeven periode. Zo kan je zien dat ik niet zo'n heel productieve week had rond Sinterklaas:

https://uren_api.infi.nl/kpi_uren_facturabel.php?startdate=2016-12-05&enddate=2016-12-10&ids=87

Er is geen api-call om een lijst van je collega's te halen, dus hieronder vind je een simpele `Employee` [data class](https://kotlinlang.org/docs/reference/data-classes.html) en een lijstje met je collega's. Omdat dat natuurlijk onderhavig is aan mutaties is de kans wel groot dat de data wat achterloopt ;).

```Kotlin
    data class Employee (val id: Int, val name: String)

    val employees = arrayOf(
        Employee(96, "Anna"),
        Employee(98, "Aziz"),
        Employee(29, "Corné"),
        Employee(69, "Daniël"),
        Employee(4, "Daniel"),
        Employee(42, "Dirk"),
        Employee(51, "Dylan"),
        Employee(92, "Elizabeth"),
        Employee(36, "Ely"),
        Employee(85, "Erik"),
        Employee(45, "Freek"),
        Employee(82, "Henri"),
        Employee(50, "Jacco"),
        Employee(95, "Jeroen"),
        Employee(41, "Jiri"),
        Employee(43, "Jiska"),
        Employee(99, "Joost"),
        Employee(40, "Jorik"),
        Employee(88, "Matthijs"),
        Employee(73, "Michael"),
        Employee(55, "Morten"),
        Employee(87, "Niels"),
        Employee(62, "Roeland"),
        Employee(83, "Rutger"),
        Employee(48, "Sander"),
        Employee(37, "Stefan"),
        Employee(86, "Steven"),
        Employee(90, "Susanne"),
        Employee(68, "Wolf"),
        Employee(94, "Wouter")
    )
```

Een dropdown-achtige in Android maak je middels een [`Spinner`](https://developer.android.com/guide/topics/ui/controls/spinner.html). De makkelijkste manier om die te voeren is door middel van een [`ArrayAdapter`](https://developer.android.com/reference/android/widget/ArrayAdapter.html), waar je een array van items in kan stoppen. De tekst in de `Spinner` komt uit de `.toString()` van de objecten. Je ziet dat de constructor van de `ArrayAdapter` een resource ID voor een layout wil, deze gebruikt hij om de views in de lijst te maken. Als je niks fancy's wil, kan je een ingebouwde layout gebruiken: `android.R.layout.simple_list_item_1`.

Je gebruikt een `OnItemSelectedListener` om te luisteren naar welk item er geselecteerd wordt. De Rx-binding daarvoor vind je in `RxAdapterView.itemSelections()`. Je krijgt dan een `int` met de index van het geselecteerde item. Vervolgens kan je aan de adapter vragen (`.getItem()`) welk item dat was.

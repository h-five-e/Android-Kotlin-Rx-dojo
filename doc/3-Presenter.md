## Step 3: Presenter

We willen onze app opzetten volgens het Model-View-Presenter-patroon. Ons model is de `WebService`. Verder hebben we onze view (de `MainActivity`), maar nog geen presenter. Daar komt wat abstractie bij kijken om het netjes te houden en dat heb ik hier ook gedaan ook al is het een beetje overkill in zo'n klein projectje als dit. Mocht je zelf verder willen typen in plaats van step-3 uitchecken dan kan je dit snel overnemen:
```Kotlin
    class MainPresenter {

        private var mvpView: MainActivity? = null
            private set

        private var viewLifeCycleSubscriptions = CompositeSubscription()

        fun attachView(view: MainActivity) {
            this.mvpView = view
            viewLifeCycleSubscriptions = CompositeSubscription()
        }

        fun detachView() {
            mvpView = null
            viewLifeCycleSubscriptions.unsubscribe()
        }

        private fun addViewLifeCycleSubscription(subscription: Subscription): Subscription {
            viewLifeCycleSubscriptions.add(subscription)
            return subscription
        }

        private fun removeViewLifeCycleSubscription(subscription: Subscription) =
            viewLifeCycleSubscriptions.remove(subscription)

    }
```

Als je dat gedaan hebt (of step-3 hebt uitgecheckt) kunnen we aan de slag.
* Om te beginnen moeten we de `MainActivity` natuurlijk een presenter geven:
```Kotlin
    private val presenter = MainPresenter()
```
* En die op de hoogte stellen als de view gemaakt is (dus ná `setContentView()`), of gedestroyed:
```Kotlin
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter.attachView(this)

        // …
    }

    override fun onDestroy() {
        liveCycleSubscriptions?.unsubscribe()
        presenter.detachView()
        super.onDestroy()
    }
```

Zo. We hebben nu een presenter, maar die heeft nog niets te doen. Tijd om daar verandering in aan te brengen. Wat willen we dat de presenter doet?
* De presenter moet bijhouden welke data zijn geselecteerd
* Als er een start- en een einddatum zijn moet hij de data in de view updaten
* Als de start- of einddatum gewijzigd wordt moet hij de data in de view updaten

Moet te doen zijn. Aan het werk!

We moeten de presenter op de hoogte stellen van het feit dat er een datum gekozen is. De makkelijke manier is om de presenter van een `updateStartDate()` method te voorzien en die aan te roepen vanuit de subscription op `Date`s in de `MainActivity`. Maar in dat geval kan de presenter daar niet reactive op reageren, tenzij we daarin weer een `Subject` in zouden maken waarop we `onNext()` aanroepen zodra de datum wordt bijgewerkt. Wat zeg je? Oh, ja, dat is best wel dubbelop, ja. En een hoop boilerplate. We kunnen net zo goed de `Observable` doorpassen, of niet dan? Nou, níet helemaal: misschien weet je het nog maar in het begin zei ik al dat je maar één keer kan subscriben op een observable, en dat opnieuw `RxView.clicks()` aanroepen niet werkt (:sparkles: de obervable gebruikt de `setOnClickListener()` op de view, en een `View` kan maar één `OnClickListener` hebben, dus dit overschrijft de listener van de vorige observable). Gelukkig kunnen we een observable delen middels de `share()` operator.
* Splits de declaratie en gebruik `share()`:
```Kotlin
    override fun onCreate(savedInstanceState: Bundle?) {

        // …

        val startDateObservable = RxView.clicks(button_start_date).flatMap { getDate() }.share()

        val sub = CompositeSubscription()

        sub.add(startDateObservable.subscribe {
            textview_start_date.text = dateFormat.format(it)
        })

        // …
    }
```
:sparkles: De `share()` operator is een shortcut voor `publish().refCount()`. De `publish()` herpubliceert de observable (als een `ConnectableObservable`), en gaat pas emitten nadat `connect()` is aangeroepen. `refCount()` roept vervolgens `connect()` aan, en houdt bij hoeveel subscribers hij heeft. Als die weer 0 is, zal hij unsubscriben van de source observable.
:heavy_exclamation_mark: Let erop dat je _alle_ gedeelde observables unsubscribet, anders krijgen we memory leaks zoals eerder beschreven.

Daar moeten we wat mee kunnen. We switchen eerst weer even naar de `MainPresenter` toe. Om te beginnen hebben we daar natuurlijk de `WebService` nodig, om de data op te halen. Dat zjn er twee, dus die stoppen we in een `CompositeSubscription`. Daarna maken we een een method om de data te observen.
* Dat kan je bijvoorbeeld zo doen:
```Kotlin
    private val webService = WebService.instance
    private var revenueAndProfitSubs: CompositeSubscription? = null

    fun observeDates(startDate: Observable<Date>, endDate: Observable<Date>) {
        val dates = Observable.combineLatest(startDate, endDate, { startDate, endDate -> Pair(startDate, endDate) }).share()

        val subs = CompositeSubscription(
            dates.observeOn(Schedulers.io()).map { webService.getRevenue(it.first, it.second) }.subscribe { Timber.i("Revenue: %f", it)},
            dates.observeOn(Schedulers.io()).map { webService.getProfit(it.first, it.second) }.subscribe { Timber.i("Profit: %f", it)}
        )
        revenueAndProfitSubs?.unsubscribe()
        revenueAndProfitSubs = subs
        addViewLifeCycleSubscription(subs)
    }
```
   We combineren eerst de twee date-observables tot één, waarbij we altijd de laatste start- en einddatum hebben. Als we beide data nog niet hebben worden ze nog niet gemerged: je krijgt echt de laatste van elke. Dus als je eerst een startdatum geeft van 5 december gebeurt er niets, geef je dan een einddatum van 6 december dan komt er een `Pair<Date, Date>` uit met 5 en 6 december. Verander je daarna de einddatum naar 9 december, dan krijg je weer een nieuw pair met weer 5 december als eerste waarde maar de tweede waarde is de nieuwe einddatum van 9 december.

   Hier willen we weer twee keer op subscriben, want we willen zowel de omzet als de winst ophalen, dus gebruiken we weer `share()`. Deze worden hier, net zoals in de `MainActivity`, toegevoegd aan een subscription die we unsubscriben als de view wordt gedetached.

   :heavy_exclamation_mark: We gebruiken als eerste de operator `observeOn`, om in deze chain vanaf dat moment van thread te switchen. We switchen naar een io-thread omdat we een netwerkrequest gaan doen, wat natuurlijk altijd een goed idee is maar het moet ook van OkHttp: als je het niet doet (en op de main thread blijft) dan zal hij throwen. Vervolgens gebruiken we de `map()` operator, hier gaan we dus het `Pair<Date, Date>` dat we hebben mappen naar de float die het (synchrone!) webrequest teruggeeft. Daarna volgt een eenvoudige subscriber.

* Laat de presenter de dates observen (vanuit `MainActivity`)
```Kotlin
    presenter.observeDates(startDateObservable, endDateObservable)
```

Het enige wat nu nog rest is dit op je scherm krijgen in plaats van in je log. Dat zal je vast niet al teveel problemen opleveren. Op de view maken we methods om de data te tonen. Daarvoor hebben we eerst een `NumberFormat` nodig (nou ja, niet per sé, maar ik wil nog even het `init` block laten zien en over constructors praten). Even terug naar de `MainActivity` dus.
* Daar voeg je dit toe:
```Kotlin
    private val numberFormat = NumberFormat.getInstance()

    init {
        numberFormat.minimumIntegerDigits = 1
        numberFormat.minimumFractionDigits = 2
        numberFormat.maximumFractionDigits = 2
    }
```

---

### :sparkles: Over Kotlin en constructors

Goed, `init`? Ja, zoals je ziet hebben we geen constructor opgegeven. In Kotlin heb je nul of één _primary_ constructors en nul of meer _secondary_ constructors. De primary bevat _geen_ body, het enige wat je kan declareren zijn properties.
```Kotlin
    class Foo constructor(var foo: String) {
    }
```
Omdat we de constructor geen visibility modifier (of annotations) geven, kunnen we het keyword `constructor` ook weglaten, en de lege body van de class ook. Dit is dus ook genoeg:
```Kotlin
    class Foo(var foo: String)
```
Note ook hier dat deze ook meteen `public` zijn omdat we geen visibility op hebben gegeven:
```Kotlin
    val foo = Foo("foo")
    foo.foo = "foobar"
```

Stel dat we `Foo` ook willen kunnen initializeren met een `Int` of een `Float`, dan kan dat bijvoorbeeld als volgt:
```Kotlin
    class Foo(var foo: String) {
        constructor(foo: Int) : this(foo.toString())
        constructor(foo: Float) : this(foo.toInt())
    }
```
Omdat we een waarde willen in de primary constructor, moeten we die ook aanroepen. De constructor met de `Float` roept nu eerst die met de `Int` aan die vervolgens de primary met een `String` aanroept.

Eventueel kan je deze constructors ook een body geven. Maar als we code hebben die we altijd willen uitvoeren hebben we daarvoor het `init` block, wat in dit geval zou neerkomen op de _body_ van de _primary_ constructor:
```Kotlin
    init {
        Timber.i("Foo init")
        foo = "Value: " + foo
    }
```

Als je echter géén primary constructor hebt dan wordt het init-block óók uitgevoerd.
```Kotlin
    class Foo {

        var foo: String? = null

        constructor(foo: String) {
            println("String constructor met foo: " + (foo ?: null))
            this.foo = foo
            println("this.foo: " + this.foo)
        }

        constructor(foo: Int): this(foo.toString()) {
          println("Int constructor met foo: " + (foo ?: null))
        }

        constructor(foo: Float) : this(foo.toInt()) {
          println("Float constructor met foo: " + (foo ?: null))
        }
        
        init {
            println("Foo init met this.foo: " + (foo ?: "null"))
        }
    }
```
De volgorde is op zich logisch, maar op het eerste gezicht niet helemaal intuïtief. Als we Foo initialiseren met een float `1.23f` dan krijg je het volgende in je logs:
```
    Foo init met this.foo: null
    String constructor met foo: 1
    this.foo: 1
    Int constructor met foo: 1
    Float constructor met foo: 1.23
```

In het geval van overerving van een class die geen primary constructor heeft is dat ook handig als je initialisatiecode wil uitvoeren en je niet weet welke constructor(s) geraakt worden, zodat je niet in iedere constructor zelf een `init()` call hoeft toe te voegen (wat je weer kan vergeten).

---

Goed, terug naar onze app! We moeten in de `MainActivity` nog methods hebben om de omzet en winst te tonen. Dat is nu niet zo spannend meer:
```Kotlin
    fun updateRevenue(amount: Float?) {
        if (amount == null) {
            textview_revenue.text = "Geen data"
            return
        }
        textview_revenue.text = StringBuilder("€ ").append(numberFormat.format(amount)).toString()
    }
```

:sparkles: Zoals eerder aangegeven is het netter om de `Geen data` string in je `strings.xml` op te nemen. IntelliJ laat je dit al weten door de string te highlighten. Als je ook hier op `⌥↩︎` drukt en dan kiest voor "Extract string resource" krijg je een dialog om hem toe te voegen. Dat werkt op zich prima, maar we zien nog even een leuke IntelliJ-quirk die nu optreedt door de manier waarop Kotlin met bepaalde methods omgaat. Hij verandert namelijk je code naar:
```Kotlin
    textview_revenue.text = getString(R.string.no_data)
```
Dat werkt prima, maar een `TextView` heeft een setter waarmee je er direct een resource ID kan opgeven: `setText(int)`. Maar wat er dus extra interessant aan is, is dat een `TextView` eigenlijk helemaal geen (publieke) property `text` heeft, maar alleen de method `setText(string)`. Kotlin interepreteert alle `set***` methods als setters en `get***` als  getters, en exposet die alsof het properties zijn. Als je dus ook bijvoorbeeld deze code hebt:
```Kotlin
    val foo = "foo"
    fun getFoo(): String {
        return "bar"
    }
```
zal hij steigeren, omdat de signatures hetzelfde zijn.
Om heel eerlijk te zijn weet ik niet precies waarom hij er in dit geval voor kiest om de `String` als property te zien en niet de `Int`, ik vermoed dat dat door de `kotlin-android` plugin komt. Je kan in ieder geval niet de volgende code gebruiken:
```Kotlin
    textview_revenue.text = R.string.no_data
```
Maar je kan wel `setText(int)` gebruiken:
```Kotlin
    textview_revenue.setText(R.string.no_data)
```

Na dit uitstapje moeten we nog even vanuit de `MainPresenter` zorgen dat dit aangeroepen wordt. Let op! We gaan hierbij aan de UI zitten dus moeten we dit doen vanuit de main thread. We moeten dus na het ophalen van de data weer switchen van thread:
```Kotlin
    dates
        .observeOn(Schedulers.io())
        .map { webService.getRevenue(it.first, it.second) }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {  mvpView?.updateRevenue(it) },
```

Als alles goed gaat, zijn we nu klaar.

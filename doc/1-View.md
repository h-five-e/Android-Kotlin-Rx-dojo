## Step 1: View

We gaan eerst zorgen dat we een paar dingen te zien krijgen als we de app starten. Daarvoor openen we eerst `res/layout/activity_main.xml`. Waarschijnlijk opent hij standaard met de Design-view. Naar mijn mening is het prettiger werken in de Text-view, waarbij je de xml ziet. Sowieso zal je daar wel eens in moeten duiken als je iets vernaggelt met de designer dus we gaan je daar meteen maar bekend mee maken. Hardcore.

Je ziet dat het bestand nu enkel een lege `RelativeLayout` bevat. Voor ons is het gemakklijker om een verticale `LinearLayout` te gebruiken. Alle views die je in een `LinearLayout` zet komen in principe onder of naast elkaar te staan (afhankelijk van de `orientation` property). Dat is precies wat we nodig hebben voor dit voorbeeld, maar voel je vrij om het zelf anders in te vullen.
* Verander het type van `RelativeLayout` en voeg de property `orientation` toe met de waarde `vertical`
```XML
<LinearLayout
    android:id="@+id/activity_main"
    orientation="vertical"
    ...
    >
```

We willen in ieder geval het volgende:
* Een knop om de startdatum te selecteren
* Een knop om de einddatum te selecteren
* Een veld om de startdatum te tonen
* Een veld om de einddatum te tonen
* Een veld om de omzet te tonen
* Een veld om de winst te tonen

Voor de knoppen hebben we `Button`s, voor de teksten `TextView`s. Je moet (ok, _bijna_) altijd de `layout_width` en `layout_height` property's opgeven. Deze geven we meestal op in `dp`s (density independent pixels), of we gebruiken de waarden `wrap_content` en `match_parent`; de eerste maakt het element precies groot genoeg voor de content, de tweede past de grootte aan aan het parent-element. De elementen die we vanuit de code willen aanspreken moeten we ook een ID geven, bijvoorbeeld `@+id/button_start_date`. De `@` geeft aan dat het om een verwijzing gaat, de `+` dat je hem dynamisch aanmaakt (we zouden ook een losse file kunnen maken met ids erin gedefiniëerd, maar dat wordt voor layouts eigenlijk nooit gedaan want dat levert je heel veel bookkeeping op), `id` is het type en het stuk na de `/` mag je zelf verzinnen. Zowel de buttons en de textviews hebben een `text` property waarmee we aan kunnen geven wat erop moet komen.
* Zo kan je bijvoorbeeld dit maken:
```XML
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        >
            <Button
                android:id="@+id/button_start_date"
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="wrap_content"
                android:text="Startdatum"
                />
            <TextView
                android:id="@+id/textview_start_date"
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="wrap_content"
                android:freezesText="true"
                />
    </LinearLayout>
```
   Dat begint met een nieuwe _horizontale_ `LinearLayout`, met daarin een `Button` en een `TextView`. De linearlayout wordt in de breedte aangepast aan het scherm, en in de hoogte wrapt hij om de content heen. We doen hier gelijk wat speciaals, want we zien bij de children dat ze een `layout_width` van `0dp` hebben: ze zouden dan dus eigenlijk helemaal niet zichbaar moeten zijn maar ze zijn dat wel. Waarom? Omdat ze beide een property `weight` hebben, dat kan je aan alle children van een `LinearLayout` geven. Dat betekent dat je de breedte (of de hoogte, bij een verticale oriëntatie) verdeelt onder de elementen. Ze hebben beide hetzelfde gewicht dus worden ze beide even breed (als er elementen tussenzitten met een bepaalde breedte dan worden die eerst berekend waarna de elementen met een gewicht de resterende ruimte verdelen).

   :warning: Vergeet de overige elementen niet.

   :heavy_exclamation_mark: De `TextView`s die we dynamisch gaan vullen krijgt de `freezesText` property op `true`. Dit hebben we nodig omdat Android de activity opnieuw zal opbouwen als het scherm bijvoorbeeld geroteerd wordt, of wordt teruggehaald uit de achtergrond. Door `freezesText` te gebruiken zal hij de state van de view bewaren en bij het opnieuw opbouwen automatisch opnieuw invullen, zodat we dat zelf niet hoeven te doen.

In de rest van de code gebruik ik de volgende ids voor verschillende elementen:
* Knop startdatum: `button_start_date`
* Knop einddatumn: `button_end_date`
* Label startdatum: `textview_start_date`
* Label einddatumn: `textview_end_date`
* Label omzet: `textview_revenue`
* Label winst: `textview_profit`

:sparkles: Als je het echt mooi wil doen, zet je de inhoud van de `text` property's in `strings.xml` zodat je later makkelijk een vertaling kan aanleveren (of teksten kan veranderen als dat moet, zonder dat je de hele app door hoeft te zoeken). Het is een best practice die ook door de linter wordt aangegeven in Android Studio. De gemakklijkste manier is op `⌥↩︎` drukken nadat je de tekst hebt ingevuld en een naam invullen in de dialog die opkomt. De tekst wordt dan naar `strings.xml` verplaatst en vervangen door een verwijzing, bijvoorbeeld `@string/label_revenue`. Makkie.

We willen natuurlijk dat er ook wat gebeurt als je op de knoppen drukt. Laten we eerst maar even heel simpel een tekstje veranderen. Code komt in de `MainActivity` class die we eerder naar Kotlin hebben geconverteerd. Middels de [RxBinding library](https://github.com/JakeWharton/RxBinding) kunnen we subscriben op events van de views. Als je subscibet krijg je een `Subscription` object terug, wat je bij moet houden. Omdat de views gedestroyed worden bij het opnieuw instantiëren van de activity (bijvoorbeeld als je het scherm roteert), moeten we op dat moment ook unsubscriben omdat we anders gaan leaken. Verder doen we niks met deze subscriptions dus gaan we ze bijhouden in een `CompositeSubscription`, dat is één subription waar je andere subscriptions aan kan toevoegen en in een keer kan unsubscriben.

* Voeg een CompositeSubscription variable toe:
```Kotlin
    private var liveCycleSubscriptions: CompositeSubscription? = null
```
   :heavy_exclamation_mark: In Kotlin heb je _values_ en _variables_. Values zijn read-only en worden gedeclareerd met `val`. Variables zijn wel variabel en declareer je met `var`. Het verschilt maar één letter, maar gelukkig let IntelliJ goed op om je voor fouten te behoeden.
   :heavy_exclamation_mark: Het type volgt in Kotlin ná de naam van de property. Als de compiler het type kan herleiden hoef je het type niet op te geven. Het vraagteken betekent dat deze waarde _nullable_ is.

* Override de onDestroy functie, waarin je de composite unsubscribet:
```Kotlin
    override fun onDestroy() {
        liveCycleSubscriptions?.unsubscribe()
        super.onDestroy()
    }
```
   :heavy_exclamation_mark: Het vraagteken staat erachter omdat `liveCycleSubscriptions` nullable is en er zo een automatische null-check uitgevoerd wordt. In dit geval wordt `unsubscribe()` dus alleen aangeroepen als `liveCycleSubscriptions` niet null is. Mocht het nou zo zijn dat `liveCycleSubscriptions` niet null mág zijn op dat moment, dan kan je twee bangs gebruiken in plaats van een vraagteken (`liveCycleSubscriptions!!.unsubscribe()`) – in dat geval wordt er een exception gethrowd als hij null is. Doorgaans is dit een teken van code smell.

* En zorg voor een nieuwe voor je gaat subscriben, dus in de `onCreate` zullen we de `liveCycleSubscriptions` toewijzen:
```Kotlin
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sub = CompositeSubscription()

        // hier subscriben we op view events

        liveCycleSubscriptions?.unsubscribe()
        liveCycleSubscriptions = sub
    }
```
   :heavy_exclamation_mark: Waarom deze constructie? We willen deze subscription gebruiken om nieuwe subscriptions aan toe te voegen. Als we dat willen doen met de class variable dan _kan die weer null zijn door interference vanuit een andere thread_. Nu is die kans vrij klein in dit geval, en zou het sowieso niet voor mogen komen, maar het is good practice om er rekening mee te houden. De lokale value (`sub`) bestaat echter, dus die kunnen we gebruiken. Nadat we straks vanalles en nogwat hebben gesubscribed, doen we nog even een potentieel noodzakelijke unsubsribe en wijzen `sub` toe aan `liveCycleSubscriptions`.

   :sparkles: We gaan hier verder niet heel diep in op de [lifecycle](https://developer.android.com/guide/components/activities/activity-lifecycle.html). Zodra de activity opnieuw wordt gemaakt (bijvoorbeeld doordat het device wordt geroteerd) maken we hier een nieuwe observable. Je zal later merken dat je daardoor opnieuw twee data moet selecteren om een response te krijgen, ook al staan er al wel een start- en einddatum in je view.

* We kunnen nu gaan subscriben op onze views!
   Dankzij `kotlin-android-extensions` kunnen we nu direct het field `button_start_date` aanspreken en weet hij dat dit een `Button` is. Zonder deze extensions zouden we de volgende code moeten gebruiken:
```Kotlin
    var button_start_date = findViewById(R.id.button_start_date) as Button
```
   NB. Deze code throwt als de view niet gevonden kan worden (`findViewById()` returnt `null`), of als de gevonden `View` geen `Button` is. Maar dat is niet heel veel anders als de synthetic `button_start_date` gebruiken.

   Wij kunnen het daarom wat korter schrijven:
```Kotlin
    sub.add(RxView.clicks(button_start_date).subscribe {
        Timber.i("Button start date clicked")
    })
```
   Dit subscribet op de clicks van `button_start_date`. Deze overload van `subscribe` pakt een lambda die uitgevoerd wordt als er een nieuw event plaatsvindt (`onNext`), in dit geval dus iedere keer als je op de knop klikt.

   :heavy_exclamation_mark: Wat belangrijk is om rekenig mee te houden is dat je niet meer dan 1x tegelijk kan subscriben op een event van een view (met `RxBinding`). Mocht je dat heel graag willen dan kan je de observable sharen, maar daar gaan we nu niet op in.

   :sparkles: Als het laatste argument van een functie een lambda is, kan je die buiten de `()` plaatsen.


Nu hebben we nog geen datum geselecteerd. Android biedt gelukkig een standaard datumprikker aan, alleen werkt die met een callback en niet mooi reactive. We zullen die dus moeten wrappen!
* Dat ziet er dan zo uit:
```Kotlin
    private fun getDate(): Observable<Date> {
        return Observable.create { subscriber: Subscriber<in Date> ->
            if (subscriber.isUnsubscribed) return@create

            val dialog = DatePickerDialog(this)

            dialog.setOnDateSetListener { datePicker, year, month, day ->
                if (subscriber.isUnsubscribed) return@setOnDateSetListener

                val calendar = Calendar.getInstance()
                calendar.clear()
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                subscriber.onNext(calendar.time)
                subscriber.onCompleted()
            }

            dialog.setOnDismissListener {
                if (subscriber.isUnsubscribed) return@setOnDismissListener
                subscriber.onCompleted()
            }

            subscriber.add { dialog.dismiss() }
            dialog.show()
        }
    }
```
   :heavy_exclamation_mark: Je moet hierbij even opletten met de auto-import van IntelliJ: importeer éérst de `rx.Observable`, en daarna pas de `java.util.Date`, want om een of andere reden wil IntelliJ altijd heel graag `java.util.*` importeren en dan krijg je ook `java.util.Observable` cadeau wat dus iets heel anders is dan `rx.Observable`.

   Hier gebeurt best veel!
   * We definiëren eerst een functie die een `Observable<Date>` teruggeeft.
   * Dat betekent dat de `Observable.create` een `Observable<Date>` terug moet geven. De lambda die we `create` meegeven krijgt daarom als eerste (en enige) argument een `Subscriber<in Date>` (`in` is een [variance modifier](https://kotlinlang.org/docs/reference/generics.html), wat gelukkig in Kotlin niet zo'n brainmelting moeilijk gebeuren is als in Java – met `in` kan hij dat type consumeren maar niet produceren en het omgekeerde geldt voor `out`). Dat zien we straks terug.
   * Een nadeel van `Observable.create` is wel dat je subscriber al unsubscribed kan zijn voordat je hier bent. We moeten hier dus returnen, en dat doen we naar het impliciete label `create@` anders wil hij vanuit de lambda returnen.
   * Vervolgens maken we een `DatePickerDialog`, en geven die een listener. Deze listener roept `onNext()` aan op de subscriber (als die nog gesubscribed is, in ieder geval) met de geselecteerde datum, gevolgd door een `onCompleted` (zodat de stream netjes afgesloten wordt).
   * Als de dialog wordt gedismissed roepen we alleen `onComplete()` aan. Dat komt er in essentie op neer dat we het event negeren. Je zou er bijvoorbeeld ook voor kunnen kiezen `onNext()` aan te roepen met `null` of `onError()` aan te roepen, afhankelijk van je eisen.
   * De regel `subscriber.add { dialog.dismiss() }` is niet erg intuïtief:
      * `subscriber.add(action)` is een shortcut voor `subscriber.add(Subscriptions.create(action))`;
      * Er wordt dus een nieuwe subscription gemaakt, met de lambda `action`;
      * Een `Subscription` is niet veel meer dan interface waarmee je kan unsubscriben (dat hebben we eerder gezien);
      * De `action` is dan ook de actie die uitgevoerd moet worden zodra de betreffende subscription wordt geunsubscribed;
      * => Als er wordt gesubscribed op de `Observable` die we returnen en we unsubscriben voordat er een datum is gekozen dan wordt de dialog gedismissed.
   * En vervolgens wordt de dialog getoond

   ---

   :sparkles: Als je SDK-level < 24 wil ondersteunen, wordt het instantiëren van de `DatePickerDialog` en het setten van de listener net iets lelijker, maar niet bijzonder anders:
```Kotlin
    val today = Calendar.getInstance()
    val dialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, month, day ->
        if (subscriber.isUnsubscribed) return@OnDateSetListener

        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        subscriber.onNext(calendar.time)
        subscriber.onCompleted()
    }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
```

   ---

Laten we dit eens gaan proberen.
* Als eerste voegen we een `DateFormat` toe aan onze `MainActivity`, die we gaan gebruiken om een datum een beetje fatsoenlijk weer te geven:
```Kotlin
    private val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
```
* Dan kunnen we nu gaan subscriben op de clicks!
```Kotlin
    sub.add(RxView.clicks(button_start_date).flatMap { getDate() }.subscribe {
        textview_start_date.text = dateFormat.format(it)
    })
```
   Middels `flatMap` mappen we de bestaande `Observable` (van clicks) naar een andere, in dit geval een die afkomstig is van `getDate()`. Daarop subscriben we en vullen de datum in op het betreffende label. Als je nu op de knop klikt wordt de datepicker getoond. Kies je een datum, dan wordt deze weergegeven in het betreffende label.

   :warning: Vergeet zelf niet ook de button te maken voor de einddatum.

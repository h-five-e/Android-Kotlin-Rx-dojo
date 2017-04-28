## Step 4: Foutafhandeling

Natuurlijk gaat niet alles goed. Dus als het fout gaan crasht de app.

Gelukkig is dat niet zo moeilijk om dat te fixen, want we hebben één flow:

1. Clicks van de `View`s
1. Die worden gemapt naar `Date`s
1. Die worden (via de `WebService`) gemapt naar `Float`s

In het stuk van de chain uit de view zullen geen fouten voorkomen: `RxBinding` geeft geen fouten (voor zover ik weet althans, maar niet in het geval van clicks).
* Laat daarom bijvoorbeeld `getRevenue()` in de `WebService` plat gaan:
```Kotlin
    fun getRevenue(startDate: Date, endDate: Date): Float? {
        throw Exception("Computer says nooo")
    }
```

Dit laat je app crashen met de volgende foutmelding:
```Kotlin
    java.lang.IllegalStateException: Exception thrown on Scheduler.Worker thread. Add `onError` handling.
```

Ja dat klopt dus, want error handling, dat gaan we nu doen.

Laten we nog even kijken naar hoe we subscriben op de de dates vanuit de `MainPresenter`:
```Kotlin
    dates
        .observeOn(Schedulers.io())
        .map { webService.getRevenue(it.first, it.second) }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { mvpView?.updateRevenue(it) }
```

We subscriben hier met enkel een lambda, die uitgevoerd wordt als er nieuwe data binnenkomt. Zo'n basic subscriber is wel leuk, maar kan dus niet met fouten omgaan. We kunnen daarom het beste subscriben met een subscriber die `Subscriber<T>` extent.
* Dat doe je zo:
```Kotlin
    .subscribe(object : Subscriber<Float?>() {
        override fun onNext(t: Float?) {
            mvpView?.updateRevenue(t)
        }

        override fun onError(e: Throwable?) {
            Timber.i("Fout")
        }

        override fun onCompleted() {}
    })
```

Het enige wat je nu nog moet doen is de gebruiker informeren.
* Voeg een simpele foutmelding dialog toe aan de `MainActivity`:
```Kotlin
    fun showError() {
        AlertDialog.Builder(this)
                .setTitle(R.string.errordialog_title)
                .setMessage(R.string.errordialog_message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }
```
   (Gebruik `⌥↩︎` om de teksten toe te voegen aan je `Strings.xml`)
* Roep deze aan vanuit de `onError()` van je `Subscriber`:
```Kotlin
    override fun onError(e: Throwable?) {
        mvpView?.showError()
    }
```

Nu krijg je een (min of meer) keurige foutmelding als er ergens iets fout gaat in de chain. Vergeet niet om `getRevenue()` weer in zijn oude staat te herstellen voor je de app update indient ;).

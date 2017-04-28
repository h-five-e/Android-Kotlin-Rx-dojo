## Step 0: Opzetten

Step 0 is het resultaat van een nieuw project met een lege activity maken in Android Studio. Dat doet nog niet zoveel. We gaan eerst Kotlin opzetten en onze dependency's regelen, zodat we leuke dingen kunnen doen.

We gaan eerst zorgen dat Kotlin werkt in dit project, want Kotlin > Java.
* Tools > Kotlin > Configure Kotlin in project > Android with Gradle.
`⌃D` (debug). Doet hij nog? Mooi.

Dan gaan we nu onze MainActivity verbouwen naar Kotlin.
* Open de `MainActivity`: `⌘O`, `MainActivity↩︎`
* Code > Convert Java to Kotlin File (`⌥⇧⌘K`)
(That was easy! Behalve die onmogelijke shortcut dan…)

We willen ook graag gebruik maken van de Kotlin Android Extensions.
* Open `./app/build.gradle` (staat als `build.gradle (app)` in `⌘⇧O`)
* Onder `apply plugin: 'kotlin-android'` voeg je toe:
```Gradle
    apply plugin: 'kotlin-android-extensions'
```

Dit is ook wel een goed moment om wat basis-dependency's op te zetten.
* Voeg deze dependencies toe:
```Gradle
    compile 'com.jakewharton.timber:timber:4.3.1'
    compile 'com.squareup.okhttp3:okhttp:3.4.2'

    // Reactive
    compile 'io.reactivex:rxjava:1.2.3'
    compile 'io.reactivex:rxandroid:1.2.1'
    compile 'io.reactivex:rxkotlin:0.60.0'
    compile 'com.jakewharton.rxbinding:rxbinding-kotlin:0.4.0'
```

Onze app wil met de Uren-api praten, maar dat lukt alleen als we ook op internet mogen. Dat is een permissie die we aan Android moeten vragen (gelukkig niet aan de gebruiker).
* Open `AndroidManifest` (fysieke lokatie: `./app/src/main/AndroidManifest.xml`)
* Voeg toe (boven `<application />`):
```XML
    <uses-permission android:name="android.permission.INTERNET" />
```
* Optioneel: als je het https-verkeer wil debuggen met een proxy en je device gebruikt API 24 of hoger moet je user-certificates toestaan:
   * In je resources map (`./app/src/main/res`, of in de Android browser `app/res`) maak je een nieuwe map `xml`
   * Rechtsklik, New > XML resource file: `network_security_config`
   * Plak:
```XML
    <?xml version="1.0" encoding="utf-8"?>
    <network-security-config>
        <base-config>
            <trust-anchors>
                <certificates src="system" />
            </trust-anchors>
        </base-config>
        <debug-overrides>
            <trust-anchors>
                <certificates src="user" />
            </trust-anchors>
        </debug-overrides>
    </network-security-config>
```
   * In je `AndroidManifest` voeg je de volgende property toe aan je `application` object:
```XML
    android:networkSecurityConfig="@xml/network_security_config"
```

Als laatste is het wel fijn om [Timber](https://github.com/JakeWharton/timber) aan te slingeren (de dependency hebben we al toegevoegd), dat maakt loggen wat prettiger.
* Maak een nieuwe class `Application` die `android.app.Application` extend:
```Kotlin
    class Application : Application() {
        override fun onCreate() {
            super.onCreate()

            if (BuildConfig.DEBUG) {
                Timber.plant(Timber.DebugTree())
            }

            Timber.i("We're off!")
        }
    }
```
* Zet in je `AndroidManifest` dat je deze applicatie wil gebruiken in plaat van de default implementatie, door dit toe te voegen aan de `application` tag:
```XML
    android:name=".Application"
```

Dan kunnen we nu eindelijk echt beginnen!

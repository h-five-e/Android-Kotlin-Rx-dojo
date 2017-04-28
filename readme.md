# Android/Kotlin/RX Dojo

Dit is de code en documentatie voor een code-dojo bij [Infi](https://www.infi.nl), als (korte) introductie in Android, Kotlin én Reactive programming. Het is daarom op geen van deze gebieden diepgaand maar levert een kleine basis-applicatie op waarmee zelf verder gespeeld kan worden.

De applicatie heeft één scherm, waarop we de winst en omzet van Infi kunnen tonen binnen een gekozen datum-range. Deze data wordt opgehaald uit de [uren-api](https://git.infi.nl/infi/uren-api). Deze api is niet publiek beschikbaar, dus buiten Infi zal je via de VPN verbinding moeten maken.

## Opbouw

De dojo is opgezet in een aantal stappen:
* [Voorbereiding](doc/Voorbereiding.md)
* [0 - Opzetten](doc/0-Opzetten.md)
* [1 - View](doc/1-View.md)
* [2 - Model](doc/2-Model.md)
* [3 - Presenter](doc/3-Presenter.md)
* [4 - Foutafhandeling](doc/4-Foutafhandeling.md)
En als je nog tijd over hebt:
* [Bonus](doc/Bonus.md)

De code om aan een bepaalde stap te beginnen is in de repo getagd met de bijbehorende stap, dus `step-0` voor stap 0, et cetera. Vanaf daar kan je de dojo volgen.

Stap 0 is het resultaat van het creeëren van een nieuw project in Android Studio. Dit kan je natuurlijk óók zelf doen, maar omdat we allemaal op gelijke voet willen beginnen is dit de handigste manier. Ten eerste moet anders elke optie in de UI beschreven worden en ten tweede verandert dat ook met updates aan het platform et cetera.

## Opmerkingen

* Keyboard shortcuts (`⌘C`) zijn voor Mac, maar zullen niet heel erg verschillen voor Windows of \*nix
* :warning: voor dingen die je niet moet vergeten
* :heavy_exclamation_mark: voor belangrijke informatie
* :sparkles: voor extra informatie

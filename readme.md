# OOP Projekt: Online chat

#### Tiimiliikmed - Karl Markus Kiudma, Robin Juul, Hendrik Jaks

### Lühikokkuvõte: 
Krüpteeritud online-chat, mille kaudu saavad tudengid omavahel suhelda ja jagada erinevates chatroomides vastavate õppeainete alast teavet / muljeid. 

### Funktsionaalsus:
* Saab luua ja ühineda channelite ja chat roomidega.
*Saab üksteisele privaatseid sõnumeid saata.
* Sõnumite krüpteerimine. Kõik privaatvestlused võiksid olla E2E-krüpteeritud.
* Sõnumeid ja kasutajakontosi hoiab keskne server, kuhu saab üle võrgu ühenduda. Sõnumitel olemas ka ajatempel. Server hoiab sõnumite ajalugu.
* Kasutaja saab saata helisõnumi teistele kasutajatele ning sõnumi saaja saab seda kuulata.
* Kasutajate autentimine, sisselogimine ja registreerimine (Firebase Authentication liidestus)
* Bot-tugi – lisada bott, mis pakub kasulikke funktsioone (uudistevoog, ilmateade, Delta kohviku menüü). 
  * https://www.err.ee/rss
  * https://www.postimees.ee/rss
  * https://xn--pevapakkumised-5hb.ee/tartu/delta-kohvik
* Kas sõnum on juba nähtud?
* Administraatori õigused – admin saab kasutajaid blokeerida või eemaldada.

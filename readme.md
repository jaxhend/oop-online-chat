# OOP Projekt: Online chat
#### Tiimiliikmed - Karl Markus Kiudma, Robin Juul, Hendrik Jaks

### Lühikokkuvõte:
OnlineChat on veebipõhine vestlusrakendus, mis võimaldab Tartu Ülikooli tudengitel omavahel reaalajas suhelda. Lisaks pakub rakendus AI-juturoboti tuge ning kuvab kasulikku teavet, sealhulgas päevast ilmateadet, aktuaalseid uudiseid ja päevapakkumisi.


## Funktsionaalsused:
* Võimalus luua uusi ja liituda olemasolevate vestlusruumidega.
* Privaatne sõnumivahetus teiste kasutajatega
* Emotikonide kasutamine sõnumites
* Kuvab reaalajas ilma, uudiseid ning Delta kohviku ja Ülikooli Kohviku päevapakkumisi.
* Võimalus vestelda AI juturobotiga, kes annab infot Tartu Ülikooli õppekavade, õppeainete ja muu kohta.
* Light/Dark mode

## Kasutatud tehnoloogiad


### 1. **Frontend (React + Tailwind CSS)**

- Kuvab vestlused, AI-roboti liides, ilmateade, uudised ja päevapakkumised.
- Kasutab WebSocket-ühendust reaalajas sõnumivahetuseks.
- Emotikonide kasutamine EmojiPicker abil.
- REST-päringud uudsite, ilmateate, päevapakkumiste pärimiseks ning AI juturobotiga suhtlemiseks
- Light/Dark mode valik, mis salvestatakse küpsisesse.

### 2. **Backend (Spring Boot + Flask + Scraperid)**

- **Spring Boot**:
  - Kasutab WebSocketit reaalajas sõnumite saatmiseks ja vastuvõtmiseks.

- **Flask server (AI-robot)**:
  - Võtab vastu kasutaja küsimusi ja tagastab vastuse, kasutades eelnevalt scrape’itud ja struktureeritud andmeid Tartu Ülikooli lehtedelt

- **Scraperid (Selenium)**:
  - CsScraper scrape’ib infot https://cs.ut.ee/sitemap.xml
  - OisCourses scrape’ib infot https://ois2.ut.ee/#/courses
  - OisCurricula scrape’ib infot https://ois2.ut.ee/#/curricula
  - Sisseastumien scrape’ib infot https://cs.ut.ee/et/sisseastumine
  - Teadus scrape’ib infot https://cs.ut.ee/et/teadus
  - UtScraper scrape’ib infot https://ut.ee/sitemap.xml


## Kasutusjuhend
Ava brauseris: [utchat.ee](https://utchat.ee)




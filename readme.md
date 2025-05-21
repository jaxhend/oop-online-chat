# OOP Projekt: UTchat
#### Tiimiliikmed - Karl Markus Kiudma, Robin Juul, Hendrik Jaks

### Lühikokkuvõte:
UTchat on veebipõhine vestlusrakendus, mis võimaldab Tartu Ülikooli tudengitel omavahel reaalajas suhelda. Lisaks pakub rakendus AI-juturoboti tuge ning kuvab kasulikku teavet, sealhulgas päevast ilmateadet, aktuaalseid uudiseid ja päevapakkumisi.


## Funktsionaalsused:
* Võimalus luua uusi ja liituda olemasolevate vestlusruumidega.
* Privaatne sõnumivahetus teiste kasutajatega
* Emotikonide kasutamine sõnumites
* Kuvab reaalajas ilma, uudiseid ning Delta kohviku ja Ülikooli Kohviku päevapakkumisi.
* Võimalus vestelda AI juturobotiga, kes annab infot Tartu Ülikooli õppekavade, õppeainete ja muu kohta.
* Light/Dark mode

## Kasutatud tehnoloogiad

## 1. **Deployment **
  - Frontend on hostitud Vercelis ja kättesaadal domeenil utchat.ee

## 2. **Frontend **

- Kuvab vestlused, AI-roboti liides, ilmateade, uudised ja päevapakkumised.
- Kasutab WebSocket-ühendust reaalajas sõnumivahetuseks.
- Emotikonide kasutamine EmojiPicker abil.
- REST-päringud uudsite, ilmateate, päevapakkumiste pärimiseks ning AI juturobotiga suhtlemiseks
- Light/Dark mode valik, mis salvestatakse küpsisesse.

## 3. **Backend **

### **Spring Boot**
- Kasutaja tuvastamine toimub serveripoolse sessioonihalduse abil, kus küpsisesse salvestatud `SESSION_ID` seotakse serveris kasutajanimega.
- Vestlusplatsi käsud ning sõnumite saatmine ja vastuvõtmine toimuvad reaalajas WebSocketi kaudu.
- REST API-de kaudu edastatakse frontendile reaalajas uudised, päevapakkumised ja ilmainfo.



#### Klasside selgitused:

- **MessageProcessor** töötleb kasutaja sisendeid (käske, sõnumeid ja nende filtreerimist) ning edastab vastava info WebSocketi kaudu.
  - Kasutab:
    - `ClientSessionManager` – seansside leidmine ja haldus
    - `CommandHandler` – käsuloogika delegeerimine
    - `ChatRoomMessageService` – sõnumite salvestamine
    - `ChatRoomManager` – ruumide haldus
    - `ProfanityFilter` – sobimatute sõnade filtreerimine

- **WebSocketHandler** seob WebSocketi sessiooni kasutajaga ning edastab kasutaja sisendid `MessageProcessor`ile töötlemiseks.
  - Kasutab:
    - `ClientSessionManager` – seansi tuvastamiseks
    - `MessageProcessor` – sisendi töötlemiseks
    - `ChatRoomManager` – kasutaja eemaldamiseks ruumist ühenduse katkestamisel

- **WebSocketHandshakeInterceptor** kontrollib enne WebSocket-ühenduse loomist, kas brauseris on kehtiv küpsis (`sessionId`), ning katkestab ühenduse, kui see puudub.
  - Lisab kehtiva `sessionId` WebSocketi sessiooni atribuutidesse

- **ChatRoomManager** haldab vestlusruumide loomist ja eemaldamist, suunab kasutajate liitumise ja lahkumise konkreetsele `ChatRoom`ile ning edastab selle kohta vajadusel teavitused teistele ruumi liikmetele.
  - Delegeerib ruumiga seotud tegevused `ChatRoom`ile

- **ChatRoom** haldab kasutajate liitumist ja lahkumist, peab arvestust ruumi liikmete üle ning kontrollib, kas kasutaja tohib ruumiga liituda.
  - Baas `PrivateChatRoom` ja `RegularChatRoom` jaoks

- **ClientSession** salvestab kasutaja seansi info (WebSocket-ühendus, sessiooni ID, kasutajanimi, aktiivne vestlusruum, viimati nähtud sõnumid).
  - Seostatakse WebSocketi sessiooniga

- **ClientSessionManager** haldab aktiivseid seansse ja nende infot (sh kasutajanime seos sessiooni ID-ga.

- **CommandHandler** töötleb käske ning suunab need käsuklassidele edasi, tagastades süsteemipoolsed vastused.
  
- **ChatRoomMessageService** salvestab ja tagastab vestlusruumide sõnumid).
  
- **PrivateChatRoom ja RegularChatRoom** realiseerivad `ChatRoom` loogika erinevatele ruumitüüpidele.



#### Websocketi toimimine:

1. Brauser avab WebSocketi ühenduse.
2. `WebSocketHandshakeInterceptor` kontrollib, kas brauseri küpsis sisaldab kehtivat `sessionId`.
3. Kui küpsis on olemas, ühendatakse sessioon `WebSocketHandler`i kaudu olemasoleva või uue `ClientSession`iga.
4. Kasutaja sisestatud sõnumid edastatakse `MessageProcessor`ile.
5. `MessageProcessor` töötleb käske, filtreerib sisu, salvestab sõnumeid ja edastab need teistele kasutajatele WebSocketi kaudu.
6. `ChatRoomManager` kontrollib ruumide olemasolu ja suunab kasutajad sobivatesse `ChatRoom`idesse.
7. `ChatRoom` haldab liikmeid ja liitumisõigusi.
  
### Scraperid

Rakenduses kasutatakse mitut scraperit, mis koguvad struktureeritud infot Tartu Ülikooli avalikelt veebilehtedelt. 

####  XML-põhised scraperid
Scraperid, mis töötlevad `sitemap.xml` faile, tuvastavad seal loetletud lehed ja külastavad neid, salvestades vajaliku info.

#### SPA-põhised scraperid
Lehekülg laaditakse dünaamiliselt, otsitakse kõik vajalikud lingid ning külastatakse neid, salvestades vajaliku info.

### Andmete salvestamine  
Kõik andmed salvestatakse NoSQL andmebaasi MongoDB


#### Scraperid koguvad infot järgmistelt Tartu Ülikooli ja informaatikainstituudi lehtedelt:
- CsScraper – kogub infot: https://cs.ut.ee/sitemap.xml
- UtScraper – kogub infot: https://ut.ee/sitemap.xml
- OisCoursesScraper – kogub õppeainete infot lehelt: https://ois2.ut.ee/#/courses
- OisCurriculaScraper – kogub õppekavade ja moodulite infot lehelt: https://ois2.ut.ee/#/curricula
- SisseastumineScraper – kogub sisseastumisega seotud infot lehelt: https://cs.ut.ee/et/sisseastumine
- TeadusScraper – kogub teadus- ja uurimistegevuse infot lehelt: https://cs.ut.ee/et/teadus





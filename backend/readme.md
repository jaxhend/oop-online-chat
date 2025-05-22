## 4. *Backend* 

### *Spring Boot*
- Kasutaja tuvastamine toimub serveripoolse sessioonihalduse abil, kus küpsisesse salvestatud `SESSION_ID` seotakse serveris kasutajanimega.
- Vestlusplatsi käsud ning sõnumite saatmine ja vastuvõtmine toimub reaalajas WebSocketi kaudu.
- REST API-de kaudu edastatakse frontendile reaalajas uudised, päevapakkumised ja ilmainfo.
- 



#### Klasside selgitused:


- **ApplicationInfoController** pakub REST API otspunkte ilmainfo, päevapakkumiste ja uudiste edastamiseks frontendile.

- **MessageProcessor** töötleb kasutaja sisendeid (käske, sõnumeid ja nende filtreerimist) ning edastab vastava info WebSocketi kaudu.
  - Kasutab:
    - `ClientSessionManager` – seansside leidmine ja haldus
    - `CommandHandler` – käsuloogika delegeerimine
    - `ChatRoomMessageService` – sõnumite salvestamine
    - `ChatRoomManager` – ruumide haldus
    - `ProfanityFilter` – sobimatute sõnade filtreerimine

- **WebSocketHandler** seob WebSocketi sessiooni kasutajaga ning edastab kasutaja sisendid `MessageProcessor`'ile töötlemiseks.
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

- **ClientSessionManager** haldab aktiivseid seansse ja nende infot (sh kasutajanime seos sessiooni ID-ga).

- **CommandHandler** töötleb käske ning suunab need käsuklassidele edasi, tagastades süsteemipoolsed vastused.
  
- **ChatRoomMessageService** salvestab ja tagastab vestlusruumide sõnumid.
  
- **PrivateChatRoom ja RegularChatRoom** realiseerivad `ChatRoom` loogika erinevatele ruumitüüpidele.



#### Websocketi toimimine:

1. Brauser avab WebSocketi ühenduse.
2. `WebSocketHandshakeInterceptor` kontrollib, kas brauseri küpsis sisaldab kehtivat `sessionId`.
3. Kui küpsis on olemas, ühendatakse sessioon `WebSocketHandler`i kaudu olemasoleva või uue `ClientSession`iga.
4. Kasutaja sisestatud sõnumid edastatakse `MessageProcessor`ile.
5. `MessageProcessor` töötleb käske, filtreerib sisu, salvestab sõnumeid ja edastab need teistele kasutajatele WebSocketi kaudu.
6. `ChatRoomManager` kontrollib ruumide olemasolu ja suunab kasutajad sobivatesse `ChatRoom`idesse.
7. `ChatRoom` haldab liikmeid ja liitumisõigusi.

#### REST API

1.	Brauser teeb GET päringu ühele API otspunktidest (/ilm, /uudised, /paevapakkumised).
2.	Spring Boot controller `ApplicationInfoController` võtab päringu vastu.
3.	Controller kutsub vastavat teenust:
4.	Saadud andmed vormistatakse JSON-vastuseks ja saadetakse frontendile.
5.	Frontendis kuvatakse kasutajale andmed reaalajas.


### Andmete salvestamine  
Kõik andmed salvestatakse NoSQL andmebaasi MongoDB

### Scraperid

Rakenduses kasutatakse mitut scraperit, mis koguvad struktureeritud infot Tartu Ülikooli avalikelt veebilehtedelt. 

#### Scraperid koguvad infot järgmistelt Tartu Ülikooli ja Tartu Ülikooli Arvutiteaduste Instituudi lehtedelt:
- CsScraper – kogub infot: https://cs.ut.ee/sitemap.xml
- UtScraper – kogub infot: https://ut.ee/sitemap.xml
- OisCoursesScraper – kogub õppeainete infot lehelt: https://ois2.ut.ee/#/courses
- OisCurriculaScraper – kogub õppekavade ja moodulite infot lehelt: https://ois2.ut.ee/#/curricula
- SisseastumineScraper – kogub sisseastumisega seotud infot lehelt: https://cs.ut.ee/et/sisseastumine
- TeadusScraper – kogub teadus- ja uurimistegevuse infot lehelt: https://cs.ut.ee/et/teadus

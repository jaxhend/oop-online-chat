# Java
Esitluse link: https://docs.google.com/presentation/d/1xlZ6shxtppz0w7f7ZXmhpdC1JnLFl2AhLyw5ghZBGZs/edit?usp=sharing

Veebirakenduse _backend_'is kasutasime Java raamistikku Spring Boot. See kiht sisaldab kogu rakenduse äriloogikat:
- vestlusruumide ja privaatsõnumite haldus,
- vulgaarsete sõnumite automaatne tuvastamine ja katmine,
- varasemate sõnumite salvestamine H2 andmebaasi ja nende ajastatud kustutamine,
- WebSocket ühenduse konfigureerimine,
- ajastatud uudiste, ilmateate ja päevapakkumiste veebikoorimine,
- ning vastavate teenuste REST API-de pakkumine.
</br></br>

### Tähtsamate klasside selgitused:
- **ApplicationInfoController** ja **SessionController** pakub REST API otspunkte ilma, päevapakkumiste, uudiste ja sessionID edastamiseks _frontend_'ile.

- **MessageProcessor** töötleb kasutaja sisendeid (käske, sõnumeid ja nende filtreerimist) ning edastab vastava info WebSocketi kaudu kasutajale.
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

- **CommandHandler** töötleb käske ning suunab need käsuklassidele edasi, tagastades käsuklassides defineeritud vastused.
  
- **ChatRoomMessageService** salvestab, kustutab ja tagastab vestlusruumide varasemaid sõnumeid H2 andmebaasist.
  
- **ProfanityFilter** tuvastab ja katab vulgaarseid sõnu. Klass kasutab Aho-Corasick algoritmi efektiivseks sõnetöötluseks.
  
</br>

### Websocket:
1. Brauser avab WebSocketi ühenduse.
2. `WebSocketHandshakeInterceptor` kontrollib, kas brauseri küpsis sisaldab kehtivat `sessionId`.
3. Kui küpsis on olemas, ühendatakse sessioon `WebSocketHandler`i kaudu olemasoleva või uue `ClientSession`iga.
4. Kasutaja sisestatud sõnumid edastatakse `MessageProcessor`ile.
5. `MessageProcessor` töötleb käske, filtreerib sisu, salvestab sõnumeid ja edastab need teistele kasutajatele WebSocketi kaudu.
6. `ChatRoomManager` kontrollib ruumide olemasolu ja suunab kasutajad sobivatesse `ChatRoom`idesse.
7. `ChatRoom` haldab liikmeid ja liitumisõigusi.

</br>

#### REST API

1.	Brauser teeb GET päringu ühele API otspunktidest (/ilm, /uudised, /paevapakkumised, /session/init).
2.	Spring Boot controller `ApplicationInfoController` või `SessionController` võtab päringu vastu.
3.	Controller kutsub vastavat teenust.
4.	Saadud andmed vormistatakse JSON-vastuseks ja saadetakse _frontend_'ile.
5.	Frontend'is kuvatakse kasutajale andmeid reaalajas.

</br>

### Veebikoorijad
Lisaks uudiste, ilmateate ja päevapakkumiste veebikoorimisele arendasime scraper-id, mille eesmärk oli koguda teavet AI-juturoboti vektorandmebaasi jaoks.
Koorisime andmeid ÕIS-ist (õppeained ja õppekavad) ning Tartu Ülikooli ja Arvutiteaduse Instituudi veebilehtedelt (ut.ee ja cs.ut.ee).
Viimastel juhtudel kasutasime sitemap’e, et tuvastada vajalikud lingid, mille põhjal filtreerisime välja asjakohase info.

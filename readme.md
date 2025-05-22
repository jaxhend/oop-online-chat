# OOP Projekt: UTchat
#### Tiimiliikmed - Karl Markus Kiudma, Robin Juul, Hendrik Jaks

### Lühikokkuvõte:
UTchat on veebipõhine vestlusrakendus, mis võimaldab Tartu Ülikooli tudengitel omavahel reaalajas suhelda. Lisaks pakub rakendus AI-juturoboti tuge ning kuvab olulist ja kasulikku teavet. Kasutajad saavad hetkega näha, mis päevapakkumisi Delta  ja Ülikooli kohvikutes pakutakse ning jälgida reaalajas Tartu ilma. Veebilehel kuvatakse ka jooksvaid uudiseid, mis aitavad kasutajatel aktuaalsete teemadega kursis olla.




## Funktsionaalsused:
* Võimalus luua uusi ja liituda olemasolevate vestlusruumidega.
* Privaatne sõnumivahetus teiste kasutajatega
* Emotikonide kasutamine sõnumites
* Kuvab reaalajas ilma, uudiseid ning Delta kohviku ja Ülikooli Kohviku päevapakkumisi.
* Võimalus vestelda AI juturobotiga, kes annab infot Tartu Ülikooli õppekavade, õppeainete ja muu kohta.
* Light/Dark mode

## Kasutatud tehnoloogiad

## AI-juturobot
Kasutame **Qwen3-4B** keelemudelit, mis osutus testides kõige efektiivsemaks
AI-mudelit jooksutame HPC Serveris. AI-mudeli efektiivsuse tõstmiseks kasutasime järgmiseid meetodeid:
- Rakendasime RAG'i (Retrieval-Augmented Generation), et anda mudelile reaalajas ette vajalik kontekst. Mudel otsib päringu hetkel etteantud andmebaasist infot juurde-
- Info kättesaamiseks kasutame vektorandmebaasi ning FAISS (Facebook AI Similarity Search) tehnoloogiat, mis leiab lähima vaste vektori kujul ja genereerib selle abil vastuse.

## 2. *Deployment* 
  ### Vercel
  - Veebirakenduse majutus
  - _front-end_ uueneb muudatuste tegemisel automaatselt
  ### Azure
  - Rakenduse _back-end_ loogika asub Azure virtuaalmasinas
  - Kasutame _back-end_'i jooksutamiseks Dockeri konteinereid
  ### ATI UTHPC
  - Lõime systemd teenuse tehisaru käivitamiseks
  - Tehisaru jooksutamiseks kasutame 1 GPU-ga virtuaalmasinat
    

## 3. *Frontend* 
- Kuvab vestlused, AI-roboti liides, ilmateade, uudised ja päevapakkumised.
- Kasutab WebSocket-ühendust reaalajas sõnumivahetuseks.
- Emotikonide kasutamine EmojiPicker abil.
- REST-päringud uudiste, ilmateate, päevapakkumiste pärimiseks ning AI juturobotiga suhtlemiseks
- Light/Dark mode valik, mis salvestatakse küpsisesse.


#### Sessioonihaldus
- Kui kasutaja avab lehe, saadab brauser serverile päringu, et saada uus või kinnitada olemasolev `sessionId`.
- Server salvestab või leiab `sessionId` alusel kasutaja varasema info ja saadab `sessionId` brauserisse küpsisena
- Kui kasutajanimi on juba seotud selle `sessionId`ga, taastatakse see automaatselt.
- Kasutajanime hoitakse ainult serveris

####  Reaalajas suhtlus WebSocketi kaudu

- WebSocket-ühendus luuakse automaatselt kohe pärast `sessionId` saamist.
- Kasutaja sisestatud sõnum või käsk edastatakse WebSocketi kaudu otse serverile.
- Server töötleb sõnumi ja saadab vastuse tagasi, mis kuvatakse kasutajaliideses.
- Kui uus sõnum saabub ajal, mil brauseri sakk ei ole aktiivne, siis kuvatakse visuaalne märguanne vahelehe pealkirjas.

#### AI juturobot
- Kasutaja küsimus saadetakse POST-päringuna serverile, kaasa saadetakse kuni 10 eelmist küsimust-vastust.
- Vastuse ootel kuvatakse kasutajale “AI mõtleb…” animatsioon.
- Kui vastus saabub, lisatakse nii kasutaja sisend kui ka roboti vastus vestluse ajalukku ning kuvatakse ekraanil

#### Andmete kuvamine
- Rakenduse käivitumisel laetakse automaatselt: ilmateade, uudised ja päevapakkumised
- Need andmed saadakse REST API-de päringute kaudu

#### Kasutajaliides
- Kasutaja saab valida Light/Dark mode'i vahel, mis salvestatakse küpsisesse ja taastatakse järgmisel külastusel.
- Leht on jagatud neljaks osaks: Vestlusala, Uudistepaneel, Infopaneel(ilm + päevapakkumised) ja AI juturobot
- Sõnumiväljal on olemas emotikoni valiku võimalus, kust kasutaja saab valida sobiva emotikoni, mis lisatakse sõnumi teksti.
- Stiilid on loodud skaleeruvaks ning toetavad mobiilivaade




## 4. *Backend* 

### *Spring Boot*
- Kasutaja tuvastamine toimub serveripoolse sessioonihalduse abil, kus küpsisesse salvestatud `SESSION_ID` seotakse serveris kasutajanimega.
- Vestlusplatsi käsud ning sõnumite saatmine ja vastuvõtmine toimub reaalajas WebSocketi kaudu.
- REST API-de kaudu edastatakse frontendile reaalajas uudised, päevapakkumised ja ilmainfo.



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

## Kuvatõmmised UTchat veebilehelt

![Kuvatõmmis 2025-05-21 232243](https://github.com/user-attachments/assets/ec0de958-a780-4d5d-9b95-e7c697461aec)
*Joonis 1. Kuvatõmmis utchat.ee veebilehest*


<br/>

![image](https://github.com/user-attachments/assets/f6162805-c919-44cd-aff1-348ceffa32c6)

*Joonis 2. Kuvatõmmis AI-juturoboti aknast*



![UTchat poster](https://github.com/user-attachments/assets/da4ca42b-8ccf-4466-a061-ef117844da3b)
*Joonis 3. UTchat projekti poster*





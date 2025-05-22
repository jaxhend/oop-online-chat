<h1><a href="https://www.utchat.ee" target="_blank" rel="noopener noreferrer">UTchat</a></h1>

#### Tiimiliikmed - Karl Markus Kiudma, Robin Juul, Hendrik Jaks

### Lühikokkuvõte:
UTchat on veebipõhine vestlusrakendus, mis võimaldab Tartu Ülikooli tudengitel omavahel reaalajas suhelda. Lisaks pakub rakendus AI-juturoboti tuge ning kuvab olulist ja kasulikku teavet. Kasutajad saavad hetkega näha, mis päevapakkumisi Delta  ja Ülikooli kohvikutes pakutakse ning jälgida reaalajas Tartu ilma. Veebilehel kuvatakse ka jooksvaid uudiseid, mis aitavad kasutajatel aktuaalsete teemadega kursis olla.

![Kuvatõmmis 2025-05-21 232243](https://github.com/user-attachments/assets/ec0de958-a780-4d5d-9b95-e7c697461aec)
*Joonis 1. Kuvatõmmis utchat.ee veebilehest*
<br>
<br>
<br>
## 1. Veebirakenduse funktsioonid:
* Võimalus liituda olemasolevate ja luua uusi avalikke vestlusruume
  * Vestlusruumide varasemate sõnumite kuvamine
* Privaatne sõnumivahetus teiste kasutajatega
* Sobimatute või vulgaarsete sõnumite automaatne tuvastamine ja katmine
* Emotikonide kasutamise võimalus
* Lugemata sõnumi teavitused
* Kuvab reaalajas:
  * Ilma
  * Uudiseid
  * Delta ja Ülikooli kohviku päevapakkumisi
* Võimalus vestelda AI juturobotiga, kes annab infot:
  * Tartu Ülikooli õppekavade kohta
  * Õppeainete kohta
  * lehekülje ut.ee kohta
  * lehekülje cs.ut.ee kohta
* _Light_/_Dark_ mode tugi
* Mobiilivaate tugi
<br>

## 2. *Deployment* 
Domeen on _hostitud_ Zone's.
  ### Vercel – [utchat.ee](https://www.utchat.ee)
  - Veebirakenduse _frontend_'i majutus
  - Automaatne CI/CD Githubi repositooriumiga
  ### Azure - [api.utchat.ee](https://api.utchat.ee)
  - Backend’i äriloogika jookseb Azure'i virtuaalmasinas (B2ls_v2).
  - Dockeri konteinerid:
    - **Nginx proxy** – turvaliseks ühenduseks internetiga
    - **Spring Boot** – rakenduse äriloogika ja API teenused
    - **Certbot** – HTTPS-sertifikaadi automaatne uuendamine
  ### Tartu Ülikooli High Performance Computing Center (HPC) - [llm.utchat.ee](https://llm.utchat.ee)
  - Tehisaru jooksutamiseks kasutame **Nvidia Tesla V100 16GB GPU**
  - Lõime systemd teenuse tehisaru automaatseks käivitamiseks
<br>

## 3. *Frontend* 
Frontend on arendatud Reacti ja CSS-iga ning kasutab Motion UI raamistiku animatsioonide jaoks.

- Kuvab vestlused, AI-juturoboti liidese, ilmateate, uudised ja päevapakkumised.
- Reaalajas sõnumivahetuseks kasutab WebSocket-ühendust.
- Emotikonide valik on lahendatud EmojiPicker komponendiga.
- Andmete pärimiseks (uudised, ilmateade, päevapakkumised, sessionID) ja AI-juturobotiga suhtlemiseks kasutatakse REST API päringuid.
- Kasutajal on võimalik valida _Light_ või _Dark_ režiim, mille eelistus salvestatakse brauseri lokaalsesse mällu (localStorage).

Lisainfo leiad [_frontend_'i README failist](./frontend/readme.md).
<br>
<br>

## 4. *Backend* 
Spring ja LLM
Lisainfo leiad [backend'i README failist](./backend/readme.md).
<br>
<br>

## 5. AI-juturobot
Kasutame **Qwen3-4B** keelemudelit, mis osutus testides kõige efektiivsemaks
AI-mudelit jooksutame HPC Serveris. AI-mudeli efektiivsuse tõstmiseks kasutasime järgmiseid meetodeid:
- Rakendasime RAG'i (Retrieval-Augmented Generation), et anda mudelile reaalajas ette vajalik kontekst. Mudel otsib päringu hetkel etteantud andmebaasist infot juurde-
- Info kättesaamiseks kasutame vektorandmebaasi ning FAISS (Facebook AI Similarity Search) tehnoloogiat, mis leiab lähima vaste vektori kujul ja genereerib selle abil vastuse.

![image](https://github.com/user-attachments/assets/94597f65-492e-45f4-a76c-d005afe867b4)
<br>
*Joonis 2. Kuvatõmmis AI-juturoboti aknast*
<br>
<br>


## 2025. aasta tudengiprojekt poster
![UTchat poster](https://github.com/user-attachments/assets/da4ca42b-8ccf-4466-a061-ef117844da3b)
*Joonis 3. UTchat poster*





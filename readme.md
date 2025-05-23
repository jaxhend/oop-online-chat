<h1><a href="https://www.utchat.ee" target="_blank" rel="noopener noreferrer">UTchat</a></h1>

#### Tiimiliikmed - Karl Markus Kiudma, Robin Juul, Hendrik Jaks

### Lühikokkuvõte:
UTchat on veebipõhine vestlusrakendus, mis võimaldab Tartu Ülikooli tudengitel omavahel reaalajas suhelda. Lisaks pakub rakendus AI-juturoboti tuge ning kuvab olulist ja kasulikku teavet. Kasutajad saavad hetkega näha, mis päevapakkumisi Delta  ja Ülikooli kohvikutes pakutakse ning jälgida reaalajas Tartu ilma. Veebilehel kuvatakse ka jooksvaid uudiseid, mis aitavad kasutajatel aktuaalsete teemadega kursis olla.

![Kuvatõmmis 2025-05-21 232243](https://github.com/user-attachments/assets/ec0de958-a780-4d5d-9b95-e7c697461aec)
*Joonis 1. Kuvatõmmis utchat.ee veebilehest.*
<br>
<br>
<br>
## 1. Veebirakenduse funktsionaalsused
* Võimalus liituda olemasolevate ja luua uusi avalikke vestlusruume
  * Vestlusruumide varasemate sõnumite kuvamine
* Privaatne sõnumivahetus teiste kasutajatega
* Sobimatute või vulgaarsete sõnumite automaatne tuvastamine ja katmine
* Emotikonide kasutamise võimalus
* Lugemata sõnumite teavitused
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
Domeen on _hostitud_ Zone'is.
  ### Vercel – [utchat.ee](https://www.utchat.ee)
  - Veebirakenduse _frontend_'i majutus
  - Automaatne CI/CD Githubi repositooriumiga
  ### Azure - [api.utchat.ee](https://api.utchat.ee)
  - Backend’i äriloogika jookseb Azure'i virtuaalmasinas (B2ls_v2).
  - Dockeri konteinerid:
    - **Nginx proxy** – turvaliseks ühenduseks internetiga
    - **Spring Boot** – rakenduse äriloogika ja REST API teenused
    - **Certbot** – HTTPS-sertifikaadi automaatne uuendamine
  ### Tartu Ülikooli High Performance Computing Center (HPC) - [llm.utchat.ee](https://llm.utchat.ee)
  - Tehisaru jooksutamiseks kasutame **Nvidia Tesla V100 16GB** graafikakaarti
  - Lõime systemd teenuse tehisaru automaatseks käivitamiseks
<br>

## 3. *Frontend* 
Frontend on arendatud Reacti ja CSS-iga ning kasutab Motion UI raamistiku animatsioonide jaoks.

- Reaalajas sõnumivahetus toimub WebSocket ühenduse kaudu.
- Andmete (uudised, ilmateade, päevapakkumised, sessionID) pärimiseks ja AI-juturobotiga suhtlemiseks kasutatakse REST API päringuid.
- Kasutaja unikaalne ID salvestatakse küpsisesse ning _Light_ või _Dark mode_ eelistus talletatakse lokaalsesse mällu.

Lisainfo leiad [_frontend_'i README failist](./frontend/readme.md).
<br>
<br>
<br>

## 4. Java Spring Boot
Rakenduse _backend_'is kasutasime Java raamistikku Spring Boot.
Selles kihis paikneb kogu veebirakenduse äriloogika:
- vestlusruumide ja privaatsõnumite haldus,
- vulgaarsete sõnumite automaatne tuvastamine ja katmine,
- varasemate sõnumite salvestamine H2 andmebaasi ja nende ajastatud kustutamine,
- WebSocket ühenduse konfigureerimine,
- ajastatud uudiste, ilmateate ja päevapakkumiste veebikoorimine,  
- ning vastavate teenuste REST API-de pakkumine.

Lisainfo leiad [backend'i README failist](./backend/readme.md).
<br>
<br>
<br>

## 5. AI-juturobot
### Andmete kogumine
 - Veebikoorisime ÕIS-i [õppeained](https://ois2.ut.ee/#/courses) ja [õppekavad](https://ois2.ut.ee/#/curricula) ning veebilehti [ut.ee](https://ut.ee/et) ja [cs.ut.ee](https://cs.ut.ee/et). 
 - Salvestasime kogutud info NoSQL andmebaasi MongoDB ja eksportisime sealt andmebaasist JSON-faili, mis sisaldas kokku 6150 JSON-dokumenti.  
 - Valisime MongoDB, sest me polnud varem NoSQL-andmebaasidega töötanud ning tahtsime katsetada, kuidas struktureerimata andmeid andmebaasi salvestada.

### Keelemudeli valimine
 - Meie eesmärk oli luua vestlusassistent, kes suudaks eesti keeles vastata ülikoolialastele küsimustele. Seetõttu oli keelemudeli valikul oluline mudeli eesti keele oskus. **Samuti soovisime ise püsti panna oma juturoboti, tagades nii sõltumatuse välistest teenustest kui ka täieliku kontrolli lahenduse üle.**
 - Algselt valisime juturoboti keelemudeliks **tartuNLP Llammas**, kuid hiljem läksime üle **Qwen3-4B** mudelile, kuna see suutis paremini töödelda etteantud andmeid ning omab kontekstiakent, mis on mitu korda suurem kui Llammas mudelil.
 - Lisaks saime Tartu Ülikooli HPC keskusest kasutada virtuaalmasina, millel oli **Nvidia Tesla V100 16GB** graafikakaart. See sobis ideaalselt meie valitud 4-miljardilise parameetriga keelemudelile, mis kasutas ära kogu GPU VRAM-i.
   
### Mudeli tööpõhimõte
Rakenduses kasutasime Pythoni FastAPI teeki, mis pakub REST API teenust veebirakendusele. Eesmärk oli rakendada RAG-i (Retrieval-Augmented Generation) lähenemist:
- Koostasime vektorandmebaasi
  - kasutasime transformerit **paraphrase-multilingual-MiniLM-L12-v2**, mis teisendas meie veebikooritud andmed vektoriteks.
  - Salvestasime need vektorid FAISS-i (Facebook AI Similarity Search) vektorandmebaasi.
- Kasutaja päringu saabumisel teisendasime sisendi samal transformeri mudelil vektoriteks ja otsisime FAISS-ist linguistiliselt lähimaid vastuseid.
- Keelemudelile andsime ette kontekstiks 15 lähimat vastust vektorandmebaasist.
- Lisaks saadab _frontend_ serverisse kasutaja viimased 5 sõnumit ja juturoboti vastust.
- Keelemudel genereerib vastuse, mis põhineb juhistel, leitud kontekstil ning varasematel sõnumitel. Server saadab selle vastuse tagasi kasutajale.

```
prompt = (
            f"""Oled abivalmis tehisintellekti assistent veebilehel UTchat. See veebileht on mõeldud Tartu Ülikooli üliõpilastele. 
            Sinu peamine ülesanne on vastata kasutaja sisendile ainult antud konteksti põhjal. Ole sõbralik ja abivalmis.

                Antud kontekst:
                {context}
                
                Kasutaja varasem sisend ja sinu vastused:
                {truncated_prior_messages}
                
                Kasutaja sisend: {q}
            
                Vastus: """
        )
```

### Kokkuvõte
Oleme tulemusega rahul. Kuigi vastused ei ole kõige täpsemad ega võrdu näiteks OpenAI ChatGPT kvaliteediga, on asjaolu, et saime keelemudelit jooksutada lokaalselt ilma kolmanda osapoole teenuseta ja piiranguteta, meie arvates väga väärtuslik.

![image](https://github.com/user-attachments/assets/94597f65-492e-45f4-a76c-d005afe867b4)
<br>
*Joonis 2. Kuvatõmmis AI-juturoboti aknast.*
<br>
<br>
<br>

## 2025. aasta tudengiprojekti poster
![UTchat poster](https://github.com/user-attachments/assets/da4ca42b-8ccf-4466-a061-ef117844da3b)
*Joonis 3. UTchat poster.*





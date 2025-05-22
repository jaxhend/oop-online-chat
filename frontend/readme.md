# Frontend
Frontend on arendatud Reacti ja CSS-iga ning kasutab Motion UI raamistiku animatsioonide jaoks.

- Kuvab vestlused, AI-roboti liides, ilmateade, uudised ja päevapakkumised.
- Reaalajas sõnumivahetuseks kasutab WebSocket-ühendust.
- Emotikonide valik on lahendatud EmojiPicker komponendiga.
- Andmete pärimiseks (uudised, ilmateade, päevapakkumised) ja AI-juturobotiga suhtlemiseks kasutatakse REST API päringuid.
- Kasutajal on võimalik valida Light või Dark režiim, mille eelistus salvestatakse brauseri lokaalsesse mällu (localStorage).

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

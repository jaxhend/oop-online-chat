# _Frontend_
_Frontend_ on loodud kasutades **Reacti** ja **CSS-i**, animatsioonide jaoks on kasutusel **Motion UI** raamistik.

#### Funktsionaalsused
- Kuvab vestlused, AI-roboti liides, ilmateade, uudised ja päevapakkumised.
- Emotikonide lisamine on lahendatud `EmojiPicker` komponendi abil.
- Reaalajas sõnumivahetus toimub WebSocket ühenduse kaudu.
- Andmete (uudised, ilmateade, päevapakkumised, sessionID) pärimiseks ja AI-juturobotiga suhtlemiseks kasutatakse REST API päringuid.
- Kasutaja unikaalne ID salvestatakse küpsisesse ning _Light_ või _Dark mode_ eelistus talletatakse lokaalsesse mällu.

#### Sessioonihaldus
- Lehe avamisel saadab brauser serverile päringu uue `sessionId` saamiseks või olemasoleva kinnitamiseks.
- Server salvestab või leiab olemasoleva sessiooni ja saadab `sessionId` brauserile küpsisena.
- Kui sessioon on juba seotud kasutajanimega, taastatakse kasutajanimi automaatselt.
- Kasutajanime hoitakse ainult serveri poolel.

####  Reaalajas suhtlus WebSocketi kaudu
- WebSocket-ühendus luuakse automaatselt pärast `sessionId` saamist.
- Kasutaja sisestatud sõnum või käsk edastatakse otse serverile läbi WebSocketi.
- Server töötleb sõnumi ja saadab kasutajale vastuse, mis kuvatakse kasutajaliideses.
- Kui uus sõnum saabub ajal, mil brauseri sakk ei ole aktiivne, kuvatakse vahelehe pealkirjas visuaalne märguanne.

#### AI juturobot
- Kasutaja küsimus saadetakse POST-päringuna koos kuni viie varasema küsimus-vastus paariga.
- Vastuse ootel kuvatakse kasutajale „AI mõtleb…” animatsioon.
- Kui vastus saabub, lisatakse nii kasutaja sisend kui ka roboti vastus vestlusajalukku ning kuvatakse ekraanil.

#### Andmete kuvamine
- Rakenduse käivitamisel laaditakse automaatselt ilmateade, uudised ja päevapakkumised.
- Vajalikud andmed saadakse REST API päringute kaudu.

#### Kasutajaliides
- Kasutaja saab valida Light/Dark mode'i vahel, mis salvestatakse küpsisesse ja taastatakse järgmisel külastusel.
- Kasutaja saab valida Light või Dark režiimi, mis salvestatakse lokaalsesse mällu ja taastatakse järgmisel külastusel.
- Leht on jaotatud neljaks osaks:
  - Vestlusala
  - Uudistepaneel
  - Infopaneel (ilm + päevapakkumised)
  - AI-juturobot
- Sõnumiväli sisaldab emotikonide valikut.
- **Kujundus on skaleeruv ja toetab ka mobiilivaadet.**

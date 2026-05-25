# Světice → Praha - widget pro Android

Widget 2×2 na ploše Androidu se 3 nejbližšími odjezdy ze Světice směrem Praha. Aktualizuje se každých 30 sekund, ale jen v dnech a časech, které si nastavíš (šetří baterii).

## Co umí

- 2×2 widget na plochu (zabírá místo 2×2 ikony)
- Zelený čas = vlak jede včas
- Přeškrtnutý čas + oranžový nový čas + `+X` = zpoždění v minutách
- Auto-aktualizace každých 30 s v aktivním okně
- Mimo okno se aktualizace zastaví (šedá tečka v rohu) - šetří data i baterii
- Konfigurace dnů (Po–Ne) a časového rozsahu

## Jak z toho udělat APK (bez instalace čehokoliv)

Použijeme GitHub Actions - kompilace proběhne v cloudu zdarma.

### Krok 1 — Založ účet na GitHubu
Pokud ho ještě nemáš: jdi na [github.com](https://github.com) a klikni "Sign up". Stačí free účet.

### Krok 2 — Nahraj projekt na GitHub
1. Klikni vpravo nahoře na "+" → **"New repository"**
2. Pojmenuj ho třeba `svetice-widget`
3. Nech ho **Public** (Private taky funguje, ale Public má neomezený build)
4. **Nezatrhuj** "Add a README" - my máme vlastní
5. Klikni **"Create repository"**

Teď ti GitHub ukáže instrukce. Vyber **"uploading an existing file"** (modře, druhý odkaz dole).

6. Rozbal stažený ZIP s tímto projektem
7. Přetáhni **všechny soubory a složky** (i skrytou `.github`) do okna v prohlížeči
8. Dole klikni **"Commit changes"**

### Krok 3 — Spusť build
1. V repu nahoře klikni na záložku **"Actions"**
2. Vlevo uvidíš workflow **"Build APK"**, klikni na něj
3. Vpravo klikni **"Run workflow"** → **"Run workflow"** (zelené tlačítko)
4. Počkej ~5 minut. Když svítí zelená fajfka, je hotovo.

### Krok 4 — Stáhni APK
1. Klikni na dokončený běh
2. Dole sekce **"Artifacts"** → klikni **"SveticeWidget-debug"**
3. Stáhne se ti ZIP. Rozbal ho. Uvnitř je `app-debug.apk`

### Krok 5 — Nainstaluj na Android
1. Pošli si APK do telefonu (e-mailem, Google Drive, USB - jak chceš)
2. V telefonu na soubor klepni → Android se zeptá na povolení instalovat z neznámých zdrojů → povol pro tu appku (typicky pro Soubory nebo prohlížeč)
3. Po instalaci appku otevři, nastav dny a časy
4. Dlouhý stisk na ploše → **Widgety** → najdi **"Světice → Praha"** → přetáhni na plochu

## Napojení na živá data

V základu widget ukazuje **demo data** (časy `06:12 / 07:42 / 08:54`). Pro skutečné odjezdy potřebuješ API klíč Golemio (zdarma).

1. Jdi na [api.golemio.cz](https://api.golemio.cz) → **"Sign Up"** (e-mail stačí)
2. V dashboardu vygeneruj **Access Token**
3. V repu otevři soubor `app/src/main/java/cz/svetice/widget/TrainRepository.kt`
4. Najdi řádek:
   ```kotlin
   private const val GOLEMIO_API_KEY = "PASTE_YOUR_GOLEMIO_TOKEN_HERE"
   ```
5. Nahraď text v uvozovkách svým tokenem
6. Klikni "Commit changes" - GitHub Actions automaticky postaví novou APK
7. Stáhni a přeinstaluj

> Golemio poskytuje data PID (Pražské integrované dopravy) včetně linky **S9**, na které Světice leží. Free tier má **1000 dotazů denně**, což pro 30s interval během 3hodinového ranního okna stačí (~360 dotazů/den).

## Časté problémy

**Widget není v nabídce widgetů**
Otevři aplikaci (klikni na ikonu vlaku) - tím se zaregistruje. Pak zkus znovu.

**Widget se neaktualizuje každých 30 s**
Android někdy "uspí" aplikace kvůli šetření baterie. V nastavení telefonu najdi appku Světice → Praha → "Baterie" → nastav **"Bez omezení"**.

**Zpoždění se neaktualizuje, jen čas**
Buď nemáš nastavený Golemio token (ukazují se demo data), nebo nejsi v aktivním okně (šedá tečka).

**Chci širší okno**
V appce otevři nastavení a uprav časy Od–Do.

## Vlastní úpravy

- **Jiná zastávka** než Světice: v `TrainRepository.kt` změň `STATION = "Světice"` na jiný název přesně jak ho má Golemio (např. `"Praha hl.n."`)
- **Jiný cílový směr**: změň filtr `headsign.contains("Praha", ...)` na jiné město
- **Více než 3 vlaky**: v `widget_train.xml` přidej další řádek a uprav `TrainWidgetProvider.kt`

Pokud něco nepůjde, pošli mi screenshot a vyřešíme to.

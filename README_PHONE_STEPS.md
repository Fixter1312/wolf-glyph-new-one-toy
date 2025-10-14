# Wilczek Glyph Toy (Nothing Phone 3)

**Cel:** Zbuduj APK z telefonu (przez GitHub) i dodaj wilczka do biblioteki Glyph Toys.

---

## 0) Co potrzebujesz
- Konto **GitHub** (załóż na https://github.com w przeglądarce w telefonie).
- Plik **Glyph Matrix SDK .aar** (pobierz z dokumentacji Nothing).

> Nazwij plik dokładnie: `glyph-matrix-sdk-1.0.aar`

---

## 1) Utwórz repozytorium i wgraj projekt (z telefonu)
1. Wejdź na GitHub → **Sign in**.
2. Prawy górny róg: **+** → **New repository** → nazwij `wilczek-glyph-toy` → **Create repository**.
3. Na stronie repo kliknij **Add file** → **Upload files**.
4. Wyślij **całą zawartość tego ZIP-a** do repo (foldery i pliki).
   - Na telefonie, jeśli nie możesz przeciągać, kliknij **choose your files** i zaznacz wszystkie pliki z ZIP-a (albo wrzuć ZIP i potem w GitHub kliknij „Add file” per folder).
5. **BARDZO WAŻNE:** wejdź do folderu `app/libs` i kliknij **Add file → Upload files** → wybierz **`glyph-matrix-sdk-1.0.aar`** (plik z SDK Nothing).
6. Na dole strony kliknij **Commit changes**.

---

## 2) Uruchom build APK w GitHub Actions
1. W repo przejdź do zakładki **Actions** (góra ekranu).
2. Po lewej wybierz workflow **“Build APK (Wilczek Glyph Toy)”**.
3. Kliknij **Run workflow** (zielony przycisk). Potwierdź.
4. Poczekaj, aż job się zakończy (1–3 min).

> Jeśli build padnie z komunikatem „Brak app/libs/glyph-matrix-sdk-1.0.aar”, to znaczy, że nie dodałeś pliku `.aar`. Wróć do kroku 1.5 i doślij go do `app/libs`.

---

## 3) Pobierz APK na telefon
1. W **Actions** wybierz zakończony run.
2. Na dole na liście **Artifacts** kliknij **app-debug**.
3. Pobierz plik `app-debug.apk` na telefon.

---

## 4) Zainstaluj APK
1. Otwórz pobrany `app-debug.apk` na telefonie i potwierdź instalację aplikacji z nieznanych źródeł (jeśli potrzeba).
2. Po instalacji aplikacja zarejestruje **Glyph Toy**.

---

## 5) Włącz wilczka w ustawieniach Nothing
1. Telefon → **Ustawienia** → **Glyph Interface** → **Glyph Toys**.
2. Znajdź „**Wilczek Glyph Toy**” i **przeciągnij do aktywnych**.
3. Krótki klik tylnego przycisku przełącza zabawki; **długie przytrzymanie** wysyła event „change”.

---

## 6) Dostosowania (opcjonalnie)
- Zmiana zachowań (karmienie, czyszczenie) – edytuj `WolfToyService.kt` (metody `feed()`, `play()`, `clean()`).
- Sprite 25×25 – edytuj `WolfSprites.kt` (tablice `OPEN`/`BLINK`).

Powodzenia! 🐺✨
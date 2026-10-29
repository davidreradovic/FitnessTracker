# Fitness Tracker 🏃‍♂️🚴‍♀️🚶‍♂️

[🇷🇸 Srpski](#-fitness-tracker---android-aplikacija) | [🇬🇧 English](#-fitness-tracker---android-application)

---

## 🇷🇸 Fitness Tracker - Android Aplikacija

Nativna Android aplikacija za praćenje fizičkih aktivnosti u realnom vremenu koristeći GPS, interaktivne OpenStreetMap mape, lokalnu bazu podataka (Room) i grafičke prikaze statistike.

---

### 🌟 Ključne Funkcionalnosti

- **Praćenje Aktivnosti u Realnom Vremenu:**
  - Meri udaljenost, trajanje, trenutnu i prosječnu brzinu.
  - Podržava više tipova aktivnosti: **Trčanje**, **Hodanje** i **Biciklizam**.
  - Radi kao **Foreground Service** sa trajnom notifikacijom, što omogućava neometano praćenje lokacije i u pozadini.

- **Interaktivne Mape:**
  - Prikaz putanje u realnom vremenu uz **OSMDroid** (OpenStreetMap).
  - Prikaz trenutne lokacije korisnika (`MyLocationNewOverlay`) i iscrtavanje ruta za prošle aktivnosti.
  - Usaglašeno sa OpenStreetMap ODbL licencom (navedena autorska prava i linkovi).

- **Istorija i Detalji Treninga:**
  - Svi treninzi se lokalno čuvaju u **Room bazi podataka**.
  - Sortiranje treninga po datumu, trajanju, udaljenosti ili prosječnoj brzini.
  - Filtriranje po tipu aktivnosti.
  - Pregled detaljne karte rute i opisa za svaki zabilježeni trening.

- **Statistika i Grafici:**
  - Vizuelni prikaz sedmičnog napretka pomoću **MPAndroidChart** biblioteke.
  - Praćenje ukupne udaljenosti, vremena i brzine.

- **Podešavanja i Prilagođavanje:**
  - **Jedinice mjere:** Izbor između metričkog (KM, KM/H) i imperijalnog sistema (Milje, MPH).
  - **Višejezičnost:** Puna podrška za srpski i engleski jezik.
  - **Dnevni ciljevi i podsjetnici:** Postavljanje ciljeva za udaljenost i vrijeme, uz notifikacije za ostvarene ciljeve i dnevne podsjetnike.

---

### 🛠️ Tehnologije i Arhitektura

- **Jezik:** Java
- **Arhitektura:** Android Jetpack arhitekturne komponente (ViewModel, LiveData, Navigation Component, Foreground Services)
- **Lokalna Baza:** Room Database sa TypeConverter-ima (Gson serijalizacija)
- **Lokacija:** Google Play Services Location (`FusedLocationProviderClient`)
- **Mape:** OSMDroid (OpenStreetMap)
- **Grafici:** MPAndroidChart
- **UI & Layouts:** Material Design 3, ConstraintLayout, PreferenceFragmentCompat
- **Učitavanje Slika:** Glide

---

### 🚀 Pokretanje Projekta

1. **Klonirajte repozitorijum:**
   ```bash
   git clone https://github.com/TVOJE_KORISNICKO_IME/FitnessTracker-Android.git
   ```
2. **Otvorite u Android Studio-u:**
   - Izaberite **Open an Existing Project** i izaberite `FitnessTracker` fasciklu.
   - Sacekajte da se završi Gradle sinhronizacija.

3. **Pokrenite aplikaciju:**
   - Povežite Android uređaj ili pokrenite emulator i kliknite na **Run 'app'** (`Shift + F10`).

---
---

## 🇬🇧 Fitness Tracker - Android Application

Native Android application for tracking physical activities in real-time using GPS, interactive OpenStreetMap maps, local Room database persistence, and statistics charts.

---

### 🌟 Key Features

- **Real-Time GPS Activity Tracking:**
  - Tracks distance, duration, current speed, and average speed.
  - Supports multiple activity types: **Running**, **Walking**, and **Cycling**.
  - Runs as a **Foreground Service** with persistent notifications, ensuring accurate tracking even when the app is in the background.

- **Interactive Map Integration:**
  - Real-time path rendering using **OSMDroid** (OpenStreetMap).
  - Current location overlay (`MyLocationNewOverlay`) and custom polyline rendering for route history.
  - OpenStreetMap ODbL compliance with proper attribution and license links.

- **Workout History & Details:**
  - Saves all recorded workouts locally in a **Room Database**.
  - Sort workouts by Date, Duration, Distance, or Average Speed.
  - Filter workouts by activity type.
  - View detailed route maps and descriptions for past activities.

- **Statistics & Charts:**
  - Visualized weekly progress using **MPAndroidChart**.
  - Track total distance, duration, and speed across different activity types.

- **Customization & Settings:**
  - **Unit Switching:** Toggle between Metric (KM, KM/H) and Imperial (Miles, MPH).
  - **Multi-Language Support:** Full support for Serbian (Srpski) and English languages.
  - **Daily Goals & Reminders:** Set distance/duration goals with congratulatory notifications upon completion and daily activity reminders.

---

### 🛠️ Tech Stack & Architecture

- **Language:** Java
- **Architecture:** Android Jetpack Architecture Components (ViewModel, LiveData, Navigation Component, Foreground Services)
- **Local Database:** Room Database with TypeConverters (Gson serialization)
- **Location Services:** Google Play Services Location (`FusedLocationProviderClient`)
- **Maps:** OSMDroid (OpenStreetMap)
- **Charts:** MPAndroidChart
- **UI & Layouts:** Material Design 3, ConstraintLayout, PreferenceFragmentCompat
- **Image Loading:** Glide

---

### 📄 License & Attribution

- **Map Data:** Map data provided by [© OpenStreetMap contributors](https://www.openstreetmap.org/copyright) under the Open Database License (ODbL).

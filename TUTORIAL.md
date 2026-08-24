# Tutorial: Build Afwan Fake GPS via GitHub Actions (versi anti-gagal)

Belajar dari percobaan sebelumnya: drag-and-drop file kadang bikin folder
`.github` hilang atau struktur berantakan. Di tutorial ini kita pakai cara
yang lebih terjamin: bikin repo baru, lalu isi file SATU PER SATU langsung
lewat editor web GitHub untuk file-file penting, supaya tidak ada yang
"nyasar" posisinya.

---

## Bagian 0 — Sebelum mulai

Matikan translate Chrome untuk github.com, supaya nama file/folder tidak
ikut diterjemahkan (ini penyebab error kemarin — "settings" jadi
"pengaturan", "workflows" jadi "alur kerja", dst).
Klik ikon translate di address bar → **"Tampilkan halaman asli"**.

---

## Bagian 1 — Buat repo baru

1. Ke https://github.com → klik **"New"**
2. Nama repo: `afwan-fake-gps` (atau bebas, tanpa spasi)
3. Public, jangan centang apapun (biar kosong)
4. **Create repository**

---

## Bagian 2 — Buat file satu per satu (metode anti-gagal)

Di halaman repo kosong, klik **"creating a new file"** (link biru di tengah
halaman). Untuk tiap file di bawah ini:
- Ketik nama file di kolom nama (termasuk tanda `/` untuk folder — GitHub
  otomatis bikin foldernya)
- Paste isi file ke kotak besar di bawahnya
- Klik **"Commit changes..."** → **"Commit changes"**
- Balik ke halaman utama repo (klik nama repo di atas) → ulangi untuk file
  berikutnya

### File 1 — Workflow (PALING PENTING, jangan sampai salah path)
Nama file (ketik persis ini di kolom nama):
```
.github/workflows/build.yml
```
Isi:
```yaml
name: Build APK

on:
  push:
    branches: [ "main" ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout kode
        uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3
        with:
          gradle-version: '8.4'

      - name: Build Debug APK
        run: gradle assembleDebug --no-daemon

      - name: Upload APK sebagai Artifact
        uses: actions/upload-artifact@v4
        with:
          name: afwan-fake-gps-debug-apk
          path: app/build/outputs/apk/debug/app-debug.apk
```

### File 2 — build.gradle (root)
Nama file: `build.gradle`
(isi ada di file `build.gradle` yang saya kirim terpisah)

### File 3 — settings.gradle
Nama file: `settings.gradle`

### File 4 — gradle.properties
Nama file: `gradle.properties`

### File 5 — gradle wrapper
Nama file: `gradle/wrapper/gradle-wrapper.properties`

### File 6 — app/build.gradle
Nama file: `app/build.gradle`

### File 7 — AndroidManifest
Nama file: `app/src/main/AndroidManifest.xml`

### File 8 — MainActivity
Nama file: `app/src/main/java/com/example/mocklocation/MainActivity.kt`

### File 9 — MockLocationService (baru, penting untuk background)
Nama file: `app/src/main/java/com/example/mocklocation/MockLocationService.kt`

### File 10 — Layout
Nama file: `app/src/main/res/layout/activity_main.xml`

### File 11 — strings.xml
Nama file: `app/src/main/res/values/strings.xml`

### File 12 — themes.xml
Nama file: `app/src/main/res/values/themes.xml`

> Semua isi file di atas (kecuali build.yml yang sudah lengkap di sini)
> ada di masing-masing file terpisah yang saya kirimkan. Tinggal buka,
> copy semua isinya, paste ke kolom konten GitHub.

---

## Bagian 3 — Cek struktur sudah benar

Setelah semua 12 file selesai di-commit, buka halaman utama repo. Harus
terlihat PERSIS begini (klik tiap folder untuk expand & cek):

```
afwan-fake-gps/
├── .github/
│   └── workflows/
│       └── build.yml
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/mocklocation/
│       │   ├── MainActivity.kt
│       │   └── MockLocationService.kt
│       └── res/
│           ├── layout/activity_main.xml
│           └── values/
│               ├── strings.xml
│               └── themes.xml
├── gradle/wrapper/gradle-wrapper.properties
├── build.gradle
├── settings.gradle
└── gradle.properties
```

---

## Bagian 4 — Build otomatis & download APK

1. Klik tab **"Actions"**
2. Kalau `.github/workflows/build.yml` sudah benar posisinya, run otomatis
   muncul dan mulai jalan (kuning berputar)
3. Tunggu sampai ✅ hijau (2-5 menit)
4. Klik run itu → scroll ke **Artifacts** → download
   `afwan-fake-gps-debug-apk`
5. Extract zip-nya → dapat `app-debug.apk`

---

## Bagian 5 — Install & aktifkan di HP

1. Pindahkan `app-debug.apk` ke HP → install (izinkan "sumber tidak dikenal")
2. **Settings → About phone** → tap "Build number" 7x → jadi developer
3. **Settings → Developer Options → Select mock location app** → pilih
   **"Afwan Fake GPS"**
4. **Settings → Apps → Afwan Fake GPS → Battery** → set **"No restrictions"**
   (penting di HP Xiaomi/Redmi supaya service background tidak dimatikan MIUI)
5. Cari juga opsi **Autostart** untuk app ini → aktifkan

---

## Bagian 6 — Cara pakai

1. Buka app, izinkan permission lokasi & notifikasi
2. Isi Latitude & Longitude
3. Tap **"Set Mock Location"** → muncul notifikasi persisten di status bar,
   tandanya service jalan di background
4. Buka app GPS lain untuk testing — lokasi akan terbaca sesuai koordinat
   yang kamu masukkan, dan **tetap bertahan** meski app ini diminimize,
   karena berjalan sebagai foreground service
5. Untuk berhenti: tap tombol **"Stop"** di notifikasi, atau buka app lagi
   dan tap **"Stop Mock Location"**

---

## Troubleshooting

| Masalah | Solusi |
|---|---|
| Build gagal | Cek Bagian 3, pastikan struktur folder persis sama |
| Actions tidak muncul sama sekali | File `build.yml` salah path — harus PERSIS `.github/workflows/build.yml` |
| Lokasi lompat balik setelah beberapa detik | Pastikan battery optimization sudah off (Bagian 5 langkah 4) |
| Notifikasi tidak muncul | Pastikan izin notifikasi diberikan saat app dibuka pertama kali |

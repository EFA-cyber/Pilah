# Rencana Pengembangan — PILAH

> Dokumen ini adalah rencana teknis untuk membangun MVP **PILAH** sesuai `docs/PRD.md`. Berfungsi sebagai roadmap fase-per-fase, keputusan arsitektur, dan daftar pekerjaan konkret.

## 1. Ringkasan Pendekatan

PILAH dibangun sebagai aplikasi Android native (Kotlin + Jetpack Compose), dengan prinsip **on-device first**: pemindaian, hashing, dan klasifikasi dasar berjalan sepenuhnya offline berbasis aturan (rule engine). Mode **Analisis Mendalam** (Cloud AI via Claude API) bersifat opt-in dan hanya dipakai untuk file yang skornya ambigu.

Pengembangan dipecah menjadi 8 fase (Fase 0–7), berurutan tapi dengan beberapa fase yang bisa berjalan paralel pada tim 2 orang (UI vs engine).

## 2. Keputusan Tech Stack

| Area | Pilihan | Alasan |
|---|---|---|
| Bahasa | Kotlin | Standar Android modern |
| UI | Jetpack Compose + Material 3 | Sesuai PRD §7.1, dev cepat |
| Arsitektur | MVVM + Clean Architecture (data/domain/presentation) | Testable, modular |
| DI | Hilt | Standar de-facto Android |
| Database lokal | Room (SQLite) | Sesuai ERD PRD §6 |
| Async | Kotlin Coroutines + Flow | Standar Compose |
| Background job | WorkManager | Scan besar, hashing, auto-purge karantina (30 hari) |
| Akses file | MediaStore API + Storage Access Framework + `MANAGE_EXTERNAL_STORAGE` (Android 11+) | Akses penuh internal & SD card untuk file manager |
| Deteksi duplikat | SHA-256 per file (batch, background) | Exact duplicate sesuai PRD |
| Deteksi foto buram | Varians Laplacian ringan (tanpa model ML besar) | Cocok RAM 3–4GB |
| Cloud AI | Claude API — Haiku (massal) + Sonnet (ambigu) | Sesuai PRD §3.2, §5 |
| Ekstraksi teks dokumen | PdfBox-Android (PDF), parser DOCX ringan | Hanya aktif saat Analisis Mendalam |
| Networking | Retrofit + OkHttp + kotlinx.serialization | Cloud AI client |
| Local prefs | DataStore (Preferences) | Mode privasi, bobot skoring |
| Charts dashboard | Vico (Compose-native) | Visualisasi storage |
| Testing | JUnit, Turbine, Robolectric, Compose UI Test | Unit + integration + UI |

## 3. Struktur Modul Proyek

```
pilah/
├── app/                     # Entry point, navigasi, Application class, Hilt graph
├── core/
│   ├── database/            # Room: entities, DAO, migrations, TypeConverters
│   ├── designsystem/        # Tema Compose, typography (Geist Mono/JetBrains Mono/serif)
│   ├── common/               # Util, ekstensi, dispatcher provider
│   ├── permissions/          # Helper izin storage
│   └── model/                # Domain model lintas fitur
├── feature/
│   ├── onboarding/           # Izin & pemilihan mode privasi
│   ├── scan/                  # Smart Scan: file scanner + hashing worker
│   ├── classification/        # Rule engine + Cloud AI client
│   ├── review/                # Tumpukan Penting/Layak Dihapus + koreksi geser
│   ├── foldering/              # Sebelum→Sesudah + eksekusi pindah file
│   ├── quarantine/             # Karantina, pemulihan, auto-purge 30 hari
│   └── dashboard/              # Visualisasi storage, riwayat, total hemat
└── docs/                       # PRD, rencana, ADR
```

Mulai sebagai modul ringan (`app` + `core/*`), modul `feature/*` ditambahkan bertahap per fase agar tidak over-engineer di awal.

## 4. Skema Database (Room)

Pemetaan langsung dari ERD PRD §6:

| Entity | Kolom kunci | Catatan implementasi |
|---|---|---|
| `FileEntity` (`files`) | id, path, name, type, size_bytes, hash, last_opened, created_at | `hash` diisi async oleh worker; index pada `hash` & `path` |
| `ClassificationEntity` (`classifications`) | id, file_id (FK), importance_score, category, reason, source, classified_at | `source` ∈ {local_rule, cloud_haiku, cloud_sonnet}; histori tetap disimpan, ambil terbaru per file |
| `ActionEntity` (`actions`) | id, file_id (FK), action_type, from_path, to_path, executed_at | `action_type` ∈ {MOVE, QUARANTINE, RESTORE, PURGE}; basis fitur undo |
| `QuarantineEntity` (`quarantine`) | id, file_id (FK), quarantined_at, purge_after, status | `status` ∈ {ACTIVE, RESTORED, PURGED}; di-cek oleh WorkManager harian |
| `UserCorrectionEntity` (`user_corrections`) | id, file_id (FK), ai_category, user_category, corrected_at | Sinyal penyesuaian bobot rule engine |

Relasi via `@Relation`/`@Embedded` Room (mis. `FileWithLatestClassification`).

## 5. Roadmap Fase

| Fase | Fokus | Estimasi | Status |
|---|---|---|---|
| 0 | Setup Proyek & Fondasi | ~1 minggu | ✅ Selesai (scaffold) |
| 1 | Smart Scan | ~2 minggu | ✅ Selesai (implementasi awal) |
| 2 | Klasifikasi Lokal (Rule Engine) | ~2 minggu | |
| 3 | Tinjau & Koreksi (Review UI) | ~2 minggu | |
| 4 | Auto-Foldering & Karantina | ~2 minggu | |
| 5 | Dashboard Penyimpanan | ~1 minggu | |
| 6 | Analisis Mendalam (Cloud AI) — opsional | ~2–3 minggu | |
| 7 | Testing, Performa, Rilis | ~2 minggu | |

**Total MVP penuh:** ~14–16 minggu. Jika Fase 6 ditunda ke v1.1, MVP inti (Fase 0–5 + 7) ≈ 10–12 minggu.

### Fase 0 — Setup Proyek & Fondasi ✅
- Proyek Gradle multi-modul (version catalog `gradle/libs.versions.toml`): `app`, `core:model`, `core:common`, `core:database`, `core:designsystem`.
- Room DB sesuai §4 — 5 entity (`FileEntity`, `ClassificationEntity`, `ActionEntity`, `QuarantineEntity`, `UserCorrectionEntity`) + DAO + `Converters` (Instant) + mapper entity↔domain model, versi awal (v1).
- Tema Compose & typography sesuai PRD §7.3: `PilahFonts.sans` (Geist Mono), `.serif`, `.mono` (JetBrains Mono) — font di-bundle sebagai resource TTF di `core/designsystem/src/main/res/font/`, dipetakan ke `PilahTypography` (Material3) + `PilahTypographyExtra.mono`.
- Compose Navigation (`PilahNavHost`) dengan 5 rute placeholder sesuai alur PRD §4 (Onboarding → Pindai → Tinjau Hasil → Dashboard → Karantina) + Hilt DI graph (`PilahApplication`, `MainActivity`, `DatabaseModule`, `CommonModule`).
- CI dasar (`.github/workflows/android-ci.yml`): lint, unit test, assemble debug via GitHub Actions.

> **Catatan:** build belum diverifikasi di sandbox ini karena tidak ada Android SDK terpasang dan repository Maven Google (`dl.google.com`, sumber Android Gradle Plugin/AndroidX) diblokir oleh kebijakan jaringan. Verifikasi `./gradlew assembleDebug` perlu dijalankan di Android Studio / CI (sudah disiapkan di GitHub Actions).

### Fase 1 — Smart Scan ✅
- Layar onboarding: edukasi kebutuhan akses penuh storage + request `MANAGE_EXTERNAL_STORAGE`.
- File scanner via foreground service / WorkManager expedited job: traverse internal storage + SD card (MediaStore + File API).
- Kumpulkan metadata: path, name, type, size, created_at, last_opened, source folder (deteksi pola path WhatsApp/Download/DCIM).
- Hitung hash SHA-256 per file secara batch (throttled agar hemat baterai).
- Simpan ke `files`. UI progres real-time (jumlah file, estimasi waktu, tombol batal).

**Implementasi:**
- `core/permissions`: `StoragePermissions` — deteksi `MANAGE_EXTERNAL_STORAGE` (Android 11+) vs izin runtime legacy, plus intent ke halaman Pengaturan.
- `app/.../ui/OnboardingScreen.kt`: alur permintaan izin nyata (redirect Settings utk API 30+, dialog runtime utk API <30), re-cek status saat `ON_RESUME`.
- `feature/scan`:
  - `FileSystemScanner` — telusuri seluruh volume penyimpanan (internal + SD card via `getExternalFilesDirs`), kumpulkan `FileItem` (path/name/type/size/createdAt), lewati berkas/folder tersembunyi.
  - `FileHasher` — SHA-256 streaming per file untuk deteksi duplikat (Fase 2).
  - `ScanRepository`/`DefaultScanRepository` — orkestrasi fase `SCANNING` → `HASHING` → `DONE`, upsert ke `FileDao`, emit `ScanProgress` real-time.
  - `ScanWorker` (`@HiltWorker`, `CoroutineWorker`) — jalankan scan di background, `setProgress` per langkah.
  - `ScanViewModel` — enqueue `OneTimeWorkRequest` unik, observe `WorkInfo` via `getWorkInfosForUniqueWorkFlow`.
  - `ui/ScanScreen.kt` — progres real-time (spinner saat scanning, progress bar saat hashing, tombol Batal/Lanjut).
- `app/.../PilahApplication.kt` mengimplementasikan `Configuration.Provider` (HiltWorkerFactory) untuk WorkManager + Hilt.

> **Catatan:** `last_opened` tetap `null` (lihat keterbatasan §6); deteksi pola folder sumber (WhatsApp/Download/DCIM) & throttling hashing berbasis baterai akan disempurnakan bersamaan rule engine di Fase 2. Build tetap belum diverifikasi di sandbox ini (lihat catatan Fase 0) — perlu dijalankan di Android Studio/CI.

### Fase 2 — Klasifikasi Lokal (Rule Engine)
Skor kepentingan 0–100 dihitung dari sinyal berbobot:

| Sinyal | Efek skor | Alasan ditampilkan |
|---|---|---|
| Duplikat persis (hash sama, bukan file pertama) | turun tajam | "Duplikat dari `<nama file asli>`" |
| Screenshot lama (folder Screenshots, usia > N hari, tak dibuka lagi) | turun | "Screenshot lama, tidak dibuka sejak X" |
| APK yang sudah terinstal (cek `PackageManager`) | turun | "APK sudah terinstal" |
| File unduhan tak pernah dibuka (usia > N hari) | turun | "File unduhan, belum pernah dibuka" |
| Foto buram (varians Laplacian rendah) | turun | "Foto terdeteksi buram" |
| Nama dokumen penting (regex: ijazah, ktp, npwp, invoice, kontrak, skripsi, dll.) | naik | "Nama file menunjukkan dokumen penting" |
| Foto/video DCIM tanpa sinyal negatif | netral-tinggi | "Foto Kenangan" |

- Threshold: ≥70 → **Penting**, ≤30 → **Layak Dihapus**, 31–69 → **Ambigu** (kandidat Fase 6 / review manual).
- Hasil disimpan ke `classifications` dengan `source = local_rule`.
- Bobot sinyal disimpan di DataStore agar dapat disesuaikan dari koreksi pengguna (Fase 3).

### Fase 3 — Tinjau & Koreksi (Review UI)
- Layar dua tumpukan (Penting / Layak Dihapus) — card berisi thumbnail, nama, ukuran, alasan, usulan folder tujuan.
- Gestur swipe untuk pindah kategori → tulis ke `user_corrections` (ai_category vs user_category).
- Penyesuaian bobot rule engine secara heuristik berdasarkan akumulasi koreksi (bukan training model di MVP).
- Bottom sheet detail file: preview, metadata lengkap, riwayat klasifikasi.

### Fase 4 — Auto-Foldering & Karantina
- Usulan struktur folder: `Dokumen Penting/`, `Foto Kenangan/`, `Kerja & Bisnis/`, `Arsip/`, `Karantina/` (root dapat dikustomisasi).
- Layar "Sebelum → Sesudah": pohon direktori awal vs hasil, ringkasan jumlah & ukuran per kategori.
- Tombol "Rapikan Sekarang": eksekusi pindah file (SAF/File API), setiap operasi dicatat ke `actions`.
- File "Layak Dihapus" → `Karantina/` + entry `quarantine` (`purge_after = now + 30 hari`, `status = ACTIVE`).
- Undo: balik `from_path`/`to_path` dari `actions` terakhir.
- WorkManager periodic (harian): purge entry `quarantine` yang `purge_after < now` & `status = ACTIVE`.
- Layar Karantina: daftar file + sisa hari, tombol "Pulihkan" / "Hapus Sekarang".

### Fase 5 — Dashboard Penyimpanan
- Pie/bar chart penggunaan storage per kategori (Vico).
- Riwayat sesi "Rapikan Sekarang" (tanggal, ruang dihemat).
- Counter total ruang dihemat sejak instalasi (agregat dari `actions`/`quarantine`).
- Halaman Pengaturan: toggle mode privasi, kelola folder kustom, lihat log `actions`.

### Fase 6 — Analisis Mendalam (Cloud AI, opsional)
- Layar consent eksplisit (onboarding + settings) menjelaskan data yang dikirim ke cloud.
- Hanya file kategori **Ambigu** (skor 31–69) atau dokumen `.pdf`/`.docx`/`.txt` yang diproses.
- Ekstraksi cuplikan teks (N karakter pertama) via PdfBox-Android / parser DOCX ringan — hanya saat mode aktif.
- Batch request ke **Claude Haiku** (klasifikasi massal); eskalasi kasus yang masih ambigu ke **Claude Sonnet**.
- Kontrak JSON: `{ file_id, importance_score, category, reason }`.
- Simpan ke `classifications` dengan `source = cloud_haiku`/`cloud_sonnet` (riwayat lokal tetap ada untuk audit).
- API key disimpan via Android Keystore (EncryptedSharedPreferences). Model bisnis (BYO key vs proxy backend) → lihat §7 (Pertanyaan Terbuka).

### Fase 7 — Testing, Performa, Rilis
- Unit test: rule engine (skoring), repository, mapper.
- Instrumented test: migrasi Room, file scanner pada storage simulasi.
- UI test (Compose): alur onboarding → scan → review → eksekusi.
- Profiling pada perangkat RAM 3–4GB dengan >10.000 file (paging daftar, baseline profile).
- Audit lokalisasi Bahasa Indonesia & audit privasi (pastikan tidak ada network call saat mode On-Device).
- Persiapan rilis: signing config, draf listing Play Store.

## 6. Izin & Privasi

- Android 11+ memerlukan `MANAGE_EXTERNAL_STORAGE` untuk akses penuh file manager — perlu deklarasi & justifikasi khusus di Play Console.
- Layar onboarding wajib menjelaskan secara eksplisit kenapa izin dibutuhkan, dalam Bahasa Indonesia.
- "Terakhir dibuka" (`last_opened`) tidak selalu tersedia native — fallback ke `UsageStatsManager` (butuh izin `PACKAGE_USAGE_STATS`) atau tanggal akses file system jika tersedia; didokumentasikan sebagai keterbatasan.
- Tidak ada konten file meninggalkan perangkat kecuali mode Analisis Mendalam aktif (sesuai PRD §7.2).

## 7. Risiko & Pertanyaan Terbuka

- **Izin storage lintas versi Android**: scoped storage & `MANAGE_EXTERNAL_STORAGE` butuh justifikasi kategori "file manager" di Play Store.
- **Performa scan skala besar**: HP low-end dengan >50.000 file — perlu strategi chunking + pause/resume.
- **Model bisnis Cloud AI**: siapa menanggung biaya API Claude — BYO key pengguna, kuota freemium, atau in-app purchase? Perlu keputusan sebelum Fase 6.
- **Akurasi deteksi blur** tanpa model ML berat — perlu validasi dengan dataset foto nyata.
- **Ukuran APK**: library ekstraksi PDF/DOCX menambah ukuran — pertimbangkan Play Feature Delivery (modul on-demand) untuk Fase 6.

## 8. Langkah Selanjutnya

1. Validasi keputusan tech stack & estimasi di atas dengan tim.
2. Mulai **Fase 0**: scaffold proyek Android (Gradle, modul `core/*`, Room schema, design system & typography).
3. Putuskan model bisnis Cloud AI sebelum memulai Fase 6.

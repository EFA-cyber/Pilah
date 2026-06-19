# Draf Listing Play Store — PILAH

> Status: draf awal (Fase 7). Perlu ditinjau ulang sebelum submit ke Play Console
> (cek batas karakter, aset grafis final, dan kebijakan terbaru Google Play).

## Info Aplikasi

- **Nama aplikasi:** PILAH
- **Tagline:** Biar AI yang Beres-Beres.
- **Kategori:** Tools (Alat) — alternatif: Productivity
- **Application ID:** `id.pilah.app`

## Deskripsi Singkat (maks. 80 karakter)

```
AI yang memilah file HP-mu: mana yang penting, mana yang layak dihapus.
```

## Deskripsi Lengkap (maks. 4000 karakter)

```
PILAH adalah asisten AI untuk merapikan penyimpanan HP-mu secara otomatis.

Bingung file mana yang penting dan mana yang bisa dihapus? PILAH memindai
seluruh penyimpananmu, menilai setiap file (foto, dokumen, unduhan, APK),
lalu mengelompokkannya menjadi dua: PENTING dan LAYAK DIHAPUS — lengkap
dengan alasannya.

FITUR UTAMA
- Pindai Cerdas: memindai penyimpanan internal & SD card, mendeteksi
  duplikat, screenshot lama, APK yang sudah terinstal, foto buram, dan
  dokumen penting (KTP, ijazah, invoice, dll).
- Tinjau & Setujui: kamu melihat usulan AI sebelum aksi apa pun dijalankan.
  Tidak ada file yang dipindah atau dihapus tanpa persetujuanmu.
- Rapi Otomatis: file penting dirapikan ke folder terstruktur (Dokumen
  Penting, Foto Kenangan, Kerja & Bisnis, Arsip).
- Karantina Aman: file "layak dihapus" disimpan di Karantina selama 30 hari
  sebelum dihapus permanen — bisa dipulihkan kapan saja.
- Dashboard Penyimpanan: lihat ruang yang berhasil dihemat dan riwayat
  pembersihan.
- Analisis Mendalam (opsional): aktifkan mode ini untuk meminta AI cloud
  menilai dokumen yang ambigu lebih akurat. Bersifat opt-in dan memerlukan
  kunci API milikmu sendiri — tanpa mengaktifkannya, semua analisis berjalan
  100% di perangkat.

PRIVASI ADALAH PRIORITAS
- Mode default "Hanya di Perangkat": seluruh pemindaian & klasifikasi
  berjalan lokal, tanpa data yang dikirim ke luar HP.
- Mode "Analisis Mendalam" (opsional) hanya mengirim nama file & cuplikan
  teks dari dokumen berkategori Ambigu — tidak pernah seluruh isi file,
  foto, atau video.
- Antarmuka penuh dalam Bahasa Indonesia.

Biar AI yang beres-beres. Unduh PILAH sekarang dan rapikan HP-mu dalam
beberapa menit.
```

## Aset Grafis (placeholder — belum dibuat)

| Aset | Ukuran | Status |
|---|---|---|
| Ikon aplikasi (hi-res) | 512x512 px PNG, 32-bit | TODO |
| Feature graphic | 1024x500 px | TODO |
| Screenshot ponsel (min. 2, maks. 8) | 16:9 atau 9:16 | TODO — alur Onboarding, Pindai, Tinjau Hasil, Sebelum/Sesudah, Dashboard |
| Promo video (opsional) | Link YouTube | TODO |

## Klasifikasi Konten & Rating

- Isi konten: tidak ada konten dewasa/kekerasan — target rating "Semua Usia" / PEGI 3.
- Perlu mengisi kuesioner Content Rating di Play Console sesuai standar (IARC).

## Izin yang Digunakan & Justifikasi

| Izin | Alasan |
|---|---|
| `MANAGE_EXTERNAL_STORAGE` | Inti fungsi aplikasi: memindai, memindahkan, dan mengelola file di seluruh penyimpanan (kategori "file manager"). Wajib mengisi formulir deklarasi Play Console (Permissions Declaration Form) dengan penjelasan ini. |
| Penyimpanan legacy (Android ≤ 10) | Fallback untuk akses baca/tulis pada versi Android lama, dijelaskan di layar Onboarding. |
| Akses internet (`core/network`) | Hanya dipakai jika pengguna mengaktifkan mode "Analisis Mendalam" secara eksplisit (PrivacyMode.DEEP_ANALYSIS) dan mengisi kunci API Claude sendiri. |

> **Catatan rilis:** Google Play mensyaratkan video & formulir justifikasi
> tambahan untuk app yang meminta `MANAGE_EXTERNAL_STORAGE`. Siapkan video
> demo singkat yang menunjukkan fitur "pindai & rapikan file" sebelum submit.

## Kebijakan Privasi

- Perlu URL publik ke kebijakan privasi (wajib oleh Play Console) yang
  menjelaskan: (1) data tidak meninggalkan perangkat pada mode default,
  (2) apa yang dikirim ke Claude API pada mode Analisis Mendalam (nama
  file + cuplikan teks dokumen Ambigu), (3) penyimpanan kunci API secara
  terenkripsi di perangkat (Android Keystore), (4) tidak ada pengumpulan
  data analitik/iklan pihak ketiga di MVP.
- TODO: tulis & hosting halaman kebijakan privasi sebelum submit produksi.

## Status Build & Signing

- Skema signing config rilis: lihat `app/build.gradle.kts`
  (`signingConfigs.release`, dibaca dari `keystore.properties`, lihat
  `keystore.properties.example` di root proyek). Belum ada keystore rilis
  yang dibuat — perlu di-generate & disimpan aman (mis. Play App Signing)
  sebelum build rilis pertama.
- `versionCode`/`versionName` saat ini: `1` / `0.1.0` (lihat
  `app/build.gradle.kts`) — naikkan sesuai kebijakan rilis sebelum upload.

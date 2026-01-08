# MountTrack – Seed News (Firestore)

Tool kecil untuk memasukkan **30 dokumen** ke Firestore collection `news` tanpa input manual di aplikasi admin.

## Prasyarat
- Node.js (disarankan 18+)
- Akses ke Firebase project yang sama dengan `google-services.json`
- **Service account key** Firebase Admin SDK (JSON)

> Catatan: ini **tidak** memakai Firebase Storage.

## Cara pakai (Windows PowerShell)
1) Buat service account key:
   - Firebase Console → Project settings → Service accounts → Generate new private key
   - Simpan file JSON-nya (mis. `serviceAccountKey.json`)

2) Install dependency:

```powershell
cd "D:\ARRAY\Materi Kuliah\Semester 5\Pengembangan Aplikasi Mobile\Tugas Akhir\MountTrack\tools\seed-news"
npm install
```

3) Jalankan seed (ini yang benar-benar memasukkan 30 data ke Firestore):

```powershell
$env:FIREBASE_SERVICE_ACCOUNT="D:\path\to\serviceAccountKey.json"
node .\seed-news.mjs
```

## Verifikasi data sudah masuk
1) Buka Firebase Console → Firestore Database
2) Pilih tab **Data**
3) Pastikan ada collection **`news`**
4) Di dalamnya harus ada dokumen dengan ID:
   - `news_001` s/d `news_030`

Kalau sudah masuk, di aplikasi user MountTrack harus muncul karena seed ini mengisi:
- `status` = `PUBLISHED`
- `published` = `true`

## Output yang diharapkan
- 30 dokumen terbuat/terupdate di collection `news`
- Field konsisten dengan model aplikasi (`HikingNews`), termasuk:
  - `newsId` (string)
  - `title`, `category`, `content`, `tags`, `coverImageUrl` (string)
  - `status` ("PUBLISHED")
  - `published` (boolean)
  - `isFeatured` (boolean)
  - `authorId`, `authorName` (string)
  - `createdAt`, `updatedAt` (Firestore Timestamp)

## Troubleshooting
- Error kredensial: pastikan env `FIREBASE_SERVICE_ACCOUNT` menunjuk ke file JSON yang benar.
- Data tidak muncul di app: pastikan `status` = `PUBLISHED` atau `published` = `true`.

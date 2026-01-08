import fs from 'node:fs';
import path from 'node:path';
import process from 'node:process';
import admin from 'firebase-admin';

const here = path.dirname(new URL(import.meta.url).pathname);

function requireEnv(name) {
  const val = process.env[name];
  if (!val) throw new Error(`Missing env ${name}. Set it to your service account JSON path.`);
  return val;
}

function parseServiceAccount(filePath) {
  const raw = fs.readFileSync(filePath, 'utf8');
  return JSON.parse(raw);
}

function toTimestamp(maybeExportedTs) {
  // Accept { _seconds, _nanoseconds } from seed JSON
  if (maybeExportedTs && typeof maybeExportedTs === 'object') {
    const s = maybeExportedTs._seconds;
    const ns = maybeExportedTs._nanoseconds;
    if (Number.isInteger(s) && Number.isInteger(ns)) {
      return new admin.firestore.Timestamp(s, ns);
    }
  }
  // Fallback now
  return admin.firestore.Timestamp.now();
}

function normalizeNewsDoc(docId, doc) {
  const now = admin.firestore.Timestamp.now();

  const newsId = doc.newsId || doc.id || docId;
  const status = (doc.status || 'PUBLISHED').toString().trim().toUpperCase();

  return {
    // Keep both for tolerance in app model
    id: doc.id ?? '',
    newsId,

    title: (doc.title ?? '').toString(),
    category: (doc.category ?? '').toString(),
    content: (doc.content ?? '').toString(),
    tags: (doc.tags ?? '').toString(),
    coverImageUrl: (doc.coverImageUrl ?? '').toString(),

    status,
    published: doc.published === true || status === 'PUBLISHED' || status === 'PUBLISH',
    isFeatured: doc.isFeatured === true,

    authorId: (doc.authorId ?? 'seed').toString(),
    authorName: (doc.authorName ?? 'MountTrack Editorial').toString(),

    createdAt: doc.createdAt ? toTimestamp(doc.createdAt) : now,
    updatedAt: doc.updatedAt ? toTimestamp(doc.updatedAt) : now
  };
}

async function main() {
  const serviceAccountPath = requireEnv('FIREBASE_SERVICE_ACCOUNT');
  const seedPath = path.join(here, 'seed-news.json');

  if (!fs.existsSync(seedPath)) {
    throw new Error(`seed file not found: ${seedPath}`);
  }

  const serviceAccount = parseServiceAccount(serviceAccountPath);

  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });

  const db = admin.firestore();

  const seedRaw = JSON.parse(fs.readFileSync(seedPath, 'utf8'));
  const news = seedRaw?.news;
  if (!news || typeof news !== 'object') {
    throw new Error('Invalid seed-news.json: expected top-level object with key "news"');
  }

  const entries = Object.entries(news);
  if (entries.length === 0) {
    console.log('No news entries found, nothing to seed.');
    return;
  }

  console.log(`Seeding ${entries.length} docs into collection: news`);

  // Batch writes (500 max) — safe for 30 docs
  const batch = db.batch();

  for (const [docId, doc] of entries) {
    const ref = db.collection('news').doc(docId);
    const payload = normalizeNewsDoc(docId, doc);
    batch.set(ref, payload, { merge: true });
  }

  await batch.commit();
  console.log('✅ Seed completed.');
}

main().catch((err) => {
  console.error('❌ Seed failed:', err);
  process.exitCode = 1;
});


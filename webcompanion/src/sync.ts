import type { Bookmark, OfflineQueueItem } from './types';
import { getOfflineQueue, saveOfflineQueue, getOfflineBookmarks, saveOfflineBookmarks, getToken } from './storage';
import { postBookmark } from './api';

export function normalizeUrl(raw: string): string {
  let trimmed = (raw || '').trim();
  if (!trimmed) return '';
  if (!/^https?:\/\//i.test(trimmed)) {
    trimmed = 'https://' + trimmed;
  }
  try {
    const parsed = new URL(trimmed);
    const host = parsed.hostname.toLowerCase().replace(/^www\./, '');
    let path = parsed.pathname || '';
    if (path.length > 1 && path.endsWith('/')) {
      path = path.slice(0, -1);
    } else if (path === '/') {
      path = '';
    }
    const search = parsed.search || '';
    const hash = parsed.hash || '';
    return `${parsed.protocol}//${host}${path}${search}${hash}`;
  } catch (e) {
    return trimmed;
  }
}

export function normalizeUrlForComparison(raw: string): string {
  let trimmed = (raw || '').trim();
  if (!trimmed) return '';
  if (!/^https?:\/\//i.test(trimmed)) {
    trimmed = 'https://' + trimmed;
  }
  try {
    const parsed = new URL(trimmed);
    const host = parsed.hostname.toLowerCase().replace(/^www\./, '');
    let path = parsed.pathname || '';
    if (path.length > 1 && path.endsWith('/')) {
      path = path.slice(0, -1);
    } else if (path === '/') {
      path = '';
    }
    const searchParams = new URLSearchParams(parsed.search);
    const trackingKeys = ['utm_source', 'utm_medium', 'utm_campaign', 'utm_term', 'utm_content', 'fbclid', 'gclid'];
    trackingKeys.forEach((k) => searchParams.delete(k));
    const search = searchParams.toString() ? '?' + searchParams.toString() : '';
    return `${host}${path}${search}`;
  } catch (e) {
    return trimmed.replace(/^https?:\/\//i, '').replace(/^www\./i, '').replace(/\/+$/, '');
  }
}

export function deduplicateBookmarks(bookmarks: Bookmark[]): Bookmark[] {
  const seen = new Set<string>();
  const result: Bookmark[] = [];
  for (const b of bookmarks) {
    const key = normalizeUrlForComparison(b.url);
    if (!key || !seen.has(key)) {
      if (key) seen.add(key);
      result.push(b);
    }
  }
  return result;
}

export function extractDomain(url: string): string {
  try {
    const parsed = new URL(url.startsWith('http') ? url : 'https://' + url);
    return parsed.hostname.replace(/^www\./, '');
  } catch (e) {
    return url;
  }
}

export async function removeOfflineQueueItem(url: string): Promise<void> {
  const normTarget = normalizeUrlForComparison(url);
  const queue = getOfflineQueue();
  const filtered = queue.filter((item) => normalizeUrlForComparison(item.url) !== normTarget);
  if (filtered.length !== queue.length) {
    await saveOfflineQueue(filtered);
  }
}

export async function flushOfflineQueue(onSyncedCount?: (count: number) => void): Promise<number> {
  const queue = getOfflineQueue();
  if (!queue || queue.length === 0) return 0;

  const token = getToken();
  if (!token) return 0;

  let syncedCount = 0;
  const remaining: OfflineQueueItem[] = [];
  const resolvedKeys = new Set<string>();

  for (const item of queue) {
    try {
      const res = await postBookmark(item.url, item.category, token);
      if (res.success || (res.error && res.error.toLowerCase().includes('already in your stack'))) {
        syncedCount++;
        resolvedKeys.add(normalizeUrlForComparison(item.url));
      } else {
        remaining.push(item);
      }
    } catch (e) {
      remaining.push(item);
    }
  }

  await saveOfflineQueue(remaining);

  if (resolvedKeys.size > 0) {
    const offlineBms = getOfflineBookmarks();
    let updated = false;
    for (const b of offlineBms) {
      if (b.isPendingSync && resolvedKeys.has(normalizeUrlForComparison(b.url))) {
        b.isPendingSync = false;
        updated = true;
      }
    }
    if (updated) {
      await saveOfflineBookmarks(offlineBms);
    }
  }

  if (syncedCount > 0 && onSyncedCount) {
    onSyncedCount(syncedCount);
  }
  return syncedCount;
}

export async function saveBookmarkLocally(rawUrl: string, category: string): Promise<Bookmark> {
  const url = normalizeUrl(rawUrl);
  const normKey = normalizeUrlForComparison(url);
  const offlineItem: Bookmark = {
    id: 'offline_' + Date.now(),
    title: url,
    url: url,
    domain: extractDomain(url),
    category: category,
    createdAtMs: Date.now(),
    isPendingSync: true
  };

  const allBookmarks = getOfflineBookmarks();
  if (!allBookmarks.some((b) => normalizeUrlForComparison(b.url) === normKey)) {
    allBookmarks.unshift(offlineItem);
    await saveOfflineBookmarks(allBookmarks);
  }

  const queue = getOfflineQueue();
  if (!queue.some((q) => normalizeUrlForComparison(q.url) === normKey)) {
    queue.push({ url, category, createdAtMs: Date.now() });
    await saveOfflineQueue(queue);
  }

  return offlineItem;
}

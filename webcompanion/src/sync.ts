import type { Bookmark, OfflineQueueItem } from './types';
import { getOfflineQueue, saveOfflineQueue, getOfflineBookmarks, saveOfflineBookmarks, getToken } from './storage';
import { postBookmark } from './api';

export function extractDomain(url: string): string {
  try {
    const parsed = new URL(url.startsWith('http') ? url : 'https://' + url);
    return parsed.hostname.replace(/^www\./, '');
  } catch (e) {
    return url;
  }
}

export async function flushOfflineQueue(onSyncedCount?: (count: number) => void): Promise<number> {
  const queue = getOfflineQueue();
  if (!queue || queue.length === 0) return 0;

  const token = getToken();
  if (!token) return 0;

  let syncedCount = 0;
  const remaining: OfflineQueueItem[] = [];

  for (const item of queue) {
    try {
      const res = await postBookmark(item.url, item.category, token);
      if (res.success) {
        syncedCount++;
      } else {
        remaining.push(item);
      }
    } catch (e) {
      remaining.push(item);
    }
  }

  await saveOfflineQueue(remaining);
  if (syncedCount > 0 && onSyncedCount) {
    onSyncedCount(syncedCount);
  }
  return syncedCount;
}

export async function saveBookmarkLocally(url: string, category: string): Promise<Bookmark> {
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
  allBookmarks.unshift(offlineItem);
  await saveOfflineBookmarks(allBookmarks);

  const queue = getOfflineQueue();
  queue.push({ url, category, createdAtMs: Date.now() });
  await saveOfflineQueue(queue);

  return offlineItem;
}

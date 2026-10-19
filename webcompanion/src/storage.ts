import type { Bookmark, InitialWebStackData, OfflineQueueItem } from './types';
import { encryptPayload, decryptPayload } from './crypto';

export function getLocalItem(key: string): string | null {
  try {
    return typeof localStorage !== 'undefined' ? localStorage.getItem(key) : null;
  } catch (e) {
    return null;
  }
}

export function setLocalItem(key: string, value: string): void {
  try {
    if (typeof localStorage !== 'undefined') localStorage.setItem(key, value);
  } catch (e) {}
}

export function removeLocalItem(key: string): void {
  try {
    if (typeof localStorage !== 'undefined') localStorage.removeItem(key);
  } catch (e) {}
}

export function getSessionItem(key: string): string | null {
  try {
    return typeof sessionStorage !== 'undefined' ? sessionStorage.getItem(key) : null;
  } catch (e) {
    return null;
  }
}

export function setSessionItem(key: string, value: string): void {
  try {
    if (typeof sessionStorage !== 'undefined') sessionStorage.setItem(key, value);
  } catch (e) {}
}

export function removeSessionItem(key: string): void {
  try {
    if (typeof sessionStorage !== 'undefined') sessionStorage.removeItem(key);
  } catch (e) {}
}

// Hydrate offline storage from baked seed data (e.g. standalone exported HTML)
export function initStorage(): void {
  try {
    const seed = (typeof window !== 'undefined' ? (window as any).INITIAL_WEBSTACK_DATA : undefined) as InitialWebStackData | undefined;
    if (seed) {
      if (!getLocalItem('webstack_server_url') && seed.serverUrl) {
        setLocalItem('webstack_server_url', seed.serverUrl);
      }
      if (!getLocalItem('webstack_last_connected') && seed.lastConnected) {
        setLocalItem('webstack_last_connected', seed.lastConnected);
      }
      if (!getLocalItem('webstack_offline_expiry_hours') && seed.offlineExpiryHours) {
        setLocalItem('webstack_offline_expiry_hours', seed.offlineExpiryHours);
      }
      if (!getLocalItem('webstack_offline_bookmarks') && seed.offlineBookmarks) {
        setLocalItem('webstack_offline_bookmarks', seed.offlineBookmarks);
      }
      if (!getLocalItem('webstack_offline_categories') && seed.offlineCategories) {
        setLocalItem('webstack_offline_categories', seed.offlineCategories);
      }
    }
  } catch (e) {}
}

initStorage();

let activePin = getSessionItem('webstack_pin') || '';
let cachedOfflineBookmarks: Bookmark[] | null = null;
let cachedOfflineQueue: OfflineQueueItem[] | null = null;

export function getActivePin(): string {
  return activePin;
}

export function setActivePin(pin: string): void {
  activePin = pin;
  if (pin) {
    setSessionItem('webstack_pin', pin);
  } else {
    removeSessionItem('webstack_pin');
  }
}

export function getToken(): string | null {
  return getLocalItem('webstack_token') || getSessionItem('webstack_token');
}

export function setToken(token: string | null): void {
  if (token) {
    setLocalItem('webstack_token', token);
    setSessionItem('webstack_token', token);
  } else {
    removeLocalItem('webstack_token');
    removeSessionItem('webstack_token');
  }
}

export function updateLastConnected(expiryHours?: number | null): void {
  setLocalItem('webstack_last_connected', Date.now().toString());
  if (expiryHours !== undefined && expiryHours !== null) {
    setLocalItem('webstack_offline_expiry_hours', expiryHours.toString());
  }
}

export function getSeedData(): InitialWebStackData | undefined {
  if (typeof window !== 'undefined' && (window as any).INITIAL_WEBSTACK_DATA) {
    return (window as any).INITIAL_WEBSTACK_DATA as InitialWebStackData;
  }
  return undefined;
}

export function hasOfflineVaultData(): boolean {
  const raw = getLocalItem('webstack_offline_bookmarks');
  if (raw && raw.trim().length > 0) return true;
  const seed = getSeedData();
  return !!seed?.offlineBookmarks && seed.offlineBookmarks.trim().length > 0;
}

export function getOfflineExpiryHours(): number {
  const local = getLocalItem('webstack_offline_expiry_hours');
  if (local) return parseInt(local, 10);
  const seed = getSeedData();
  if (seed?.offlineExpiryHours) return parseInt(seed.offlineExpiryHours, 10);
  return 24;
}

export function isOfflineSessionValid(): boolean {
  let lastConnected = parseInt(getLocalItem('webstack_last_connected') || '0', 10);
  if (!lastConnected) {
    const seed = getSeedData();
    if (seed?.lastConnected) {
      lastConnected = parseInt(seed.lastConnected, 10);
    }
  }
  if (!lastConnected) return false;
  const expiryHours = getOfflineExpiryHours();
  if (expiryHours === 0) return true; // Never expires
  const elapsedMs = Date.now() - lastConnected;
  return elapsedMs < expiryHours * 3600 * 1000;
}

export function getOfflineBookmarks(): Bookmark[] {
  if (cachedOfflineBookmarks && Array.isArray(cachedOfflineBookmarks)) {
    return cachedOfflineBookmarks;
  }
  try {
    let raw = getLocalItem('webstack_offline_bookmarks');
    if (!raw) {
      const seed = getSeedData();
      if (seed?.offlineBookmarks) {
        raw = typeof seed.offlineBookmarks === 'string' ? seed.offlineBookmarks : JSON.stringify(seed.offlineBookmarks);
      }
    }
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    if (parsed && parsed.__encrypted) return [];
    return Array.isArray(parsed) ? parsed : [];
  } catch (e) {
    return [];
  }
}

export async function saveOfflineBookmarks(bms: Bookmark[]): Promise<void> {
  cachedOfflineBookmarks = bms;
  try {
    setLocalItem('webstack_offline_bookmarks', JSON.stringify(bms));
  } catch (e) {}
}

export function setCachedOfflineBookmarks(bms: Bookmark[]): void {
  cachedOfflineBookmarks = bms;
}

export function getOfflineCategories(): string[] {
  try {
    let raw = getLocalItem('webstack_offline_categories');
    if (!raw) {
      const seed = getSeedData();
      if (seed?.offlineCategories) {
        raw = typeof seed.offlineCategories === 'string' ? seed.offlineCategories : JSON.stringify(seed.offlineCategories);
      }
    }
    return raw ? JSON.parse(raw) : ['Personal', 'Work', 'Social', 'Reading'];
  } catch (e) {
    return ['Personal', 'Work', 'Social', 'Reading'];
  }
}

export function saveOfflineCategories(cats: string[]): void {
  try {
    setLocalItem('webstack_offline_categories', JSON.stringify(cats));
  } catch (e) {}
}

export function getOfflineQueue(): OfflineQueueItem[] {
  if (cachedOfflineQueue && Array.isArray(cachedOfflineQueue)) {
    return cachedOfflineQueue;
  }
  try {
    const raw = getLocalItem('webstack_offline_queue');
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    if (parsed && parsed.__encrypted) return [];
    return Array.isArray(parsed) ? parsed : [];
  } catch (e) {
    return [];
  }
}

export async function saveOfflineQueue(q: OfflineQueueItem[]): Promise<void> {
  cachedOfflineQueue = q;
  try {
    setLocalItem('webstack_offline_queue', JSON.stringify(q));
  } catch (e) {}
}

export function setCachedOfflineQueue(q: OfflineQueueItem[]): void {
  cachedOfflineQueue = q;
}

export async function unlockWithPin(pin: string): Promise<boolean> {
  let rawBms = getLocalItem('webstack_offline_bookmarks');
  if (!rawBms) {
    const seed = getSeedData();
    if (seed?.offlineBookmarks) {
      rawBms = typeof seed.offlineBookmarks === 'string' ? seed.offlineBookmarks : JSON.stringify(seed.offlineBookmarks);
    }
  }
  if (rawBms) {
    const dec = await decryptPayload<Bookmark[]>(rawBms, pin);
    if (dec) {
      cachedOfflineBookmarks = dec;
    }
  }
  const rawQ = getLocalItem('webstack_offline_queue');
  if (rawQ) {
    const decQ = await decryptPayload<OfflineQueueItem[]>(rawQ, pin);
    if (decQ) {
      cachedOfflineQueue = decQ;
    }
  }
  return true;
}

export function clearSession(): void {
  setToken(null);
  setActivePin('');
  cachedOfflineBookmarks = null;
  cachedOfflineQueue = null;
  removeLocalItem('webstack_last_connected');
}

import type { Bookmark, OfflineQueueItem } from './types';
import { encryptPayload, decryptPayload } from './crypto';

let activePin = sessionStorage.getItem('webstack_pin') || '';
let cachedOfflineBookmarks: Bookmark[] | null = null;
let cachedOfflineQueue: OfflineQueueItem[] | null = null;

export function getActivePin(): string {
  return activePin;
}

export function setActivePin(pin: string): void {
  activePin = pin;
  if (pin) {
    sessionStorage.setItem('webstack_pin', pin);
  } else {
    sessionStorage.removeItem('webstack_pin');
  }
}

export function getToken(): string | null {
  return localStorage.getItem('webstack_token') || sessionStorage.getItem('webstack_token');
}

export function setToken(token: string | null): void {
  if (token) {
    localStorage.setItem('webstack_token', token);
    sessionStorage.setItem('webstack_token', token);
  } else {
    localStorage.removeItem('webstack_token');
    sessionStorage.removeItem('webstack_token');
  }
}

export function updateLastConnected(expiryHours?: number | null): void {
  localStorage.setItem('webstack_last_connected', Date.now().toString());
  if (expiryHours !== undefined && expiryHours !== null) {
    localStorage.setItem('webstack_offline_expiry_hours', expiryHours.toString());
  }
}

export function isOfflineSessionValid(): boolean {
  const lastConnected = parseInt(localStorage.getItem('webstack_last_connected') || '0', 10);
  if (!lastConnected) return false;
  const expiryHours = parseInt(localStorage.getItem('webstack_offline_expiry_hours') || '24', 10);
  if (expiryHours === 0) return true; // Never expires
  const elapsedMs = Date.now() - lastConnected;
  return elapsedMs < expiryHours * 3600 * 1000;
}

export function getOfflineBookmarks(): Bookmark[] {
  if (cachedOfflineBookmarks && Array.isArray(cachedOfflineBookmarks)) {
    return cachedOfflineBookmarks;
  }
  try {
    const raw = localStorage.getItem('webstack_offline_bookmarks');
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
    if (activePin) {
      const enc = await encryptPayload(bms, activePin);
      localStorage.setItem('webstack_offline_bookmarks', enc);
    } else {
      localStorage.setItem('webstack_offline_bookmarks', JSON.stringify(bms));
    }
  } catch (e) {}
}

export function setCachedOfflineBookmarks(bms: Bookmark[]): void {
  cachedOfflineBookmarks = bms;
}

export function getOfflineCategories(): string[] {
  try {
    const raw = localStorage.getItem('webstack_offline_categories');
    return raw ? JSON.parse(raw) : ['Personal', 'Work', 'Social', 'Reading'];
  } catch (e) {
    return ['Personal', 'Work', 'Social', 'Reading'];
  }
}

export function saveOfflineCategories(cats: string[]): void {
  try {
    localStorage.setItem('webstack_offline_categories', JSON.stringify(cats));
  } catch (e) {}
}

export function getOfflineQueue(): OfflineQueueItem[] {
  if (cachedOfflineQueue && Array.isArray(cachedOfflineQueue)) {
    return cachedOfflineQueue;
  }
  try {
    const raw = localStorage.getItem('webstack_offline_queue');
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
    if (activePin) {
      const enc = await encryptPayload(q, activePin);
      localStorage.setItem('webstack_offline_queue', enc);
    } else {
      localStorage.setItem('webstack_offline_queue', JSON.stringify(q));
    }
  } catch (e) {}
}

export function setCachedOfflineQueue(q: OfflineQueueItem[]): void {
  cachedOfflineQueue = q;
}

export async function unlockWithPin(pin: string): Promise<boolean> {
  const rawBms = localStorage.getItem('webstack_offline_bookmarks');
  if (rawBms) {
    const dec = await decryptPayload<Bookmark[]>(rawBms, pin);
    if (dec) {
      cachedOfflineBookmarks = dec;
    }
  }
  const rawQ = localStorage.getItem('webstack_offline_queue');
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
  localStorage.removeItem('webstack_last_connected');
}

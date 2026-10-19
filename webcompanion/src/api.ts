import type { AuthResponse, Bookmark, CreateBookmarkResponse, ServerStatus } from './types';
import { getLocalItem, setLocalItem } from './storage';

export function getApiBase(): string {
  if (typeof window !== 'undefined' && window.location && window.location.protocol.startsWith('http')) {
    const origin = window.location.origin;
    setLocalItem('webstack_server_url', origin);
    return origin;
  }
  const saved = getLocalItem('webstack_server_url');
  if (saved && saved.startsWith('http')) return saved.replace(/\/+$/, '');

  const injected = typeof window !== 'undefined' ? (window as any).DEFAULT_PHONE_ORIGIN : undefined;
  if (injected && typeof injected === 'string' && injected.startsWith('http')) {
    const cleanInjected = injected.replace(/\/+$/, '');
    setLocalItem('webstack_server_url', cleanInjected);
    return cleanInjected;
  }

  return 'http://localhost:8080';
}

export function setApiBase(url: string): string {
  let clean = url.trim().replace(/\/+$/, '');
  if (!clean.startsWith('http://') && !clean.startsWith('https://')) {
    clean = 'http://' + clean;
  }
  setLocalItem('webstack_server_url', clean);
  return clean;
}

export function apiUrl(path: string): string {
  const base = getApiBase();
  return base + (path.startsWith('/') ? path : '/' + path);
}

export async function checkServerStatus(token?: string | null): Promise<ServerStatus> {
  const headers: Record<string, string> = {};
  if (token) headers['Authorization'] = 'Bearer ' + token;
  const q = token ? '?token=' + encodeURIComponent(token) : '';
  const res = await fetch(apiUrl('/api/status' + q), { headers });
  if (!res.ok) throw new Error('Status check failed: ' + res.status);
  return res.json();
}

export async function authenticateWithPin(pin: string): Promise<AuthResponse> {
  const res = await fetch(apiUrl('/api/auth'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ pin: pin.trim() })
  });
  return res.json();
}

export async function fetchBookmarks(token: string): Promise<Bookmark[]> {
  const q = '?token=' + encodeURIComponent(token);
  const res = await fetch(apiUrl('/api/bookmarks' + q), {
    headers: { 'Authorization': 'Bearer ' + token }
  });
  if (!res.ok) throw new Error('Failed to fetch bookmarks: ' + res.status);
  return res.json();
}

export async function fetchCategories(token: string): Promise<string[]> {
  const q = '?token=' + encodeURIComponent(token);
  const res = await fetch(apiUrl('/api/categories' + q), {
    headers: { 'Authorization': 'Bearer ' + token }
  });
  if (!res.ok) throw new Error('Failed to fetch categories: ' + res.status);
  return res.json();
}

export async function postBookmark(url: string, category: string, token: string): Promise<CreateBookmarkResponse> {
  const q = '?token=' + encodeURIComponent(token);
  const res = await fetch(apiUrl('/api/bookmarks' + q), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token
    },
    body: JSON.stringify({ url: url.trim(), category: category.trim() })
  });
  if (!res.ok && res.status === 401) {
    return { success: false, error: 'Unauthorized: Session expired on phone' };
  }
  return res.json();
}

export async function disconnectServer(token?: string | null): Promise<void> {
  if (!token) return;
  try {
    const q = '?token=' + encodeURIComponent(token);
    await fetch(apiUrl('/api/disconnect' + q), {
      method: 'POST',
      headers: { 'Authorization': 'Bearer ' + token }
    });
  } catch (e) {
    // Ignore network error on disconnect
  }
}

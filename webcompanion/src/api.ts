import type { AuthResponse, Bookmark, ServerStatus } from './types';

export function getApiBase(): string {
  if (window.location && window.location.protocol.startsWith('http')) {
    const origin = window.location.origin;
    localStorage.setItem('webstack_server_url', origin);
    return origin;
  }
  const saved = localStorage.getItem('webstack_server_url');
  if (saved && saved.startsWith('http')) return saved.replace(/\/+$/, '');
  return 'http://localhost:8080';
}

export function apiUrl(path: string): string {
  const base = getApiBase();
  return base + (path.startsWith('/') ? path : '/' + path);
}

export async function checkServerStatus(token?: string | null): Promise<ServerStatus> {
  const headers: Record<string, string> = {};
  if (token) headers['Authorization'] = 'Bearer ' + token;
  const res = await fetch(apiUrl('/api/status'), { headers });
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
  const res = await fetch(apiUrl('/api/bookmarks'), {
    headers: { 'Authorization': 'Bearer ' + token }
  });
  if (!res.ok) throw new Error('Failed to fetch bookmarks: ' + res.status);
  return res.json();
}

export async function fetchCategories(token: string): Promise<string[]> {
  const res = await fetch(apiUrl('/api/categories'), {
    headers: { 'Authorization': 'Bearer ' + token }
  });
  if (!res.ok) throw new Error('Failed to fetch categories: ' + res.status);
  return res.json();
}

export async function postBookmark(url: string, category: string, token: string): Promise<{ success: boolean; error?: string }> {
  const res = await fetch(apiUrl('/api/bookmarks'), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token
    },
    body: JSON.stringify({ url: url.trim(), category: category.trim() })
  });
  return res.json();
}

export async function disconnectServer(token?: string | null): Promise<void> {
  if (!token) return;
  try {
    await fetch(apiUrl('/api/disconnect'), {
      method: 'POST',
      headers: { 'Authorization': 'Bearer ' + token }
    });
  } catch (e) {
    // Ignore network error on disconnect
  }
}

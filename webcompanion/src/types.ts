export interface Bookmark {
  id: string | number;
  title: string;
  url: string;
  domain: string;
  category: string;
  createdAtMs: number;
  isPendingSync?: boolean;
}

export interface ServerStatus {
  status: string;
  device: string;
  bookmarksCount: number;
  offlineExpiryHours: number;
}

export interface AuthResponse {
  success: boolean;
  token?: string;
  error?: string;
  offlineExpiryHours?: number;
}

export interface CreateBookmarkResponse {
  success: boolean;
  id?: number | string;
  title?: string;
  url?: string;
  domain?: string;
  category?: string;
  createdAtMs?: number;
  error?: string;
}

export interface OfflineQueueItem {
  url: string;
  category: string;
  createdAtMs: number;
}

export interface EncryptedPayload {
  __encrypted: true;
  iv: number[];
  data: number[];
}

export interface InitialWebStackData {
  serverUrl?: string;
  lastConnected?: string;
  offlineExpiryHours?: string;
  offlineBookmarks?: string;
  offlineCategories?: string;
}


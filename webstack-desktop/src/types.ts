export interface Bookmark {
  id: string | number;
  title: string;
  url: string;
  domain: string;
  category: string;
  createdAtMs: number;
  isPendingSync?: boolean;
}

export interface CompanionStatus {
  authenticated: boolean;
  activeConnections?: number;
  offlineExpiryHours?: number;
  serverTime?: number;
}

export interface DiscoveredServer {
  ip: string;
  port: number;
  url: string;
  name?: string;
}

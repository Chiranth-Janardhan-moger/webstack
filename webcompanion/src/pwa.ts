import { getOfflineQueue } from './storage';

let deferredPrompt: any = null;

export function initPwa(onInstallable?: () => void): void {
  // Service Worker Registration for Offline Shell
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      navigator.serviceWorker.register('/sw.js').catch(() => {});
    });
  }

  // Desktop PWA Install Prompt
  window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault();
    deferredPrompt = e;
    if (onInstallable) onInstallable();
  });

  // Accidental Tab Close Guard when offline or sync pending
  window.addEventListener('beforeunload', (e) => {
    const queue = getOfflineQueue();
    if (queue && queue.length > 0) {
      e.preventDefault();
      e.returnValue = 'You have pending bookmarks that have not synced to your phone yet. Leave anyway?';
      return e.returnValue;
    }
  });
}

export async function promptPwaInstall(): Promise<boolean> {
  if (!deferredPrompt) return false;
  deferredPrompt.prompt();
  const choice = await deferredPrompt.userChoice;
  deferredPrompt = null;
  return choice.outcome === 'accepted';
}

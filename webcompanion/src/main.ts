import './style.css';
import type { Bookmark } from './types';
import {
  getToken,
  setToken,
  getActivePin,
  setActivePin,
  updateLastConnected,
  isOfflineSessionValid,
  getOfflineBookmarks,
  saveOfflineBookmarks,
  getOfflineCategories,
  saveOfflineCategories,
  getOfflineQueue,
  unlockWithPin,
  clearSession,
  getLocalItem
} from './storage';
import {
  apiUrl,
  getApiBase,
  setApiBase,
  checkServerStatus,
  authenticateWithPin,
  fetchBookmarks,
  fetchCategories,
  postBookmark,
  disconnectServer
} from './api';
import {
  normalizeUrl,
  normalizeUrlForComparison,
  deduplicateBookmarks,
  removeOfflineQueueItem,
  flushOfflineQueue,
  saveBookmarkLocally,
  extractDomain
} from './sync';
import {
  showToast,
  copyToClipboard,
  setStatusState,
  renderCategories,
  renderBookmarks,
  renderTagGrid
} from './ui';
import { decryptPayload } from './crypto';

// Global bridges for inline HTML onclick handlers
(window as any).webstackShowToast = showToast;
(window as any).webstackCopyLink = (url: string) => {
  copyToClipboard(url);
  showToast('Copied link!');
};

let allBookmarks: Bookmark[] = [];
let availableCategories: string[] = ['Personal', 'Work', 'Social', 'Reading'];
let currentCategory = 'ALL';
let searchQuery = '';
let isOffline = false;

// DOM Elements
const authModal = document.getElementById('authModal') as HTMLDivElement;
const pinInput = document.getElementById('pinInput') as HTMLInputElement;
const btnConnect = document.getElementById('btnConnect') as HTMLButtonElement;
const authError = document.getElementById('authError') as HTMLDivElement;
const btnDisconnect = document.getElementById('btnDisconnect') as HTMLButtonElement;

const authServerDisplay = document.getElementById('authServerDisplay') as HTMLSpanElement | null;
const btnToggleServerEdit = document.getElementById('btnToggleServerEdit') as HTMLButtonElement | null;
const authServerEditBox = document.getElementById('authServerEditBox') as HTMLDivElement | null;
const authServerUrlInput = document.getElementById('authServerUrlInput') as HTMLInputElement | null;
const btnSaveServerUrl = document.getElementById('btnSaveServerUrl') as HTMLButtonElement | null;

const addModal = document.getElementById('addModal') as HTMLDivElement;
const btnOpenAddModal = document.getElementById('btnOpenAddModal') as HTMLButtonElement;
const btnCloseAddModal = document.getElementById('btnCloseAddModal') as HTMLButtonElement;
const btnCancelAdd = document.getElementById('btnCancelAdd') as HTMLButtonElement;
const addBookmarkForm = document.getElementById('addBookmarkForm') as HTMLFormElement;
const addUrl = document.getElementById('addUrl') as HTMLInputElement;
const addCategory = document.getElementById('addCategory') as HTMLInputElement;
const btnSaveBookmark = document.getElementById('btnSaveBookmark') as HTMLButtonElement;

const customCategoryWrapper = document.getElementById('customCategoryWrapper') as HTMLDivElement;
const tagDropdownTrigger = document.getElementById('tagDropdownTrigger') as HTMLButtonElement;
const selectedCategoryLabel = document.getElementById('selectedCategoryLabel') as HTMLSpanElement;
const tagDropdownMenu = document.getElementById('tagDropdownMenu') as HTMLDivElement;
const tagSearchWrapper = document.getElementById('tagSearchWrapper') as HTMLDivElement;
const tagSearchInput = document.getElementById('tagSearchInput') as HTMLInputElement;

const shortcutsModal = document.getElementById('shortcutsModal') as HTMLDivElement;
const btnOpenShortcutsModal = document.getElementById('btnOpenShortcutsModal') as HTMLButtonElement;
const btnCloseShortcutsModal = document.getElementById('btnCloseShortcutsModal') as HTMLButtonElement;
const btnGotItShortcuts = document.getElementById('btnGotItShortcuts') as HTMLButtonElement;

const offlineModal = document.getElementById('offlineModal') as HTMLDivElement;
const btnOpenOfflineModal = document.getElementById('btnOpenOfflineModal') as HTMLButtonElement;
const btnCloseOfflineModal = document.getElementById('btnCloseOfflineModal') as HTMLButtonElement;
const btnGotItOffline = document.getElementById('btnGotItOffline') as HTMLButtonElement;
const btnDownloadDesktop = document.getElementById('btnDownloadDesktop') as HTMLButtonElement | null;

const searchNav = document.getElementById('searchNav') as HTMLDivElement;
const btnSearchToggle = document.getElementById('btnSearchToggle') as HTMLButtonElement;
const btnClearSearch = document.getElementById('btnClearSearch') as HTMLButtonElement;
const searchInput = document.getElementById('searchInput') as HTMLInputElement;

// Modal Controls
function updateAuthServerDisplay() {
  const current = getApiBase();
  if (authServerDisplay) authServerDisplay.textContent = current;
  if (authServerUrlInput) authServerUrlInput.value = current;
}

function showAuthModal() {
  authModal.classList.remove('hidden');
  updateAuthServerDisplay();
  pinInput.value = '';
  pinInput.focus();
}

function hideAuthModal() {
  authModal.classList.add('hidden');
}

function openAddModal() {
  addModal.classList.remove('hidden');
  closeTagDropdown();
  setTimeout(() => addUrl.focus(), 80);
}

function closeAddModal() {
  addModal.classList.add('hidden');
  addUrl.value = '';
  closeTagDropdown();
}

function openShortcutsModal() {
  shortcutsModal.classList.remove('hidden');
}

function closeShortcutsModal() {
  shortcutsModal.classList.add('hidden');
}

function openOfflineModal() {
  offlineModal.classList.remove('hidden');
}

function closeOfflineModal() {
  offlineModal.classList.add('hidden');
}

// Custom Tag Dropdown
function toggleTagDropdown() {
  if (customCategoryWrapper.classList.contains('open')) {
    closeTagDropdown();
  } else {
    openTagDropdown();
  }
}

function openTagDropdown() {
  customCategoryWrapper.classList.add('open');
  tagDropdownTrigger.setAttribute('aria-expanded', 'true');
  if (availableCategories.length > 6) {
    tagSearchWrapper.style.display = 'flex';
    setTimeout(() => tagSearchInput.focus(), 60);
  } else {
    tagSearchWrapper.style.display = 'none';
  }
}

function closeTagDropdown() {
  customCategoryWrapper.classList.remove('open');
  tagDropdownTrigger.setAttribute('aria-expanded', 'false');
  if (tagSearchInput && tagSearchInput.value) {
    tagSearchInput.value = '';
    renderTagGrid(availableCategories, addCategory.value, selectCategory);
  }
}

function selectCategory(cat: string) {
  addCategory.value = cat;
  selectedCategoryLabel.textContent = cat;
  renderTagGrid(availableCategories, cat, selectCategory);
  closeTagDropdown();
}

// Search Operations
function openSearch() {
  searchNav.classList.add('open');
  setTimeout(() => {
    searchInput.focus();
    searchInput.select();
  }, 50);
}

function closeSearch() {
  if (searchQuery) {
    searchQuery = '';
    searchInput.value = '';
    refreshView();
  }
  searchNav.classList.remove('open');
}

function refreshView() {
  renderCategories(availableCategories, currentCategory, allBookmarks.length, (cat) => {
    currentCategory = cat;
    refreshView();
  });
  renderBookmarks(allBookmarks, currentCategory, searchQuery, openAddModal);
}

// Data loading and Offline fallback
function handleOfflineFallback() {
  if (isOfflineSessionValid()) {
    hideAuthModal();
    setStatusState('offline');
    isOffline = true;
    loadOfflineData();
  } else {
    showAuthModal();
    authError.style.display = 'block';
    authError.textContent = 'Offline session expired. Reconnect to phone to refresh.';
  }
}

function loadOfflineData() {
  allBookmarks = getOfflineBookmarks();
  availableCategories = getOfflineCategories();

  const queue = getOfflineQueue();
  queue.forEach((qItem) => {
    const qKey = normalizeUrlForComparison(qItem.url);
    if (!allBookmarks.some((b) => normalizeUrlForComparison(b.url) === qKey)) {
      allBookmarks.unshift({
        id: 'offline_' + qItem.createdAtMs,
        title: qItem.url,
        url: qItem.url,
        domain: extractDomain(qItem.url),
        category: qItem.category,
        createdAtMs: qItem.createdAtMs,
        isPendingSync: true
      });
    }
  });

  allBookmarks = deduplicateBookmarks(allBookmarks);
  renderTagGrid(availableCategories, addCategory.value, selectCategory);
  refreshView();
}

async function loadData() {
  const token = getToken();
  if (!token) {
    showAuthModal();
    return;
  }

  try {
    const [bms, cats] = await Promise.all([
      fetchBookmarks(token),
      fetchCategories(token)
    ]);

    allBookmarks = bms;
    availableCategories = cats;

    await saveOfflineBookmarks(allBookmarks);
    saveOfflineCategories(availableCategories);

    // Merge pending offline queue without duplicates
    const queue = getOfflineQueue();
    queue.forEach((qItem) => {
      const qKey = normalizeUrlForComparison(qItem.url);
      if (!allBookmarks.some((b) => normalizeUrlForComparison(b.url) === qKey)) {
        allBookmarks.unshift({
          id: 'offline_' + qItem.createdAtMs,
          title: qItem.url,
          url: qItem.url,
          domain: extractDomain(qItem.url),
          category: qItem.category,
          createdAtMs: qItem.createdAtMs,
          isPendingSync: true
        });
      }
    });

    allBookmarks = deduplicateBookmarks(allBookmarks);
    renderTagGrid(availableCategories, addCategory.value, selectCategory);
    refreshView();
  } catch (err: any) {
    if (err && (err.message?.includes('401') || err.message?.includes('403'))) {
      setToken(null);
      showAuthModal();
      authError.style.display = 'block';
      authError.textContent = 'Session expired on phone. Please enter the 6-digit PIN.';
      return;
    }
    handleOfflineFallback();
    throw err;
  }
}

// Authentication
async function submitPin(pin: string) {
  const clean = (pin || '').trim();
  if (clean.length !== 6 || !/^\d{6}$/.test(clean)) return;

  btnConnect.textContent = 'Connecting...';
  authError.style.display = 'none';

  const isLocalOffline = (typeof navigator !== 'undefined' && !navigator.onLine) || (typeof window !== 'undefined' && window.location.protocol === 'file:');

  if (!isLocalOffline) {
    try {
      const data = await authenticateWithPin(clean);
      if (data.success && data.token) {
        setActivePin(clean);
        setToken(data.token);
        updateLastConnected(data.offlineExpiryHours);
        setStatusState('online');
        isOffline = false;
        hideAuthModal();
        showToast('Connected to WebStack on Android');

        try {
          await flushOfflineQueue((count) => {
            showToast(`Synced ${count} offline bookmark${count > 1 ? 's' : ''} to phone!`);
          });
          await loadData();
        } catch (e) {}
        return;
      } else {
        authError.style.display = 'block';
        authError.textContent = data.error || 'Invalid PIN. Check your phone screen.';
        return;
      }
    } catch (networkErr) {
      // Fall through to offline vault unlock
    }
  }

  // Phone server offline or unreachable: attempt offline vault unlock with PIN
  let rawBms = getLocalItem('webstack_offline_bookmarks');
  if (!rawBms) {
    const seed = (typeof window !== 'undefined' ? (window as any).INITIAL_WEBSTACK_DATA : undefined);
    if (seed && seed.offlineBookmarks) {
      rawBms = typeof seed.offlineBookmarks === 'string' ? seed.offlineBookmarks : JSON.stringify(seed.offlineBookmarks);
    }
  }
  let vaultDecrypted = false;

  if (rawBms) {
    try {
      const decrypted = await decryptPayload<Bookmark[]>(rawBms, clean);
      if (decrypted) {
        await unlockWithPin(clean);
        vaultDecrypted = true;
      }
    } catch (decryptErr: any) {
      if (decryptErr && decryptErr.message === 'DECRYPT_FAILED') {
        authError.style.display = 'block';
        authError.textContent = 'Incorrect PIN. Unable to decrypt offline bookmarks.';
        return;
      }
    }
  }

  if (isOfflineSessionValid() && (vaultDecrypted || rawBms)) {
    setActivePin(clean);
    hideAuthModal();
    setStatusState('offline');
    isOffline = true;
    loadOfflineData();
    showToast('Unlocked offline vault');
  } else if (rawBms && !isOfflineSessionValid()) {
    authError.style.display = 'block';
    authError.textContent = 'Offline session expired. Reconnect to phone to refresh.';
  } else {
    authError.style.display = 'block';
    authError.textContent = `Cannot reach phone at ${getApiBase()}. Ensure phone companion is running and on the same Wi-Fi.`;
  }
  btnConnect.textContent = 'Connect to Phone';
}

async function checkAuth() {
  const params = new URLSearchParams(window.location.search);
  const urlPin = params.get('pin');
  if (urlPin && urlPin.length === 6) {
    pinInput.value = urlPin;
    await submitPin(urlPin);
    window.history.replaceState({}, document.title, window.location.pathname);
    return;
  }

  const savedPin = getActivePin();
  if (savedPin) {
    await unlockWithPin(savedPin);
  }

  // Instant optimistic offline render (0ms delay)
  const cachedBms = getOfflineBookmarks();
  if (cachedBms.length > 0) {
    hideAuthModal();
    setStatusState('offline');
    isOffline = true;
    allBookmarks = cachedBms;
    availableCategories = getOfflineCategories();
    renderTagGrid(availableCategories, addCategory.value, selectCategory);
    refreshView();
  }

  const token = getToken();
  if (!token) {
    if (cachedBms.length === 0 && !isOfflineSessionValid()) {
      showAuthModal();
    }
    return;
  }

  try {
    const statusData = await checkServerStatus(token);
    if (statusData.authenticated === false) {
      setToken(null);
      showAuthModal();
      authError.style.display = 'block';
      authError.textContent = 'Session expired on phone. Please enter the 6-digit PIN.';
      return;
    }
    updateLastConnected(statusData.offlineExpiryHours);
    await loadData();
    setStatusState('online');
    isOffline = false;
    hideAuthModal();
    await flushOfflineQueue();
  } catch (err: any) {
    if (err && (err.message?.includes('401') || err.message?.includes('403'))) {
      setToken(null);
      showAuthModal();
      authError.style.display = 'block';
      authError.textContent = 'Session expired on phone. Please enter the 6-digit PIN.';
      return;
    }
    handleOfflineFallback();
  }
}

async function disconnect() {
  const token = getToken();
  if (token) {
    await disconnectServer(token);
  }
  clearSession();
  allBookmarks = [];
  refreshView();
  showAuthModal();
  showToast('Disconnected from WebStack');
}


// Event Bindings
btnConnect.addEventListener('click', () => submitPin(pinInput.value));
pinInput.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') submitPin(pinInput.value);
});
pinInput.addEventListener('input', () => {
  const val = pinInput.value.trim();
  if (val.length === 6 && /^\d{6}$/.test(val)) {
    submitPin(val);
  }
});

// Auth Server Settings Edit Controls
if (btnToggleServerEdit && authServerEditBox) {
  btnToggleServerEdit.addEventListener('click', () => {
    authServerEditBox.classList.toggle('hidden');
    if (!authServerEditBox.classList.contains('hidden') && authServerUrlInput) {
      authServerUrlInput.focus();
      authServerUrlInput.select();
    }
  });
}

if (btnSaveServerUrl && authServerUrlInput && authServerEditBox) {
  btnSaveServerUrl.addEventListener('click', () => {
    const newUrl = authServerUrlInput.value.trim();
    if (newUrl) {
      const saved = setApiBase(newUrl);
      updateAuthServerDisplay();
      authServerEditBox.classList.add('hidden');
      showToast(`Phone server set to ${saved}`);
    }
  });
}

if (authServerUrlInput) {
  authServerUrlInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      const newUrl = authServerUrlInput.value.trim();
      if (newUrl) {
        const saved = setApiBase(newUrl);
        updateAuthServerDisplay();
        if (authServerEditBox) authServerEditBox.classList.add('hidden');
        showToast(`Phone server set to ${saved}`);
      }
    }
  });
}

btnDisconnect.addEventListener('click', disconnect);

btnOpenAddModal.addEventListener('click', openAddModal);
btnCloseAddModal.addEventListener('click', closeAddModal);
btnCancelAdd.addEventListener('click', closeAddModal);

addModal.addEventListener('click', (e) => {
  if (e.target === addModal) closeAddModal();
});

btnOpenShortcutsModal.addEventListener('click', openShortcutsModal);
btnCloseShortcutsModal.addEventListener('click', closeShortcutsModal);
btnGotItShortcuts.addEventListener('click', closeShortcutsModal);

shortcutsModal.addEventListener('click', (e) => {
  if (e.target === shortcutsModal) closeShortcutsModal();
});

btnOpenOfflineModal.addEventListener('click', openOfflineModal);
btnCloseOfflineModal.addEventListener('click', closeOfflineModal);
btnGotItOffline.addEventListener('click', closeOfflineModal);

offlineModal.addEventListener('click', (e) => {
  if (e.target === offlineModal) closeOfflineModal();
});

if (btnDownloadDesktop) {
  btnDownloadDesktop.addEventListener('click', () => {
    window.open('https://webstack7.vercel.app/download', '_blank');
  });
}

tagDropdownTrigger.addEventListener('click', (e) => {
  e.stopPropagation();
  toggleTagDropdown();
});

tagSearchInput.addEventListener('input', (e: Event) => {
  const q = (e.target as HTMLInputElement).value.toLowerCase().trim();
  const filtered = availableCategories.filter((c) => c.toLowerCase().includes(q));
  renderTagGrid(filtered, addCategory.value, selectCategory);
});

tagSearchInput.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') {
    e.stopPropagation();
    closeTagDropdown();
  }
});

tagDropdownMenu.addEventListener('click', (e) => {
  e.stopPropagation();
});

btnSearchToggle.addEventListener('click', (e) => {
  e.stopPropagation();
  openSearch();
});

btnClearSearch.addEventListener('click', (e) => {
  e.stopPropagation();
  if (searchInput.value) {
    searchInput.value = '';
    searchQuery = '';
    refreshView();
    searchInput.focus();
  } else {
    closeSearch();
  }
});

searchInput.addEventListener('input', (e: Event) => {
  searchQuery = (e.target as HTMLInputElement).value;
  renderBookmarks(allBookmarks, currentCategory, searchQuery, openAddModal);
});

// Click outside to collapse search bar smoothly
document.addEventListener('click', (e) => {
  const target = e.target as HTMLElement;
  if (searchNav && searchNav.classList.contains('open') && !searchNav.contains(target)) {
    closeSearch();
  }
});

// Bookmark Submission
addBookmarkForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const rawUrl = addUrl.value.trim();
  const cat = addCategory.value || 'Personal';
  if (!rawUrl) return;

  const normalizedUrl = normalizeUrl(rawUrl);
  const token = getToken();

  btnSaveBookmark.disabled = true;
  btnSaveBookmark.innerHTML = '<span>Saving...</span>';

  if (isOffline || !token) {
    const offlineItem = await saveBookmarkLocally(normalizedUrl, cat);
    allBookmarks.unshift(offlineItem);
    allBookmarks = deduplicateBookmarks(allBookmarks);
    closeAddModal();
    refreshView();
    btnSaveBookmark.disabled = false;
    btnSaveBookmark.innerHTML = '<span>Save Bookmark</span>';
    showToast('Saved offline. Will sync when phone connects.');
    return;
  }

  try {
    const res = await postBookmark(normalizedUrl, cat, token);
    if (res.success) {
      await removeOfflineQueueItem(normalizedUrl);
      const newBm: Bookmark = {
        id: res.id ?? ('bm_' + Date.now()),
        title: res.title || normalizedUrl,
        url: res.url || normalizedUrl,
        domain: res.domain || extractDomain(res.url || normalizedUrl),
        category: res.category || cat,
        createdAtMs: res.createdAtMs || Date.now(),
        isPendingSync: false
      };
      allBookmarks.unshift(newBm);
      allBookmarks = deduplicateBookmarks(allBookmarks);
      closeAddModal();
      refreshView();
      showToast('Bookmark synced to phone!');

      loadData().catch(() => {});
    } else if (res.error && res.error.toLowerCase().includes('already in your stack')) {
      await removeOfflineQueueItem(normalizedUrl);
      closeAddModal();
      showToast(res.error);
    } else if (res.error && res.error.toLowerCase().includes('unauthorized')) {
      setToken(null);
      showAuthModal();
      authError.style.display = 'block';
      authError.textContent = 'Session expired on phone. Please enter the 6-digit PIN.';
    } else {
      showToast(res.error || 'Failed to sync bookmark.');
    }
  } catch (err) {
    setStatusState('offline');
    isOffline = true;
    const offlineItem = await saveBookmarkLocally(normalizedUrl, cat);
    allBookmarks.unshift(offlineItem);
    allBookmarks = deduplicateBookmarks(allBookmarks);
    closeAddModal();
    refreshView();
    showToast('Saved offline. Will sync when phone connects.');
  } finally {
    btnSaveBookmark.disabled = false;
    btnSaveBookmark.innerHTML = '<span>Save Bookmark</span>';
  }
});

// Keyboard Navigation
document.addEventListener('keydown', (e) => {
  const activeEl = document.activeElement;
  const isInputActive =
    activeEl && ['INPUT', 'SELECT', 'TEXTAREA'].includes(activeEl.tagName);

  if (e.key === 'Escape') {
    if (customCategoryWrapper.classList.contains('open')) {
      closeTagDropdown();
    } else if (!shortcutsModal.classList.contains('hidden')) {
      closeShortcutsModal();
    } else if (!offlineModal.classList.contains('hidden')) {
      closeOfflineModal();
    } else if (!addModal.classList.contains('hidden')) {
      closeAddModal();
    } else if (searchNav.classList.contains('open')) {
      closeSearch();
    }
  } else if (!isInputActive) {
    if (e.key === '?' || (e.shiftKey && e.key === '/')) {
      e.preventDefault();
      if (shortcutsModal.classList.contains('hidden')) {
        openShortcutsModal();
      } else {
        closeShortcutsModal();
      }
    } else if (e.key === 'n' || e.key === 'N') {
      e.preventDefault();
      openAddModal();
    } else if (e.key === '/') {
      e.preventDefault();
      openSearch();
    } else if (e.shiftKey && (e.key === 'D' || e.key === 'd')) {
      e.preventDefault();
      disconnect();
    } else if (e.key >= '1' && e.key <= '9') {
      const index = parseInt(e.key, 10) - 1;
      const pills = document.querySelectorAll('#categoryPills .chip') as NodeListOf<HTMLButtonElement>;
      if (pills[index]) {
        e.preventDefault();
        pills[index].click();
      }
    }
  }
});

// Periodic Heartbeat & Sync Loop (every 8 seconds)
setInterval(async () => {
  const token = getToken();
  if (!token || !authModal.classList.contains('hidden')) return;

  try {
    const statusData = await checkServerStatus(token);
    if (statusData.authenticated === false) {
      setToken(null);
      showAuthModal();
      authError.style.display = 'block';
      authError.textContent = 'Session expired on phone. Please enter the 6-digit PIN.';
      return;
    }
    updateLastConnected(statusData.offlineExpiryHours);
    if (isOffline) {
      await loadData();
      setStatusState('online');
      isOffline = false;
      showToast('Reconnected to phone');
      await flushOfflineQueue();
    } else {
      const queue = getOfflineQueue();
      if (queue.length > 0) {
        await flushOfflineQueue();
      }
    }
  } catch (e: any) {
    if (e && (e.message?.includes('401') || e.message?.includes('403'))) {
      setToken(null);
      showAuthModal();
      authError.style.display = 'block';
      authError.textContent = 'Session expired on phone. Please enter the 6-digit PIN.';
      return;
    }
    if (!isOffline) {
      setStatusState('offline');
      isOffline = true;
    }
    if (!isOfflineSessionValid()) {
      showAuthModal();
      authError.style.display = 'block';
      authError.textContent = 'Offline session expired. Reconnect to phone to refresh.';
    }
  }
}, 8000);

checkAuth();

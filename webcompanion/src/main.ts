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
  clearSession
} from './storage';
import {
  apiUrl,
  checkServerStatus,
  authenticateWithPin,
  fetchBookmarks,
  fetchCategories,
  postBookmark,
  disconnectServer
} from './api';
import { flushOfflineQueue, saveBookmarkLocally } from './sync';
import { initPwa, promptPwaInstall } from './pwa';
import {
  showToast,
  setStatusState,
  renderCategories,
  renderBookmarks,
  renderTagGrid,
  escapeHtml
} from './ui';
import { decryptPayload } from './crypto';

// Global bridge for inline HTML onclick handlers
(window as any).webstackShowToast = showToast;

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
const btnDownloadStandalone = document.getElementById('btnDownloadStandalone') as HTMLButtonElement;
const btnTriggerPwaInstall = document.getElementById('btnTriggerPwaInstall') as HTMLButtonElement;
const btnCopyChromeFlag = document.getElementById('btnCopyChromeFlag') as HTMLButtonElement;
const offlineServerUrlInput = document.getElementById('offlineServerUrlInput') as HTMLInputElement;
const chromeFlagHostDisplay = document.getElementById('chromeFlagHostDisplay') as HTMLSpanElement;

const searchNav = document.getElementById('searchNav') as HTMLDivElement;
const btnSearchToggle = document.getElementById('btnSearchToggle') as HTMLButtonElement;
const btnClearSearch = document.getElementById('btnClearSearch') as HTMLButtonElement;
const searchInput = document.getElementById('searchInput') as HTMLInputElement;

// Modal Controls
function showAuthModal() {
  authModal.classList.remove('hidden');
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
  if (offlineServerUrlInput) {
    offlineServerUrlInput.value = apiUrl('/');
  }
  if (chromeFlagHostDisplay) {
    chromeFlagHostDisplay.textContent = apiUrl('/');
  }
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
    if (!allBookmarks.some((b) => b.url === qItem.url)) {
      allBookmarks.unshift({
        id: 'offline_' + qItem.createdAtMs,
        title: qItem.url,
        url: qItem.url,
        domain: qItem.url.replace(/^https?:\/\//, '').split('/')[0],
        category: qItem.category,
        createdAtMs: qItem.createdAtMs,
        isPendingSync: true
      });
    }
  });

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

    // Merge pending offline queue
    const queue = getOfflineQueue();
    queue.forEach((qItem) => {
      if (!allBookmarks.some((b) => b.url === qItem.url)) {
        allBookmarks.unshift({
          id: 'offline_' + qItem.createdAtMs,
          title: qItem.url,
          url: qItem.url,
          domain: qItem.url.replace(/^https?:\/\//, '').split('/')[0],
          category: qItem.category,
          createdAtMs: qItem.createdAtMs,
          isPendingSync: true
        });
      }
    });

    renderTagGrid(availableCategories, addCategory.value, selectCategory);
    refreshView();
  } catch (err) {
    handleOfflineFallback();
  }
}

// Authentication
async function submitPin(pin: string) {
  const clean = (pin || '').trim();
  if (clean.length !== 6 || !/^\d{6}$/.test(clean)) return;

  btnConnect.textContent = 'Verifying...';
  authError.style.display = 'none';

  try {
    const data = await authenticateWithPin(clean);
    if (data.success && data.token) {
      setActivePin(clean);
      setToken(data.token);
      updateLastConnected(data.offlineExpiryHours);
      setStatusState('online');
      isOffline = false;
      hideAuthModal();
      await flushOfflineQueue((count) => {
        showToast(`Synced ${count} offline bookmark${count > 1 ? 's' : ''} to phone!`);
      });
      await loadData();
      showToast('Connected to WebStack on Android');
      return;
    } else {
      authError.style.display = 'block';
      authError.textContent = data.error || 'Invalid PIN. Check your phone screen.';
      return;
    }
  } catch (err) {
    // Phone server offline: attempt offline vault unlock with PIN
    const rawBms = localStorage.getItem('webstack_offline_bookmarks');
    if (rawBms) {
      try {
        const decrypted = await decryptPayload<Bookmark[]>(rawBms, clean);
        if (decrypted) {
          await unlockWithPin(clean);
        }
      } catch (decryptErr: any) {
        if (decryptErr && decryptErr.message === 'DECRYPT_FAILED') {
          authError.style.display = 'block';
          authError.textContent = 'Incorrect PIN. Unable to decrypt offline bookmarks.';
          return;
        }
      }
    }

    if (isOfflineSessionValid()) {
      setActivePin(clean);
      hideAuthModal();
      setStatusState('offline');
      isOffline = true;
      loadOfflineData();
      showToast('Unlocked offline vault');
    } else {
      authError.style.display = 'block';
      authError.textContent = 'Offline session expired. Reconnect to phone to refresh.';
    }
  } finally {
    btnConnect.textContent = 'Connect to Phone';
  }
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

  const token = getToken();
  if (!token) {
    showAuthModal();
    return;
  }

  try {
    const statusData = await checkServerStatus(token);
    updateLastConnected(statusData.offlineExpiryHours);
    setStatusState('online');
    isOffline = false;
    hideAuthModal();
    await flushOfflineQueue();
    await loadData();
  } catch (err) {
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

// 1-Click Standalone Desktop App Export (.html)
function downloadStandaloneHtmlApp() {
  let docHtml = document.documentElement.outerHTML;

  const serverUrl = (offlineServerUrlInput ? offlineServerUrlInput.value.trim() : '') || apiUrl('/');
  const originInjection = `<script>window.DEFAULT_PHONE_ORIGIN = ${JSON.stringify(serverUrl)};</script>`;

  if (docHtml.includes('</head>')) {
    docHtml = docHtml.replace('</head>', originInjection + '</head>');
  } else {
    docHtml = originInjection + docHtml;
  }

  const blob = new Blob([docHtml], { type: 'text/html;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'WebStack.html';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);

  showToast('Downloaded standalone WebStack app!');
  closeOfflineModal();
}

// Event Bindings
btnConnect.addEventListener('click', () => submitPin(pinInput.value));
pinInput.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') submitPin(pinInput.value);
});

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

if (btnDownloadStandalone) {
  btnDownloadStandalone.addEventListener('click', downloadStandaloneHtmlApp);
}

if (btnTriggerPwaInstall) {
  btnTriggerPwaInstall.addEventListener('click', async () => {
    const installed = await promptPwaInstall();
    if (installed) {
      showToast('WebStack installed successfully!');
      closeOfflineModal();
    } else {
      showToast('Use browser menu (⋮) -> "Install WebStack"');
    }
  });
}

if (btnCopyChromeFlag) {
  btnCopyChromeFlag.addEventListener('click', () => {
    navigator.clipboard.writeText('chrome://flags/#unsafely-treat-insecure-origin-as-secure');
    showToast('Copied flag URL!');
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

// Bookmark Submission
addBookmarkForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const rawUrl = addUrl.value.trim();
  const cat = addCategory.value || 'Personal';
  if (!rawUrl) return;

  const token = getToken();
  btnSaveBookmark.disabled = true;
  btnSaveBookmark.innerHTML = '<span>Saving...</span>';

  if (isOffline || !token) {
    const offlineItem = await saveBookmarkLocally(rawUrl, cat);
    allBookmarks.unshift(offlineItem);
    closeAddModal();
    refreshView();
    btnSaveBookmark.disabled = false;
    btnSaveBookmark.innerHTML = '<span>Save Bookmark</span>';
    showToast('Saved offline. Will sync when phone connects.');
    return;
  }

  try {
    const res = await postBookmark(rawUrl, cat, token);
    if (res.success) {
      closeAddModal();
      await loadData();
      showToast('Bookmark synced to phone!');
    } else {
      showToast(res.error || 'Failed to sync bookmark.');
    }
  } catch (err) {
    setStatusState('offline');
    isOffline = true;
    const offlineItem = await saveBookmarkLocally(rawUrl, cat);
    allBookmarks.unshift(offlineItem);
    closeAddModal();
    refreshView();
    showToast('Network error: saved offline. Will sync when phone connects.');
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
    updateLastConnected(statusData.offlineExpiryHours);
    if (isOffline) {
      setStatusState('online');
      isOffline = false;
      showToast('Reconnected to phone');
      await flushOfflineQueue();
      await loadData();
    } else {
      const queue = getOfflineQueue();
      if (queue.length > 0) {
        await flushOfflineQueue();
      }
    }
  } catch (e) {
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

// Initialize PWA and App
initPwa(() => {
  // PWA is installable on PC
  if (btnTriggerPwaInstall) {
    btnTriggerPwaInstall.classList.add('pwa-ready');
  }
});

checkAuth();

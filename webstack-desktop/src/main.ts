import {
  authenticatePin,
  discoverPhoneServer,
  fetchBookmarks,
  fetchCategories,
  fetchServerStatus,
  addBookmark,
  getAuthToken,
  setAuthToken,
  getServerBase,
  setServerBase,
  openUrlInBrowser,
} from './api';
import type { Bookmark } from './types';

// State
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
const authDiscoveryStatus = document.getElementById('authDiscoveryStatus') as HTMLParagraphElement;
const currentServerDisplay = document.getElementById('currentServerDisplay') as HTMLElement;
const btnEditServerInAuth = document.getElementById('btnEditServerInAuth') as HTMLButtonElement;

const statusBadge = document.getElementById('statusBadge') as HTMLSpanElement;
const searchInput = document.getElementById('searchInput') as HTMLInputElement;
const btnOpenAddModal = document.getElementById('btnOpenAddModal') as HTMLButtonElement;
const btnDisconnect = document.getElementById('btnDisconnect') as HTMLButtonElement;
const btnSettings = document.getElementById('btnSettings') as HTMLButtonElement;

const categoryPills = document.getElementById('categoryPills') as HTMLDivElement;
const totalCount = document.getElementById('totalCount') as HTMLSpanElement;
const bookmarksGrid = document.getElementById('bookmarksGrid') as HTMLDivElement;

const addModal = document.getElementById('addModal') as HTMLDivElement;
const btnCloseAddModal = document.getElementById('btnCloseAddModal') as HTMLButtonElement;
const btnCancelAdd = document.getElementById('btnCancelAdd') as HTMLButtonElement;
const addBookmarkForm = document.getElementById('addBookmarkForm') as HTMLFormElement;
const addUrl = document.getElementById('addUrl') as HTMLInputElement;
const addCategory = document.getElementById('addCategory') as HTMLInputElement;
const btnSaveBookmark = document.getElementById('btnSaveBookmark') as HTMLButtonElement;

const settingsModal = document.getElementById('settingsModal') as HTMLDivElement;
const btnCloseSettingsModal = document.getElementById('btnCloseSettingsModal') as HTMLButtonElement;
const serverUrlInput = document.getElementById('serverUrlInput') as HTMLInputElement;
const btnAutoScanServer = document.getElementById('btnAutoScanServer') as HTMLButtonElement;
const btnSaveSettings = document.getElementById('btnSaveSettings') as HTMLButtonElement;

const toast = document.getElementById('toast') as HTMLDivElement;

function showToast(msg: string) {
  if (!toast) return;
  toast.textContent = msg;
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), 2600);
}

function updateServerLabel() {
  if (currentServerDisplay) currentServerDisplay.textContent = getServerBase();
  if (serverUrlInput) serverUrlInput.value = getServerBase();
}

function setBadgeState(state: 'online' | 'offline' | 'searching') {
  if (!statusBadge) return;
  statusBadge.className = 'badge-live ' + (state === 'online' ? '' : state);
  statusBadge.textContent = state === 'online' ? 'Live Sync' : state === 'offline' ? 'Offline Vault' : 'Searching Wi-Fi';
}

// Render Category Pills
function renderCategoryPills() {
  const currentTotal = allBookmarks.length;
  if (totalCount) totalCount.textContent = currentTotal.toString();

  const cats = ['ALL', ...availableCategories];
  categoryPills.innerHTML = cats
    .map((cat) => {
      const active = currentCategory === cat ? ' active' : '';
      const count = cat === 'ALL' ? currentTotal : allBookmarks.filter((b) => b.category.toLowerCase() === cat.toLowerCase()).length;
      return `<button class="chip${active}" data-cat="${cat}">${cat === 'ALL' ? 'All' : cat} (${count})</button>`;
    })
    .join('');

  categoryPills.querySelectorAll('.chip').forEach((btn) => {
    btn.addEventListener('click', () => {
      const cat = btn.getAttribute('data-cat') || 'ALL';
      currentCategory = cat;
      renderCategoryPills();
      renderBookmarks();
    });
  });
}

function getFaviconUrl(url: string): string {
  try {
    const domain = new URL(url).hostname;
    return `https://www.google.com/s2/favicons?domain=${domain}&sz=64`;
  } catch {
    return '';
  }
}

function escapeHtml(text: string): string {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

// Render Bookmarks Grid
function renderBookmarks() {
  let filtered = allBookmarks;

  if (currentCategory !== 'ALL') {
    filtered = filtered.filter((b) => b.category.toLowerCase() === currentCategory.toLowerCase());
  }

  if (searchQuery.trim()) {
    const q = searchQuery.toLowerCase().trim();
    filtered = filtered.filter(
      (b) =>
        b.title.toLowerCase().includes(q) ||
        b.url.toLowerCase().includes(q) ||
        b.domain.toLowerCase().includes(q) ||
        b.category.toLowerCase().includes(q)
    );
  }

  if (filtered.length === 0) {
    bookmarksGrid.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 60px 20px; background: var(--surface); border: 1px dashed var(--border); border-radius: var(--radius); color: var(--text-muted);">
        <p style="font-size: 15px; font-weight: 600; color: var(--text); margin-bottom: 4px;">No bookmarks found</p>
        <p style="font-size: 13px;">${searchQuery ? 'Try adjusting your search terms' : 'Add bookmarks from your phone or use (N) here'}</p>
      </div>
    `;
    return;
  }

  bookmarksGrid.innerHTML = filtered
    .map((b) => {
      const favicon = getFaviconUrl(b.url);
      return `
        <div class="bookmark-card" data-url="${escapeHtml(b.url)}">
          <div class="card-top">
            <div class="favicon">
              ${favicon ? `<img src="${favicon}" alt="" onerror="this.style.display='none'">` : ''}
            </div>
            <div class="card-info">
              <div class="card-title" title="${escapeHtml(b.title)}">${escapeHtml(b.title)}</div>
              <div class="card-domain">${escapeHtml(b.domain)}</div>
            </div>
          </div>
          <div class="card-bottom">
            <span class="card-tag">${escapeHtml(b.category)}</span>
            <div class="card-actions">
              <button class="action-btn btn-copy" data-url="${escapeHtml(b.url)}" title="Copy URL">Copy</button>
              <button class="action-btn btn-open" data-url="${escapeHtml(b.url)}" title="Open in Browser">Open ↗</button>
            </div>
          </div>
        </div>
      `;
    })
    .join('');

  // Event handlers for cards
  bookmarksGrid.querySelectorAll('.bookmark-card').forEach((card) => {
    card.addEventListener('click', (e) => {
      const target = e.target as HTMLElement;
      if (target.closest('.action-btn')) return;
      const url = card.getAttribute('data-url');
      if (url) openUrlInBrowser(url);
    });
  });

  bookmarksGrid.querySelectorAll('.btn-copy').forEach((btn) => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const url = btn.getAttribute('data-url');
      if (url) {
        navigator.clipboard.writeText(url);
        showToast('Copied URL to clipboard');
      }
    });
  });

  bookmarksGrid.querySelectorAll('.btn-open').forEach((btn) => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const url = btn.getAttribute('data-url');
      if (url) openUrlInBrowser(url);
    });
  });
}

// Load data from phone or offline cache
async function loadData() {
  try {
    const [bms, cats] = await Promise.all([fetchBookmarks(), fetchCategories()]);
    allBookmarks = bms;
    availableCategories = cats;
    localStorage.setItem('webstack_desktop_cached_bookmarks', JSON.stringify(bms));
    localStorage.setItem('webstack_desktop_cached_categories', JSON.stringify(cats));
    setBadgeState('online');
    isOffline = false;
  } catch (err) {
    console.warn('Network sync failed, loading offline cache:', err);
    isOffline = true;
    setBadgeState('offline');
    const cached = localStorage.getItem('webstack_desktop_cached_bookmarks');
    if (cached) {
      try {
        allBookmarks = JSON.parse(cached);
      } catch {}
    }
    const cachedCats = localStorage.getItem('webstack_desktop_cached_categories');
    if (cachedCats) {
      try {
        availableCategories = JSON.parse(cachedCats);
      } catch {}
    }
  }

  renderCategoryPills();
  renderBookmarks();
}

// Submit PIN
async function submitPin(pin: string) {
  if (pin.length !== 6 || !/^\d{6}$/.test(pin)) {
    authError.style.display = 'block';
    authError.textContent = 'Please enter a 6-digit numeric PIN.';
    return;
  }

  btnConnect.disabled = true;
  btnConnect.textContent = 'Connecting...';
  authError.style.display = 'none';

  try {
    const res = await authenticatePin(pin);
    if (res.success && res.token) {
      authModal.classList.add('hidden');
      showToast('Connected to phone!');
      await loadData();
    } else {
      authError.style.display = 'block';
      authError.textContent = res.error || 'Invalid PIN. Check your phone screen.';
    }
  } catch (err: any) {
    authError.style.display = 'block';
    authError.textContent = `Unable to reach phone at ${getServerBase()}. Make sure phone is on same Wi-Fi.`;
  } finally {
    btnConnect.disabled = false;
    btnConnect.textContent = 'Connect to Phone';
  }
}

// Setup & Initialization
async function init() {
  updateServerLabel();

  // Try Auto-Discovery in background
  discoverPhoneServer()
    .then((server) => {
      if (server) {
        updateServerLabel();
        if (authDiscoveryStatus) {
          authDiscoveryStatus.textContent = `Found phone companion at ${server.url}!`;
        }
      }
    })
    .catch(() => {});

  const token = getAuthToken();
  if (token) {
    try {
      const status = await fetchServerStatus();
      if (status.authenticated) {
        authModal.classList.add('hidden');
        await loadData();
        return;
      }
    } catch {
      // Offline fallback
      const cached = localStorage.getItem('webstack_desktop_cached_bookmarks');
      if (cached) {
        authModal.classList.add('hidden');
        loadData();
        return;
      }
    }
  }

  // Not authenticated or no cache
  authModal.classList.remove('hidden');
  pinInput.value = '';
  pinInput.focus();
}

// Periodic Background Sync (every 10s)
setInterval(async () => {
  const token = getAuthToken();
  if (!token || !authModal.classList.contains('hidden')) return;

  try {
    const status = await fetchServerStatus();
    if (status.authenticated) {
      if (isOffline) {
        showToast('Reconnected to phone!');
        await loadData();
      }
      setBadgeState('online');
      isOffline = false;
    }
  } catch {
    if (!isOffline) {
      setBadgeState('offline');
      isOffline = true;
    }
  }
}, 10000);

// Event Listeners
btnConnect.addEventListener('click', () => submitPin(pinInput.value.trim()));
pinInput.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') submitPin(pinInput.value.trim());
});
pinInput.addEventListener('input', () => {
  const val = pinInput.value.trim();
  if (val.length === 6) submitPin(val);
});

btnDisconnect.addEventListener('click', () => {
  setAuthToken(null);
  allBookmarks = [];
  renderBookmarks();
  authModal.classList.remove('hidden');
  pinInput.value = '';
  showToast('Disconnected from phone');
});

btnOpenAddModal.addEventListener('click', () => {
  addModal.classList.remove('hidden');
  addUrl.value = '';
  setTimeout(() => addUrl.focus(), 60);
});
btnCloseAddModal.addEventListener('click', () => addModal.classList.add('hidden'));
btnCancelAdd.addEventListener('click', () => addModal.classList.add('hidden'));

addBookmarkForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const url = addUrl.value.trim();
  const cat = addCategory.value.trim() || 'Personal';
  if (!url) return;

  btnSaveBookmark.disabled = true;
  btnSaveBookmark.textContent = 'Saving...';

  try {
    const res = await addBookmark(url, cat);
    if (res.success) {
      showToast('Bookmark synced to phone!');
      addModal.classList.add('hidden');
      await loadData();
    } else {
      showToast(res.error || 'Failed to save');
    }
  } catch {
    showToast('Cannot connect to phone. Try again.');
  } finally {
    btnSaveBookmark.disabled = false;
    btnSaveBookmark.textContent = 'Save Bookmark';
  }
});

btnSettings.addEventListener('click', () => {
  serverUrlInput.value = getServerBase();
  settingsModal.classList.remove('hidden');
});
btnEditServerInAuth.addEventListener('click', () => {
  serverUrlInput.value = getServerBase();
  settingsModal.classList.remove('hidden');
});
btnCloseSettingsModal.addEventListener('click', () => settingsModal.classList.add('hidden'));
btnSaveSettings.addEventListener('click', () => {
  const val = serverUrlInput.value.trim();
  if (val) {
    setServerBase(val);
    updateServerLabel();
    settingsModal.classList.add('hidden');
    showToast(`Server address updated to ${getServerBase()}`);
  }
});

btnAutoScanServer.addEventListener('click', async () => {
  btnAutoScanServer.textContent = 'Scanning Wi-Fi...';
  const server = await discoverPhoneServer();
  if (server) {
    serverUrlInput.value = server.url;
    showToast(`Found phone at ${server.url}!`);
  } else {
    showToast('Could not auto-detect phone. Check IP on phone companion.');
  }
  btnAutoScanServer.textContent = 'Scan Wi-Fi for Phone (mDNS)';
});

// Search input
searchInput.addEventListener('input', (e) => {
  searchQuery = (e.target as HTMLInputElement).value;
  renderBookmarks();
});

// Keyboard shortcuts
document.addEventListener('keydown', (e) => {
  const active = document.activeElement;
  const isInput = active && ['INPUT', 'TEXTAREA'].includes(active.tagName);

  if (e.key === 'Escape') {
    addModal.classList.add('hidden');
    settingsModal.classList.add('hidden');
    if (searchQuery) {
      searchQuery = '';
      searchInput.value = '';
      renderBookmarks();
    }
  } else if (!isInput) {
    if (e.key === '/') {
      e.preventDefault();
      searchInput.focus();
    } else if (e.key === 'n' || e.key === 'N') {
      e.preventDefault();
      addModal.classList.remove('hidden');
      setTimeout(() => addUrl.focus(), 60);
    } else if (e.key >= '1' && e.key <= '9') {
      const idx = parseInt(e.key, 10) - 1;
      const chips = categoryPills.querySelectorAll<HTMLButtonElement>('.chip');
      if (chips[idx]) {
        e.preventDefault();
        chips[idx].click();
      }
    }
  }
});

init();

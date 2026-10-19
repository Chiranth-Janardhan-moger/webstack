import type { Bookmark } from './types';

export function escapeHtml(str: string): string {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

export function escapeJs(str: string): string {
  if (!str) return '';
  return String(str)
    .replace(/\\/g, '\\\\')
    .replace(/'/g, "\\'")
    .replace(/"/g, '\\"')
    .replace(/\n/g, '\\n')
    .replace(/\r/g, '\\r');
}

export function copyToClipboard(text: string): boolean {
  try {
    if (typeof navigator !== 'undefined' && navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text).catch(() => fallbackCopy(text));
      return true;
    }
  } catch (e) {}
  return fallbackCopy(text);
}

function fallbackCopy(text: string): boolean {
  try {
    const el = document.createElement('textarea');
    el.value = text;
    el.setAttribute('readonly', '');
    el.style.position = 'fixed';
    el.style.left = '-9999px';
    el.style.top = '-9999px';
    document.body.appendChild(el);
    el.select();
    const ok = document.execCommand('copy');
    document.body.removeChild(el);
    return ok;
  } catch (e) {
    return false;
  }
}

export function showToast(message: string): void {
  const toast = document.getElementById('toast');
  const toastMessage = document.getElementById('toastMessage');
  if (toast && toastMessage) {
    toastMessage.textContent = message;
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 2600);
  }
}

export function setStatusState(state: 'online' | 'offline' | 'syncing'): void {
  const statusBadge = document.getElementById('statusBadge');
  if (!statusBadge) return;
  if (state === 'online') {
    statusBadge.className = 'badge-live';
    statusBadge.textContent = 'Live Sync';
    statusBadge.title = 'Connected to phone. Real-time sync active.';
  } else if (state === 'offline') {
    statusBadge.className = 'badge-live offline';
    statusBadge.textContent = 'Offline Mode';
    statusBadge.title = 'Phone disconnected. Working offline; bookmarks will sync on reconnect.';
  } else if (state === 'syncing') {
    statusBadge.className = 'badge-live syncing';
    statusBadge.textContent = 'Syncing...';
    statusBadge.title = 'Syncing offline changes with phone...';
  }
}

export function renderCategories(
  categories: string[],
  activeCategory: string,
  totalBookmarksCount: number,
  onSelectCategory: (cat: string) => void
): void {
  const categoryPills = document.getElementById('categoryPills');
  if (!categoryPills) return;

  categoryPills.innerHTML = '';

  const allBtn = document.createElement('button');
  allBtn.className = 'chip' + (activeCategory === 'ALL' ? ' active' : '');
  allBtn.textContent = 'All (' + totalBookmarksCount + ')';
  allBtn.addEventListener('click', () => onSelectCategory('ALL'));
  categoryPills.appendChild(allBtn);

  categories.forEach((cat) => {
    const btn = document.createElement('button');
    btn.className = 'chip' + (activeCategory === cat ? ' active' : '');
    btn.textContent = cat;
    btn.addEventListener('click', () => onSelectCategory(cat));
    categoryPills.appendChild(btn);
  });
}

export function renderBookmarks(
  bookmarks: Bookmark[],
  activeCategory: string,
  searchQuery: string,
  onOpenAddModal: () => void
): void {
  const bookmarksGrid = document.getElementById('bookmarksGrid');
  if (!bookmarksGrid) return;

  const query = searchQuery.toLowerCase().trim();
  const filtered = bookmarks.filter((b) => {
    const matchesCategory =
      activeCategory === 'ALL' ||
      (b.category || 'Personal').toLowerCase() === activeCategory.toLowerCase();
    if (!matchesCategory) return false;

    if (!query) return true;
    const titleMatch = (b.title || '').toLowerCase().includes(query);
    const urlMatch = (b.url || '').toLowerCase().includes(query);
    const domainMatch = (b.domain || '').toLowerCase().includes(query);
    const catMatch = (b.category || '').toLowerCase().includes(query);
    return titleMatch || urlMatch || domainMatch || catMatch;
  });

  if (filtered.length === 0) {
    if (query) {
      bookmarksGrid.innerHTML = `
        <div class="empty-state">
          <div class="empty-title">No matching bookmarks</div>
          <p class="empty-sub">Try searching for a different keyword or domain.</p>
        </div>
      `;
    } else if (activeCategory !== 'ALL') {
      bookmarksGrid.innerHTML = `
        <div class="empty-state">
          <div class="empty-title">No bookmarks in ${escapeHtml(activeCategory)}</div>
          <p class="empty-sub">Add a bookmark to this category using the + button above.</p>
        </div>
      `;
    } else {
      bookmarksGrid.innerHTML = `
        <div class="empty-state">
          <div class="empty-title">No bookmarks in your stack</div>
          <p class="empty-sub">Sync bookmarks from WebStack Android or add one from PC.</p>
          <button class="btn-primary" id="btnEmptyAdd" style="margin-top: 14px;">+ Add First Bookmark</button>
        </div>
      `;
      const btn = document.getElementById('btnEmptyAdd');
      if (btn) btn.addEventListener('click', onOpenAddModal);
    }
    return;
  }

  bookmarksGrid.innerHTML = filtered
    .map((b) => {
      const displayDomain = b.domain || b.url.replace(/^https?:\/\//, '').split('/')[0];
      const initial = (displayDomain || 'W').charAt(0).toUpperCase();
      const faviconUrl = `https://www.google.com/s2/favicons?domain=${displayDomain}&sz=64`;

      return `
        <div class="bookmark-card ${b.isPendingSync ? 'card-pending-sync' : ''}" onclick="window.open('${escapeJs(b.url)}', '_blank')">
          <div class="card-top">
            <div class="card-icon-wrapper">
              <img src="${faviconUrl}" class="card-favicon" alt="" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
              <div class="card-initial-fallback" style="display: none;">${escapeHtml(initial)}</div>
            </div>
            <div class="card-meta">
              <div class="card-title" title="${escapeHtml(b.title || b.url)}">${escapeHtml(b.title || b.url)}</div>
              <div class="card-domain">${escapeHtml(displayDomain)}</div>
            </div>
          </div>

          <div class="card-bottom">
            <div style="display: flex; align-items: center; gap: 6px;">
              <span class="card-tag">${escapeHtml(b.category || 'Personal')}</span>
              ${b.isPendingSync ? '<span class="tag-pending-sync" title="Saved locally, will sync when phone connects">Pending Sync</span>' : ''}
            </div>
            <div class="card-actions">
              <button class="action-btn" onclick="event.stopPropagation(); window.webstackCopyLink('${escapeJs(b.url)}');" title="Copy link">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                  <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
                </svg>
              </button>
              <button class="action-btn" onclick="event.stopPropagation(); window.open('${escapeJs(b.url)}', '_blank')" title="Open in new tab">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
                  <polyline points="15 3 21 3 21 9"></polyline>
                  <line x1="10" y1="14" x2="21" y2="3"></line>
                </svg>
              </button>
            </div>
          </div>
        </div>
      `;
    })
    .join('');
}

export function renderTagGrid(
  cats: string[],
  selectedCategory: string,
  onSelectTag: (cat: string) => void
): void {
  const tagGridContainer = document.getElementById('tagGridContainer');
  if (!tagGridContainer) return;

  tagGridContainer.innerHTML = '';
  if (!cats || cats.length === 0) {
    tagGridContainer.innerHTML =
      '<div style="grid-column: 1 / -1; text-align: center; color: var(--text-muted); font-size: 12px; padding: 12px 0;">No matching tags</div>';
    return;
  }

  cats.forEach((cat) => {
    const isSelected = selectedCategory === cat;
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'tag-option-btn' + (isSelected ? ' selected' : '');
    btn.setAttribute('data-cat', cat);
    btn.setAttribute('role', 'option');
    btn.setAttribute('aria-selected', isSelected ? 'true' : 'false');
    btn.title = cat;
    btn.innerHTML = `
      <span class="tag-option-name">${escapeHtml(cat)}</span>
      <span class="tag-check" aria-hidden="true">✓</span>
    `;
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      onSelectTag(cat);
    });
    tagGridContainer.appendChild(btn);
  });
}

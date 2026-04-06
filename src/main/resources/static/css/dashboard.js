/* ══ Format 24h time to 12h AM/PM ══ */
function fmt12(t) {
    if (!t) return '';
    const [h, m] = t.split(':').map(Number);
    const ampm = h >= 12 ? 'PM' : 'AM';
    const h12 = h % 12 || 12;
    return `${h12}:${String(m).padStart(2,'0')} ${ampm}`;
}
document.querySelectorAll('.shop-time').forEach(el => {
    el.textContent = el.textContent.replace(/(\d{2}:\d{2})\s*[–-]\s*(\d{2}:\d{2})/, (_, a, b) => `${fmt12(a)} – ${fmt12(b)}`);
});

/* ══ Toast ══ */
const toastContainer = (() => {
    const el = document.createElement('div');
    el.id = 'toast-container';
    document.body.appendChild(el);
    return el;
})();

function showToast(msg, type = 'info') {
    const t = document.createElement('div');
    t.className = `toast ${type}`;
    const icon = type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️';
    t.innerHTML = `<span style="font-size:18px">${icon}</span><span>${msg}</span>`;
    toastContainer.appendChild(t);
    t.addEventListener('click', () => dismissToast(t));
    setTimeout(() => dismissToast(t), 3500);
}
function dismissToast(t) {
    if (!t.parentNode) return;
    t.style.animation = 'toastOut 0.3s cubic-bezier(.4,0,.2,1) forwards';
    setTimeout(() => t.remove(), 300);
}

/* ══ Convert alerts to toasts ══ */
document.querySelectorAll('.alert').forEach(a => {
    const msg = a.textContent.trim();
    const type = a.classList.contains('alert-success') ? 'success' : 'error';
    setTimeout(() => showToast(msg, type), 400);
    setTimeout(() => { a.style.transition = 'opacity 0.4s'; a.style.opacity = '0'; setTimeout(() => a.remove(), 400); }, 100);
});

/* ══ Animated stat counters ══ */
document.querySelectorAll('.sc-val').forEach(el => {
    const target = parseInt(el.textContent, 10);
    if (isNaN(target) || target === 0) return;
    el.textContent = '0';
    let start = null;
    const duration = 900;
    function step(ts) {
        if (!start) start = ts;
        const progress = Math.min((ts - start) / duration, 1);
        const eased = 1 - Math.pow(1 - progress, 3); // ease-out cubic
        el.textContent = Math.round(eased * target);
        if (progress < 1) requestAnimationFrame(step);
    }
    requestAnimationFrame(step);
});

/* ══ Ripple effect on buttons ══ */
document.addEventListener('click', e => {
    const btn = e.target.closest('.btn');
    if (!btn) return;
    const r = document.createElement('span');
    r.className = 'ripple';
    const rect = btn.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height);
    r.style.cssText = `width:${size}px;height:${size}px;left:${e.clientX - rect.left - size/2}px;top:${e.clientY - rect.top - size/2}px`;
    btn.appendChild(r);
    setTimeout(() => r.remove(), 600);
});

/* ══ Collapsible forms ══ */
function toggleForm(id) {
    const el = document.getElementById(id);
    if (!el) return;
    const collapsed = el.classList.toggle('collapsed');
    const btn = document.querySelector(`[onclick="toggleForm('${id}')"]`);
    if (btn) btn.textContent = collapsed ? '▼ Show Form' : '▲ Hide Form';
}

/* ══ Search debounce ══ */
let _searchTimer = null;
function debounceSearch(fn, delay = 300) {
    clearTimeout(_searchTimer);
    _searchTimer = setTimeout(fn, delay);
}

/* ══ Search: shops (client-side filter for paginated results) ══ */
function filterShops() {
    debounceSearch(() => {
        const q = (document.getElementById('shopSearch')?.value || '').toLowerCase();
        let visible = 0;
        document.querySelectorAll('#shopGrid .shop-tile').forEach(tile => {
            const match = (tile.dataset.name || '').toLowerCase().includes(q) ||
                          (tile.dataset.cat  || '').toLowerCase().includes(q);
            tile.style.display = match ? '' : 'none';
            if (match) visible++;
        });
        const counter = document.getElementById('shopCount');
        if (counter) counter.textContent = visible + ' shops';
    });
}

/* ══ Search: items ══ */
function filterItems() {
    debounceSearch(() => {
        const q = (document.getElementById('itemSearch')?.value || '').toLowerCase();
        let visible = 0;
        document.querySelectorAll('#itemGrid .item-card').forEach(card => {
            const match = (card.dataset.name || '').toLowerCase().includes(q);
            card.style.display = match ? '' : 'none';
            if (match) visible++;
        });
        const counter = document.getElementById('itemCount');
        if (counter) counter.textContent = visible + ' items';
    });
}

/* ══ Custom confirm dialog ══ */
let _confirmCb = null;
function confirmAction(msg, cb) {
    document.getElementById('confirmMsg').textContent = msg;
    _confirmCb = cb;
    document.getElementById('confirmDialog').classList.add('active');
}
function confirmYes() {
    document.getElementById('confirmDialog').classList.remove('active');
    if (_confirmCb) _confirmCb();
    _confirmCb = null;
}
function confirmNo() {
    document.getElementById('confirmDialog').classList.remove('active');
    _confirmCb = null;
}

/* Intercept data-confirm forms */
document.querySelectorAll('form[data-confirm]').forEach(form => {
    form.addEventListener('submit', e => {
        e.preventDefault();
        confirmAction(form.dataset.confirm, () => form.submit());
    });
});

/* ══ Active nav on hash click ══ */
document.querySelectorAll('.nav-item[href^="#"]').forEach(link => {
    link.addEventListener('click', () => {
        document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
        link.classList.add('active');
    });
});

/* ══ Mobile sidebar ══ */
const sidebar  = document.querySelector('.sidebar');
const overlay  = document.querySelector('.sidebar-overlay');
const toggleBtn = document.querySelector('.sidebar-toggle');
if (toggleBtn) toggleBtn.addEventListener('click', () => { sidebar.classList.toggle('open'); overlay.classList.toggle('active'); });
if (overlay)   overlay.addEventListener('click',   () => { sidebar.classList.remove('open'); overlay.classList.remove('active'); });

/* ══ Keyboard shortcuts ══ */
document.addEventListener('keydown', e => {
    if (e.key === '/' && !['INPUT','TEXTAREA','SELECT'].includes(document.activeElement.tagName)) {
        e.preventDefault();
        (document.getElementById('shopSearch') || document.getElementById('itemSearch'))?.focus();
    }
    if (e.key === 'Escape') { confirmNo(); sidebar?.classList.remove('open'); overlay?.classList.remove('active'); }
});

/* ══ Stagger card animations ══ */
document.querySelectorAll('.shop-tile, .item-card').forEach((el, i) => {
    el.style.animationDelay = `${i * 0.04}s`;
});

/* ══ Add pulse dot to OPEN badges ══ */
document.querySelectorAll('.badge-open').forEach(b => {
    if (b.textContent.trim() === 'OPEN' || b.textContent.trim() === 'Available') {
        b.innerHTML = `<span class="pulse-dot"></span>${b.textContent.trim()}`;
    }
});

/* ══ Row hover highlight with smooth transition ══ */
document.querySelectorAll('tbody tr').forEach(row => {
    row.addEventListener('mouseenter', () => row.style.transform = 'scale(1.002)');
    row.addEventListener('mouseleave', () => row.style.transform = '');
});

/* ══ Form input character counter for description fields ══ */
document.querySelectorAll('input[placeholder*="description"], input[placeholder*="Description"]').forEach(input => {
    const counter = document.createElement('div');
    counter.style.cssText = 'font-size:11px;color:#aaa;text-align:right;margin-top:3px;';
    counter.textContent = '0 / 120';
    input.parentNode.appendChild(counter);
    input.addEventListener('input', () => {
        const len = input.value.length;
        counter.textContent = `${len} / 120`;
        counter.style.color = len > 100 ? '#e65100' : '#aaa';
    });
});

/* ══ Page load progress bar ══ */
const bar = document.createElement('div');
bar.style.cssText = 'position:fixed;top:0;left:0;height:3px;background:linear-gradient(90deg,#1a237e,#ffca28);z-index:9999;transition:width 0.3s;width:0';
document.body.prepend(bar);
bar.style.width = '70%';
window.addEventListener('load', () => { bar.style.width = '100%'; setTimeout(() => bar.remove(), 400); });

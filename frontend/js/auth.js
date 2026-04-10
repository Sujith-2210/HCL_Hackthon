/**
 * auth.js – Auth state management (JWT stored in localStorage)
 */

const Auth = {
  save(data) {
    localStorage.setItem('hotel_token', data.token);
    localStorage.setItem('hotel_user', JSON.stringify({
      userId: data.userId,
      email: data.email,
      firstName: data.firstName,
      lastName: data.lastName,
      role: data.role,
    }));
  },

  getToken() {
    return localStorage.getItem('hotel_token');
  },

  getUser() {
    try {
      return JSON.parse(localStorage.getItem('hotel_user'));
    } catch {
      return null;
    }
  },

  isLoggedIn() {
    const token = this.getToken();
    if (!token) return false;
    try {
      const payload = this.decodeJwtPayload(token);
      if (!payload?.exp) return false;
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  },

  decodeJwtPayload(token) {
    const payload = token.split('.')[1];
    if (!payload) return null;

    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
    const padded = normalized.padEnd(normalized.length + ((4 - normalized.length % 4) % 4), '=');
    return JSON.parse(atob(padded));
  },

  logout() {
    localStorage.removeItem('hotel_token');
    localStorage.removeItem('hotel_user');
    window.location.href = 'login.html';
  },

  getLandingPage() {
    const role = this.getUser()?.role;
    if (role === 'ADMIN') return 'admin.html';
    if (role === 'RECEPTIONIST') return 'reception.html';
    return 'dashboard.html';
  },
};

// Update nav dynamically on every page
(function updateNav() {
  const navActions = document.getElementById('navActions');
  if (!navActions) return;

  if (Auth.isLoggedIn()) {
    const user = Auth.getUser();
    const initials = ((user?.firstName || '')[0] + (user?.lastName || '')[0]).toUpperCase();
    navActions.innerHTML = `
      <div class="nav-user-menu">
        <button class="nav-user-btn" id="navUserBtn">
          <div class="nav-avatar">${initials}</div>
          ${user?.firstName || 'Account'} <span style="opacity:0.6;margin-left:2px">▾</span>
        </button>
        <div class="dropdown" id="navDropdown">
          <div class="dropdown-item" onclick="window.location='${Auth.getLandingPage()}'">🏠 Dashboard</div>
          ${user?.role === 'CUSTOMER' ? `<div class="dropdown-item" onclick="window.location='dashboard.html'">📋 My Bookings</div>` : ''}
          ${user?.role === 'RECEPTIONIST' ? `<div class="dropdown-item" onclick="window.location='reception.html'">🏨 Reception Panel</div>` : ''}
          ${user?.role === 'ADMIN' ? `<div class="dropdown-item" onclick="window.location='admin.html'">🛠️ Admin Panel</div>` : ''}
          <div class="dropdown-divider"></div>
          <div class="dropdown-item danger" onclick="Auth.logout()">🚪 Sign Out</div>
        </div>
      </div>`;

    document.getElementById('navUserBtn').addEventListener('click', (e) => {
      e.stopPropagation();
      document.getElementById('navDropdown').classList.toggle('open');
    });
    document.addEventListener('click', () => {
      document.getElementById('navDropdown')?.classList.remove('open');
    });
  }
})();

import { useEffect, useState } from 'react';
import { apiRequest } from './api';
import { getAccessToken, clearTokens } from './auth';
import { Routes, Route, Navigate, Link, NavLink } from 'react-router-dom';
import LoginPage from './LoginPage';
import HomePage from './HomePage';
import VenuesPage from './VenuesPage';
import EventsPage from './EventsPage';
import EventDetailPage from './EventDetailPage';
import type { User } from './types';
import MyOrdersPage from './MyOrdersPage';
import RegisterPage from './RegisterPage';
import NotFoundPage from './NotFoundPage';
import { TicketIcon } from './icons';
import { Loading } from './ui';

function navClass({ isActive }: { isActive: boolean }) {
  return isActive ? 'nav-link active' : 'nav-link';
}

function App() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function checkAuth() {
      const token = getAccessToken();
      if (!token) {
        setLoading(false);
        return;
      }
      try {
        const currentUser = await apiRequest<User>('/api/auth/me');
        setUser(currentUser);
      } catch {
        clearTokens();
      } finally {
        setLoading(false);
      }
    }
    checkAuth();
  }, []);

  function handleLogout() {
    clearTokens();
    setUser(null);
  }

  if (loading) {
    return <Loading />;
  }

  return (
    <div className="app">
      <header className="site-header">
        <div className="container">
          <Link to="/" className="brand" aria-label="SaveMySeat home">
            <span className="brand-mark">
              <TicketIcon size={17} />
            </span>
            <span className="brand-text">SaveMySeat</span>
          </Link>

          <nav className="nav" aria-label="Main">
            <NavLink to="/events" className={navClass}>Events</NavLink>
            <NavLink to="/venues" className={navClass}>Venues</NavLink>
            {user && <NavLink to="/my-orders" className={navClass}>My orders</NavLink>}
          </nav>

          <div className="header-actions">
            {user ? (
              <>
                <span className="user-chip">
                  <span className="avatar" aria-hidden="true">
                    {user.firstName.charAt(0).toUpperCase()}
                  </span>
                  <span className="user-name">{user.firstName}</span>
                </span>
                <button className="btn btn-ghost btn-sm" onClick={handleLogout}>
                  Log out
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="btn btn-ghost btn-sm">Log in</Link>
                <Link to="/register" className="btn btn-primary btn-sm">Sign up</Link>
              </>
            )}
          </div>
        </div>
      </header>

      <main className="main">
        <div className="container">
          <Routes>
            <Route path="/" element={<HomePage user={user} />} />
            <Route path="/venues" element={<VenuesPage />} />
            <Route path="/events" element={<EventsPage />} />
            <Route path="/events/:id" element={<EventDetailPage user={user} />} />
            <Route path="/my-orders" element={user ? <MyOrdersPage /> : <Navigate to="/login" />} />
            <Route path="/register" element={user ? <Navigate to="/" /> : <RegisterPage onRegisterSuccess={setUser} />} />
            <Route path="/login" element={user ? <Navigate to="/" /> : <LoginPage onLoginSuccess={setUser} />} />
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </div>
      </main>

      <footer className="site-footer">
        <div className="container">SaveMySeat · Ticketing that never oversells</div>
      </footer>
    </div>
  );
}

export default App;


import { useEffect, useState } from 'react';
import { apiRequest } from './api';
import { getAccessToken, clearTokens } from './auth';
import { Routes, Route, Navigate, Link } from 'react-router-dom';
import LoginPage from './LoginPage';
import HomePage from './HomePage';
import VenuesPage from './VenuesPage';
import EventsPage from './EventsPage';
import EventDetailPage from './EventDetailPage';
import type { User } from './types';
import MyOrdersPage from './MyOrdersPage';
import RegisterPage from './RegisterPage';

function App() {
  const[user, setUser] = useState<User | null>(null);
  const[loading, setLoading] = useState(true);

  useEffect(() => {
    async function checkAuth(){
      const token = getAccessToken();
      if(!token){
        setLoading(false);
        return
      }
      try{
        const currentUser = await apiRequest<User>('/api/auth/me');
        setUser(currentUser);
      }catch{
        clearTokens();
      }finally{
        setLoading(false)
      }
    }
    checkAuth();
  }, []);

  function handleLogout(){
    clearTokens();
    setUser(null);
  }

  if(loading){
    return <div>Loading...</div>
  }


  return (
    <div>
      <nav>
        <Link to="/">Home</Link>
        <span> | </span>
        <Link to="/venues">Venues</Link>
        <span> | </span>
        <Link to="/events">Events</Link>
        {user ? (
          <>
            <span> | </span>
            <Link to="/my-orders">My Orders</Link>
            <span> | </span>
            <span> | Signed in as {user.firstName}</span>
            <button onClick={handleLogout}>Log out</button>
          </>
        ) : (
           <>
          <span> | </span>
          <Link to="/login">Log in</Link>
          <span> | </span>
          <Link to="/register">Sign up</Link>
        </>
        )}
      </nav>
      
      <Routes>
        <Route path="/" element={<HomePage user={user} />} />
        <Route path="/venues" element={<VenuesPage />} />
        <Route path="/events" element={<EventsPage />} />
        <Route path="/events/:id" element={<EventDetailPage user={user}/>} />
        <Route path="/my-orders" element={user ? <MyOrdersPage /> : <Navigate to="/login" />} />
        <Route path="/register"element={user ? <Navigate to="/" /> : <RegisterPage onRegisterSuccess={setUser} />}/>
        <Route
          path="/login"
          element={
            user ? <Navigate to="/" /> : <LoginPage onLoginSuccess={setUser} />
          }
          />
      </Routes>

    </div>
  );
}


export default App;
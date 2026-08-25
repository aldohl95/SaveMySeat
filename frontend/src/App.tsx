import LoginForm from './LoginForm'
import { useEffect, useState } from 'react';
import { apiRequest } from './api';
import { getAccessToken, clearTokens } from './auth';
import { Routes, Route, Navigate, Link } from 'react-router-dom';
import LoginPage from './LoginPage';
import HomePage from './HomePage';

type User = {
  id: number,
  firstName: string;
  lastName: string;
  email: string;
  role: string;
};

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

  if(user){
    return(
      <div>
        <h1>SaveMySeat</h1>
        <h2>Welcome, {user?.firstName} {user?.lastName}!</h2>
        <p>Role: {user?.role}</p>
      </div>
    )
  }


  return (
    <div>
      <nav>
        <Link to="/">Home</Link>
        {user ? (
          <>
            <span> | Signed in as {user.firstName}</span>
            <button onClick={handleLogout}>Log out</button>
          </>
        ) : (
          <Link to="/login"> | Log in</Link>
        )}
      </nav>
      
      <Routes>
        <Route path="/" element={<HomePage user={user} />} />
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
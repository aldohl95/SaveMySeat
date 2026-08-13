import { useState } from 'react';

type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user:{
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    role: string;
  };

};

function LoginForm() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [user, setUser] = useState<AuthResponse['user'] | null>(null);


  async function handleSubmit(){
    setLoading(true);
    setError(null);
    try{
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json'},
        body: JSON.stringify({email,password}),
      });

      if(!response.ok){
        throw new Error('Invalid credentials');
      }

      const data : AuthResponse = await response.json();
      setUser(data.user)
      console.log('Access token:', data.accessToken);
    }catch(e){
      setError(e instanceof Error ? e.message : 'Login failed');
    }finally{
      setLoading(false);
    }
  }

  if(user){
    return(
      <div>
        <h2>Welcome, {user.firstName}!</h2>
        <p>Role: {user.role}</p>
      </div>
    );
  }

  return (
    <div>
      <h2>Log in</h2>
      <div>
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          disabled={loading}
        />
      </div>
      <div>
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          disabled={loading}
        />
      </div>
      <button onClick={handleSubmit} disabled={loading}>
          {loading ? 'Logging in...' : 'Log in'}
      </button>
      {error && <p style={{ color: 'red'}}>{error}</p>}
    </div>
  );

}

export default LoginForm;

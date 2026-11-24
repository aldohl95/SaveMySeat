import { useState } from 'react';
import { saveTokens } from './auth'
import { apiRequest } from './api'
import type { User, AuthResponse } from './types';


type LoginFormProps = {
  onLoginSuccess: (user: User) => void;
}

function LoginForm({onLoginSuccess}: LoginFormProps) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);



  async function handleSubmit(e: React.FormEvent<HTMLFormElement>){
    e.preventDefault();
    setLoading(true);
    setError(null);
    try{
      const data = await apiRequest<AuthResponse>('/api/auth/login', {
        method: 'POST',
        body: {email, password},
      });
      saveTokens(data.accessToken, data.refreshToken)
      onLoginSuccess(data.user);
    }catch(e){
      setError(e instanceof Error ? e.message : 'Login failed');
    }finally{
      setLoading(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
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
      <button type="submit" disabled={loading}>
          {loading ? 'Logging in...' : 'Log in'}
      </button>
      {error && <p style={{ color: 'red'}}>{error}</p>}
    </form>
  );

}

export default LoginForm;

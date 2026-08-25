import { useState } from 'react';
import { saveTokens } from './auth'
import { apiRequest } from './api'
import type { User } from './types';

type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
};

type LoginFormProps = {
  onLoginSuccess: (user: User) => void;
}

function LoginForm({onLoginSuccess}: LoginFormProps) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);



  async function handleSubmit(){
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

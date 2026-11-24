import { useState } from 'react';
import type { FormEvent } from 'react';
import { saveTokens } from './auth';
import { apiRequest } from './api';
import type { User, AuthResponse } from './types';
import { ErrorAlert } from './ui';

type LoginFormProps = {
  onLoginSuccess: (user: User) => void;
};

function LoginForm({ onLoginSuccess }: LoginFormProps) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const data = await apiRequest<AuthResponse>('/api/auth/login', {
        method: 'POST',
        body: { email, password },
      });
      saveTokens(data.accessToken, data.refreshToken);
      onLoginSuccess(data.user);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Login failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <form className="form" onSubmit={handleSubmit}>
      <div className="field">
        <label htmlFor="login-email">Email</label>
        <input
          id="login-email"
          className="input"
          type="email"
          autoComplete="email"
          placeholder="you@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          disabled={loading}
          required
        />
      </div>
      <div className="field">
        <label htmlFor="login-password">Password</label>
        <input
          id="login-password"
          className="input"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          disabled={loading}
          required
        />
      </div>
      {error && <ErrorAlert message={error} />}
      <button type="submit" className="btn btn-primary btn-block btn-lg" disabled={loading}>
        {loading ? 'Logging in…' : 'Log in'}
      </button>
    </form>
  );
}

export default LoginForm;

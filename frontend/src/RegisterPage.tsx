import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { apiRequest } from './api';
import { saveTokens } from './auth';
import type { AuthResponse, User } from './types';
import { ErrorAlert } from './ui';

type RegisterPageProps = {
  onRegisterSuccess: (user: User) => void;
};

function RegisterPage({ onRegisterSuccess }: RegisterPageProps) {
  const navigate = useNavigate();
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      await apiRequest<User>('/api/auth/register', {
        method: 'POST',
        body: { firstName, lastName, email, password },
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed');
      setLoading(false);
      return;
    }


    try {
      const data = await apiRequest<AuthResponse>('/api/auth/login', {
        method: 'POST',
        body: { email, password },
      });
      saveTokens(data.accessToken, data.refreshToken);
      onRegisterSuccess(data.user);
      navigate('/');
    } catch {
      navigate('/login');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth">
      <div className="card auth-card">
        <header>
          <h1>Create your account</h1>
          <p>Sign up to hold and buy tickets.</p>
        </header>
        <form className="form" onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="field">
              <label htmlFor="firstName">First name</label>
              <input
                id="firstName"
                className="input"
                type="text"
                autoComplete="given-name"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                disabled={loading}
                required
              />
            </div>
            <div className="field">
              <label htmlFor="lastName">Last name</label>
              <input
                id="lastName"
                className="input"
                type="text"
                autoComplete="family-name"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                disabled={loading}
                required
              />
            </div>
          </div>
          <div className="field">
            <label htmlFor="email">Email</label>
            <input
              id="email"
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
            <label htmlFor="password">Password</label>
            <input
              id="password"
              className="input"
              type="password"
              autoComplete="new-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={loading}
              required
              minLength={8}
              maxLength={72}
              aria-describedby="password-hint"
            />
            <span id="password-hint" className="field-hint">8 to 72 characters.</span>
          </div>
          {error && <ErrorAlert message={error} />}
          <button type="submit" className="btn btn-primary btn-block btn-lg" disabled={loading}>
            {loading ? 'Creating account…' : 'Create account'}
          </button>
        </form>
        <p className="auth-footer">
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      </div>
    </div>
  );
}

export default RegisterPage;
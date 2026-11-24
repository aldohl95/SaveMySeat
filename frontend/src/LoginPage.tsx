import { Link, useNavigate } from 'react-router-dom';
import LoginForm from './LoginForm';
import type { User } from './types';

type LoginPageProps = {
  onLoginSuccess: (user: User) => void;
};

function LoginPage({ onLoginSuccess }: LoginPageProps) {
  const navigate = useNavigate();

  function handleSuccess(user: User) {
    onLoginSuccess(user);
    navigate('/');
  }

  return (
    <div className="auth">
      <div className="card auth-card">
        <header>
          <h1>Welcome back</h1>
          <p>Log in to hold and buy tickets.</p>
        </header>
        <LoginForm onLoginSuccess={handleSuccess} />
        <p className="auth-footer">
          New to SaveMySeat? <Link to="/register">Create an account</Link>
        </p>
      </div>
    </div>
  );
}

export default LoginPage;

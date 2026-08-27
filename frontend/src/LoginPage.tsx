import {useNavigate } from 'react-router-dom';
import LoginForm from './LoginForm';
import type { User } from './types';

type LoginPageProps = {
  onLoginSuccess: (user: User) => void;
};

function LoginPage({onLoginSuccess}: LoginPageProps){
  const navigate = useNavigate();

  function handleSuccess(user: User){
    onLoginSuccess(user);
    navigate('/')
  }

  return(
    <div>
      <h1>Log in</h1>
      <LoginForm onLoginSuccess={handleSuccess} />
    </div>
  );

}
export default LoginPage;
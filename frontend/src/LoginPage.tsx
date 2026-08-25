import {useNavigate } from 'react-router-dom';
import LoginForm from './LoginForm';

type User = {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
};

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
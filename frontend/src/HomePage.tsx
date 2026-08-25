type User = {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
};

type HomePageProps = {
  user: User | null;
}

function HomePage({ user }: HomePageProps){
  if(user){
    return(
      <div>
        <h1>Welcome, {user.firstName}!</h1>
        <p>Role: {user.role}</p>
      </div>
    );
  }

  return(
    <div>
      <h1>SaveMySeat</h1>
      <p>Ticketing that doesn't oversell</p>
    </div>
  );
}

export default HomePage;
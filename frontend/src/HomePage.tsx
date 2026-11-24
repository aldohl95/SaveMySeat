import { Link } from 'react-router-dom';
import type { User } from './types';
import EventCard from './EventCard';
import { useEvents } from './useEvents';
import { ArrowRightIcon, ShieldIcon } from './icons';

type HomePageProps = {
  user: User | null;
};

const steps = [
  {
    title: 'Hold your seats',
    text: 'Pick a tier and quantity. Your tickets are reserved for you while you check out.',
  },
  {
    title: 'Confirm payment',
    text: 'Pay before the hold expires. Seats you hold are never sold to anyone else.',
  },
  {
    title: "You're in",
    text: 'Your order shows up in My orders straight away, with everything you need.',
  },
];

function HomePage({ user }: HomePageProps) {
  const { upcoming: allUpcoming, venues, loading, error } = useEvents();
  const upcoming = allUpcoming.slice(0, 3);

  return (
    <>
      <section className="hero">
        <span className="eyebrow">
          <ShieldIcon size={14} />
          Every seat sold once
        </span>
        <h1>{user ? `Welcome back, ${user.firstName}.` : 'Get your seat. No overselling, ever.'}</h1>
        <p className="hero-lead">
          SaveMySeat holds your tickets while you check out, so the seat you pick is the seat you
          get, even when thousands of fans are buying at once.
        </p>
        <div className="hero-actions">
          <Link to="/events" className="btn btn-primary btn-lg">
            Browse events
            <ArrowRightIcon />
          </Link>
          {user ? (
            <Link to="/my-orders" className="btn btn-secondary btn-lg">My orders</Link>
          ) : (
            <Link to="/register" className="btn btn-secondary btn-lg">Create an account</Link>
          )}
        </div>
      </section>

      {!loading && !error && upcoming.length > 0 && (
        <section className="section">
          <div className="section-header">
            <h2>Upcoming events</h2>
            <Link to="/events">View all</Link>
          </div>
          <div className="grid">
            {upcoming.map((event) => (
              <EventCard key={event.id} event={event} venue={venues[event.venueId]} />
            ))}
          </div>
        </section>
      )}

      <section className="section">
        <div className="section-header">
          <h2>How it works</h2>
        </div>
        <div className="steps">
          {steps.map((step, index) => (
            <div key={step.title} className="card step">
              <div className="step-number">{index + 1}</div>
              <h3>{step.title}</h3>
              <p>{step.text}</p>
            </div>
          ))}
        </div>
      </section>
    </>
  );
}

export default HomePage;

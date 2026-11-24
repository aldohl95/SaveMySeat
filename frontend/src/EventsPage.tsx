import EventCard from './EventCard';
import { useEvents } from './useEvents';
import { CalendarIcon } from './icons';
import { EmptyState, ErrorState, Loading } from './ui';
import { pluralize } from './format';

function EventsPage() {
  const { events, venues, loading, error } = useEvents();

  if (loading) {
    return <Loading label="Loading events" />;
  }

  if (error) {
    return <ErrorState message={error} />;
  }

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Events</h1>
          <p>{events.length > 0 ? `${pluralize(events.length, 'event')} to choose from.` : 'Find your next night out.'}</p>
        </div>
      </div>

      {events.length === 0 ? (
        <EmptyState icon={<CalendarIcon size={22} />} title="No events yet">
          <p>Check back soon. New events are added all the time.</p>
        </EmptyState>
      ) : (
        <div className="grid">
          {events.map((event) => (
            <EventCard key={event.id} event={event} venue={venues[event.venueId]} />
          ))}
        </div>
      )}
    </>
  );
}

export default EventsPage;

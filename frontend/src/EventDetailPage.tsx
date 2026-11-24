import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { apiRequest } from './api';
import type { Venue, Event, TicketTier, User, PagedResponse } from './types';
import TierRow from './TierRow';
import { ArrowLeftIcon, CalendarIcon, ClockIcon, PinIcon, TicketIcon } from './icons';
import { EmptyState, ErrorState, Loading } from './ui';
import { formatDay, formatTimeRange } from './format';

type EventDetailPageProps = {
  user: User | null;
};

function EventDetailPage({ user }: EventDetailPageProps) {
  const { id } = useParams();
  const eventId = Number(id);

  const [event, setEvent] = useState<Event | null>(null);
  const [venue, setVenue] = useState<Venue | null>(null);
  const [tiers, setTiers] = useState<TicketTier[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadData() {
      try {
        const eventData = await apiRequest<Event>(`/api/events/${eventId}`);
        const [venueData, tiersData] = await Promise.all([
          apiRequest<Venue>(`/api/venues/${eventData.venueId}`),
          apiRequest<PagedResponse<TicketTier>>(`/api/tickettiers?eventId=${eventId}`),
        ]);
        setEvent(eventData);
        setVenue(venueData);
        setTiers([...tiersData.content].sort((a, b) => a.priceCents - b.priceCents || a.id - b.id));
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Failed to load event');
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, [eventId]);

  if (loading) {
    return <Loading label="Loading event" />;
  }

  if (error || !event) {
    return <ErrorState message={error ?? 'Event not found'} />;
  }

  return (
    <>
      <Link to="/events" className="back-link">
        <ArrowLeftIcon />
        All events
      </Link>

      <div className="detail-layout">
        <article>
          <header className="detail-header">
            {event.status !== 'PUBLISHED' && (
              <span className="badge badge-warning" style={{ width: 'fit-content' }}>
                {event.status === 'DRAFT' ? 'Draft' : 'Cancelled'}
              </span>
            )}
            <h1>{event.name}</h1>
            <div className="detail-meta">
              <span className="meta">
                <CalendarIcon />
                {formatDay(event.startsAt)}
              </span>
              <span className="meta">
                <ClockIcon />
                {formatTimeRange(event.startsAt, event.endsAt)}
              </span>
              {venue && (
                <span className="meta">
                  <PinIcon />
                  {venue.name} · {venue.streetName}, {venue.city}, {venue.state} {venue.zip}
                </span>
              )}
            </div>
          </header>

          {event.description && (
            <section className="detail-description">
              <h2>About this event</h2>
              <p>{event.description}</p>
            </section>
          )}
        </article>

        <aside className="tickets-panel" aria-label="Tickets">
          <h2>Tickets</h2>
          {tiers.length === 0 ? (
            <div className="card">
              <EmptyState icon={<TicketIcon size={22} />} title="No tickets yet">
                <p>Tickets for this event haven't been released.</p>
              </EmptyState>
            </div>
          ) : (
            tiers.map((tier) => <TierRow key={tier.id} tier={tier} user={user} />)
          )}
        </aside>
      </div>
    </>
  );
}

export default EventDetailPage;

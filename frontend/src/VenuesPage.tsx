import { useEffect, useState } from 'react';
import { apiRequest } from './api';
import type { Venue, PagedResponse } from './types';
import { PinIcon } from './icons';
import { EmptyState, ErrorState, Loading } from './ui';

function VenuesPage() {
  const [venues, setVenues] = useState<Venue[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadVenues() {
      try {
        const data = await apiRequest<PagedResponse<Venue>>('/api/venues');
        setVenues(data.content);
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Failed to load venues');
      } finally {
        setLoading(false);
      }
    }
    loadVenues();
  }, []);

  if (loading) {
    return <Loading label="Loading venues" />;
  }

  if (error) {
    return <ErrorState message={error} />;
  }

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Venues</h1>
          <p>Where the events happen.</p>
        </div>
      </div>

      {venues.length === 0 ? (
        <EmptyState icon={<PinIcon size={22} />} title="No venues yet" />
      ) : (
        <div className="grid">
          {venues.map((venue) => (
            <article key={venue.id} className="card venue-card">
              <h3>{venue.name}</h3>
              {venue.description && <p>{venue.description}</p>}
              <span className="meta">
                <PinIcon size={14} />
                {venue.streetName}, {venue.city}, {venue.state}
              </span>
            </article>
          ))}
        </div>
      )}
    </>
  );
}

export default VenuesPage;

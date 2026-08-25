import { useEffect, useState } from 'react';
import { apiRequest } from './api';
import type { Venue } from './types';


type PagedResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

function VenuesPage() {
  const [venues, setVenues] = useState<Venue[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadVenues(){
      setLoading(true);
      setError(null);
      try{
        const data = await apiRequest<PagedResponse<Venue>>('/api/venues');
        setVenues(data.content)
      }catch(e){
        setError(e instanceof Error ? e.message : 'Failed to load venues');
      }finally{
        setLoading(false);
      }
    }
    loadVenues();
  }, []);

  if(loading){
    return <div>Loading Venues...</div>
  }

  if(error){
    return <div style={{ color: 'red' }}>Error: {error}</div>
  }

  if(venues.length === 0){
    return <div>No Venues yet.</div>
  }

  return (
    <div>
      <h1>Venues</h1>
      <ul>
        {venues.map((venue) => (
          <li key={venue.id}>
            <h3>{venue.name}</h3>
            <p>{venue.description}</p>
            <p>{venue.city}, {venue.state}</p>
          </li>
        ))}
      </ul>
    </div>
  );

}

export default VenuesPage;
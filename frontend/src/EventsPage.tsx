import { useEffect, useState } from 'react';
import { apiRequest } from './api';
import type { Venue } from './types';

type EventStatus = 'DRAFT' | 'PUBLISHED' | 'CANCELLED';

type Event = {
  id: number;
  venueId: number;
  name: string;
  description: string;
  startsAt: string;
  endsAt: string;
  status: EventStatus;
  createdAt: string;
  updatedAt: string;
}

type PagedResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

function EventsPage(){
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [venueLookup, setVenueLookup] = useState<Record<number, Venue>>({});

  useEffect(() => {
    async function loadData(){
      setLoading(true);
      setError(null);
      try{
        const [eventsData, venuesData] = await Promise.all([
          apiRequest<PagedResponse<Event>>('/api/events'),
          apiRequest<PagedResponse<Venue>>('/api/venues'),
        ]);
        setEvents(eventsData.content);
        const lookup: Record<number, Venue> = {};
        for (const venue of venuesData.content){
          lookup[venue.id] = venue;
        }
        setVenueLookup(lookup);
      }catch(e){
        setError(e instanceof Error ? e.message : 'failed to load events');
      }finally{
        setLoading(false);
      }
    }
    loadData();
  }, []);


  if(loading){
    return <div>Loading Events...</div>
  }

  if(error){
    return <div style={{ color: 'red'}}>Error: {error}</div>
  }

  if(events.length === 0){
    return <div>No events yet!</div>
  }
  //TODO format dates
  return(
    <div>
      <ul>
        {events.map((event) => {
          const venue = venueLookup[event.venueId];
          return (
            <li key={event.id}>
              <h3>{event.name}</h3>
              <p>{event.description}</p>
              <p>Starts at: {event.startsAt}</p>
              <p>Ends at: {event.endsAt} </p>
              {venue ? (
                <p>{venue.streetName}, {venue.city}, {venue.state}</p>
              ) : (
                <p>Location unavailable</p>
              )}
            </li>
          );
        })}
      </ul>
    </div>
  );  
}

export default EventsPage;
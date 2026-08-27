import { useEffect, useState } from 'react';
import { apiRequest } from './api';
import type { Venue , Event , TicketTier, User} from './types'
import { useParams } from 'react-router-dom';
import TierRow from './TierRow';

type PagedResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

type EventDetailPageProps = {
  user: User | null;
}


function EventDetailPage({user}: EventDetailPageProps){
  const { id } = useParams();
  const eventId = Number(id);

  const [event, setEvent] = useState<Event | null>(null);
  const [venue, setVenue] = useState<Venue | null>(null);
  const [tiers, setTiers] = useState<TicketTier[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadData(){
      setLoading(true);
      setError(null);
      try{
        const eventData = await apiRequest<Event>(`/api/events/${eventId}`)
        const [venueData, tiersData] = await Promise.all([
          apiRequest<Venue>(`/api/venues/${eventData.venueId}`),
          apiRequest<PagedResponse<TicketTier>>(`/api/tickettiers?eventId=${eventId}`),
        ]);

        setEvent(eventData);
        setVenue(venueData)
        setTiers(tiersData.content);
      }catch(e){
        setError(e instanceof Error ? e.message : 'Failed to load event');
      }finally{
        setLoading(false);
      }
    }
    loadData();
  }, [eventId]);

  if(loading){
    return <div>Loading Tiers...</div>
  }

  if(error){
    return <div style={{ color: 'red'}}>Error: {error}</div>
  }


  return(
    <div>
      <h1>{event?.name}</h1>
      <p>{event?.description}</p>
      <p>Starts: {event?.startsAt}</p>
      <p>Ends: {event?.endsAt}</p>

      <h2>Venue</h2>
      <p>{venue?.streetName}, {venue?.city}, {venue?.state}, {venue?.zip}</p>

      <h2>Tickets</h2>
      {tiers.length === 0 ? (
        <p> No Ticket tiers yet.</p>
        ) : (
        <ul>
          {tiers.map((tier) => (
            <TierRow key={tier.id} tier={tier} user={user}/>
          ))}
        </ul>
      )}
    </div>
  )


}
export default EventDetailPage;

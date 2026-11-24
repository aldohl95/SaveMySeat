import { Link } from 'react-router-dom';
import { ClockIcon, PinIcon } from './icons';
import { dateParts, formatDay, formatTime } from './format';
import type { Event, Venue } from './types';

type EventCardProps = {
  event: Event;
  venue?: Venue;
};

function EventCard({ event, venue }: EventCardProps) {
  const { month, day } = dateParts(event.startsAt);

  return (
    <Link to={`/events/${event.id}`} className="card card-link">
      <div className="event-card">
        <div className="date-block" aria-hidden="true">
          <span className="date-block-month">{month}</span>
          <span className="date-block-day">{day}</span>
        </div>
        <div className="event-card-body">
          <h3 className="event-card-title">{event.name}</h3>
          <span className="meta">
            <ClockIcon size={14} />
            {formatDay(event.startsAt)} · {formatTime(event.startsAt)}
          </span>
          <span className="meta">
            <PinIcon size={14} />
            {venue ? `${venue.name}, ${venue.city}` : 'Venue to be announced'}
          </span>
        </div>
      </div>
    </Link>
  );
}

export default EventCard;

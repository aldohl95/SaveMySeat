import { useEffect, useState } from 'react';
import { apiRequest } from './api';
import type { Event, PagedResponse, Venue } from './types';

type EventsState = {
  events: Event[];
  upcoming: Event[];
  venues: Record<number, Venue>;
  loading: boolean;
  error: string | null;
};

export function useEvents(): EventsState {
  const [state, setState] = useState<EventsState>({
    events: [],
    upcoming: [],
    venues: {},
    loading: true,
    error: null,
  });

  useEffect(() => {
    async function load() {
      try {
        const [eventsData, venuesData] = await Promise.all([
          apiRequest<PagedResponse<Event>>('/api/events'),
          apiRequest<PagedResponse<Venue>>('/api/venues'),
        ]);
        const venues: Record<number, Venue> = {};
        for (const venue of venuesData.content) {
          venues[venue.id] = venue;
        }
        const events = [...eventsData.content].sort(
          (a, b) => new Date(a.startsAt).getTime() - new Date(b.startsAt).getTime()
        );
        const now = Date.now();
        const upcoming = events.filter((e) => new Date(e.startsAt).getTime() >= now);
        setState({ events, upcoming, venues, loading: false, error: null });
      } catch (e) {
        setState({
          events: [],
          upcoming: [],
          venues: {},
          loading: false,
          error: e instanceof Error ? e.message : 'Failed to load events',
        });
      }
    }
    load();
  }, []);

  return state;
}

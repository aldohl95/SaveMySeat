export type User = {
  id: number;
  firstName: string;
  lastname: string;
  email: string;
  role: string;
  createdAt?: string;
  updatedAt?: string;
}

export type Venue = {
  id: number;
  organizerId: number;
  name: string;
  description: string;
  streetName: string;
  city: string;
  state: string;
  zip: string;
  createdAt: string;
  updatedAt: string;
}

 export type EventStatus = 'DRAFT' | 'PUBLISHED' | 'CANCELLED';

export type Event = {
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

export type TicketTier = {
    id: number;
    eventId: number;
    name: string;
    priceCents: number;
    capacity: number;
    reserved: number;
    sold: number;
    createdAt: string;
    updatedAt: string;
};

export type HoldStatus = 'ACTIVE' | 'CONVERTED' | 'RELEASED' | 'EXPIRED'

export type HoldResponse = {
    id: number;
    tierId: number;
    quantity: number;
    status: HoldStatus;
    expiresAt: string;
    createdAt: string;
    updatedAt: string;
};
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
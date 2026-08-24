import { getAccessToken } from './auth'

const API_BASE = 'http://localhost:8080';

type FetchOptions = {
  method?: string;
  body?: unknown;
};

export async function apiRequest<T>(
  path: string,
  options: FetchOptions = {}
): Promise<T> {
    const token = getAccessToken();
  
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };

    if(token){
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE}${path}`, {
      method: options.method ?? 'GET',
      headers,
      body: options.body ? JSON.stringify(options.body) : undefined,
    });

    if(!response.ok){
      throw new Error('Request failed: ${response.status}');
    }

    return response.json();
}
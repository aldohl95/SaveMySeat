// src/api/client.ts
import axios from 'axios'

export const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use((config) => {
  const token = getAccessToken() // keep this in memory, not localStorage
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
import { useEffect, useState } from 'react';
import { apiRequest } from './api';
import type { OrderResponse, PagedResponse } from './types';

function MyOrdersPage() {
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadOrders() {
      setLoading(true);
      setError(null);
      try {
        const data = await apiRequest<PagedResponse<OrderResponse>>('/api/orders');
        setOrders(data.content);
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Failed to load orders');
      } finally {
        setLoading(false);
      }
    }
    loadOrders();
  }, []);

  if (loading) {
    return <div>Loading orders...</div>;
  }

  if (error) {
    return <div style={{ color: 'red' }}>Error: {error}</div>;
  }

  return (
    <div>
      <h1>My Orders</h1>
      {orders.length === 0 ? (
        <p>No orders yet.</p>
      ) : (
        <ul>
          {orders.map((order) => (
            <li key={order.id}>
              <h3>Order #{order.id}</h3>
              <p>Status: {order.status}</p>
              <p>Tier #{order.tierId}, {order.quantity} ticket(s)</p>
              <p>Total: ${(order.totalCents / 100).toFixed(2)}</p>
              <p>Placed: {new Date(order.createdAt).toLocaleString()}</p>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default MyOrdersPage;
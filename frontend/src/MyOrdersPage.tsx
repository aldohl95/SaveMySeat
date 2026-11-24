import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiRequest } from './api';
import type { Event, OrderResponse, OrderStatus, PagedResponse, TicketTier } from './types';
import { CalendarIcon, TicketIcon } from './icons';
import { EmptyState, ErrorState, Loading } from './ui';
import { formatDateTime, formatDay, formatPrice, pluralize } from './format';

type OrderDetails = {
  tier?: TicketTier;
  event?: Event;
};

const statusBadge: Record<OrderStatus, { label: string; className: string }> = {
  PAID: { label: 'Paid', className: 'badge badge-success' },
  PENDING: { label: 'Pending', className: 'badge badge-warning' },
  CANCELLED: { label: 'Cancelled', className: 'badge' },
  REFUNDED: { label: 'Refunded', className: 'badge badge-accent' },
};

async function loadDetails(orders: OrderResponse[]): Promise<Record<number, OrderDetails>> {
  const tierIds = [...new Set(orders.map((o) => o.tierId))];
  const tierResults = await Promise.allSettled(
    tierIds.map((id) => apiRequest<TicketTier>(`/api/tickettiers/${id}`))
  );
  const tiers = tierResults.flatMap((r) => (r.status === 'fulfilled' ? [r.value] : []));

  const eventIds = [...new Set(tiers.map((t) => t.eventId))];
  const eventResults = await Promise.allSettled(
    eventIds.map((id) => apiRequest<Event>(`/api/events/${id}`))
  );
  const events = new Map(
    eventResults.flatMap((r) => (r.status === 'fulfilled' ? [[r.value.id, r.value] as const] : []))
  );

  const details: Record<number, OrderDetails> = {};
  for (const tier of tiers) {
    details[tier.id] = { tier, event: events.get(tier.eventId) };
  }
  return details;
}

function MyOrdersPage() {
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [details, setDetails] = useState<Record<number, OrderDetails>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadOrders() {
      try {
        const data = await apiRequest<PagedResponse<OrderResponse>>('/api/orders');
        setOrders(data.content);
        setDetails(await loadDetails(data.content));
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Failed to load orders');
      } finally {
        setLoading(false);
      }
    }
    loadOrders();
  }, []);

  if (loading) {
    return <Loading label="Loading your orders" />;
  }

  if (error) {
    return <ErrorState message={error} />;
  }

  return (
    <>
      <div className="page-header">
        <div>
          <h1>My orders</h1>
          <p>Your tickets, newest first.</p>
        </div>
      </div>

      {orders.length === 0 ? (
        <EmptyState icon={<TicketIcon size={22} />} title="No orders yet">
          <p>When you buy tickets, they'll show up here.</p>
          <Link to="/events" className="btn btn-primary" style={{ marginTop: 8 }}>
            Browse events
          </Link>
        </EmptyState>
      ) : (
        <div className="order-list">
          {orders.map((order) => {
            const { tier, event } = details[order.tierId] ?? {};
            const badge = statusBadge[order.status];
            return (
              <article key={order.id} className="card order">
                <div className="order-main">
                  <div className="order-title">
                    <h3>
                      {event ? (
                        <Link to={`/events/${event.id}`} style={{ color: 'inherit' }}>{event.name}</Link>
                      ) : (
                        `Order #${order.id}`
                      )}
                    </h3>
                    <span className={badge.className}>{badge.label}</span>
                  </div>
                  <span className="meta">
                    <TicketIcon size={14} />
                    {pluralize(order.quantity, 'ticket')} · {tier ? tier.tierName : `Tier #${order.tierId}`}
                  </span>
                  {event && (
                    <span className="meta">
                      <CalendarIcon size={14} />
                      {formatDay(event.startsAt)}
                    </span>
                  )}
                </div>
                <div className="order-total">
                  <strong>{formatPrice(order.totalCents)}</strong>
                  <span className="subtle" style={{ fontSize: '0.8rem' }}>
                    Order #{order.id} · {formatDateTime(order.createdAt)}
                  </span>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </>
  );
}

export default MyOrdersPage;

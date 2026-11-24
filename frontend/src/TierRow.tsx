import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiRequest } from './api';
import type { TicketTier, HoldResponse, User, OrderResponse } from './types';
import { CheckCircleIcon, ClockIcon } from './icons';
import { ErrorAlert } from './ui';
import { formatCountdown, formatPrice, pluralize } from './format';

const MAX_PER_ORDER = 10;
const URGENT_MS = 60_000;

type TierProps = {
  tier: TicketTier;
  user: User | null;
};

function TierRow({ tier: initialTier, user }: TierProps) {
  const [tier, setTier] = useState(initialTier);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [hold, setHold] = useState<HoldResponse | null>(null);
  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [now, setNow] = useState(() => Date.now());

  const available = Math.max(0, tier.capacity - tier.reserved - tier.sold);
  const soldOut = available <= 0;
  const maxQuantity = Math.max(1, Math.min(available, MAX_PER_ORDER));
  const selected = Math.min(quantity, maxQuantity);
  const takenPercent = tier.capacity > 0 ? ((tier.capacity - available) / tier.capacity) * 100 : 100;
  const lowStock = !soldOut && available <= Math.max(1, Math.ceil(tier.capacity * 0.2));
  const remainingMs = hold ? new Date(hold.expiresAt).getTime() - now : 0;

  async function refreshTier() {
    try {
      setTier(await apiRequest<TicketTier>(`/api/tickettiers/${tier.id}`));
    } catch {
      // Keep the last known counts if the refresh fails.
    }
  }

  const tierId = tier.id;

  useEffect(() => {
    if (!hold) return;
    const expiresAt = new Date(hold.expiresAt).getTime();
    const timer = setInterval(() => {
      const current = Date.now();
      setNow(current);
      if (current >= expiresAt) {
        setHold(null);
        setNotice('Your hold expired and the tickets were released.');
        apiRequest<TicketTier>(`/api/tickettiers/${tierId}`).then(setTier).catch(() => {});
      }
    }, 1000);
    return () => clearInterval(timer);
  }, [hold, tierId]);

  async function handleCreateHold() {
    setLoading(true);
    setError(null);
    setNotice(null);
    try {
      const created = await apiRequest<HoldResponse>('/api/holds', {
        method: 'POST',
        body: { tierId: tier.id, quantity: selected },
      });
      setNow(Date.now());
      setHold(created);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to create hold');
    } finally {
      await refreshTier();
      setLoading(false);
    }
  }

  async function handleConfirmPayment() {
    if (!hold) return;
    setLoading(true);
    setError(null);
    try {
      const createdOrder = await apiRequest<OrderResponse>('/api/orders', {
        method: 'POST',
        body: { holdId: hold.id },
      });
      const fakeSessionId = `cs_test_fake_${hold.id}_${Date.now()}`;
      const paidOrder = await apiRequest<OrderResponse>(`/api/orders/${createdOrder.id}/pay`, {
        method: 'POST',
        body: { stripeSessionId: fakeSessionId },
      });
      setHold(null);
      setOrder(paidOrder);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to confirm payment');
    } finally {
      await refreshTier();
      setLoading(false);
    }
  }

  async function handleCancelHold() {
    if (!hold) return;
    setLoading(true);
    setError(null);
    try {
      await apiRequest(`/api/holds/${hold.id}`, { method: 'DELETE' });
      setHold(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to cancel hold');
    } finally {
      await refreshTier();
      setLoading(false);
    }
  }

  return (
    <div className="card tier">
      <div className="tier-top">
        <div>
          <h3 className="tier-name">{tier.tierName}</h3>
          {hold ? (
            <span className="badge badge-accent" style={{ marginTop: 6 }}>Held for you</span>
          ) : soldOut ? (
            <span className="badge badge-danger" style={{ marginTop: 6 }}>Sold out</span>
          ) : lowStock ? (
            <span className="badge badge-warning" style={{ marginTop: 6 }}>Almost gone</span>
          ) : null}
        </div>
        <div className="tier-price">
          {formatPrice(tier.priceCents)}
          <small>each</small>
        </div>
      </div>

      <div className="availability">
        <div className="availability-row">
          <span>{soldOut ? 'No tickets left' : `${available} of ${tier.capacity} left`}</span>
        </div>
        <div
          className="meter"
          role="meter"
          aria-label={`${tier.tierName} tickets taken`}
          aria-valuemin={0}
          aria-valuemax={tier.capacity}
          aria-valuenow={tier.capacity - available}
        >
          <div className={lowStock ? 'meter-fill low' : 'meter-fill'} style={{ width: `${takenPercent}%` }} />
        </div>
      </div>

      {order ? (
        <div className="success-panel" role="status">
          <strong>
            <CheckCircleIcon />
            You're in!
          </strong>
          <span>
            Order #{order.id} · {pluralize(order.quantity, 'ticket')} · {formatPrice(order.totalCents)}
          </span>
          <Link to="/my-orders">View my orders</Link>
        </div>
      ) : hold ? (
        <div className="hold-panel">
          <div className="hold-summary">
            <span>
              {pluralize(hold.quantity, 'ticket')} · {formatPrice(hold.quantity * tier.priceCents)}
            </span>
            <span className={remainingMs <= URGENT_MS ? 'countdown urgent' : 'countdown'}>
              <ClockIcon size={14} />
              {formatCountdown(remainingMs)}
            </span>
          </div>
          <div className="tier-actions">
            <button className="btn btn-ghost" onClick={handleCancelHold} disabled={loading}>
              Release
            </button>
            <button className="btn btn-primary" onClick={handleConfirmPayment} disabled={loading}>
              {loading ? 'Processing…' : 'Confirm payment'}
            </button>
          </div>
        </div>
      ) : soldOut ? null : !user ? (
        <Link to="/login" className="btn btn-secondary btn-block">Log in to buy tickets</Link>
      ) : (
        <div className="tier-actions">
          <div className="stepper">
            <button
              type="button"
              aria-label="Fewer tickets"
              onClick={() => setQuantity(Math.max(1, selected - 1))}
              disabled={loading || selected <= 1}
            >
              −
            </button>
            <output aria-live="polite" aria-label="Quantity">{selected}</output>
            <button
              type="button"
              aria-label="More tickets"
              onClick={() => setQuantity(Math.min(maxQuantity, selected + 1))}
              disabled={loading || selected >= maxQuantity}
            >
              +
            </button>
          </div>
          <button className="btn btn-primary" onClick={handleCreateHold} disabled={loading}>
            {loading ? 'Holding…' : `Hold ${pluralize(selected, 'ticket')}`}
          </button>
        </div>
      )}

      {notice && <p className="subtle" style={{ fontSize: '0.85rem' }}>{notice}</p>}
      {error && <ErrorAlert message={error} />}
    </div>
  );
}

export default TierRow;

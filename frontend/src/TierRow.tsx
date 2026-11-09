import { useState } from 'react';
import { apiRequest } from './api';
import type { TicketTier, HoldResponse, User, OrderResponse } from './types';

type TierProps = {
    tier: TicketTier;
    user: User | null;
};

function TierRow({ tier, user }: TierProps) {
    const [quantity, setQuantity] = useState(1);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [hold, setHold] = useState<HoldResponse | null>(null);
    const [order, setOrder] = useState<OrderResponse | null>(null);

    const available = tier.capacity - tier.reserved - tier.sold;
    const priceDollars = (tier.priceCents / 100).toFixed(2);
    const soldOut = available <= 0;

    async function handleCreateHold() {
        setLoading(true);
        setError(null);
        try {
            const created = await apiRequest<HoldResponse>('/api/holds', {
                method: 'POST',
                body: { tierId: tier.id, quantity },
            });
            setHold(created);
        } catch (e) {
            setError(e instanceof Error ? e.message : 'Failed to create hold');
        } finally {
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
            const paidOrder = await apiRequest<OrderResponse>(
                `/api/orders/${createdOrder.id}/pay`,
                {
                    method: 'POST',
                    body: { stripeSessionId: fakeSessionId },
                }
            );
            setOrder(paidOrder);
        } catch (e) {
            setError(e instanceof Error ? e.message : 'Failed to confirm payment');
        } finally {
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
            setLoading(false);
        }
    }

    return (
        <li>
            <h3>{tier.name}</h3>
            <p>${priceDollars}</p>
            <p>{soldOut ? 'Sold Out' : `${available} available`}</p>

            {order ? (
                <div style={{ padding: '1em', background: '#efe' }}>
                    <p>Payment confirmed</p>
                    <p>Order #{order.id}</p>
                    <p>Total: ${(order.totalCents / 100).toFixed(2)}</p>
                </div>
            ) : hold ? (
                <div style={{ padding: '1em', background: '#eef' }}>
                    <p>Hold created for {hold.quantity} ticket(s)</p>
                    <p>Expires at: {hold.expiresAt}</p>
                    <button onClick={handleConfirmPayment} disabled={loading}>
                        {loading ? 'Processing...' : 'Confirm Payment'}
                    </button>
                    <button onClick={handleCancelHold} disabled={loading}>
                        Cancel Hold
                    </button>
                    {error && <p style={{ color: 'red' }}>{error}</p>}
                </div>
            ) : soldOut ? null : !user ? (
                <p><em>Log in to buy tickets</em></p>
            ) : (
                <div>
                    <input
                        type="number"
                        min={1}
                        max={Math.min(available, 10)}
                        value={quantity}
                        onChange={(e) => setQuantity(Number(e.target.value))}
                        disabled={loading}
                    />
                    <button onClick={handleCreateHold} disabled={loading}>
                        {loading ? 'Holding...' : 'Hold Tickets'}
                    </button>
                    {error && <p style={{ color: 'red' }}>{error}</p>}
                </div>
            )}
        </li>
    );
}

export default TierRow;
import { useState } from 'react';
import { apiRequest } from './api';
import type { TicketTier, HoldResponse, User } from './types';

type TierProps = {
  tier: TicketTier;
  user: User | null;
};

function TierRow({tier, user}: TierProps){
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [hold, setHold] = useState<HoldResponse | null>(null);

  const available = tier.capacity - tier.reserved - tier.sold;
  const priceDollars = (tier.priceCents / 100).toFixed(2);
  const soldOut = available <= 0;

  async function handleSubmit(){
    setLoading(true);
    setError(null);

    try{
      const created = await apiRequest<HoldResponse>('/api/holds', {
        method: 'POST',
        body: { tierId: tier.id, quantity},
      });

      setHold(created)
    }catch (e){
      setError(e instanceof Error ? e.message : 'Failed to create Hold')
    }finally{
      setLoading(false);
    }

  }

  return(
    <li>
      <h3>{tier.name}</h3>
      <p>${priceDollars}</p>
      <p>{soldOut ? 'Sold Out' : `${available} avaliable`}</p>

      { hold ? (
        <div>
          <p>Hold created for {hold.quantity} ticket(s)</p>
          <p>Expires at: {hold.expiresAt}</p>
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
          <button onClick={handleSubmit} disabled={loading}>
            {loading ? 'Holding...' : 'Hold Tickets'}
          </button>
          {error && <p style = {{ color: 'red'}}>{error}</p>}
        </div>
      )}
    </li>
  );
}

export default TierRow;
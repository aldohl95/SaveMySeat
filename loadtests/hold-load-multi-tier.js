import http from 'k6/http';
import { check } from 'k6';

export const options = {
    stages: [
        { duration: '10s', target: 10 },
        { duration: '20s', target: 10 },
        { duration: '5s',  target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<1000'],
    },
};


const TIER_IDS = [ 152,
  251,
  301,
  351,
  401,
  451,
  501,
  551,
  601,
  651,
  701,
  751,
  801,
  851,
  901,
  951,
 1001,
 1051,
 1101];

export function setup() {
    const loginRes = http.post('http://localhost:8080/api/auth/login', 
        JSON.stringify({ email: 'alice@example.com', password: 'password123' }),
        { headers: { 'Content-Type': 'application/json' } }
    );
    
    if (loginRes.status !== 200) {
        throw new Error(`Login failed: ${loginRes.status}`);
    }
    
    return { token: loginRes.json('accessToken') };
}

export default function (data) {
    // Random tier per request — distributes load across tiers
    const tierId = TIER_IDS[Math.floor(Math.random() * TIER_IDS.length)];
    
    const payload = JSON.stringify({
        tierId: tierId,
        quantity: 1,
    });

    const res = http.post('http://localhost:8080/api/holds', payload, {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${data.token}`,
        },
    });

    check(res, {
        'status is 201 or 409': (r) => r.status === 201 || r.status === 409,
    });
}
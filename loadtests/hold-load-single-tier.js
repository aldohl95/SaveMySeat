import http from 'k6/http';
import { check } from 'k6';

export const options = {
    stages: [
        { duration: '10s', target: 10 },
        { duration: '20s', target: 10 },
        { duration: '5s',  target: 0 },
    ],
    thresholds: {
        // Deliberately loose — we EXPECT lots of "capacity" failures once tickets run out
        http_req_duration: ['p(95)<1000'],
    },
};

const TIER_ID = 152;  // Alice's tier

export function setup() {
    // Log in as Alice, capture token
    const loginRes = http.post('http://localhost:8080/api/auth/login', 
        JSON.stringify({ email: 'alice@example.com', password: 'password123' }),
        { headers: { 'Content-Type': 'application/json' } }
    );
    
    if (loginRes.status !== 200) {
        throw new Error(`Login failed: ${loginRes.status} ${loginRes.body}`);
    }
    
    const token = loginRes.json('accessToken');
    return { token: token };
}

export default function (data) {
    const payload = JSON.stringify({
        tierId: TIER_ID,
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
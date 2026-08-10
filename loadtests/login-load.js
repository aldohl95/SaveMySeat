import http from 'k6/http';
import { check } from 'k6';

export const options = {
    stages: [
        { duration: '10s', target: 20 },   //ramps to 5 virtual users over 10s
        { duration: '20s', target: 20 },   //holds at 5 virtual users for 20s
        { duration: '5s',  target: 0 },   //ramps down
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'],           //fewers than 1% failures
        http_req_duration: ['p(95)<1000'],       //95 % of users must be under 1000ms
    },
};

export default function () {
    const payload = JSON.stringify({
        email: 'alice@example.com',
        password: 'password123',
    });

    const res = http.post('http://localhost:8080/api/auth/login', payload, {
        headers: { 'Content-Type': 'application/json' },
    });

    check(res, {
        'status is 200': (r) => r.status === 200,
        'has token': (r) => r.json('accessToken') !== undefined,
    });
}
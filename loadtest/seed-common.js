import http from 'k6/http';
import { fail } from 'k6';

// 시드 스크립트 공통 헬퍼. seed-sunrin.js / seed-volume.js 가 공유한다.

export const BASE = __ENV.BASE_URL || 'https://api.edumanager.uk';
export const PASSWORD = '1';

export function rnd(min, max) { return Math.floor(Math.random() * (max - min + 1)) + min; }
export function pad(n) { return n < 10 ? `0${n}` : `${n}`; }

export function jsonHeaders(token) {
    const h = { 'Content-Type': 'application/json' };
    if (token) h.Authorization = `Bearer ${token}`;
    return h;
}

export function post(path, body, token) {
    return http.post(`${BASE}${path}`, JSON.stringify(body), { headers: jsonHeaders(token) });
}

export function put(path, body, token) {
    return http.put(`${BASE}${path}`, JSON.stringify(body), { headers: jsonHeaders(token) });
}

export function get(path, token) {
    return http.get(`${BASE}${path}`, { headers: jsonHeaders(token) });
}

export function login(email) {
    const res = post('/api/auth/login/email', { email, password: PASSWORD });
    if (res.status !== 200) fail(`login failed: ${email} status=${res.status} body=${res.body}`);
    return res.json();
}

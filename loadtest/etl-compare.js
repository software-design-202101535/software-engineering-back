import http from 'k6/http';
import { check } from 'k6';
import { Trend } from 'k6/metrics';

// OLAP "전체 재집계(쓰기)" vs "사전집계 조회(읽기)" 성능 비교.
// 같은 데이터에 대해 ETL rebuild 1회 비용과 대시보드 조회 비용을 나란히 측정한다.
// 사전 조건: seed.js 를 먼저 실행해 데이터(특히 볼륨)가 있어야 rebuild 가 의미 있게 무겁다.
// rebuild 는 교사 권한이 필요하므로 교사 토큰으로 호출한다.
//
// 실행:  k6 run loadtest/etl-compare.js

const BASE = __ENV.BASE_URL || 'https://api.edumanager.uk';
const PASSWORD = '1';
const SEMESTER = '2025-1';
const REBUILDS = Number(__ENV.REBUILDS || 3);
const READS = Number(__ENV.READS || 30);

const rebuildTrend = new Trend('etl_rebuild_ms', true);          // 전체 재집계(쓰기)
const classSummaryTrend = new Trend('read_class_summary_ms', true); // 사전집계 조회(읽기)
const ranksTrend = new Trend('read_ranks_ms', true);            // 석차 조회(읽기)

export const options = {
    vus: 1,
    iterations: 1,
    setupTimeout: '5m',
};

function jsonHeaders(token) {
    const h = { 'Content-Type': 'application/json' };
    if (token) h.Authorization = `Bearer ${token}`;
    return h;
}

function login(email) {
    const res = http.post(`${BASE}/api/auth/login/email`,
        JSON.stringify({ email, password: PASSWORD }), { headers: jsonHeaders() });
    if (res.status !== 200) throw new Error(`login failed: ${email} status=${res.status}`);
    return res.json();
}

export function setup() {
    const teacherToken = login('teacher1@gmail.com').accessToken;   // SUNRIN grade1 class1 담임
    const studentId = login('student1@gmail.com').studentId;        // SUNRIN grade1 class1
    return { teacherToken, studentId };
}

export default function (data) {
    const headers = jsonHeaders(data.teacherToken);

    // ── 1. ETL 전체 재집계 (무거운 쓰기 경로) ──
    for (let i = 0; i < REBUILDS; i++) {
        const res = http.post(`${BASE}/api/analytics/etl/rebuild`, null, { headers });
        check(res, { 'rebuild 200': r => r.status === 200 });
        rebuildTrend.add(res.timings.duration);
        console.log(`rebuild #${i + 1}: ${Math.round(res.timings.duration)} ms (status ${res.status})`);
    }

    // ── 2. 사전집계 조회 (가벼운 읽기 경로) ──
    for (let i = 0; i < READS; i++) {
        const cs = http.get(
            `${BASE}/api/analytics/class-summary?school=SUNRIN_HIGH_SCHOOL&grade=1&semester=${SEMESTER}`,
            { headers });
        check(cs, { 'class-summary 200': r => r.status === 200 });
        classSummaryTrend.add(cs.timings.duration);

        const rk = http.get(
            `${BASE}/api/analytics/students/${data.studentId}/ranks?semester=${SEMESTER}`,
            { headers });
        check(rk, { 'ranks 200': r => r.status === 200 });
        ranksTrend.add(rk.timings.duration);
    }
}

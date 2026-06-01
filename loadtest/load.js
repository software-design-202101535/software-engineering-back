import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';

const BASE = __ENV.BASE_URL || 'https://api.edumanager.uk';
const PASSWORD = '1';
const SEMESTER = '2025-1';
const TEACHER_COUNT = 10;
const STUDENT_COUNT = 100;
const PARENT_COUNT = 50;
const STUDENTS_PER_TEACHER = 10;
const COUNSELING_YEAR = 2025;

export const options = {
    setupTimeout: '10m',
    scenarios: {
        teacher_read: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 20 },
                { duration: '2m',  target: 50 },
                { duration: '30s', target: 0 },
            ],
            exec: 'teacherReadGrades',
        },
        student_read: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 20 },
                { duration: '2m',  target: 50 },
                { duration: '30s', target: 0 },
            ],
            exec: 'studentReadGrades',
        },
        parent_read: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 10 },
                { duration: '2m',  target: 30 },
                { duration: '30s', target: 0 },
            ],
            exec: 'parentReadGrades',
        },
        teacher_write: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 5 },
                { duration: '2m',  target: 10 },
                { duration: '30s', target: 0 },
            ],
            exec: 'teacherUpdateGrade',
        },
        shared_counseling_read: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 10 },
                { duration: '2m',  target: 30 },
                { duration: '30s', target: 0 },
            ],
            exec: 'teacherReadSharedCounseling',
        },
    },
    thresholds: {
        'http_req_failed': ['rate<0.05'],
        'http_req_duration{api:teacher_read_grades}':       ['p(95)<1500'],
        'http_req_duration{api:student_read_grades}':       ['p(95)<1500'],
        'http_req_duration{api:parent_read_grades}':        ['p(95)<1500'],
        'http_req_duration{api:teacher_update_grade}':      ['p(95)<2000'],
        'http_req_duration{api:shared_counseling_read}':    ['p(95)<2000'],
    },
};

function jsonHeaders(token) {
    const h = { 'Content-Type': 'application/json' };
    if (token) h.Authorization = `Bearer ${token}`;
    return h;
}

function loginAccount(email) {
    const res = http.post(`${BASE}/api/auth/login/email`,
        JSON.stringify({ email, password: PASSWORD }),
        { headers: jsonHeaders() });
    if (res.status !== 200) {
        throw new Error(`login failed: ${email} status=${res.status} body=${res.body}`);
    }
    return res.json();
}

export function setup() {
    console.log('=== setup: teacher login ===');
    const teacherTokens = [];
    for (let i = 1; i <= TEACHER_COUNT; i++) {
        teacherTokens.push(loginAccount(`teacher${i}@gmail.com`).accessToken);
    }

    console.log('=== setup: student login ===');
    const students = [];
    for (let i = 1; i <= STUDENT_COUNT; i++) {
        const data = loginAccount(`student${i}@gmail.com`);
        students.push({ token: data.accessToken, studentId: data.studentId });
    }

    console.log('=== setup: parent login ===');
    const parents = [];
    for (let i = 1; i <= PARENT_COUNT; i++) {
        const data = loginAccount(`parent${i}@gmail.com`);
        const childIds = (data.children || []).map(c => c.studentId);
        parents.push({ token: data.accessToken, childStudentIds: childIds });
    }

    console.log('=== setup: fetch MIDTERM grade ids ===');
    const gradeIdByStudentIdx = {};
    for (let i = 1; i <= STUDENT_COUNT; i++) {
        const teacherIdx = Math.floor((i - 1) / STUDENTS_PER_TEACHER);
        const studentId = students[i - 1].studentId;
        const res = http.get(
            `${BASE}/api/students/${studentId}/grades?semester=${SEMESTER}&examType=MIDTERM`,
            { headers: jsonHeaders(teacherTokens[teacherIdx]) });
        if (res.status !== 200) {
            throw new Error(`fetch grades failed: student=${i} status=${res.status}`);
        }
        const grades = res.json();
        if (grades.length === 0) {
            throw new Error(`no MIDTERM grades for student=${i}. seed.js 부터 다시 실행해주세요`);
        }
        gradeIdByStudentIdx[i] = grades.map(g => ({ id: g.id, subject: g.subject }));
    }

    console.log('=== setup done ===');
    return { teacherTokens, students, parents, gradeIdByStudentIdx };
}

function randomExam() {
    return Math.random() < 0.5 ? 'MIDTERM' : 'FINAL';
}

export function teacherReadGrades(data) {
    const teacherIdx = (__VU % TEACHER_COUNT);
    const studentIdxInClass = teacherIdx * STUDENTS_PER_TEACHER + Math.floor(Math.random() * STUDENTS_PER_TEACHER);
    const target = data.students[studentIdxInClass];
    const res = http.get(
        `${BASE}/api/students/${target.studentId}/grades?semester=${SEMESTER}&examType=${randomExam()}`,
        { headers: jsonHeaders(data.teacherTokens[teacherIdx]), tags: { api: 'teacher_read_grades' } });
    check(res, { 'teacher_read 200': r => r.status === 200 });
    sleep(Math.random());
}

export function studentReadGrades(data) {
    const idx = __VU % STUDENT_COUNT;
    const target = data.students[idx];
    const res = http.get(
        `${BASE}/api/students/${target.studentId}/grades?semester=${SEMESTER}&examType=${randomExam()}`,
        { headers: jsonHeaders(target.token), tags: { api: 'student_read_grades' } });
    check(res, { 'student_read 200': r => r.status === 200 });
    sleep(Math.random());
}

export function parentReadGrades(data) {
    const parent = data.parents[__VU % PARENT_COUNT];
    if (parent.childStudentIds.length === 0) return;
    const childId = parent.childStudentIds[Math.floor(Math.random() * parent.childStudentIds.length)];
    const res = http.get(
        `${BASE}/api/students/${childId}/grades?semester=${SEMESTER}&examType=${randomExam()}`,
        { headers: jsonHeaders(parent.token), tags: { api: 'parent_read_grades' } });
    check(res, { 'parent_read 200': r => r.status === 200 });
    sleep(Math.random());
}

// 공유상담 조회. 같은 학교(SUNRIN) 다른 교사가 공유한 상담을 본다.
// 쿼리가 YEAR(date)/MONTH(date)/name LIKE '%..%' 로 인덱스를 못 타는 경로 → 슬로우쿼리 측정 대상.
export function teacherReadSharedCounseling(data) {
    const teacherIdx = (__VU % TEACHER_COUNT);
    let url = `${BASE}/api/counselings/shared?year=${COUNSELING_YEAR}`;
    const r = Math.random();
    if (r < 0.25) url += `&month=${1 + Math.floor(Math.random() * 12)}`;
    else if (r < 0.5) url += `&grade=${1 + Math.floor(Math.random() * 3)}`;
    else if (r < 0.7) url += `&name=student`;   // 선두 와일드카드 LIKE → 풀스캔 유발
    const res = http.get(url, {
        headers: jsonHeaders(data.teacherTokens[teacherIdx]),
        tags: { api: 'shared_counseling_read' },
    });
    check(res, { 'shared_counseling 200': r => r.status === 200 });
    sleep(Math.random());
}

export function teacherUpdateGrade(data) {
    const teacherIdx = (__VU % TEACHER_COUNT);
    const studentIdxInClass = teacherIdx * STUDENTS_PER_TEACHER + Math.floor(Math.random() * STUDENTS_PER_TEACHER);
    const studentNum = studentIdxInClass + 1;
    const target = data.students[studentIdxInClass];
    const grades = data.gradeIdByStudentIdx[studentNum];
    const pick = grades[Math.floor(Math.random() * grades.length)];
    const newScore = Math.floor(Math.random() * 41) + 60;

    const res = http.put(
        `${BASE}/api/students/${target.studentId}/grades/batch`,
        JSON.stringify({
            semester: SEMESTER, examType: 'MIDTERM',
            create: [],
            update: [{ id: pick.id, subject: pick.subject, score: newScore }],
            delete: [],
        }),
        { headers: jsonHeaders(data.teacherTokens[teacherIdx]), tags: { api: 'teacher_update_grade' } });
    check(res, { 'teacher_update 200': r => r.status === 200 });
    sleep(Math.random());
}

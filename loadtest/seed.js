import http from 'k6/http';
import { check, fail } from 'k6';

const BASE = __ENV.BASE_URL || 'https://api.edumanager.uk';
const PASSWORD = '1';
const SCHOOL = 'SUNRIN_HIGH_SCHOOL';
const SEMESTER = '2025-1';
const SUBJECTS = ['KOREAN', 'MATH', 'ENGLISH', 'SCIENCE', 'SOCIAL'];

export const options = {
    vus: 1,
    iterations: 1,
    setupTimeout: '10m',
    teardownTimeout: '10m',
};

function jsonHeaders(token) {
    const h = { 'Content-Type': 'application/json' };
    if (token) h.Authorization = `Bearer ${token}`;
    return h;
}

function post(path, body, token) {
    return http.post(`${BASE}${path}`, JSON.stringify(body), { headers: jsonHeaders(token) });
}

function put(path, body, token) {
    return http.put(`${BASE}${path}`, JSON.stringify(body), { headers: jsonHeaders(token) });
}

function get(path, token) {
    return http.get(`${BASE}${path}`, { headers: jsonHeaders(token) });
}

function login(email) {
    const res = post('/api/auth/login/email', { email, password: PASSWORD });
    if (res.status !== 200) fail(`login failed: ${email} status=${res.status} body=${res.body}`);
    return res.json();
}

function teacherClass(i) {
    const idx = i - 1;
    return { grade: Math.floor(idx / 4) + 1, classNum: (idx % 4) + 1 };
}

function studentMeta(i) {
    const cohort = Math.floor((i - 1) / 10) + 1;
    const { grade, classNum } = teacherClass(cohort);
    return { grade, classNum, number: ((i - 1) % 10) + 1, teacherIdx: cohort };
}

function ensureTeacher(i) {
    const email = `teacher${i}@gmail.com`;
    const { grade, classNum } = teacherClass(i);
    post('/api/auth/register/teacher', {
        email, password: PASSWORD, passwordConfirm: PASSWORD,
        name: `teacher${i}`, school: SCHOOL, grade, classNum,
        termsAgreed: true, privacyAgreed: true,
    });
    return login(email).accessToken;
}

function ensureStudent(i) {
    const email = `student${i}@gmail.com`;
    const { grade, classNum, number } = studentMeta(i);
    post('/api/auth/register/student', {
        email, password: PASSWORD, passwordConfirm: PASSWORD,
        name: `student${i}`, school: SCHOOL, grade, classNum, number,
        termsAgreed: true, privacyAgreed: true,
    });
    const data = login(email);
    return { token: data.accessToken, studentId: data.studentId };
}

function ensureParent(i, childEmails) {
    const email = `parent${i}@gmail.com`;
    post('/api/auth/register/parent', {
        email, password: PASSWORD, passwordConfirm: PASSWORD,
        name: `parent${i}`, childEmails,
        termsAgreed: true, privacyAgreed: true,
    });
}

function seedGrades(teacherToken, studentId) {
    for (const examType of ['MIDTERM', 'FINAL']) {
        const existing = get(
            `/api/students/${studentId}/grades?semester=${SEMESTER}&examType=${examType}`,
            teacherToken);
        if (existing.status === 200 && existing.json().length > 0) continue;
        const create = SUBJECTS.map(s => ({ subject: s, score: Math.floor(Math.random() * 41) + 60 }));
        const res = put(`/api/students/${studentId}/grades/batch`,
            { semester: SEMESTER, examType, create, update: [], delete: [] },
            teacherToken);
        check(res, { [`batch ${examType} ok`]: r => r.status === 200 });
    }
}

export default function () {
    console.log('=== teachers ===');
    const teacherTokens = [];
    for (let i = 1; i <= 10; i++) {
        teacherTokens.push(ensureTeacher(i));
        if (i % 5 === 0) console.log(`teachers ${i}/10`);
    }

    console.log('=== students ===');
    const students = [];
    for (let i = 1; i <= 100; i++) {
        students.push(ensureStudent(i));
        if (i % 20 === 0) console.log(`students ${i}/100`);
    }

    console.log('=== parents ===');
    for (let i = 1; i <= 50; i++) {
        ensureParent(i, [`student${2 * i - 1}@gmail.com`, `student${2 * i}@gmail.com`]);
        if (i % 10 === 0) console.log(`parents ${i}/50`);
    }

    console.log('=== grades ===');
    for (let i = 1; i <= 100; i++) {
        const meta = studentMeta(i);
        seedGrades(teacherTokens[meta.teacherIdx - 1], students[i - 1].studentId);
        if (i % 20 === 0) console.log(`grades ${i}/100`);
    }

    console.log('=== seed done ===');
}

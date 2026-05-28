import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'https://api.edumanager.uk';
const PASSWORD = '1';
const SEMESTER = '2025-1';
const STUDENT_COUNT = 100;
const TEACHER_COUNT = 10;
const STUDENTS_PER_TEACHER = 10;

export const options = {
    scenarios: {
        teacher_read: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 20 },
                { duration: '2m', target: 50 },
                { duration: '30s', target: 0 },
            ],
            exec: 'teacherReadGrades',
            tags: { scenario: 'teacher_read' },
        },
        student_read: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 20 },
                { duration: '2m', target: 50 },
                { duration: '30s', target: 0 },
            ],
            exec: 'studentReadGrades',
            tags: { scenario: 'student_read' },
        },
        teacher_write: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 5 },
                { duration: '2m', target: 10 },
                { duration: '30s', target: 0 },
            ],
            exec: 'teacherWriteQuiz',
            tags: { scenario: 'teacher_write' },
        },
    },
    thresholds: {
        'http_req_failed': ['rate<0.05'],
        'http_req_duration{api:teacher_read_grades}': ['p(95)<1500'],
        'http_req_duration{api:student_read_grades}': ['p(95)<1500'],
        'http_req_duration{api:teacher_write_quiz}':  ['p(95)<2000'],
    },
};

const teacherTokenCache = {};
const studentSessionCache = {};

function jsonHeaders(token) {
    const h = { 'Content-Type': 'application/json' };
    if (token) h.Authorization = `Bearer ${token}`;
    return h;
}

function loginTeacher(idx) {
    if (teacherTokenCache[idx]) return teacherTokenCache[idx];
    const res = http.post(`${BASE}/api/auth/login/email`,
        JSON.stringify({ email: `teacher${idx}@gmail.com`, password: PASSWORD }),
        { headers: jsonHeaders() });
    teacherTokenCache[idx] = { token: res.json('accessToken') };
    return teacherTokenCache[idx];
}

function loginStudent(idx) {
    if (studentSessionCache[idx]) return studentSessionCache[idx];
    const res = http.post(`${BASE}/api/auth/login/email`,
        JSON.stringify({ email: `student${idx}@gmail.com`, password: PASSWORD }),
        { headers: jsonHeaders() });
    studentSessionCache[idx] = {
        token: res.json('accessToken'),
        studentId: res.json('studentId'),
    };
    return studentSessionCache[idx];
}

function randomExam() {
    return Math.random() < 0.5 ? 'MIDTERM' : 'FINAL';
}

export function teacherReadGrades() {
    const teacherIdx = (__VU % TEACHER_COUNT) + 1;
    const { token } = loginTeacher(teacherIdx);
    const studentInClass =
        (teacherIdx - 1) * STUDENTS_PER_TEACHER + Math.floor(Math.random() * STUDENTS_PER_TEACHER) + 1;
    const examType = randomExam();
    const res = http.get(
        `${BASE}/api/students/${studentInClass}/grades?semester=${SEMESTER}&examType=${examType}`,
        { headers: jsonHeaders(token), tags: { api: 'teacher_read_grades' } });
    check(res, { 'teacher_read 200': r => r.status === 200 });
    sleep(Math.random());
}

export function studentReadGrades() {
    const studentIdx = (__VU % STUDENT_COUNT) + 1;
    const { token, studentId } = loginStudent(studentIdx);
    const examType = randomExam();
    const res = http.get(
        `${BASE}/api/students/${studentId}/grades?semester=${SEMESTER}&examType=${examType}`,
        { headers: jsonHeaders(token), tags: { api: 'student_read_grades' } });
    check(res, { 'student_read 200': r => r.status === 200 });
    sleep(Math.random());
}

export function teacherWriteQuiz() {
    const teacherIdx = (__VU % TEACHER_COUNT) + 1;
    const { token } = loginTeacher(teacherIdx);
    const studentInClass =
        (teacherIdx - 1) * STUDENTS_PER_TEACHER + Math.floor(Math.random() * STUDENTS_PER_TEACHER) + 1;
    const res = http.put(
        `${BASE}/api/students/${studentInClass}/grades/batch`,
        JSON.stringify({
            semester: SEMESTER, examType: 'QUIZ',
            create: [{ subject: 'KOREAN', score: Math.floor(Math.random() * 41) + 60 }],
            update: [], delete: [],
        }),
        { headers: jsonHeaders(token), tags: { api: 'teacher_write_quiz' } });
    check(res, { 'teacher_write 200': r => r.status === 200 });
    sleep(Math.random());
}

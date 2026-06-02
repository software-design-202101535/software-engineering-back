import { check } from 'k6';
import { PASSWORD, pad, post, put, get, login } from './seed-common.js';

// ── 기능/시연용 시드 (SUNRIN) ──
// 교사 10 · 학생 100 · 학부모 50. 성적(2025-1) + 상담(공유 포함).
// 부하용 대량 데이터는 seed-volume.js 로 분리.
const SCHOOL = 'SUNRIN_HIGH_SCHOOL';
const SEMESTER = '2025-1';
const SUBJECTS = ['KOREAN', 'MATH', 'ENGLISH', 'SCIENCE', 'SOCIAL'];

// ── 피드백 시드 (학생/학부모 공개여부 섞어 시연용 데이터 확보) ──
const FEEDBACK_CATEGORIES = ['GRADE', 'BEHAVIOR', 'ATTENDANCE', 'ATTITUDE', 'OTHER'];

// ── 상담 시드 (공유상담 검증 + 슬로우쿼리 측정용, SUNRIN 100명 대상) ──
const COUNSELING_PER_STUDENT = Number(__ENV.CPERSTUDENT || 8);
const COUNSELING_SHARED_OUT_OF_10 = 6;   // 10건 중 6건 공유 ≈ 60%

export const options = {
    vus: 1,
    iterations: 1,
    setupTimeout: '10m',
    teardownTimeout: '10m',
};

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

// 피드백 데이터: 학생당 5카테고리 각 1건. 담임이 작성하고, 학생/학부모 공개여부를 섞는다.
// 공개로 만든 건은 생성 시점에 공유 알림(FeedbackSharedEvent)도 함께 발생한다.
function seedFeedbacks(teacherTokens, students) {
    console.log('=== feedbacks ===');
    let count = 0;
    for (let s = 0; s < students.length; s++) {
        const studentId = students[s].studentId;
        const teacherToken = teacherTokens[studentMeta(s + 1).teacherIdx - 1];
        for (let c = 0; c < FEEDBACK_CATEGORIES.length; c++) {
            const category = FEEDBACK_CATEGORIES[c];
            const month = 3 + (c % 6);                   // 3~8월에 분산
            const day = 1 + ((s + c) % 28);
            const studentVisible = ((s + c) % 5) < 3;    // ≈60% 학생 공개
            const parentVisible = ((s + c) % 2) === 0;   // ≈50% 학부모 공개
            const res = post(`/api/students/${studentId}/feedbacks`, {
                category,
                date: `2025-${pad(month)}-${pad(day)}`,
                content: `${category} 관련 피드백 (student${studentId})`,
                studentVisible,
                parentVisible,
            }, teacherToken);
            check(res, { 'feedback created': r => r.status === 201 });
            count++;
        }
        if ((s + 1) % 20 === 0) console.log(`feedbacks ${s + 1}/${students.length}`);
    }
    console.log(`feedbacks done: ${count}`);
}

// 상담 데이터: Part 1 SUNRIN 100명 대상. 작성자는 10명 교사 라운드로빈, 약 60% 공유.
// 공유상담 조회(/shared) 검증 + 슬로우쿼리(YEAR()/LIKE 비-sargable) 측정용.
function seedCounselings(teacherTokens, students) {
    console.log('=== counselings ===');
    let count = 0;
    for (let s = 0; s < students.length; s++) {
        const studentId = students[s].studentId;
        for (let k = 0; k < COUNSELING_PER_STUDENT; k++) {
            const author = teacherTokens[(s + k) % teacherTokens.length];
            const month = 1 + ((s + k) % 12);
            const day = 1 + ((s * 3 + k) % 28);
            const shared = ((s + k) % 10) < COUNSELING_SHARED_OUT_OF_10;
            const res = post(`/api/students/${studentId}/counselings`, {
                counselingDate: `2025-${pad(month)}-${pad(day)}`,
                content: `상담내용 student${studentId} #${k}`,
                nextPlan: '다음 상담 예정',
                nextDate: `2025-${pad(month)}-28`,
                sharedWithTeachers: shared,
            }, author);
            check(res, { 'counseling created': r => r.status === 201 });
            count++;
        }
        if ((s + 1) % 20 === 0) console.log(`counselings ${s + 1}/${students.length}`);
    }
    console.log(`counselings done: ${count} (≈${Math.round(count * COUNSELING_SHARED_OUT_OF_10 / 10)} shared)`);
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

    seedFeedbacks(teacherTokens, students);      // 피드백 (학생/학부모 공개 섞음)
    seedCounselings(teacherTokens, students);    // 공유상담 검증·슬로우쿼리용 (SUNRIN 100명)

    console.log('=== seed-sunrin done ===');
}

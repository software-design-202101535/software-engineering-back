import { check } from 'k6';
import { PASSWORD, rnd, post, put, login } from './seed-common.js';

// ── 볼륨 시드 (ETL/OLAP·인덱스 측정용 대량 데이터, SUNRIN 기능시드와 별개 학교) ──
// 기본 3학년 × 7반 × 30명 = 630명, × 4학기 × 2시험 × 10과목 ≈ 50,400 grade rows
// 작게 돌려보려면: k6 run -e VCLASSES=1 -e VSTUDENTS=5 seed-volume.js
const VOLUME_SCHOOL = 'SEOUL_HIGH_SCHOOL';
const VOLUME_GRADES = [1, 2, 3];
const VOLUME_CLASSES_PER_GRADE = Number(__ENV.VCLASSES || 7);
const VOLUME_STUDENTS_PER_CLASS = Number(__ENV.VSTUDENTS || 30);
const VOLUME_SEMESTERS = ['2024-1', '2024-2', '2025-1', '2025-2'];
const VOLUME_SUBJECTS = ['KOREAN', 'MATH', 'ENGLISH', 'SCIENCE', 'SOCIAL',
                         'HISTORY', 'PHYSICS', 'CHEMISTRY', 'BIOLOGY', 'ETHICS'];

export const options = {
    vus: 1,
    iterations: 1,
    setupTimeout: '10m',
    teardownTimeout: '10m',
};

// 볼륨 데이터: 별도 학교(SEOUL)에 대량 학생·다학기 성적 생성. ETL/OLAP·인덱스 측정용.
// 성적 쓰기는 담임(grade·classNum·school 일치)만 가능 → 반별 담임 교사를 짝지어 생성한다.
function seedVolume() {
    console.log('=== volume: teachers ===');
    const classTeacherToken = {};   // `${grade}-${classNum}` → token
    let tIdx = 0;
    for (const g of VOLUME_GRADES) {
        for (let c = 1; c <= VOLUME_CLASSES_PER_GRADE; c++) {
            tIdx++;
            const email = `vteacher${tIdx}@gmail.com`;
            post('/api/auth/register/teacher', {
                email, password: PASSWORD, passwordConfirm: PASSWORD,
                name: `vteacher${tIdx}`, school: VOLUME_SCHOOL, grade: g, classNum: c,
                termsAgreed: true, privacyAgreed: true,
            });
            classTeacherToken[`${g}-${c}`] = login(email).accessToken;
        }
    }

    console.log('=== volume: students + grades ===');
    let sNum = 0;
    let rows = 0;
    for (const g of VOLUME_GRADES) {
        for (let c = 1; c <= VOLUME_CLASSES_PER_GRADE; c++) {
            const tToken = classTeacherToken[`${g}-${c}`];
            for (let n = 1; n <= VOLUME_STUDENTS_PER_CLASS; n++) {
                sNum++;
                const email = `vstudent${sNum}@gmail.com`;
                post('/api/auth/register/student', {
                    email, password: PASSWORD, passwordConfirm: PASSWORD,
                    name: `vstudent${sNum}`, school: VOLUME_SCHOOL, grade: g, classNum: c, number: n,
                    termsAgreed: true, privacyAgreed: true,
                });
                const studentId = login(email).studentId;
                for (const semester of VOLUME_SEMESTERS) {
                    for (const examType of ['MIDTERM', 'FINAL']) {
                        const create = VOLUME_SUBJECTS.map(subj => ({ subject: subj, score: rnd(60, 100) }));
                        const res = put(`/api/students/${studentId}/grades/batch`,
                            { semester, examType, create, update: [], delete: [] }, tToken);
                        check(res, { 'volume batch ok': r => r.status === 200 });
                        rows += create.length;
                    }
                }
            }
            console.log(`volume class ${g}-${c} done (rows so far: ${rows})`);
        }
    }
    console.log(`volume done: ${sNum} students, ${rows} grade rows`);
}

export default function () {
    seedVolume();   // ETL/OLAP·인덱스 측정용 대량 데이터 (SEOUL)
    console.log('=== seed-volume done ===');
}

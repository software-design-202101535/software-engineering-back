package com.example.edumanager.domain.grade.entity;

public enum GradeLevel {
    A, B, C, D, F;

    public static GradeLevel from(int score) {
        if (score >= 90) return A;
        if (score >= 80) return B;
        if (score >= 70) return C;
        if (score >= 60) return D;
        return F;
    }
}

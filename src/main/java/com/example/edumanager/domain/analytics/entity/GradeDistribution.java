package com.example.edumanager.domain.analytics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 등급(A~F) 분포 값 객체. 반/학년 집계 행에 임베드된다.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GradeDistribution {

    @Column(name = "dist_a", nullable = false)
    private int a;

    @Column(name = "dist_b", nullable = false)
    private int b;

    @Column(name = "dist_c", nullable = false)
    private int c;

    @Column(name = "dist_d", nullable = false)
    private int d;

    @Column(name = "dist_f", nullable = false)
    private int f;

    private GradeDistribution(int a, int b, int c, int d, int f) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.f = f;
    }

    public static GradeDistribution of(int a, int b, int c, int d, int f) {
        return new GradeDistribution(a, b, c, d, f);
    }
}

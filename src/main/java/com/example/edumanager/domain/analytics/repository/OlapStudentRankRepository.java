package com.example.edumanager.domain.analytics.repository;

import com.example.edumanager.domain.analytics.entity.OlapStudentRank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OlapStudentRankRepository extends JpaRepository<OlapStudentRank, Long> {

    List<OlapStudentRank> findByStudentIdAndSemester(Long studentId, String semester);
}

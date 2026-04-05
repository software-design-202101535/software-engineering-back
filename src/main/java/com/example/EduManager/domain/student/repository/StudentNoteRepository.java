package com.example.EduManager.domain.student.repository;

import com.example.EduManager.domain.student.entity.NoteCategory;
import com.example.EduManager.domain.student.entity.StudentNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentNoteRepository extends JpaRepository<StudentNote, Long> {

    @Query("SELECT n FROM StudentNote n JOIN FETCH n.student JOIN FETCH n.teacher " +
            "WHERE n.student.id = :studentId ORDER BY n.date DESC")
    List<StudentNote> findAllByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT n FROM StudentNote n JOIN FETCH n.student JOIN FETCH n.teacher " +
            "WHERE n.student.id = :studentId AND n.category = :category ORDER BY n.date DESC")
    List<StudentNote> findAllByStudentIdAndCategory(@Param("studentId") Long studentId,
                                                    @Param("category") NoteCategory category);

    Optional<StudentNote> findByIdAndStudentId(Long id, Long studentId);
}

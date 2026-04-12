package com.example.EduManager.domain.student.service;

import com.example.EduManager.domain.student.dto.CreateNoteRequest;
import com.example.EduManager.domain.student.dto.UpdateNoteRequest;
import com.example.EduManager.domain.student.entity.NoteCategory;
import com.example.EduManager.domain.student.entity.StudentNote;
import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.student.repository.StudentNoteRepository;
import com.example.EduManager.domain.user.entity.User;
import com.example.EduManager.global.exception.CustomException;
import com.example.EduManager.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentNoteService {

    private final StudentNoteRepository studentNoteRepository;

    public List<StudentNote> findByStudentAndCategory(Long studentId, NoteCategory category) {
        if (category != null) {
            return studentNoteRepository.findAllByStudentIdAndCategory(studentId, category);
        }
        return studentNoteRepository.findAllByStudentId(studentId);
    }

    public StudentNote save(StudentProfile student, CreateNoteRequest request, User teacher) {
        return studentNoteRepository.save(
                StudentNote.of(student, request.getCategory(), request.getContent(), request.getDate(), teacher)
        );
    }

    public StudentNote update(StudentNote note, UpdateNoteRequest request) {
        note.update(request.getCategory(), request.getContent(), request.getDate());
        return note;
    }

    public void delete(StudentNote note) {
        studentNoteRepository.delete(note);
    }

    public StudentNote getByIdAndStudentId(Long noteId, Long studentId) {
        return studentNoteRepository.findByIdAndStudentId(noteId, studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTE_NOT_FOUND));
    }
}

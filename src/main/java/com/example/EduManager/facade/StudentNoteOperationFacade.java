package com.example.EduManager.facade;

import com.example.EduManager.domain.student.dto.CreateNoteRequest;
import com.example.EduManager.domain.student.dto.NoteResponse;
import com.example.EduManager.domain.student.dto.UpdateNoteRequest;
import com.example.EduManager.domain.student.entity.NoteCategory;
import com.example.EduManager.domain.student.entity.StudentNote;
import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.student.service.StudentNoteService;
import com.example.EduManager.domain.student.service.StudentService;
import com.example.EduManager.domain.teacher.entity.TeacherProfile;
import com.example.EduManager.domain.teacher.service.TeacherService;
import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.global.exception.CustomException;
import com.example.EduManager.global.exception.ErrorCode;
import com.example.EduManager.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StudentNoteOperationFacade {

    private final StudentNoteService studentNoteService;
    private final StudentService studentService;
    private final TeacherService teacherService;

    @Transactional(readOnly = true)
    public List<NoteResponse> getList(Long studentId, NoteCategory category, UserDetailsImpl userDetails) {
        StudentProfile student = studentService.getById(studentId);
        checkHomeroomAccess(userDetails.getUserId(), student, userDetails.getRole());
        return studentNoteService.findByStudentAndCategory(studentId, category).stream()
                .map(NoteResponse::of)
                .toList();
    }

    @Transactional
    public NoteResponse create(Long studentId, CreateNoteRequest request, UserDetailsImpl userDetails) {
        StudentProfile student = studentService.getById(studentId);
        checkHomeroomAccess(userDetails.getUserId(), student, userDetails.getRole());
        return NoteResponse.of(
                studentNoteService.save(student, request, teacherService.getProfileByUserId(userDetails.getUserId()))
        );
    }

    @Transactional
    public NoteResponse update(Long studentId, Long noteId, UpdateNoteRequest request, UserDetailsImpl userDetails) {
        StudentProfile student = studentService.getById(studentId);
        checkHomeroomAccess(userDetails.getUserId(), student, userDetails.getRole());
        StudentNote note = studentNoteService.getByIdAndStudentId(noteId, student.getId());
        return NoteResponse.of(studentNoteService.update(note, request));
    }

    @Transactional
    public void delete(Long studentId, Long noteId, UserDetailsImpl userDetails) {
        StudentProfile student = studentService.getById(studentId);
        checkHomeroomAccess(userDetails.getUserId(), student, userDetails.getRole());
        StudentNote note = studentNoteService.getByIdAndStudentId(noteId, student.getId());
        studentNoteService.delete(note);
    }

    private void checkHomeroomAccess(Long teacherUserId, StudentProfile student, Role role) {
        if (role == Role.ADMIN) return;
        if (role != Role.TEACHER) throw new CustomException(ErrorCode.STUDENT_ACCESS_DENIED);

        TeacherProfile teacher = teacherService.getProfileByUserId(teacherUserId);
        boolean isHomeroom = teacher.getGrade() == student.getGrade()
                && teacher.getClassNum() == student.getClassNum()
                && teacher.getUser().getSchool() == student.getUser().getSchool();

        if (!isHomeroom) throw new CustomException(ErrorCode.STUDENT_ACCESS_DENIED);
    }
}

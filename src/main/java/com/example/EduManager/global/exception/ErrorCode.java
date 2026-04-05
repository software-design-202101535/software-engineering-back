package com.example.EduManager.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "해당 유저를 찾을 수 없습니다."),
    USER_DELETED(HttpStatus.GONE, 410, "삭제된 사용자입니다."),
    DUPLICATED_USER(HttpStatus.CONFLICT, 409, "이미 존재하는 유저입니다."),

    // JWT
    JWT_ENTRY_POINT(HttpStatus.UNAUTHORIZED, 401, "로그인이 필요합니다."),
    JWT_ACCESS_DENIED(HttpStatus.FORBIDDEN, 403, "접근 권한이 없습니다."),
    JWT_SIGNATURE(HttpStatus.UNAUTHORIZED, 401, "인증에 실패했습니다. 다시 로그인해주세요."),
    JWT_MALFORMED(HttpStatus.UNAUTHORIZED, 401, "인증에 실패했습니다. 다시 로그인해주세요."),
    JWT_ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 401, "로그인이 만료되었습니다. 다시 로그인해주세요."),
    JWT_UNSUPPORTED(HttpStatus.UNAUTHORIZED, 401, "인증에 실패했습니다. 다시 로그인해주세요."),
    JWT_NOT_VALID(HttpStatus.UNAUTHORIZED, 401, "인증에 실패했습니다. 다시 로그인해주세요."),
    JWT_REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, 401, "Refresh Token을 찾을 수 없습니다."),
    JWT_REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 401, "Refresh Token이 만료되었습니다. 다시 로그인해주세요."),

    // 인증
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, 401, "이메일 또는 비밀번호가 올바르지 않습니다."),

    // HTTP 요청
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 405, "잘못된 요청 방식입니다."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, 400, "요청에 필요한 입력이 누락되었습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, 400, "입력 형식이 올바르지 않습니다."),
    API_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "요청한 페이지를 찾을 수 없습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, 400, "잘못된 입력입니다."),

    // 학생
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "해당 학생을 찾을 수 없습니다."),
    STUDENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, 403, "학생 정보에 접근할 권한이 없습니다."),

    // 출결
    ATTENDANCE_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "해당 출결 기록을 찾을 수 없습니다."),

    // 성적
    GRADE_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "해당 성적을 찾을 수 없습니다."),
    GRADE_ALREADY_EXISTS(HttpStatus.CONFLICT, 409, "이미 등록된 성적입니다."),
    GRADE_ACCESS_DENIED(HttpStatus.FORBIDDEN, 403, "해당 성적에 접근할 권한이 없습니다."),

    // 데이터베이스
    DATABASE_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, 409, "이미 존재하는 데이터이거나 처리할 수 없는 요청입니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}

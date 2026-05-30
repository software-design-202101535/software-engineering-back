package com.example.edumanager.domain.counseling.controller;

import com.example.edumanager.domain.counseling.dto.SharedCounselingResponse;
import com.example.edumanager.facade.CounselingOperationFacade;
import com.example.edumanager.global.security.UserDetailsImpl;
import com.example.edumanager.global.swagger.SharedCounselingApiSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/counselings")
@RequiredArgsConstructor
public class SharedCounselingController implements SharedCounselingApiSpecification {

    private final CounselingOperationFacade counselingOperationFacade;

    @GetMapping("/shared")
    public ResponseEntity<List<SharedCounselingResponse>> getSharedList(
            @RequestParam int year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer grade,
            @RequestParam(required = false) Integer classNum,
            @RequestParam(required = false) String name,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(
                counselingOperationFacade.getSharedList(year, month, grade, classNum, name, userDetails));
    }
}

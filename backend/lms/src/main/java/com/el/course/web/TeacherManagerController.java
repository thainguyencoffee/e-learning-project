package com.el.course.web;

import com.el.course.application.TeacherService;
import com.el.course.application.dto.teacher.TeacherDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/teachers", produces = "application/json")
public class TeacherManagerController {

    private final TeacherService teacherService;

    public TeacherManagerController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public ResponseEntity<Page<TeacherDTO>> getTeachers(Pageable pageable) {
        List<TeacherDTO> result = teacherService.getAllTeacherInfoAndCountData(pageable);
        return ResponseEntity.ok(new PageImpl<>(result, pageable, result.size()));
    }

}

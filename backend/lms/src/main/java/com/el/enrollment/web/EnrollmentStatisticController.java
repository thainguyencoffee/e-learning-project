package com.el.enrollment.web;

import com.el.enrollment.application.EnrollmentStatisticService;
import com.el.enrollment.application.dto.CourseInfoWithEnrollmentStatisticDTO;
import com.el.enrollment.application.dto.CourseInfoWithEnrollmentsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "enrollments/statistics", produces = "application/json")
public class EnrollmentStatisticController {

    private final EnrollmentStatisticService enrollmentStatisticService;

    public EnrollmentStatisticController(EnrollmentStatisticService enrollmentStatisticService) {
        this.enrollmentStatisticService = enrollmentStatisticService;
    }

    @GetMapping
    public ResponseEntity<Page<CourseInfoWithEnrollmentStatisticDTO>> minInfoWithEnrollmentStatistic(Pageable pageable) {
        log.info("Request to get courses min info and enrollment statistics");
        List<CourseInfoWithEnrollmentStatisticDTO> result = enrollmentStatisticService.getCourseMinInfoWithEnrollmentStatistics(pageable);
        return ResponseEntity.ok(new PageImpl<>(result, pageable, result.size()));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseInfoWithEnrollmentsDTO> courseWithEnrollmentStatistic(@PathVariable Long courseId) {
        log.info("Request to get course with enrollment statistics");
        CourseInfoWithEnrollmentsDTO result = enrollmentStatisticService.getCourseWithEnrollmentStatistics(courseId);
        return ResponseEntity.ok(result);
    }


}

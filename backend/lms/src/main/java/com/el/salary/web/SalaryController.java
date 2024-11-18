package com.el.salary.web;

import com.el.common.exception.ResourceNotFoundException;
import com.el.salary.domain.Salary;
import com.el.salary.domain.SalaryDTO;
import com.el.salary.domain.SalaryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "salaries", produces = "application/json")
public class SalaryController {

    private final SalaryRepository salaryRepository;

    public SalaryController(SalaryRepository salaryRepository) {
        this.salaryRepository = salaryRepository;
    }

    @GetMapping
    public ResponseEntity<Page<SalaryDTO>> getSalaries(Pageable pageable) {
        return ResponseEntity.ok(salaryRepository.findAll(pageable));
    }

    @GetMapping("/{teacher}")
    public ResponseEntity<Salary> getSalaryDetail(@PathVariable String teacher) {
        return ResponseEntity.ok(salaryRepository.findByTeacher(teacher).orElseThrow(ResourceNotFoundException::new));
    }

}

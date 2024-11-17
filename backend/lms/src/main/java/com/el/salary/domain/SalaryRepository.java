package com.el.salary.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SalaryRepository extends CrudRepository<Salary, Long> {

    Page<SalaryDTO> findAll(Pageable pageable);

    Optional<Salary> findByTeacher(String teacher);

}

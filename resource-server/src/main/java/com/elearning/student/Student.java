package com.elearning.student;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("student")
@ToString
public class Student {
    @Id
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
}

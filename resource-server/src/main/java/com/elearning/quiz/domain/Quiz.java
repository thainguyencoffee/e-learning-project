package com.elearning.quiz.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.HashSet;
import java.util.Set;

@Table("quiz")
public class Quiz {
    @Id
    private Long id;
    private String title;
    @MappedCollection(idColumn = "quiz")
    private Set<Question> questions = new HashSet<>();

}
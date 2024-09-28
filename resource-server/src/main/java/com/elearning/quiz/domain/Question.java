package com.elearning.quiz.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Table("question")
public class Question {
    @Id
    private Long id;
    private String prompt;
    private List<AnswerOption> options;
    private Integer correct;

}
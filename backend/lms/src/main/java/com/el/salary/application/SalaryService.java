package com.el.salary.application;

public interface SalaryService {

    void adjustRank(String teacher, boolean byCourse);

    void addSalaryRecordForAllTeachers();

    void decreaseStudent(String teacher);

    void studentChanged(String oldTeacher, String newTeacher);
}

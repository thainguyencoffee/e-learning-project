package com.el.salary.application;

public interface SalaryService {

    void adjustRank(String teacher, boolean byCourse);

    void addSalaryRecordForAllTeachers();
}

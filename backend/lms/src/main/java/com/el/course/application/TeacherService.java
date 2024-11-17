package com.el.course.application;

import com.el.common.auth.application.impl.KeycloakUsersManagement;
import com.el.common.auth.web.dto.UserInfo;
import com.el.course.application.dto.teacher.CountDataDTO;
import com.el.course.domain.CourseRepository;
import com.el.course.application.dto.teacher.TeacherDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TeacherService {

    private final KeycloakUsersManagement keycloakUsersManagement;
    private final CourseRepository courseRepository;

    public TeacherService(KeycloakUsersManagement keycloakUsersManagement, CourseRepository courseRepository) {
        this.keycloakUsersManagement = keycloakUsersManagement;
        this.courseRepository = courseRepository;
    }

    public List<TeacherDTO> getAllTeacherInfoAndCountData(Pageable pageable) {
        List<UserInfo> userInfoList = keycloakUsersManagement.searchByRole("teacher", pageable)
                .stream().map(UserInfo::fromUserRepresentation)
                .toList();
        log.info("TeacherService found {} teachers", userInfoList.size());
        return userInfoList.stream().map(info -> {
            CountDataDTO countData = courseRepository.getCountDataDTOByTeacher(info.username());
            return new TeacherDTO(info, countData);
        }).toList();
    }

}

package com.el.common;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public interface TimeUtils {

    static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

}

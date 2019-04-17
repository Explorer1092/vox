package com.voxlearning.utopia.service.guest.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.guest.api.mapper.XuebaStudentMapper;
import com.voxlearning.utopia.service.guest.api.mapper.XuebaTeacherMapper;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 *
 *
 * @author changyuan
 * @since 2017/2/28
 */
@ServiceVersion(version = "20170228")
@ServiceTimeout(timeout = 300, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface XuebaService {

    MapMessage bulkCreateTeachers(Collection<XuebaTeacherMapper> teacherMappers);

    MapMessage bulkAddStudents(Collection<XuebaStudentMapper> studentMappers, Long teacherId);
}

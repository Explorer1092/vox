package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.newhomework.api.mapper.SemesterReport;

public interface FetchStudentSemesterReportTemple {
    Subject getSubject();

    SemesterReport doFetchSemesterReport(Long studentId, String subject);
}

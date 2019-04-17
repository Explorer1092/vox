package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.context.outside.OutsideReadingContext;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181115")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface OutsideReadingService {
    MapMessage loadTeacherClazzList(Long teacherId);

    MapMessage loadReportClazzList(Long teacherId);

    MapMessage loadBookTypeList();

    MapMessage loadBookList(Long teacherId, Long groupId, Integer clazzLevel, String bookType, Integer pageNumber, Integer pageSize);

    MapMessage loadBookDetail(Long teacherId, Long groupId, String bookId);

    MapMessage confirm(Long teacherId, Long groupId);

    MapMessage assign(Long teacherId, Long groupId, String bookIds, Integer planDays, String endDate);

    MapMessage processResult(OutsideReadingContext context);

    MapMessage saveGoldenWords(Long userId, String outsideReadingId, String missionId, List<String> missionIndexes);

    MapMessage modifyEndTime(String readingId, Date endTime);

    MapMessage loadReadingReport(TeacherDetail teacherDetail, Map<Long, String> groupIdNameMap, String cdnUrl);

    MapMessage crmDeleteOutsideReading(String readingId);
}

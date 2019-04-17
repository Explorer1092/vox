package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.context.outside.OutsideReadingContext;
import com.voxlearning.utopia.service.newhomework.api.service.OutsideReadingService;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import lombok.Getter;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class OutsideReadingServiceClient implements OutsideReadingService {
    @Getter
    @ImportService(interfaceClass = OutsideReadingService.class)
    private OutsideReadingService remoteReference;

    @Override
    public MapMessage loadTeacherClazzList(Long teacherId) {
        return remoteReference.loadTeacherClazzList(teacherId);
    }

    @Override
    public MapMessage loadReportClazzList(Long teacherId) {
        return remoteReference.loadReportClazzList(teacherId);
    }

    @Override
    public MapMessage loadBookTypeList() {
        return remoteReference.loadBookTypeList();
    }

    @Override
    public MapMessage loadBookList(Long teacherId, Long groupId, Integer clazzLevel, String bookType, Integer pageNumber, Integer pageSize) {
        return remoteReference.loadBookList(teacherId, groupId, clazzLevel, bookType, pageNumber, pageSize);
    }

    @Override
    public MapMessage loadBookDetail(Long teacherId, Long groupId, String bookId) {
        return remoteReference.loadBookDetail(teacherId, groupId, bookId);
    }

    @Override
    public MapMessage confirm(Long teacherId, Long groupId) {
        return remoteReference.confirm(teacherId, groupId);
    }

    @Override
    public MapMessage assign(Long teacherId, Long groupId, String bookIds, Integer planDays, String endDate) {
        return remoteReference.assign(teacherId, groupId, bookIds, planDays, endDate);
    }

    @Override
    public MapMessage processResult(OutsideReadingContext context) {
        return remoteReference.processResult(context);
    }

    @Override
    public MapMessage saveGoldenWords(Long userId, String outsideReadingId, String missionId, List<String> missionIndexes) {
        return remoteReference.saveGoldenWords(userId, outsideReadingId, missionId, missionIndexes);
    }

    @Override
    public MapMessage modifyEndTime(String readingId, Date endTime) {
        return remoteReference.modifyEndTime(readingId, endTime);
    }

    @Override
    public MapMessage loadReadingReport(TeacherDetail teacherDetail, Map<Long, String> groupIdNameMap, String cdnUrl) {
        return remoteReference.loadReadingReport(teacherDetail, groupIdNameMap, cdnUrl);
    }

    @Override
    public MapMessage crmDeleteOutsideReading(String readingId) {
        return remoteReference.crmDeleteOutsideReading(readingId);
    }
}

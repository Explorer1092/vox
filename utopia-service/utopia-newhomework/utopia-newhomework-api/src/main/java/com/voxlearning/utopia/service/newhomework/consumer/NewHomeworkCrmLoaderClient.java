package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.mapper.DisplayStudentHomeWorkHistoryMapper;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkCrmLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkBlackWhiteList;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.crm.CrmUnitQuestion;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import java.util.*;

/**
 * @author xuesong.zhang
 * @since 2017/1/16
 */
public class NewHomeworkCrmLoaderClient implements NewHomeworkCrmLoader {

    @ImportService(interfaceClass = NewHomeworkCrmLoader.class)
    private NewHomeworkCrmLoader remoteReference;

    @Override
    public Collection<String> findIdsByCheckedTimes(Date start, Date end) {
        return remoteReference.findIdsByCheckedTimes(start, end);
    }

    @Override
    public Collection<String> findIdsByTeacherIdAndCheckedTimes(Long teacherId, Date start) {
        return remoteReference.findIdsByTeacherIdAndCheckedTimes(teacherId, start);
    }

    @Override
    public Collection<NewHomework.Location> findIdsByTeacherIdAndCreateAt(Long teacherId, Date start, Date end) {
        return remoteReference.findIdsByTeacherIdAndCreateAt(teacherId, start, end);
    }

    @Override
    public List<NewHomework.Location> findHomeworkByEndTime(Date begin, Date end) {
        return remoteReference.findHomeworkByEndTime(begin, end);
    }

    @Override
    public Page<NewHomework.Location> loadGroupNewHomeworks(Collection<Long> groupIds, Date startDate, Date endDate, Pageable pageable, boolean includeDisabled) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return new PageImpl<>(Collections.<NewHomework.Location>emptyList(), pageable, 0);
        }
        return remoteReference.loadGroupNewHomeworks(groupIds, startDate, endDate, pageable, includeDisabled);
    }

    @Override
    public List<DisplayStudentHomeWorkHistoryMapper> crmLoadStudentNewHomeworkHistory(StudentDetail student, Date startDate, Date endDate) {
        return remoteReference.crmLoadStudentNewHomeworkHistory(student, startDate, endDate);
    }

    @Override
    public List<CrmUnitQuestion> fetchCrmUnitQuestion(boolean isVacationHomework, String hid) {
        return remoteReference.fetchCrmUnitQuestion(isVacationHomework, hid);
    }

    @Override
    public Map<String, Object> studentSpecNewHomeworkDetail(Long studentId, String homeworkId) {
        return remoteReference.studentSpecNewHomeworkDetail(studentId, homeworkId);
    }

    @Override
    public Map<String, Object> homeworkNewHomepage(String homeworkId) {
        return remoteReference.homeworkNewHomepage(homeworkId);
    }

    @Override
    public Map<String, Object> vacationHomeworkNewHomepage(String hid) {
        return remoteReference.vacationHomeworkNewHomepage(hid);
    }

    @Override
    public PageImpl<HomeworkBlackWhiteList> loadNewHomeworkBlackWhiteLists(String businessType, String idType, String blackWhiteId, Pageable pageable) {
        return remoteReference.loadNewHomeworkBlackWhiteLists(businessType, idType, blackWhiteId, pageable);
    }

}

package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.mapper.DisplayStudentHomeWorkHistoryMapper;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkBlackWhiteList;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.crm.CrmUnitQuestion;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 注：这里的方法由于是给CRM使用，所以请先去对应的实现里确认一下参数和返回数据是否是期望的
 *
 * @author xuesong.zhang
 * @since 2017/1/16
 */
@ServiceVersion(version = "20190123")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 1)
@CyclopsMonitor("utopia")
public interface NewHomeworkCrmLoader extends IPingable {

    @Idempotent
    Collection<String> findIdsByCheckedTimes(Date start, Date end);

    @Idempotent
    Collection<String> findIdsByTeacherIdAndCheckedTimes(Long teacherId, Date start);

    @Idempotent
    Collection<NewHomework.Location> findIdsByTeacherIdAndCreateAt(Long teacherId, Date start, Date end);

    @Idempotent
    List<NewHomework.Location> findHomeworkByEndTime(Date begin, Date end);

    @Idempotent
    Page<NewHomework.Location> loadGroupNewHomeworks(Collection<Long> groupIds, Date startDate, Date endDate, Pageable pageable, boolean includeDisabled);

    @Idempotent
    List<DisplayStudentHomeWorkHistoryMapper> crmLoadStudentNewHomeworkHistory(StudentDetail student, Date startDate, Date endDate);

    @Idempotent
    List<CrmUnitQuestion> fetchCrmUnitQuestion(boolean isVacationHomework, String hid);

    @Idempotent
    Map<String, Object> studentSpecNewHomeworkDetail(Long studentId, String homeworkId);

    @Idempotent
    Map<String, Object> homeworkNewHomepage(String homeworkId);

    @Idempotent
    Map<String, Object> vacationHomeworkNewHomepage(String hid);

    @Idempotent
    PageImpl<HomeworkBlackWhiteList> loadNewHomeworkBlackWhiteLists(String businessType, String idType, String blackWhiteId, Pageable pageable);

}

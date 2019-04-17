package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.ExClazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author tanguohong
 * @since 2016/11/29
 */
@ServiceVersion(version = "20171207")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface VacationHomeworkLoader extends IPingable {

    /**
     * 根据clazzGroupIds获取假期作业包（不包含已删除），CreateAt desc
     *
     * @param groupIds 班组ids
     * @return Map
     */
    Map<Long, List<VacationHomework.Location>> loadVacationHomeworksByClazzGroupIds(Collection<Long> groupIds);

    /**
     * 根据clazzGroupIds获取假期作业（不包含已删除），CreateAt desc
     *
     * @param groupIds 班组ids
     * @return Map
     */
    Map<Long, List<VacationHomeworkPackage.Location>> loadVacationHomeworkPackageByClazzGroupIds(Collection<Long> groupIds);

    /**
     * 获取可布置假期作业的班级
     */
    List<ExClazz> findTeacherClazzsCanBeAssignedHomework(Teacher teacher);

    MapMessage loadTeachersClazzListForApp(Collection<Long> teacherIds,String domain);

    MapMessage loadBookPlanInfo(String bookId);

    MapMessage loadDayPlanElements(TeacherDetail teacherDetail, String bookId, Integer weekRank, Integer dayRank);

    MapMessage loadStudentDayPackages(String packageId, Long studentId);

    Map<String, Object> levelState(Long studentId, String packageId, Integer finishedCount);

    Map<String, Object> loadQuestionAnswer(ObjectiveConfigType objectiveConfigType, String homeworkId, Integer categoryId, String lessonId, String videoId, String questionBoxId);

    Map<String, Object> loadHomeworkQuestions(String homeworkId, ObjectiveConfigType objectiveConfigType, Integer categoryId, String lessonId, String videoId, String questionBoxId);

    Map<String, Object> indexData(String homeworkId, Long studentId);

    VacationHomework loadVacationHomeworkIncludeDisabled(String id);

    Map<String, VacationHomework> loadVacationHomeworksIncludeDisabled(Collection<String> ids);

    VacationHomework loadVacationHomeworkById(String id);

    VacationHomeworkPackage loadVacationHomeworkPackageById(String packageId);
}

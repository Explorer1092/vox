package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.VacationHomeworkLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.ExClazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author tanguohong
 * @since 2016/11/29
 */
public class VacationHomeworkLoaderClient implements VacationHomeworkLoader {
    @ImportService(interfaceClass = VacationHomeworkLoader.class)
    private VacationHomeworkLoader hydraRemoteReference;

    @Override
    public Map<Long, List<VacationHomework.Location>> loadVacationHomeworksByClazzGroupIds(Collection<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyMap();
        }
        return hydraRemoteReference.loadVacationHomeworksByClazzGroupIds(groupIds);
    }

    @Override
    public Map<Long, List<VacationHomeworkPackage.Location>> loadVacationHomeworkPackageByClazzGroupIds(Collection<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyMap();
        }
        return hydraRemoteReference.loadVacationHomeworkPackageByClazzGroupIds(groupIds);
    }

    @Override
    public List<ExClazz> findTeacherClazzsCanBeAssignedHomework(Teacher teacher) {
        return hydraRemoteReference.findTeacherClazzsCanBeAssignedHomework(teacher);
    }

    @Override
    public MapMessage loadTeachersClazzListForApp(Collection<Long> teacherIds,String domain) {
        return hydraRemoteReference.loadTeachersClazzListForApp(teacherIds,domain);
    }

    @Override
    public MapMessage loadBookPlanInfo(String bookId) {
        return hydraRemoteReference.loadBookPlanInfo(bookId);
    }

    @Override
    public MapMessage loadDayPlanElements(TeacherDetail teacherDetail, String bookId, Integer weekRank, Integer dayRank) {
        return hydraRemoteReference.loadDayPlanElements(teacherDetail, bookId, weekRank, dayRank);
    }

    @Override
    public MapMessage loadStudentDayPackages(String packageId, Long studentId) {
        return hydraRemoteReference.loadStudentDayPackages(packageId, studentId);
    }

    @Override
    public Map<String, Object> levelState(Long studentId, String packageId, Integer finishedCount) {
        return hydraRemoteReference.levelState(studentId, packageId, finishedCount);
    }

    @Override
    public VacationHomework loadVacationHomeworkIncludeDisabled(String id) {
        if (id == null) {
            return null;
        }
        return hydraRemoteReference.loadVacationHomeworkIncludeDisabled(id);
    }

    @Override
    public Map<String, VacationHomework> loadVacationHomeworksIncludeDisabled(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return hydraRemoteReference.loadVacationHomeworksIncludeDisabled(ids);
    }

    @Override
    public Map<String, Object> loadQuestionAnswer(ObjectiveConfigType objectiveConfigType, String homeworkId, Integer categoryId, String lessonId, String videoId, String questionBoxId) {
        return hydraRemoteReference.loadQuestionAnswer(objectiveConfigType, homeworkId, categoryId, lessonId, videoId, questionBoxId);
    }

    @Override
    public Map<String, Object> loadHomeworkQuestions(String homeworkId, ObjectiveConfigType objectiveConfigType, Integer categoryId, String lessonId, String videoId, String questionBoxId) {
        return hydraRemoteReference.loadHomeworkQuestions(homeworkId, objectiveConfigType, categoryId, lessonId, videoId, questionBoxId);
    }

    @Override
    public Map<String, Object> indexData(String homeworkId, Long studentId) {
        return hydraRemoteReference.indexData(homeworkId, studentId);
    }

    @Override
    public VacationHomework loadVacationHomeworkById(String id) {
        return hydraRemoteReference.loadVacationHomeworkById(id);
    }

    @Override
    public VacationHomeworkPackage loadVacationHomeworkPackageById(String packageId) {
        return hydraRemoteReference.loadVacationHomeworkPackageById(packageId);
    }
}

package com.voxlearning.utopia.service.newhomework.api.service;


import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentFinishHomeworkPopup;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author xuesong.zhang
 * @since 2017/6/6
 */
@ServiceVersion(version = "20180322")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface NewHomeworkCacheService {

    Map<Long, Integer> monthFinishHomeworkCountManager_currentCount(Collection<Long> userIds, HomeworkType homeworkType);

    // AssignHomeworkAndQuizDayCountManager
    Map<Long, Set<String>> assignHomeworkAndQuizDayCountManager_currentDays(Collection<Long> userIds);

    // TeacherNewHomeworkCommentLibraryManager
    List<String> teacherNewHomeworkCommentLibraryManager_find(Long teacherId);

    void teacherNewHomeworkCommentLibraryManager_addComment(Long teacherId, String comment);

    boolean teacherNewHomeworkCommentLibraryManager_removeComment(Long teacherId, String comment);

    void vacationHomeworkIntegralCacheManager_recordStudentReward(Long studentId, String vacationHomeworkId, Integer level);

    boolean vacationHomeworkIntegralCacheManager_studentRewarded(Long studentId, String vacationHomeworkId, Integer level);

    StudentFinishHomeworkPopup studentFinishHomeworkPopupManager_loadStudentPopup(Long studentId, Long parentId);

    Map<Long,String> fetchHomeworkIdByGroupIdFromCache(Set<Long> groupIds);
}

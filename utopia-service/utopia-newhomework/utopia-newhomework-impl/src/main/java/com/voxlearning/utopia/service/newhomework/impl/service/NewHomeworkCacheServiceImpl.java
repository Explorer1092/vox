package com.voxlearning.utopia.service.newhomework.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentFinishHomeworkPopup;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkCacheService;
import com.voxlearning.utopia.service.newhomework.cache.HomeworkCache;
import com.voxlearning.utopia.service.newhomework.consumer.cache.*;
import lombok.Getter;

import javax.inject.Named;
import java.util.*;

/**
 * @author xuesong.zhang
 * @since 2017/6/6
 */
@Named
@ExposeService(interfaceClass = NewHomeworkCacheService.class)
public class NewHomeworkCacheServiceImpl extends SpringContainerSupport implements NewHomeworkCacheService {

    @Getter private MonthFinishHomeworkCountManager monthFinishHomeworkCountManager;
    @Getter private CheckHomeworkIntegralCacheManager checkHomeworkIntegralCacheManager;
    @Getter private AssignHomeworkAndQuizDayCountManager assignHomeworkAndQuizDayCountManager;
    @Getter private TeacherNewHomeworkCommentLibraryManager teacherNewHomeworkCommentLibraryManager;
    @Getter private VacationHomeworkIntegralCacheManager vacationHomeworkIntegralCacheManager;
    @Getter private VacationHomeworkCacheManager vacationHomeworkCacheManager;
    @Getter private VacationHomeworkWinterPlanCacheManager vacationHomeworkWinterPlanCacheManager;
    @Getter private StudentFinishHomeworkPopupManager studentFinishHomeworkPopupManager;
    @Getter private WeekReportCacheManager weekReportCacheManager;
    @Getter private UrgeNewHomeworkUnFinishCacheManager urgeNewHomeworkUnFinishCacheManager;
    @Getter private UrgeNewHomeworkUnCorrectCacheManager urgeNewHomeworkUnCorrectCacheManager;
    @Getter private BasicReviewHomeworkShareCacheManager basicReviewHomeworkShareCacheManager;
    @Getter private NoticeShareReportToJztCacheManager noticeShareReportToJztCacheManager;
    @Getter private BasicReviewHomeworkCacheManager basicReviewHomeworkCacheManager;
    @Getter private ShareVacationReportCacheManager shareVacationReportCacheManager;
    @Getter private ShareWeiXinVacationReportCacheManager shareWeiXinVacationReportCacheManager;
    @Getter private GroupHomeworkRecordCacheManager groupHomeworkRecordCacheManager;
    @Getter private ActivityHomeworkParentRewardCacheManager activityHomeworkParentRewardCacheManager;
    @Getter private RecommendContentCacheManager recommendContentCacheManager;
    @Getter private RemindStudentVacationProgressCacheManager remindStudentVacationProgressCacheManager;
    @Getter private OutsideReadingDynamicCacheManager outsideReadingDynamicCacheManager;
    @Getter private OralCommunicationRemindAssignCacheManager oralCommunicationRemindAssignCacheManager;
    @Getter private AncientPoetryResultCacheManager ancientPoetryResultCacheManager;
    @Getter private ViewPoetryActivityCacheManager viewPoetryActivityCacheManager;
    @Getter private AncientPoetryGlobalRankCacheManager ancientPoetryGlobalRankCacheManager;
    @Getter private IndependentOcrRewardCacheManager independentOcrRewardCacheManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        basicReviewHomeworkCacheManager = new BasicReviewHomeworkCacheManager(HomeworkCache.getHomeworkCache());
        vacationHomeworkCacheManager = new VacationHomeworkCacheManager(HomeworkCache.getHomeworkCacheFlushable());
        urgeNewHomeworkUnFinishCacheManager = new UrgeNewHomeworkUnFinishCacheManager(HomeworkCache.getHomeworkCacheFlushable());
        urgeNewHomeworkUnCorrectCacheManager = new UrgeNewHomeworkUnCorrectCacheManager(HomeworkCache.getHomeworkCacheFlushable());
        vacationHomeworkWinterPlanCacheManager = new VacationHomeworkWinterPlanCacheManager(HomeworkCache.getHomeworkCacheFlushable());
        noticeShareReportToJztCacheManager = new NoticeShareReportToJztCacheManager(HomeworkCache.getHomeworkCacheFlushable());
        basicReviewHomeworkShareCacheManager = new BasicReviewHomeworkShareCacheManager(HomeworkCache.getHomeworkCacheFlushable());
        studentFinishHomeworkPopupManager = new StudentFinishHomeworkPopupManager(HomeworkCache.getHomeworkCacheUnflushable());
        vacationHomeworkIntegralCacheManager = new VacationHomeworkIntegralCacheManager(HomeworkCache.getHomeworkCacheUnflushable());
        weekReportCacheManager = new WeekReportCacheManager(HomeworkCache.getHomeworkCacheUnflushable());
        shareVacationReportCacheManager = new ShareVacationReportCacheManager(HomeworkCache.getHomeworkCacheFlushable());
        shareWeiXinVacationReportCacheManager = new ShareWeiXinVacationReportCacheManager(HomeworkCache.getHomeworkCacheFlushable());
        remindStudentVacationProgressCacheManager = new RemindStudentVacationProgressCacheManager(HomeworkCache.getHomeworkCacheFlushable());
        groupHomeworkRecordCacheManager = new GroupHomeworkRecordCacheManager(HomeworkCache.getHomeworkCacheFlushable());

        assignHomeworkAndQuizDayCountManager = new AssignHomeworkAndQuizDayCountManager(HomeworkCache.getHomeworkCachePersistence());
        checkHomeworkIntegralCacheManager = new CheckHomeworkIntegralCacheManager(HomeworkCache.getHomeworkCachePersistence());
        monthFinishHomeworkCountManager = new MonthFinishHomeworkCountManager(HomeworkCache.getHomeworkCachePersistence());

        teacherNewHomeworkCommentLibraryManager = new TeacherNewHomeworkCommentLibraryManager(HomeworkCache.getHomeworkCacheStorage());
        activityHomeworkParentRewardCacheManager = new ActivityHomeworkParentRewardCacheManager(HomeworkCache.getHomeworkCacheStorage());
        recommendContentCacheManager = new RecommendContentCacheManager(HomeworkCache.getHomeworkCacheFlushable());
        outsideReadingDynamicCacheManager = new OutsideReadingDynamicCacheManager(HomeworkCache.getHomeworkCache());
        oralCommunicationRemindAssignCacheManager = new OralCommunicationRemindAssignCacheManager(HomeworkCache.getHomeworkCache());
        ancientPoetryResultCacheManager = new AncientPoetryResultCacheManager(HomeworkCache.getHomeworkCache());
        viewPoetryActivityCacheManager = new ViewPoetryActivityCacheManager(HomeworkCache.getHomeworkCache());
        ancientPoetryGlobalRankCacheManager = new AncientPoetryGlobalRankCacheManager(HomeworkCache.getHomeworkCache());
        independentOcrRewardCacheManager = new IndependentOcrRewardCacheManager(HomeworkCache.getHomeworkCache());
    }

    @Override
    public Map<Long, Integer> monthFinishHomeworkCountManager_currentCount(Collection<Long> userIds, HomeworkType homeworkType) {
        return monthFinishHomeworkCountManager.currentCount(userIds, homeworkType);
    }

    @Override
    public Map<Long, Set<String>> assignHomeworkAndQuizDayCountManager_currentDays(Collection<Long> userIds) {
        return assignHomeworkAndQuizDayCountManager.currentDays(userIds);
    }

    @Override
    public List<String> teacherNewHomeworkCommentLibraryManager_find(Long teacherId) {
        return teacherNewHomeworkCommentLibraryManager.find(teacherId);
    }

    @Override
    public void teacherNewHomeworkCommentLibraryManager_addComment(Long teacherId, String comment) {
        teacherNewHomeworkCommentLibraryManager.addComment(teacherId, comment);
    }

    @Override
    public boolean teacherNewHomeworkCommentLibraryManager_removeComment(Long teacherId, String comment) {
        return teacherNewHomeworkCommentLibraryManager.removeComment(teacherId, comment);
    }

    @Override
    public void vacationHomeworkIntegralCacheManager_recordStudentReward(Long studentId, String vacationHomeworkId, Integer level) {
        vacationHomeworkIntegralCacheManager.recordStudentReward(studentId, vacationHomeworkId, level);
    }

    @Override
    public boolean vacationHomeworkIntegralCacheManager_studentRewarded(Long studentId, String vacationHomeworkId, Integer level) {
        return vacationHomeworkIntegralCacheManager.studentRewarded(studentId, vacationHomeworkId, level);
    }

    @Override
    public StudentFinishHomeworkPopup studentFinishHomeworkPopupManager_loadStudentPopup(Long studentId, Long parentId) {
        return studentFinishHomeworkPopupManager.loadStudentPopup(studentId, parentId);
    }

    @Override
    public Map<Long, String> fetchHomeworkIdByGroupIdFromCache(Set<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds))
            return Collections.emptyMap();
        Map<Long, String> result = new LinkedHashMap<>();
        for (Long gid : groupIds) {
            if (gid == null)
                continue;
            String cacheKey = groupHomeworkRecordCacheManager.getCacheKey(gid);
            String hid = groupHomeworkRecordCacheManager.load(cacheKey);
            if (StringUtils.isBlank(hid))
                continue;
            result.put(gid, hid);
        }
        return result;
    }
}

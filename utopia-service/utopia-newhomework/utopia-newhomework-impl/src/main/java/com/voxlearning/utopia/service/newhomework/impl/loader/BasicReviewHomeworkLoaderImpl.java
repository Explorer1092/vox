package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.BasicReviewHomeworkLoader;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewStage;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.BasicReviewHomeworkCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.BasicReviewHomeworkDetailCacheMapper;
import com.voxlearning.utopia.service.newhomework.impl.dao.basicreview.BasicReviewHomeworkPackageDao;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author guoqiang.li
 * @since 2017/11/8
 */
@Named
@Service(interfaceClass = BasicReviewHomeworkLoader.class)
@ExposeService(interfaceClass = BasicReviewHomeworkLoader.class)
public class BasicReviewHomeworkLoaderImpl extends SpringContainerSupport implements BasicReviewHomeworkLoader {
    @Inject private BasicReviewHomeworkPackageDao basicReviewHomeworkPackageDao;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private BasicReviewHomeworkCacheLoaderImpl basicReviewHomeworkCacheLoader;
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject private StudentLoaderClient studentLoaderClient;

    @Override
    public BasicReviewHomeworkPackage load(String packageId) {
        return basicReviewHomeworkPackageDao.load(packageId);
    }

    @Override
    public Map<Long, List<BasicReviewHomeworkPackage>> loadBasicReviewHomeworkPackageByClazzGroupIds(Collection<Long> groupId) {
        return basicReviewHomeworkPackageDao.loadBasicReviewHomeworkPackageByClazzGroupIds(groupId);
    }

    @Override
    public MapMessage loadStudentDayPackages(String packageId, Long studentId) {
        BasicReviewHomeworkPackage basicReviewHomeworkPackage = basicReviewHomeworkPackageDao.load(packageId);
        if (basicReviewHomeworkPackage == null) {
            return MapMessage.errorMessage("期末基础复习作业不存在");
        }
        if (basicReviewHomeworkPackage.isDisabledTrue()) {
            return MapMessage.errorMessage("期末基础复习作业已删除");
        }
        int homeworkDays = SafeConverter.toInt(basicReviewHomeworkPackage.getHomeworkDays());
        if (homeworkDays < 0) {
            return MapMessage.errorMessage("作业天数错误");
        }
        if (CollectionUtils.isEmpty(basicReviewHomeworkPackage.getStages())) {
            return MapMessage.errorMessage("作业关卡错误");
        }

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return MapMessage.errorMessage("学生id错误");
        }

        BasicReviewHomeworkCacheMapper basicReviewHomeworkCacheMapper = basicReviewHomeworkCacheLoader.loadBasicReviewHomeworkCacheMapper(packageId, studentId);
        LinkedHashMap<String, BasicReviewHomeworkDetailCacheMapper> homeworkDetail = new LinkedHashMap<>();
        Map<String, Integer> finishedCountMap = new HashMap<>();

        if (basicReviewHomeworkCacheMapper != null && basicReviewHomeworkCacheMapper.getHomeworkDetail() != null) {
            homeworkDetail = basicReviewHomeworkCacheMapper.getHomeworkDetail();
            for (BasicReviewHomeworkDetailCacheMapper mapper : homeworkDetail.values()) {
                if (mapper.getFinishAt() != null) {
                    String day = DateUtils.dateToString(mapper.getFinishAt(), DateUtils.FORMAT_SQL_DATE);
                    int count = finishedCountMap.getOrDefault(day, 0) + 1;
                    finishedCountMap.put(day, count);
                }
            }
        }

        int maxFinishCount = (int) Math.ceil((double) basicReviewHomeworkPackage.getStages().size() / homeworkDays);
        String currentDay = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE);
        int todayFinishedCount = finishedCountMap.getOrDefault(currentDay, 0);
        //  作业包开放规则
        //  计算当天完成作业数量有没有达到最大完成量maxFinishCount
        //  达到：所有已完成的作业打开
        //  未达到 或者 灰度全部打开区域：所有已完成的作业打开，未完成的第一个打开
        boolean unlockUnFinished = todayFinishedCount < maxFinishCount
                || grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "BasicReview", "UnlockAll");
        List<Map<String, Object>> stageList = new ArrayList<>();
        for (BasicReviewStage basicReviewStage : basicReviewHomeworkPackage.getStages()) {
            boolean locked = true;
            boolean finished = false;
            BasicReviewHomeworkDetailCacheMapper cacheMapper = homeworkDetail.get(basicReviewStage.getHomeworkId());
            if (cacheMapper == null || cacheMapper.getFinishAt() == null) {
                if (unlockUnFinished) {
                    unlockUnFinished = false;
                    locked = false;
                }
            } else {
                locked = false;
                finished = true;
            }

            Map<String, Object> stageMapper = new LinkedHashMap<>();
            stageMapper.put("stageId", basicReviewStage.getStageId());
            stageMapper.put("stageName", basicReviewStage.getStageName());
            stageMapper.put("homeworkId", basicReviewStage.getHomeworkId());
            stageMapper.put("finished", finished);
            stageMapper.put("locked", locked);
            stageList.add(stageMapper);
        }

        int homeworkMinutes = 5;
        if (homeworkDays == 7) {
            homeworkMinutes = 7;
        } else if (homeworkDays == 15) {
            homeworkMinutes = 3;
        }
        String packageDescription = StringUtils.formatMessage("布置{}天温习计划，每天不要超过{}分钟哦", basicReviewHomeworkPackage.getHomeworkDays(), homeworkMinutes);
        int maxIntegral = 10 * basicReviewHomeworkPackage.getStages().size();
        String endDate = DateUtils.dateToString(NewHomeworkConstants.BASIC_REVIEW_END_DATE, "yyyy-MM-dd HH:mm");
        Teacher teacher = teacherLoaderClient.loadTeacher(basicReviewHomeworkPackage.getTeacherId());
        String teacherName = teacher != null ? teacher.fetchRealname() : "";
        String teacherImageUrl = teacher != null ? teacher.fetchImageUrl() : "";

        return MapMessage.successMessage()
                .add("subject", basicReviewHomeworkPackage.getSubject())
                .add("description", packageDescription)
                .add("maxIntegral", maxIntegral)
                .add("endDate", endDate)
                .add("teacherName", teacherName)
                .add("teacherImageUrl", teacherImageUrl)
                .add("homeworkDays", basicReviewHomeworkPackage.getHomeworkDays())
                .add("stageList", stageList)
                .add("newProcess", grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "StudentHomework", "NewIndexUrl"));

    }
}

package com.voxlearning.utopia.service.business.impl.service.student.internal.app.card;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.AbstractStudentAppIndexDataLoader;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.StudentAppIndexDataContext;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.BasicReviewHomeworkCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.BasicReviewHomeworkDetailCacheMapper;
import com.voxlearning.utopia.service.newhomework.consumer.BasicReviewHomeworkCacheLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.BasicReviewHomeworkLoaderClient;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class LoadStudentAppNationalDayHomeworkCard extends AbstractStudentAppIndexDataLoader {

    @Inject private BasicReviewHomeworkLoaderClient basicReviewHomeworkLoaderClient;
    @Inject private BasicReviewHomeworkCacheLoaderClient basicReviewHomeworkCacheLoaderClient;

    @Override
    protected StudentAppIndexDataContext doAppProcess(StudentAppIndexDataContext context) {
        Date currentDate = new Date();
        // 1号之前并且非灰度内学生 不显示卡片
        if (currentDate.before(NewHomeworkConstants.NATIONAL_DAY_HOMEWORK_START_DATE)
                && !grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(context.getStudent(), "BasicReview", "UnlockAll")) {
            return context;
        }
        // 10号之后卡片消失
        if (currentDate.after(NewHomeworkConstants.NATIONAL_DAY_HOMEWORK_END_DATE)) {
            return context;
        }
        List<Long> groupIds = context.__studentGroups
                .stream()
                .filter(groupMapper -> groupMapper.getSubject() != null)
                .sorted(Comparator.comparingInt(a -> a.getSubject().getKey()))
                .map(GroupMapper::getId)
                .collect(Collectors.toList());
        Map<Long, List<BasicReviewHomeworkPackage>> groupBasicReviewHomeworkPackage = basicReviewHomeworkLoaderClient.loadBasicReviewHomeworkPackageByClazzGroupIds(groupIds);
        List<BasicReviewHomeworkPackage> basicReviewHomeworkPackages = new ArrayList<>();
        for (List<BasicReviewHomeworkPackage> packages : groupBasicReviewHomeworkPackage.values()) {
            basicReviewHomeworkPackages.addAll(packages);
        }
        if (CollectionUtils.isNotEmpty(basicReviewHomeworkPackages)) {
            String currentDay = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE);
            for (BasicReviewHomeworkPackage basicReviewHomeworkPackage : basicReviewHomeworkPackages) {
                BasicReviewHomeworkCacheMapper basicReviewHomeworkCacheMapper = basicReviewHomeworkCacheLoaderClient.loadBasicReviewHomeworkCacheMapper(basicReviewHomeworkPackage.getId(), context.getStudent().getId());
                // 已经全部完成，卡片消失
                if (basicReviewHomeworkCacheMapper != null && SafeConverter.toBoolean(basicReviewHomeworkCacheMapper.getFinished())) {
                    continue;
                }
                // 如果当日完成关卡数到上限，按钮文案：今日已复习
                String startComment = "开始作业";
                int homeworkDays = SafeConverter.toInt(basicReviewHomeworkPackage.getHomeworkDays());
                if (homeworkDays > 0 && CollectionUtils.isNotEmpty(basicReviewHomeworkPackage.getStages())) {
                    int maxFinishCount = (int) Math.ceil((double) basicReviewHomeworkPackage.getStages().size() / homeworkDays);
                    int todayFinishedCount = 0;
                    if (basicReviewHomeworkCacheMapper != null && MapUtils.isNotEmpty(basicReviewHomeworkCacheMapper.getHomeworkDetail())) {
                        for (BasicReviewHomeworkDetailCacheMapper mapper : basicReviewHomeworkCacheMapper.getHomeworkDetail().values()) {
                            if (mapper.getFinishAt() != null) {
                                String finishDay = DateUtils.dateToString(mapper.getFinishAt(), DateUtils.FORMAT_SQL_DATE);
                                if (StringUtils.equals(currentDay, finishDay)) {
                                    todayFinishedCount++;
                                }
                            }
                        }
                    }
                    if (todayFinishedCount >= maxFinishCount) {
                        startComment = "今日已完成";
                    }
                }
                String desc = "英语-国庆趣味作业";
                String homeworkType = "EXPAND_BASIC_REVIEW_ENGLISH";
                if (Subject.MATH == basicReviewHomeworkPackage.getSubject()) {
                    desc = "数学-国庆趣味作业";
                    homeworkType = "EXPAND_BASIC_REVIEW_MATH";
                } else if (Subject.CHINESE == basicReviewHomeworkPackage.getSubject()) {
                    desc = "语文-国庆趣味作业";
                    homeworkType = "EXPAND_BASIC_REVIEW_CHINESE";
                }
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("homeworkId", basicReviewHomeworkPackage.getId());
                resultMap.put("endDate", NewHomeworkConstants.NATIONAL_DAY_HOMEWORK_END_DATE);
                resultMap.put("homeworkType", homeworkType);
                resultMap.put("desc", desc);
                resultMap.put("makeup", true);
                resultMap.put("subject", basicReviewHomeworkPackage.getSubject().name());
                resultMap.put("types", Collections.singletonList("BASIC_REVIEW"));
                //前端需要params这个参数所以先硬塞一个吧！！！
                resultMap.put("params", JsonUtils.toJson(MapUtils.m("packageId", basicReviewHomeworkPackage.getId(), "subject", basicReviewHomeworkPackage.getSubject())));
                resultMap.put("url", UrlUtils.buildUrlQuery("resources/apps/hwh5/homework/V2_5_0/basic-review-2017-autumn/index.html", MapUtils.m("packageId", basicReviewHomeworkPackage.getId(), "subject", basicReviewHomeworkPackage.getSubject())));
                resultMap.put("startComment", startComment);
                context.__basicReviewHomeworkCards.add(resultMap);
            }
        }
        return context;
    }
}

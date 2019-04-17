package com.voxlearning.utopia.service.business.impl.service.student.internal;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.BasicReviewHomeworkCacheMapper;
import com.voxlearning.utopia.service.newhomework.consumer.BasicReviewHomeworkCacheLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.BasicReviewHomeworkLoaderClient;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2017/11/8
 */
@Named
public class LoadStudentBasicReviewHomeworkCard extends AbstractStudentIndexDataLoader {
    @Inject private BasicReviewHomeworkLoaderClient basicReviewHomeworkLoaderClient;
    @Inject private BasicReviewHomeworkCacheLoaderClient basicReviewHomeworkCacheLoaderClient;

    @Override
    protected StudentIndexDataContext doProcess(StudentIndexDataContext context) {
        Date currentDate = new Date();
        if (currentDate.after(NewHomeworkConstants.BASIC_REVIEW_END_DATE)) {
            return context;
        }
        if (!grayFunctionManagerClient.getStudentGrayFunctionManager()
                .isWebGrayFunctionAvailable(context.getStudent(), "BasicReview", "StudentWhiteList")) {
            return context;
        }
        List<Long> groupIds = context.__studentGroups.stream().map(GroupMapper::getId).collect(Collectors.toList());
        Map<Long, List<BasicReviewHomeworkPackage>> groupBasicReviewHomeworkPackage = basicReviewHomeworkLoaderClient.loadBasicReviewHomeworkPackageByClazzGroupIds(groupIds);
        List<BasicReviewHomeworkPackage> basicReviewHomeworkPackages = new ArrayList<>();
        for (List<BasicReviewHomeworkPackage> packages : groupBasicReviewHomeworkPackage.values()) {
            basicReviewHomeworkPackages.addAll(packages);
        }
        if (CollectionUtils.isNotEmpty(basicReviewHomeworkPackages)) {
            boolean hasUnFinishedPackage = false;
            String homeworkType = "EXPAND_BASIC_REVIEW_ENGLISH";
            String packageId = null;
            for (BasicReviewHomeworkPackage basicReviewHomeworkPackage : basicReviewHomeworkPackages) {
                packageId = basicReviewHomeworkPackage.getId();
                BasicReviewHomeworkCacheMapper basicReviewHomeworkCacheMapper = basicReviewHomeworkCacheLoaderClient.loadBasicReviewHomeworkCacheMapper(packageId, context.getStudent().getId());
                if (basicReviewHomeworkCacheMapper != null && !SafeConverter.toBoolean(basicReviewHomeworkCacheMapper.getFinished())) {
                    hasUnFinishedPackage = true;
                    if (Subject.MATH == basicReviewHomeworkPackage.getSubject()) {
                        homeworkType = "EXPAND_BASIC_REVIEW_MATH";
                    } else if (Subject.CHINESE == basicReviewHomeworkPackage.getSubject()) {
                        homeworkType = "EXPAND_BASIC_REVIEW_CHINESE";
                    }
                    break;
                }
            }
            if (hasUnFinishedPackage) {
                Map<String, Object> homeworkCard = new HashMap<>();
                homeworkCard.put("packageId", packageId);
                homeworkCard.put("homeworkType", homeworkType);
                context.__basicReviewHomeworkCards.add(homeworkCard);
            }
        }
        return context;
    }
}
/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.service.student.internal;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.user.api.constants.GroupType;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Load student homework locations.
 *
 * @author Xiaohai Zhang
 * @since Oct 10, 2015
 */
@Named
public class LoadStudentHomeworkLocation extends AbstractStudentIndexDataLoader {

    @Override
    protected StudentIndexDataContext doProcess(StudentIndexDataContext context) {
        List<Long> groupIds = new ArrayList<>();
        for (GroupMapper gm : context.__studentGroups) {
            if (gm.getGroupType().equals(GroupType.TEACHER_GROUP)) {
                groupIds.add(gm.getId());
            }
        }
        Map<Long, List<NewHomework.Location>> newHomeworks = newHomeworkLoaderClient.loadNewHomeworksByClazzGroupIds(groupIds);
        //作业卡片只显示近一个月内布置的作业。
        Date startDate = DateUtils.calculateDateDay(new Date(context.timestamp), -60);
        for (Long groupId : newHomeworks.keySet()) {
            for (NewHomework.Location location : newHomeworks.get(groupId)) {
                if (location.getCreateTime() < startDate.getTime()) {
                    continue;
                }
                if (new Date(location.getCreateTime()).before(NewHomeworkConstants.ALLOW_UPDATE_HOMEWORK_START_TIME)) {
                    continue;
                }
                switch (location.getSubject()) {
                    case ENGLISH:
                        switch (location.getType()) {
                            case Normal:
                                context.__englishNormalHomeworkLocations.add(location);
                                break;
                            case TermReview:
                                context.__englishTermReviewHomeworkLocations.add(location);
                                break;
                            case MothersDay:
                                context.__mothersDayHomeworkLocations.add(location);
                                break;
                            case Activity:
                                if (HomeworkTag.KidsDay == location.getHomeworkTag()) {
                                    context.__kidsDayHomeworkLocations.add(location);
                                }
                                break;
                            case OCR:
                                context.__englishOcrHomeworkLocations.add(location);
                                break;
                            default:
                                break;
                        }
                        break;
                    case MATH:
                        switch (location.getType()) {
                            case Normal:
                                context.__mathNormalHomeworkLocations.add(location);
                                break;
                            case TermReview:
                                context.__mathTermReviewHomeworkLocations.add(location);
                                break;
                            case OCR:
                                context.__mathOcrHomeworkLocations.add(location);
                                break;
                            default:
                                break;
                        }
                        break;
                    case CHINESE:
                        switch (location.getType()) {
                            case Normal:
                                context.__chineseNormalHomeworkLocations.add(location);
                                break;
                            case TermReview:
                                context.__chineseTermReviewHomeworkLocations.add(location);
                            default:
                                break;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return context;
    }
}

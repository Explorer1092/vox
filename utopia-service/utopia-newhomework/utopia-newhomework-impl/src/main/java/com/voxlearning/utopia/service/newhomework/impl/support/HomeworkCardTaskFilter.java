/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.support;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTaskType;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkTask;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 类HomeworkCardTaskFilter的实现：获取老师任务卡片中的配置信息
 *
 * @author zhangbin
 * @since 2017/4/17 17:30
 */
@Named
public class HomeworkCardTaskFilter {

    @Inject private PageBlockContentServiceClient pageBlockContentServiceClient;

    public List<HomeworkTask> loadValidHomeworkTaskList() {
        List<HomeworkCardInfo> homeworkCardInfoList = getMappingInfo();
        if (CollectionUtils.isEmpty(homeworkCardInfoList)) {
            return Collections.emptyList();
        }
        Date nowDate = new Date();
        List<HomeworkTask> homeworkTaskList = new ArrayList<>();
        for (HomeworkCardInfo homeworkCardInfo : homeworkCardInfoList) {
            Date startTime = DateUtils.stringToDate(homeworkCardInfo.getStartTime());
            Date endTime = DateUtils.stringToDate(homeworkCardInfo.getEndTime());
            if (nowDate.after(startTime) && nowDate.before(endTime)) {
                HomeworkTask homeworkTask = new HomeworkTask();
                homeworkTask.setTaskId(homeworkCardInfo.getTaskId());
                homeworkTask.setHomeworkTaskType(homeworkCardInfo.getHomeworkTaskType());
                homeworkTask.setStartTime(startTime);
                homeworkTask.setEndTime(endTime);
                homeworkTask.setTaskName(homeworkCardInfo.getTaskName());
                homeworkTask.setIntegralCount(homeworkCardInfo.getIntegralCount());
                homeworkTask.setTaskSubjects(homeworkCardInfo.getTaskSubjects());
                homeworkTask.setTaskDescription(homeworkCardInfo.getTaskDescription());
                homeworkTask.setTaskRules(homeworkCardInfo.getTaskRules());
                homeworkTask.setPcImgUrl(homeworkCardInfo.getPcImgUrl());
                homeworkTask.setNativeImgUrl(homeworkCardInfo.getNativeImgUrl());
                homeworkTask.setH5ImgUrl(homeworkCardInfo.getH5ImgUrl());
                homeworkTaskList.add(homeworkTask);
            }
        }
        return homeworkTaskList;
    }

    private List<HomeworkCardInfo> getMappingInfo() {
        //读取页面内容的配置信息
        List<PageBlockContent> teacherTask = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("teacher_task");
        if (CollectionUtils.isNotEmpty(teacherTask)) {
            PageBlockContent configPageBlockContent = teacherTask.stream()
                    .filter(p -> "homeworkcard_task_type".equals(p.getBlockName()))
                    .findFirst()
                    .orElse(null);
            if (configPageBlockContent != null) {
                String configContent = configPageBlockContent.getContent();
                if (StringUtils.isBlank(configContent)) {
                    return null;
                }
                configContent = configContent.replaceAll("[\n\r\t]", "").trim();
                return JsonUtils.fromJsonToList(configContent, HomeworkCardInfo.class);
            }
        }
        return null;
    }

    @Getter
    @Setter
    private static class HomeworkCardInfo {
        private Integer taskId;
        private HomeworkTaskType homeworkTaskType;
        private String startTime;
        private String endTime;
        private String taskName;
        private Integer integralCount;
        private List<Subject> taskSubjects;
        private String taskDescription;
        private List<String> taskRules;
        private String pcImgUrl;
        private String nativeImgUrl;
        private String h5ImgUrl;
    }
}

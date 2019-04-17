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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.impl.service.AsyncAvengerHomeworkServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.NewHomeworkQueueServiceImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author tanguohong
 * @version 0.1
 * @since 2016/1/7
 */
@Named
public class AH_SaveHomework extends AbstractAssignHomeworkProcessor {
    @Inject private NewHomeworkQueueServiceImpl newHomeworkQueueService;
    @Inject private AsyncAvengerHomeworkServiceImpl asyncAvengerHomeworkService;

    @Override
    protected void doProcess(AssignHomeworkContext context) {
        Date currentDate = new Date();
        Long teacherId = context.getTeacher().getId();
        String actionId = StringUtils.join(Arrays.asList(teacherId, currentDate.getTime()), "_");
        Subject subject = context.getTeacher().getSubject();

        for (Long groupId : context.getGroupIds()) {
            List<NewHomeworkPracticeContent> practiceContents = context.getGroupPractices().get(groupId);
            if (CollectionUtils.isNotEmpty(practiceContents)) {
                Long duration = context.getGroupDurations().get(groupId);
                if (duration == null) {
                    duration = context.getDuration();
                }
                NewHomework newHomework = new NewHomework();
                newHomework.setActionId(actionId);
                newHomework.setTeacherId(teacherId);
                newHomework.setSubject(subject);
                newHomework.setClazzGroupId(groupId);
                newHomework.setStartTime(context.getHomeworkStartTime());
                newHomework.setEndTime(context.getHomeworkEndTime());
                newHomework.setRemark(context.getRemark());
                newHomework.setDuration(duration);
                newHomework.setCreateAt(currentDate);
                newHomework.setUpdateAt(currentDate);
                newHomework.setSource(context.getHomeworkSourceType());
                newHomework.setDisabled(false);
                newHomework.setChecked(false);
                newHomework.setPractices(practiceContents);
                newHomework.setIncludeSubjective(context.isIncludeSubjective());
                newHomework.setAdditions(context.getAdditions());
                newHomework.setType(context.getNewHomeworkType());
                newHomework.setHomeworkTag(context.getHomeworkTag());
                if (context.getTeacher().getKtwelve() != null) {
                    newHomework.setSchoolLevel(SchoolLevel.safeParse(context.getTeacher().getKtwelve().getLevel()));
                }
                if (context.getSource().containsKey("sourceHomeworkId")) {
                    newHomework.setSourceHomeworkId(SafeConverter.toString(context.getSource().get("sourceHomeworkId")));
                }
                //是否包含重点讲练测作业形式
                Boolean includeIntelligentTeaching = Boolean.FALSE;
                Set<ObjectiveConfigType> objectiveConfigTypes = practiceContents.stream().map(NewHomeworkPracticeContent::getType).collect(Collectors.toSet());
                if (objectiveConfigTypes.contains(ObjectiveConfigType.INTELLIGENT_TEACHING) || objectiveConfigTypes.contains(ObjectiveConfigType.ORAL_INTELLIGENT_TEACHING)
                        || (!NewHomeworkType.OCR.equals(context.getNewHomeworkType()) && objectiveConfigTypes.contains(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC))
                        || objectiveConfigTypes.contains(ObjectiveConfigType.CALC_INTELLIGENT_TEACHING)) {
                    includeIntelligentTeaching = Boolean.TRUE;
                }
                newHomework.setIncludeIntelligentTeaching(includeIntelligentTeaching);

                context.getAssignedGroupHomework().put(groupId, newHomework);
            }

        }
        if (!context.getAssignedGroupHomework().isEmpty()) {
            newHomeworkService.inserts(context.getAssignedGroupHomework().values());

            // 记录题的使用次数(总次数和该老师的使用次数)
            if (MapUtils.isNotEmpty(context.getGroupPracticesBooksMap())) {
                Map<ObjectiveConfigType, List<NewHomeworkBookInfo>> practiceBooksMap = context.getGroupPracticesBooksMap().values().iterator().next();
                if (MapUtils.isNotEmpty(practiceBooksMap) && CollectionUtils.isNotEmpty(practiceBooksMap.values().iterator().next())) {
                    // 一次只能布置一个课本里面的习题，取第一个题的的bookId
                    String bookId = practiceBooksMap.values().iterator().next().iterator().next().getBookId();
                    List<NewHomeworkPracticeContent> practiceContents = context.getAssignedGroupHomework().values().iterator().next().getPractices();
                    // 批量更新，需要传入这次作业的班组数
                    teacherAssignmentRecordDao.updateTeacherAssignmentRecord(subject, teacherId, bookId, practiceContents, context.getGroupIds().size());
                    // 总次数通过MQ来更新
                    newHomeworkQueueService.sendUpdateTotalAssignmentRecordMessage(subject, practiceContents, context.getGroupIds().size());
                }
            }

            List<NewHomeworkBook> newHomeworkBookList = new ArrayList<>();
            for (NewHomework newHomework : context.getAssignedGroupHomework().values()) {
                NewHomeworkBook newHomeworkBook = new NewHomeworkBook();
                newHomeworkBook.setId(newHomework.getId());
                newHomeworkBook.setSubject(subject);
                newHomeworkBook.setActionId(actionId);
                newHomeworkBook.setTeacherId(teacherId);
                newHomeworkBook.setClazzGroupId(newHomework.getClazzGroupId());
                newHomeworkBook.setPractices(context.getGroupPracticesBooksMap().get(newHomework.getClazzGroupId()));
                newHomeworkBookList.add(newHomeworkBook);
            }
            if (!newHomeworkBookList.isEmpty()) {
                newHomeworkService.insertNewHomeworkBooks(newHomeworkBookList);
                // to avenger
                toAvenger(context.getAssignedGroupHomework().values(), newHomeworkBookList);
            }
        } else {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK,
                    "mod3", JsonUtils.toJson(context.getSource()),
                    "op", "assign homework"
            ));
            context.errorResponse("保存作业失败");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK);
            context.setTerminateTask(true);
        }
    }


    private void toAvenger(Collection<NewHomework> homeworks, Collection<NewHomeworkBook> homeworkBooks) {
        Map<String, NewHomework> homeworkMap = homeworks.stream()
                .collect(Collectors.toMap(NewHomework::getId, Function.identity()));

        Map<String, NewHomeworkBook> homeworkBookMap = homeworkBooks.stream()
                .collect(Collectors.toMap(NewHomeworkBook::getId, Function.identity()));

        for (Map.Entry<String, NewHomework> entry : homeworkMap.entrySet()) {
            NewHomeworkBook homeworkBook = homeworkBookMap.get(entry.getKey());
            asyncAvengerHomeworkService.informHomeworkToBigData(entry.getValue(), homeworkBook);
        }
    }
}

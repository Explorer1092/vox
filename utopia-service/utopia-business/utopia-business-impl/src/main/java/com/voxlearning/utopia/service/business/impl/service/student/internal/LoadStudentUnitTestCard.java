package com.voxlearning.utopia.service.business.impl.service.student.internal;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.utopia.service.newexam.api.constant.NewExamConstants;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamResult;
import com.voxlearning.utopia.service.newexam.consumer.client.NewExamResultLoaderClient;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 单元检测
 * @author: Mr_VanGogh
 * @date: 2019/3/26 下午3:20
 */
@Named
public class LoadStudentUnitTestCard extends AbstractStudentIndexDataLoader {
    @Inject
    private NewExamResultLoaderClient newExamResultLoaderClient;

    @Override
    protected StudentIndexDataContext doProcess(StudentIndexDataContext context) {
        StudentDetail studentDetail = context.getStudent();
        if (studentDetail != null && studentDetail.getClazz() != null) {
            List<GroupMapper> groups = groupLoaderClient.loadStudentGroups(studentDetail.getId(), false);
            Set<Long> groupIds = groups.stream().map(GroupMapper::getId).collect(Collectors.toSet());
            Map<Long, List<NewExam>> newExamMap = newExamLoaderClient.loadByGroupIds(groupIds);
            //根据时间过滤历史数据:时间
            List<NewExam> newExams = newExamMap.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(n -> n.getCreatedAt().after(NewExamConstants.UNIT_TEST_DUE_DATE))
                    .filter(n -> n.getExamStopAt().after(new Date()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(newExams)) {
                List<String> newExamResultIds = new ArrayList<>();
                for (NewExam newExam : newExams) {
                    String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
                    NewExamResult.ID id = new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), studentDetail.getId().toString());
                    newExamResultIds.add(id.toString());
                }
                Map<String, NewExamResult> newExamResultMap = newExamResultLoaderClient.loadNewExamResults(newExamResultIds);
                List<NewExam> unSubmitExamList = newExams
                        .stream()
                        .filter(e -> {
                            //过滤掉已交卷的考试
                            String month = MonthRange.newInstance(e.getCreatedAt().getTime()).toString();
                            NewExamResult.ID id = new NewExamResult.ID(month, e.getSubject(), e.getId(), studentDetail.getId().toString());
                            NewExamResult newExamResult = newExamResultMap.get(id.toString());
                            return newExamResult == null || newExamResult.getSubmitAt() == null;
                        })
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(unSubmitExamList)) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("startComment", "开始");
                    resultMap.put("desc", "单元检测");
                    resultMap.put("homeworkType", "NEWEXAM_MATH");
                    context.__enterableUnitTestCards.add(resultMap);
                }
            }
        }
        return context;
    }
}

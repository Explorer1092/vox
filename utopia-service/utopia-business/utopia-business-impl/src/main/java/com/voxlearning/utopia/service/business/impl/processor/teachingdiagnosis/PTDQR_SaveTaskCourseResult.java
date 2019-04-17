package com.voxlearning.utopia.service.business.impl.processor.teachingdiagnosis;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.business.api.context.teachingdiagnosis.PreQuestionResultContext;
import com.voxlearning.utopia.entity.teachingdiagnosis.TeachingDiagnosisTask;
import com.voxlearning.utopia.entity.teachingdiagnosis.TeachingDiagnosisTaskCourse;
import com.voxlearning.utopia.service.business.impl.processor.ExecuteTask;
import com.voxlearning.utopia.service.business.impl.support.AbstractTeachingDiagnosisSupport;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author songtao
 * @since 2018/02/08
 */
@Named
public class PTDQR_SaveTaskCourseResult extends AbstractTeachingDiagnosisSupport implements ExecuteTask<PreQuestionResultContext> {
    @Override
    public void execute(PreQuestionResultContext context) {
        List<TeachingDiagnosisTaskCourse> courseList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(context.getCourseIds())) {
            Map<String, IntelDiagnosisCourse> courseMap = intelDiagnosisClient.loadDiagnosisCoursesByIds(context.getCourseIds()).stream().collect(Collectors.toMap(IntelDiagnosisCourse::getId, e->e));
            for(String course : context.getCourseIds()) {
                if (MapUtils.isNotEmpty(courseMap) && courseMap.get(course) != null) {
                    TeachingDiagnosisTaskCourse teachingDiagnosisTaskCourse = new TeachingDiagnosisTaskCourse();
                    teachingDiagnosisTaskCourse.setId(course);
                    teachingDiagnosisTaskCourse.setName(courseMap.get(course).getName());
                    teachingDiagnosisTaskCourse.setDescription(courseMap.get(course).getDescription());
                    courseList.add(teachingDiagnosisTaskCourse);
                }
            }
        }

        Date now = new Date();
        TeachingDiagnosisTask task = new TeachingDiagnosisTask();
        String id = TeachingDiagnosisTask.generateId(context.getStudent().getId(), context.getConfig().getGroupId());
        task.setId(id);
        task.setCourses(courseList);
        task.setExperimentGroupId(context.getConfig().getGroupId());
        task.setExperimentId(context.getConfig().getId());
        task.setPreviewQuestionList(Arrays.asList(context.getConfig().getPreQuestion()));
        task.setCreateTime(now);
        task.setTotalNumber(context.getTotalNum());
        task.setWrongNumber(context.getWrongNum());
        task.setUpdateTime(now);
        task.setUserId(context.getStudent().getId());
        teachingDiagnosisTaskDao.upsert(task);
        context.getResult().put("taskId", id);
    }
}

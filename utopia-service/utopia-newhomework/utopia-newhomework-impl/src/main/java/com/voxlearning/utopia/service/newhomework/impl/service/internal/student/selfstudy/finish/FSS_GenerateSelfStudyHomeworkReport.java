package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy.finish;

import com.mongodb.MongoWriteException;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.FinishSelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkReport;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkReportQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyHomeworkReportDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 只有错题订正的tag才会进入，其他的都路过
 *
 * @author xuesong.zhang
 * @since 2017/3/27
 */
@Named
public class FSS_GenerateSelfStudyHomeworkReport extends SpringContainerSupport implements FinishSelfStudyHomeworkTask {

    @Inject private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;
    @Inject private SelfStudyHomeworkReportDao selfStudyHomeworkReportDao;

    @Override
    public void execute(FinishSelfStudyHomeworkContext context) {
        SelfStudyHomework homework = context.getSelfStudyHomework();
        if (homework.getHomeworkTag() != HomeworkTag.Correct || !context.isHomeworkFinished()) {
            return;
        }

        LinkedHashMap<ObjectiveConfigType, LinkedHashMap<String, SelfStudyHomeworkReportQuestion>> practiceMap = new LinkedHashMap<>();
        Map<ObjectiveConfigType, List<String>> answerIdMap = context.getAnswerIdMap();
        for (Map.Entry<ObjectiveConfigType, List<String>> answerIdEntry : answerIdMap.entrySet()) {
            ObjectiveConfigType configType = answerIdEntry.getKey();
            Map<String, SubHomeworkResultAnswer> answerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(answerIdEntry.getValue());
            List<String> processIds = answerMap.values().stream().filter(o -> StringUtils.isNotBlank(o.getProcessId())).map(SubHomeworkResultAnswer::getProcessId).collect(Collectors.toList());
            Map<String, SubHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(processIds);

            LinkedHashMap<String, SelfStudyHomeworkReportQuestion> reportQuestionMap = new LinkedHashMap<>();
            if (configType.equals(ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS)) {
                Map<String, List<SubHomeworkProcessResult>> courseProcessMap = processResultMap.values().stream().collect(Collectors.groupingBy(SubHomeworkProcessResult::getCourseId));
                for (Map.Entry<String, List<SubHomeworkProcessResult>> courseEntry : courseProcessMap.entrySet()) {
                    for (SubHomeworkProcessResult processResult : courseEntry.getValue()) {
                        SelfStudyHomeworkReportQuestion reportQuestion = new SelfStudyHomeworkReportQuestion();
                        reportQuestion.setProcessId(processResult.getId());
                        reportQuestion.setGrasp(processResult.getGrasp());
                        reportQuestion.setCourseId(processResult.getCourseId());
                        reportQuestionMap.put(processResult.getCourseId() + "|" + processResult.getQuestionId(), reportQuestion);
                    }
                }
            } else {
                for (Map.Entry<String, SubHomeworkProcessResult> entry : processResultMap.entrySet()) {
                    SubHomeworkProcessResult processResult = entry.getValue();
                    SelfStudyHomeworkReportQuestion reportQuestion = new SelfStudyHomeworkReportQuestion();
                    reportQuestion.setProcessId(processResult.getId());
                    reportQuestion.setGrasp(processResult.getGrasp());
                    reportQuestionMap.put(processResult.getQuestionId(), reportQuestion);
                }
            }
            if (MapUtils.isNotEmpty(reportQuestionMap)) {
                practiceMap.put(configType, reportQuestionMap);
            }
        }

        //发音矫正特殊处理
        SelfStudyHomeworkResult selfStudyHomeworkResult = context.getSelfStudyHomeworkResult();
        LinkedHashMap<String, BaseHomeworkResultAppAnswer> appAnswer = selfStudyHomeworkResult.findAppAnswer(ObjectiveConfigType.ORAL_INTERVENTIONS);
        LinkedHashMap<String, SelfStudyHomeworkReportQuestion> reportQuestionMap = new LinkedHashMap<>();
        for (BaseHomeworkResultAppAnswer answer : appAnswer.values()) {
            SelfStudyHomeworkReportQuestion reportQuestion = new SelfStudyHomeworkReportQuestion();
            reportQuestion.setGrasp(answer.isGrasp());
            reportQuestion.setCourseId(answer.getCourseId());
            reportQuestionMap.put(answer.getCourseId(), reportQuestion);
        }
        if (MapUtils.isNotEmpty(reportQuestionMap)) {
            practiceMap.put(ObjectiveConfigType.ORAL_INTERVENTIONS, reportQuestionMap);
        }

        SelfStudyHomeworkReport report = new SelfStudyHomeworkReport();
        if (MapUtils.isNotEmpty(practiceMap)) {
            report.setId(context.getHomeworkId());
            report.setPractices(practiceMap);
            report.setHomeworkId(homework.getSourceHomeworkId());
            report.setSelfStudyId(homework.getId());
            report.setSubject(homework.getSubject());
            report.setGroupId(homework.getClazzGroupId());
            report.setStudentId(homework.getStudentId());
            try {
                selfStudyHomeworkReportDao.insert(report);
            } catch (MongoWriteException ignored) {
            }
        }
    }
}

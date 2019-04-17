package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkReport;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkReportDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.basicreview.BasicReviewHomeworkReportDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * 2017秋季期末基础复习 完成作业后生成一份报告
 *
 * @author guoqiang.li
 * @since 2017/11/14
 */
@Named
public class FH_CreateBasicReviewHomeworkReport extends SpringContainerSupport implements FinishHomeworkTask {
    @Inject
    private BasicReviewHomeworkReportDao basicReviewHomeworkReportDao;
    @Inject
    private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;

    @Override
    public void execute(FinishHomeworkContext context) {
        if (!Objects.equals(NewHomeworkType.BasicReview, context.getNewHomeworkType())) {
            return;
        }
        NewHomework newHomework = context.getHomework();
        NewHomeworkResult newHomeworkResult = context.getResult();
        processBasicReviewHomeworkReport(newHomeworkResult, newHomework);
    }


    public BasicReviewHomeworkReport processBasicReviewHomeworkReport(NewHomeworkResult newHomeworkResult, NewHomework newHomework) {
        BasicReviewHomeworkReport report = basicReviewHomeworkReportDao.load(newHomeworkResult.getId());
        if (report != null) {
            return report;
        }
        report = new BasicReviewHomeworkReport();
        report.setId(newHomeworkResult.getId());
        report.setHomeworkId(newHomework.getId());
        report.setPackageId(newHomework.getBasicReviewPackageId());
        report.setSubject(newHomework.getSubject());
        report.setClazzGroupId(newHomework.getClazzGroupId());
        report.setUserId(newHomeworkResult.getUserId());
        Date currentTime = new Date();
        report.setCreateAt(currentTime);
        report.setUpdateAt(currentTime);
        List<String> processIds = newHomeworkResult.findAllHomeworkProcessIds(true);
        Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds);
        LinkedHashMap<ObjectiveConfigType, BasicReviewHomeworkReportDetail> practices = new LinkedHashMap<>();
        report.setPractices(practices);
        for (Map.Entry<ObjectiveConfigType, NewHomeworkResultAnswer> entry : newHomeworkResult.getPractices().entrySet()) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = entry.getValue();
            BasicReviewHomeworkReportDetail detail = new BasicReviewHomeworkReportDetail();
            //普通形式
            if (newHomeworkResultAnswer.getAppAnswers() == null) {
                if (newHomeworkResultAnswer.getAnswers() == null)
                    continue;
                LinkedHashMap<String, BasicReviewHomeworkAnswer> answers = new LinkedHashMap<>();
                detail.setAnswers(answers);
                for (Map.Entry<String, String> answerEntry : newHomeworkResultAnswer.getAnswers().entrySet()) {
                    if (processResultMap.containsKey(answerEntry.getValue())) {
                        NewHomeworkProcessResult newHomeworkProcessResult = processResultMap.get(answerEntry.getValue());
                        BasicReviewHomeworkAnswer basicReviewHomeworkAnswer = new BasicReviewHomeworkAnswer();
                        basicReviewHomeworkAnswer.setGrasp(newHomeworkProcessResult.getGrasp());
                        basicReviewHomeworkAnswer.setQuestionId(newHomeworkProcessResult.getQuestionId());
                        answers.put(answerEntry.getKey(), basicReviewHomeworkAnswer);
                    }
                }
            } else {
                //app 形式
                LinkedHashMap<String, BasicReviewHomeworkAppAnswer> appAnswers = new LinkedHashMap<>();
                detail.setAppAnswers(appAnswers);
                for (Map.Entry<String, NewHomeworkResultAppAnswer> appAnswerEntry : newHomeworkResultAnswer.getAppAnswers().entrySet()) {
                    if (appAnswerEntry.getValue().getAnswers() == null)
                        continue;
                    NewHomeworkResultAppAnswer value = appAnswerEntry.getValue();
                    BasicReviewHomeworkAppAnswer basicReviewHomeworkAppAnswer = new BasicReviewHomeworkAppAnswer();
                    basicReviewHomeworkAppAnswer.setCategoryId(value.getCategoryId());
                    basicReviewHomeworkAppAnswer.setLessonId(value.getLessonId());
                    basicReviewHomeworkAppAnswer.setPracticeName(value.getPracticeName());
                    appAnswers.put(appAnswerEntry.getKey(), basicReviewHomeworkAppAnswer);
                    LinkedHashMap<String, BasicReviewHomeworkAnswer> answers = new LinkedHashMap<>();
                    basicReviewHomeworkAppAnswer.setAnswers(answers);
                    for (Map.Entry<String, String> answerEntry : appAnswerEntry.getValue().getAnswers().entrySet()) {
                        if (processResultMap.containsKey(answerEntry.getValue())) {
                            NewHomeworkProcessResult newHomeworkProcessResult = processResultMap.get(answerEntry.getValue());
                            BasicReviewHomeworkAnswer basicReviewHomeworkAnswer = new BasicReviewHomeworkAnswer();
                            basicReviewHomeworkAnswer.setGrasp(newHomeworkProcessResult.getGrasp());
                            basicReviewHomeworkAnswer.setQuestionId(newHomeworkProcessResult.getQuestionId());
                            answers.put(answerEntry.getKey(), basicReviewHomeworkAnswer);
                        }
                    }
                }
            }
            practices.put(entry.getKey(), detail);
        }
        basicReviewHomeworkReportDao.insert(report);
        return report;
    }

}

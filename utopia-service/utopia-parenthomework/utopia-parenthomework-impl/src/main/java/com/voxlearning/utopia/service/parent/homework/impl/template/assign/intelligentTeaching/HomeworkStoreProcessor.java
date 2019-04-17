package com.voxlearning.utopia.service.parent.homework.impl.template.assign.intelligentTeaching;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.parent.homework.api.entity.Homework;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkPractice;
import com.voxlearning.utopia.service.parent.homework.api.entity.Practices;
import com.voxlearning.utopia.service.parent.homework.api.entity.Questions;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.api.mapper.QuestionPackage;
import com.voxlearning.utopia.service.parent.homework.api.mapper.StudentInfo;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkDao;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkPracticeDao;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 保存作业
 *
 * @author Wenlong Meng
 * @since Feb 19, 2019
 */
@Named("IntelliagentTeaching.HomeworkStoreProcessor")
public class HomeworkStoreProcessor implements HomeworkProcessor {

    @Inject
    private QuestionLoaderClient questionLoaderClient;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    @Inject
    private HomeworkDao homeworkDao;
    @Inject
    private HomeworkPracticeDao homeworkPracticeDao;

    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();
        StudentInfo studentInfo = hc.getStudentInfo();
        QuestionPackage questionPackage = hc.getQuestionPackages().get(0);
        String id = questionPackage.getId();
        Homework homework = homeworkDao.load(id);
        if(homework != null){
            hc.setHomework(homework);
            return;
        }

        List<NewQuestion> newQuestions;
        if(CollectionUtils.isNotEmpty(questionPackage.getDocIds())){
            newQuestions = questionLoaderClient.loadQuestionByDocIds(questionPackage.getDocIds());
        }else{
            newQuestions = new ArrayList<>(questionLoaderClient.loadQuestions(questionPackage.getQuestonIds()).values());
        }

        if (newQuestions.isEmpty()) {
            hc.setMapMessage(MapMessage.errorMessage("没有对应的题"));
            LoggerUtils.info("assignHomework.error", "no questions",param);
            return;
        }
        homework = new Homework();
        homework.setActionId(ObjectUtils.get(() -> SafeConverter.toString(param.getData().get("actionId")))); // 批次id
        homework.setBizType(questionPackage.getBizType());
        homework.setSubject(param.getSubject());
        homework.setDuration(questionPackage.getDuration());
        homework.setGrade(studentInfo.getClazzLevel()); // 年级
//        homework.setFromUserId(param.getCurrentUserId());
        homework.setPublisherId("-1");
        homework.setId(id);
        NewBookProfile newBookProfile = newContentLoaderClient.loadBook(param.getBookId());
        NewBookCatalog newBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(hc.getUnitId());
        homework.setAdditions(MapUtils.m("bookId", param.getBookId(),
                "unitId", hc.getUnitId(),
                "bookName", newBookProfile != null ? newBookProfile.getName() : "",
                "unitName", newBookCatalog != null ? HomeworkUtil.unitName(newBookCatalog) : "",
                "level", questionPackage.getName()));
        homework.setType(NewHomeworkType.Normal.name());
        homework.setHomeworkTag(HomeworkTag.Normal.name());
        homework.setStartTime(new Date());
        homework.setEndTime(DateUtils.getTodayEnd());
        homework.setQuestionCount(newQuestions.size());
        homework.setScore(100d);
        homework.setSource(param.getSource());
        homeworkDao.insert(homework);

        HomeworkPractice homeworkPractice = new HomeworkPractice();
        List<Practices> practicesList = new ArrayList<>();
        Practices practices = new Practices();
        // 作业类型
        String type = questionPackage.getObjectiveConfigType();
        if(StringUtils.isEmpty(type)){
            type = questionPackage.getBizType();
        }
        practices.setType(type);
        if (ObjectUtils.get(() -> questionPackage.getData().get("timeLimit")) != null) {
            // 限时
            practices.setTimeLimit(SafeConverter.toInt(questionPackage.getData().get("timeLimit")));
        }
        List<Questions> questionsList = new ArrayList<>();
        Map<String, Double> questionScoreMap = questionLoaderClient.parseExamScoreByQuestions(newQuestions, homework.getScore());
        newQuestions.forEach(newQuestion -> {
            Questions questions = new Questions();
            questions.setQuestionBoxId(questionPackage.getId());
            questions.setSeconds(newQuestion.getSeconds());
            questions.setQuestionId(newQuestion.getId());
            questions.setDocId(newQuestion.getDocId());
            questions.setQuestionVersion(newQuestion.getVersion());
            questions.setSubmitWay(newQuestion.getSubmitWays());
            questions.setScore(questionScoreMap.get(newQuestion.getId()));
            questionsList.add(questions);
        });
        practices.setQuestions(questionsList);
        practicesList.add(practices);
        homeworkPractice.setPractices(practicesList);
        homeworkPractice.setId(homework.getId());
        homeworkPracticeDao.insert(homeworkPractice);
        hc.setHomework(homework);
        hc.setHomeworkPractice(homeworkPractice);
    }
}

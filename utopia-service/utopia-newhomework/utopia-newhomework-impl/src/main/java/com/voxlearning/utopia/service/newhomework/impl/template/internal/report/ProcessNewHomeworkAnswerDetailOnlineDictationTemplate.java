package com.voxlearning.utopia.service.newhomework.impl.template.internal.report;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypePartContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.NewQuestionReportBO;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: 线上听写
 * @author: Mr_VanGogh
 * @date: 2019/1/28 上午11:39
 */
@Named
public class ProcessNewHomeworkAnswerDetailOnlineDictationTemplate extends ProcessNewHomeworkAnswerDetailCommonTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.ONLINE_DICTATION;
    }

    @Override
    public void fetchNewHomeworkCommonObjectiveConfigTypePart(ObjectiveConfigTypePartContext context) {
        //************ begin 数据初始化准备 *******************//
        NewHomeworkPracticeContent target = context.getTarget();
        NewHomework newHomework = context.getNewHomework();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = context.getNewHomeworkResultMap();
        ObjectiveConfigType type = context.getType();
        Map<Long, User> userMap = context.getUserMap();
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(target.processNewHomeworkQuestion(false).stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList()));


        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        List<String> subHomeworkResultAnswerIds = new LinkedList<>();
        Map<ObjectiveConfigType, List<NewHomework.NewHomeworkQuestionObj>> objectiveConfigTypeListMap = newHomework.processSubHomeworkResultAnswerIds(Collections.singleton(type));
        List<NewHomework.NewHomeworkQuestionObj> newHomeworkQuestionObjs = objectiveConfigTypeListMap.getOrDefault(type, Collections.emptyList());
        for (NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj : newHomeworkQuestionObjs) {
            for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
                subHomeworkResultAnswerIds.add(newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId()));
            }
        }
        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);
        List<String> newHomeworkProcessResultIds = subHomeworkResultAnswerMap.values()
                .stream()
                .map(SubHomeworkResultAnswer::getProcessId)
                .collect(Collectors.toList());
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(context.getNewHomework().getId(), newHomeworkProcessResultIds);
        // 课程信息
        LinkedHashSet<String> lessonIds = target.getQuestions().stream().map(NewHomeworkQuestion::getQuestionBoxId).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        //************ end 数据初始化准备 *******************//

        //************** begin  数据初始化:对应每题的信息  NewQuestionReportBO 对应着返回数据的结构 *************//
        List<NewQuestionReportBO> newQuestionReportBOs = target.processNewHomeworkQuestion(false)
                .stream()
                .map(o -> {
                    if (!allQuestionMap.containsKey(o.getQuestionId())) {
                        return null;
                    }
                    NewQuestionReportBO newQuestionReportBO = new NewQuestionReportBO(o.getQuestionId());
                    newQuestionReportBO.setType(1);
                    NewQuestion question = allQuestionMap.get(o.getQuestionId());
                    List<NewQuestionsSubContents> subContents = question.getContent().getSubContents();
                    //复合体的结构
                    for (int i = 0; i < subContents.size(); i++) {
                        NewQuestionReportBO.SubQuestion subQuestion = new NewQuestionReportBO.SubQuestion();
                        newQuestionReportBO.getSubQuestions().add(subQuestion);
                    }
                    newQuestionReportBO.setLessonId(o.getQuestionBoxId());
                    newQuestionReportBO.setQuestionAnswer(subContents.get(0).getAnswers().get(0).getAnswer());
                    newQuestionReportBO.setContentType(
                            contentTypeMap.containsKey(question.getContentTypeId()) ?
                                    contentTypeMap.get(question.getContentTypeId()).getName() :
                                    "无题型");
                    newQuestionReportBO.setDifficulty(question.getDifficultyInt());
                    newQuestionReportBO.setDifficultyName(QuestionConstants.newDifficultyMap.get(question.getDifficultyInt()));
                    return newQuestionReportBO;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        //************** end  数据初始化:对应每题的信息  NewQuestionReportBO 对应着返回数据的结构 *************//

        Map<String, NewQuestionReportBO> newQuestionReportBOMap = newQuestionReportBOs
                .stream()
                .collect(Collectors.toMap(NewQuestionReportBO::getQid, Function.identity()));
        //********* begin 数据处理：NewHomeworkProcessResult questionId 来根据 newQuestionReportBOMap 获得NewQuestionReportBO 然后数据汇聚 ********//
        for (NewHomeworkProcessResult p : newHomeworkProcessResultMap.values()) {
            if (!newQuestionReportBOMap.containsKey(p.getQuestionId())) {
                continue;
            }
            if (!allQuestionMap.containsKey(p.getQuestionId())) {
                continue;
            }
            NewQuestion newQuestion = allQuestionMap.get(p.getQuestionId());
            List<NewQuestionsSubContents> subContents = newQuestion.getContent().getSubContents();
            List<List<String>> userAnswers = p.getUserAnswers();
            if (userAnswers.size() != subContents.size()) {
                continue;
            }
            NewQuestionReportBO newQuestionReportBO = newQuestionReportBOMap.get(p.getQuestionId());
            newQuestionReportBO.setNum(newQuestionReportBO.getNum() + 1);
            NewQuestionReportBO.UserToQuestion u = new NewQuestionReportBO.UserToQuestion();
            u.setUid(p.getUserId());
            u.setUserName(userMap.containsKey(u.getUid()) ? userMap.get(u.getUid()).fetchRealname() : "");
            for (int i = 0; i < subContents.size(); i++) {
                NewQuestionsSubContents newQuestionsSubContents = subContents.get(i);
                List<String> strings = userAnswers.get(i);
                boolean b = p.getSubGrasp().get(i).stream().allMatch(SafeConverter::toBoolean);
                String answer;
                if (b) {
                    answer = "全对学生";
                } else {
                    answer = NewHomeworkUtils.pressAnswer(Collections.singletonList(newQuestionsSubContents), Collections.singletonList(strings));
                    if (!newQuestionReportBO.getSubQuestions().get(i).getUserAnswersMap().containsKey(answer)) {
                        newQuestionReportBO.getSubQuestions().get(i).getUserAnswersMap().put(answer, strings);
                    }
                }
                newQuestionReportBO.getSubQuestions().get(i).getMap().computeIfAbsent(answer, l -> new ArrayList<>()).add(u);
            }
            if (!SafeConverter.toBoolean(p.getGrasp())) {
                newQuestionReportBO.setErrorNum(newQuestionReportBO.getErrorNum() + 1);
            }
        }
        //********* end 数据处理 ********//


        //********* begin 数据后处理，失分率和答案的显示 ********//
        newQuestionReportBOMap.values()
                .stream()
                .filter(o -> o.getNum() > 0)
                .forEach(o -> {
                    int errorRate = new BigDecimal(100 * o.getErrorNum()).divide(new BigDecimal(o.getNum()), BigDecimal.ROUND_HALF_UP, 0).intValue();
                    o.setErrorRate(errorRate);
                    List<NewQuestionReportBO.SubQuestion> subQuestions = o.getSubQuestions();
                    if (subQuestions.size() > 0) {
                        //复合体信息处理
                        for (NewQuestionReportBO.SubQuestion subQuestion : subQuestions) {
                            Map<String, List<NewQuestionReportBO.UserToQuestion>> map = subQuestion.getMap();
                            if (map.containsKey("全对学生")) {
                                List<NewQuestionReportBO.UserToQuestion> userToQuestions = map.get("全对学生");
                                subQuestion.getAnswer().add(MapUtils.m(
                                        "grasp", true,
                                        "answerWord", "全对学生", "userToQuestions", userToQuestions
                                ));
                                map.remove("全对学生");
                            }
                            for (Map.Entry<String, List<NewQuestionReportBO.UserToQuestion>> entry : map.entrySet()) {
                                subQuestion.getAnswer().add(MapUtils.m(
                                        "grasp", false,
                                        "answerWord", entry.getKey(), "userToQuestions", entry.getValue(),
                                        "userAnswers", subQuestion.getUserAnswersMap().get(entry.getKey())
                                ));
                            }
                            subQuestion.setUserAnswersMap(null);
                            subQuestion.setMap(null);
                        }
                    }
                });
        //********* end 数据后处理，失分率和答案的显示 ********//
        List<NewQuestionReportBO> reportBOs = newQuestionReportBOMap.values()
                .stream()
                .sorted((o1, o2) -> Integer.compare(o2.getErrorRate(), o1.getErrorRate()))
                .collect(Collectors.toList());

        List<Map> result = new LinkedList<>();
        for (String lessonId : lessonIds) {
            List<NewQuestionReportBO> questionReportBOS = new LinkedList<>();
            for (NewQuestionReportBO bo : reportBOs) {
                if (!lessonId.equals(bo.getLessonId())) {
                    continue;
                }
                questionReportBOS.add(bo);
            }
            Map map = new LinkedHashMap();
            map.put("lessonId", lessonId);
            map.put("lessonName", newBookCatalogMap.containsKey(lessonId) ? newBookCatalogMap.get(lessonId).getName() : "");
            map.put("questions", questionReportBOS);
            result.add(map);
        }

        MapMessage mapMessage = MapMessage.successMessage();
        context.setMapMessage(mapMessage);
        mapMessage.add("result", result);
    }
}

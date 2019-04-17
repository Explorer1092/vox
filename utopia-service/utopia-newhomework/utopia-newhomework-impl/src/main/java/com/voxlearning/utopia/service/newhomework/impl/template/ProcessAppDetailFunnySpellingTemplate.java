package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.newhomework.api.constant.NatureSpellingType;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.CategoryClazzHandlerContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.CategoryHandlerContext;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class ProcessAppDetailFunnySpellingTemplate extends ProcessAppDetailByCategoryIdTemplate {
    @Override
    public NatureSpellingType getNatureSpellingType() {
        return NatureSpellingType.FUNNY_SPELLING;
    }

    @Override
    public void processPersonalCategory(CategoryHandlerContext categoryHandlerContext) {
        List<String> qIds = categoryHandlerContext.getQIds();
        Map<String, NewHomeworkProcessResult> processResultMap = categoryHandlerContext.getProcessResultMap();
        Map<String, NewQuestion> newQuestionMap = categoryHandlerContext.getNewQuestionMap();
        Map<Long, Sentence> sentenceMap = categoryHandlerContext.getSentenceMap();
        List<Map<String, Object>> questionInfo = categoryHandlerContext.getResult();
        PracticeType practiceType = categoryHandlerContext.getPracticeType();
        for (String qId : qIds) {
            Boolean answerInfo = null;
            String answerResultWord = null;
            //非口语题
            NewHomeworkProcessResult ls = processResultMap.get(qId);
            if (Objects.nonNull(ls)) {
                answerInfo = SafeConverter.toBoolean(ls.getGrasp());
                //趣味拼写和读音归类 显示的文案，并没有正确与错误，只有完成
                answerResultWord = "完成";

            }
            NewQuestion newQuestion = newQuestionMap.get(qId);
            List<Long> _sentenceIds = newQuestion.getSentenceIds();
            _sentenceIds = _sentenceIds == null ? Collections.emptyList() : _sentenceIds;
            //题的句子文案
            List<Map<String, Object>> sentences = _sentenceIds
                    .stream()
                    .map(l ->
                            MapUtils.m(
                                    "sentenceId", l,
                                    "sentenceContent", Objects.nonNull(sentenceMap.get(l)) ? sentenceMap.get(l).getEnText() : ""))
                    .collect(Collectors.toList());
            //兼容句子没有文案
            if (CollectionUtils.isEmpty(sentences)) {
                sentences.add(MapUtils.m(
                        "sentenceId", "1",
                        "sentenceContent", "单词正在赶来中"));//词穷
            }
            questionInfo.add(
                    MapUtils.m(
                            "questionId", qId,
                            "answerResultWord", answerResultWord,
                            "sentences", sentences,
                            "answerInfo", answerInfo,
                            "needRecord", practiceType.getNeedRecord(),
                            "recordInfo", null
                    ));
        }
    }

    @Override
    public void processClazzCategory(CategoryClazzHandlerContext categoryClazzHandlerContext) {
        Map<String, NewQuestion> newQuestionMap = categoryClazzHandlerContext.getNewQuestionMap();
        Map<String, List<NewHomeworkProcessResult>> qIdMapNewHomeworkProcessResult = categoryClazzHandlerContext.getNewHomeworkProcessResultMap();
        Map<Long, User> userMap = categoryClazzHandlerContext.getUserMap();
        Map<Long, Sentence> sentenceMap = categoryClazzHandlerContext.getSentenceMap();
        PracticeType practiceType = categoryClazzHandlerContext.getPracticeType();
        List<Map<String, Object>> questionInfo = categoryClazzHandlerContext.getResult();
        for (String qId : newQuestionMap.keySet()) {
            NewQuestion newQuestion = newQuestionMap.get(qId);
            if (newQuestion == null)
                continue;
            List<String> answerRightInfo = new LinkedList<>();
            if (MapUtils.isNotEmpty(qIdMapNewHomeworkProcessResult)) {
                //是否是口语题
                List<NewHomeworkProcessResult> ls = qIdMapNewHomeworkProcessResult.get(qId);
                if (CollectionUtils.isNotEmpty(ls)) {
                    for (NewHomeworkProcessResult n : ls) {
                        //读音归类和趣味拼写只有完成，没有正确错误之分
                        answerRightInfo.add(userMap.get(n.getUserId()).fetchRealname());
                    }
                }
            }
            List<Long> _sentenceIds = newQuestion.getSentenceIds();
            List<Map<String, Object>> sentences = CollectionUtils.isNotEmpty(_sentenceIds) ?
                    _sentenceIds
                            .stream()
                            .map(l ->
                                    MapUtils.m(
                                            "sentenceId", l,
                                            "sentenceContent", Objects.isNull(sentenceMap.get(l)) ? "" : sentenceMap.get(l).getEnText()))
                            .collect(Collectors.toList()) :
                    Collections.emptyList();
            //题库没有数据的情况
            if (CollectionUtils.isEmpty(sentences)) {
                sentences.add(MapUtils.m(
                        "sentenceId", "1",
                        "sentenceContent", "单词正在赶来中"));//
            }
            questionInfo.add(
                    MapUtils.m(
                            "questionId", qId,
                            "sentences", sentences,
                            "needRecord", practiceType.getNeedRecord(),
                            "errorProportion", 0,
                            "rightProportion", 100,
                            "answerRightInfo", answerRightInfo,
                            "answerErrorInfo", Collections.emptyList(),
                            "recordInfo", null
                    ));
        }
    }
}

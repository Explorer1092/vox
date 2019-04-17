
package com.voxlearning.utopia.service.newhomework.impl.template.vacation;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.newhomework.api.constant.NatureSpellingType;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.vacationhomework.CategoryHandlerContext;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Named
public class VacationProcessAppDetailFunnySpellingTemplate extends VacationProcessAppDetailByCategoryIdTemplate {
    @Override
    public NatureSpellingType getNatureSpellingType() {
        return NatureSpellingType.FUNNY_SPELLING;
    }

    @Override
    public void processPersonalCategory(CategoryHandlerContext categoryHandlerContext) {
        List<String> qIds = categoryHandlerContext.getQIds();
        Map<String, VacationHomeworkProcessResult> processResultMap = categoryHandlerContext.getProcessResultMap();
        Map<String, NewQuestion> newQuestionMap = categoryHandlerContext.getNewQuestionMap();
        Map<Long, Sentence> sentenceMap = categoryHandlerContext.getSentenceMap();
        List<Map<String, Object>> questionInfo = categoryHandlerContext.getResult();
        PracticeType practiceType = categoryHandlerContext.getPracticeType();
        for (String qId : qIds) {
            Boolean answerInfo = null;
            String answerResultWord = null;
            //非口语题
            VacationHomeworkProcessResult ls = processResultMap.get(qId);
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


}

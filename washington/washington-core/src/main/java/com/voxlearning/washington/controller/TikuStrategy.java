package com.voxlearning.washington.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.helper.ObjectCopyUtils;
import com.voxlearning.utopia.service.question.api.entity.NewPaper;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.PaperLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Map;

/**
 * @Auther: majianxin
 * @Date: 2018/4/19
 * @Description:
 */
@Named
public class TikuStrategy {

    @Inject private PaperLoaderClient paperLoaderClient;
    @Inject private com.voxlearning.utopia.service.mid_english_question.consumer.PaperLoaderClient paperLoaderClientMid;
    @Inject private QuestionLoaderClient questionLoaderClient;
    @Inject private com.voxlearning.utopia.service.mid_english_question.consumer.QuestionLoaderClient questionLoaderClientMid;

    public NewPaper loadLatestPaperByDocId(String docId) {
        if (isMiddle(docId)) {
            com.voxlearning.utopia.service.mid_english_question.api.entity.NewPaper newPaper = paperLoaderClientMid.loadLatestPaperByDocId(docId);
            return ObjectCopyUtils.copyPropertiesByJson(NewPaper.class, newPaper);
        }
        return paperLoaderClient.loadLatestPaperByDocId(docId);
    }

    public Map<String, NewQuestion> loadQuestionsIncludeDisabled(Collection<String> questionIds, SchoolLevel schoolLevel) {
        if (isMiddle(schoolLevel)) {
            Map<String, com.voxlearning.utopia.service.mid_english_question.api.entity.NewQuestion> stringNewQuestionMap = questionLoaderClientMid.loadQuestionsIncludeDisabled(questionIds);
            return ObjectCopyUtils.copyMapPropertiesByJson(NewQuestion.class, stringNewQuestionMap);
        }
        return questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
    }

    public MapMessage loadQuestionFilterAnswersByIds(Collection<String> ids, Boolean containsAnswer) {
        if (CollectionUtils.isEmpty(ids)) {
            return MapMessage.successMessage().add("result", Lists.newArrayList());
        }
        if (isMiddle(ids)) {
            return questionLoaderClientMid.loadQuestionFilterAnswersByIds(ids, containsAnswer);
        }
        return  questionLoaderClient.loadQuestionFilterAnswersByIds(ids, containsAnswer);
    }

    public Map<String, NewQuestion> loadLatestQuestionByDocIds(Collection<String> docIds) {
        if (CollectionUtils.isEmpty(docIds)) {
            return Maps.newHashMap();
        }
        if (isMiddle(docIds)) {
            Map<String, com.voxlearning.utopia.service.mid_english_question.api.entity.NewQuestion> stringNewQuestionMap = questionLoaderClientMid.loadLatestQuestionByDocIds(docIds);
            return ObjectCopyUtils.copyMapPropertiesByJson(NewQuestion.class, stringNewQuestionMap);
        }
        return questionLoaderClient.loadLatestQuestionByDocIds(docIds);
    }

    /**
     * 根据ID判断是否为中学数据
     * @param id
     * @return
     */
    private boolean isMiddle(String id) {
        if (StringUtils.isEmpty(id)) {
            return false;
        }
        String regex = "^[A-Z]_2.*$";
        return id.matches(regex);
    }

    private boolean isMiddle(Collection<String> ids) {
        String id = ids.iterator().next();
        return isMiddle(id);
    }

    /**
     * 根据schoolLevel判断是否为中学数据
     * @param schoolLevel
     * @return
     */
    private boolean isMiddle(SchoolLevel schoolLevel) {
        if (schoolLevel == null) {
            return false;
        }
        return SchoolLevel.MIDDLE == schoolLevel;
    }

}

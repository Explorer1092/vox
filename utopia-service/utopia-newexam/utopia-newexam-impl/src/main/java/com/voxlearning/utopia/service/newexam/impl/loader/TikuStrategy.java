package com.voxlearning.utopia.service.newexam.impl.loader;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.core.helper.ObjectCopyUtils;
import com.voxlearning.utopia.service.question.api.entity.NewPaper;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.UserAnswerMapper;
import com.voxlearning.utopia.service.question.consumer.PaperLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.question.consumer.ScoreCalculationLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @Auther: majianxin
 * @Date: 2018/4/19
 * @Description:
 */
@Named
public class TikuStrategy {

    @Inject
    private PaperLoaderClient paperLoaderClient;
    @Inject
    private com.voxlearning.utopia.service.mid_english_question.consumer.PaperLoaderClient paperLoaderClientMid;
    @Inject
    private QuestionLoaderClient questionLoaderClient;
    @Inject
    private com.voxlearning.utopia.service.mid_english_question.consumer.QuestionLoaderClient questionLoaderClientMid;
    @Inject
    private ScoreCalculationLoaderClient scoreCalculationLoaderClient;
    @Inject
    private com.voxlearning.utopia.service.mid_english_question.consumer.ScoreCalculationLoaderClient scoreCalculationLoaderClientMid;

    public NewQuestion loadQuestionByDocId(String questionId) {
        if (isMiddle(questionId)) {
            com.voxlearning.utopia.service.mid_english_question.api.entity.NewQuestion newQuestion = questionLoaderClientMid.loadQuestionByDocId(questionId);
            return ObjectCopyUtils.copyPropertiesByJson(NewQuestion.class, newQuestion);
        }
        return questionLoaderClient.loadQuestionByDocId(questionId);
    }

    public NewPaper loadPaperByDocid(String paperDocId) {
        if (isMiddle(paperDocId)) {
            com.voxlearning.utopia.service.mid_english_question.api.entity.NewPaper newPaper = paperLoaderClientMid.loadPaperByDocid(paperDocId);
            return ObjectCopyUtils.copyPropertiesByJson(NewPaper.class, newPaper);
        }
        return paperLoaderClient.loadPaperByDocid(paperDocId);
    }

    public QuestionScoreResult loadQuestionScoreResult(UserAnswerMapper userAnswerMapper, String questionId, SchoolLevel schoolLevel) {
        boolean middle;
        if (schoolLevel != null) {
            middle = isMiddle(schoolLevel);
        }else {
            middle = isMiddle(questionId);
        }
        if (middle) {
            if (userAnswerMapper == null) {
                return null;
            }
            com.voxlearning.utopia.service.mid_english_question.api.mapper.UserAnswerMapper userAnswerMapperMid =
                    new com.voxlearning.utopia.service.mid_english_question.api.mapper.UserAnswerMapper(userAnswerMapper.getQuestionId(), userAnswerMapper.getQuestionScore(), userAnswerMapper.getUserAnswers(), userAnswerMapper.getCaseSensitive());
            userAnswerMapperMid.setUserId(userAnswerMapper.getUserId());
            userAnswerMapperMid.setUserAgent(userAnswerMapper.getUserAgent());
            userAnswerMapperMid.setHomeworkType(userAnswerMapper.getHomeworkType());
            userAnswerMapperMid.setHomeworkId(userAnswerMapper.getHomeworkId());

            com.voxlearning.utopia.service.mid_english_question.api.mapper.QuestionScoreResult questionScoreResult = scoreCalculationLoaderClientMid.loadQuestionScoreResult(userAnswerMapperMid);
            return ObjectCopyUtils.copyPropertiesByJson(QuestionScoreResult.class, questionScoreResult);
        }
        return scoreCalculationLoaderClient.loadQuestionScoreResult(userAnswerMapper);
    }

    public NewQuestion loadQuestionIncludeDisabled(String questionId) {
        if (isMiddle(questionId)) {
            com.voxlearning.utopia.service.mid_english_question.api.entity.NewQuestion newQuestion = questionLoaderClientMid.loadQuestionIncludeDisabled(questionId);
            return ObjectCopyUtils.copyPropertiesByJson(NewQuestion.class, newQuestion);
        }
        return questionLoaderClient.loadQuestionIncludeDisabled(questionId);
    }

    public NewPaper loadLatestPaperByDocId(String docId, SchoolLevel schoolLevel) {
        if (isMiddle(schoolLevel)) {
            com.voxlearning.utopia.service.mid_english_question.api.entity.NewPaper newPaper = paperLoaderClientMid.loadLatestPaperByDocId(docId);
            return ObjectCopyUtils.copyPropertiesByJson(NewPaper.class, newPaper);
        }
        return paperLoaderClient.loadLatestPaperByDocId(docId);
    }

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

    public Map<String, NewQuestion> loadQuestionsIncludeDisabled(Collection<String> questionIds) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return Collections.emptyMap();
        }
        if (isMiddle(questionIds)) {
            Map<String, com.voxlearning.utopia.service.mid_english_question.api.entity.NewQuestion> stringNewQuestionMap = questionLoaderClientMid.loadQuestionsIncludeDisabled(questionIds);
            return ObjectCopyUtils.copyMapPropertiesByJson(NewQuestion.class, stringNewQuestionMap);
        }
        return questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
    }

    public List<NewQuestion> loadQuestionsIncludeDisabledAsList(Collection<String> questionIds, SchoolLevel schoolLevel) {
        if (isMiddle((schoolLevel))) {
            Collection<com.voxlearning.utopia.service.mid_english_question.api.entity.NewQuestion> values = questionLoaderClientMid.loadQuestionsIncludeDisabled(questionIds).values();
            return ObjectCopyUtils.copyCollectionPropertiesByJson(NewQuestion.class, values);
        }
        return new ArrayList<>(questionLoaderClient.loadQuestionsIncludeDisabled(questionIds).values());
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

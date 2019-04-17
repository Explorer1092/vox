package com.voxlearning.utopia.service.psr.impl.mathnewkp.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.psr.entity.mathnewkp.MathQuestion;
import com.voxlearning.utopia.service.psr.entity.newhomework.MathQuestionProfile;
import com.voxlearning.utopia.service.psr.entity.newhomework.SectionProgressId;
import com.voxlearning.utopia.service.psr.entity.newhomework.SeriesProgress;
import com.voxlearning.utopia.service.psr.impl.dao.newhomework.SectionProgressIdDao;
import com.voxlearning.utopia.service.psr.impl.newhomework.service.NewHomeWorkCacheService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: hui.cheng
 * Date: 2016/10/20.
 * Time: 16:08
 */
@Slf4j
@Named
public class PsrQuestionService {

    @Inject
    private SectionProgressIdDao sectionProgressIdDao;
    @Inject
    private NewHomeWorkCacheService newHomeWorkCacheService;

    private static final String MATH_SIMILAR_PREFIX = "MSP$";

    private boolean isAboveLevel(SectionProgressId sectionProgressId, MathQuestionProfile mathQuestionProfile) {
        if (sectionProgressId == null)
            return false;
        String seriesId = sectionProgressId.getSeriesId();
        long progressId = sectionProgressId.getProgressId();
        List<SeriesProgress> seriesProgresses = mathQuestionProfile.getSeriesProgresses();
        if (CollectionUtils.isEmpty(seriesProgresses))
            return true;
        for (SeriesProgress seriesProgress : seriesProgresses) {
            if (StringUtils.equals(seriesProgress.getSeries_id(), seriesId)) {
                return seriesProgress.getProgress_id() > progressId;
            }
        }
        return true;
    }

    public Map<String, List<MathQuestion>>  getQuestions(String unitId, List<String> knowledgePoints, Integer count){
        SectionProgressId sectionProgressId = null;
        List<SectionProgressId> sectionProgressIds = sectionProgressIdDao.getSectionProgressIdsByUnit(unitId);
        if (CollectionUtils.isNotEmpty(sectionProgressIds))
            sectionProgressId = Collections.max(sectionProgressIds, Comparator.comparing(SectionProgressId::getProgressId));

        return filterQuestions(knowledgePoints,count,sectionProgressId);
    }

    public Map<String, List<MathQuestion>>  filterQuestions(List<String> knowledgePoints, Integer count,SectionProgressId sectionProgressId){
        Map<String, List<MathQuestion>> kpMathQuestionsMap = Maps.newHashMap();

        List<String> couchKeys = Lists.newArrayList();
        knowledgePoints.stream().forEach(e->couchKeys.add(MATH_SIMILAR_PREFIX + e));
        Map<String,List<MathQuestionProfile>> kpQuestionsMap = newHomeWorkCacheService.gets(couchKeys);
        for (Map.Entry<String, List<MathQuestionProfile>> entry : kpQuestionsMap.entrySet()){
            List<MathQuestion> mathQuestions = Lists.newArrayList();

            String knowledgePoint = StringUtils.split(entry.getKey(),"$")[1];
            List<MathQuestionProfile> questionsInfo = entry.getValue();

            if (CollectionUtils.isNotEmpty(questionsInfo)) {
                questionsInfo.stream()
                        .filter(q-> !isAboveLevel(sectionProgressId,q))
                        .limit(count)
                        .forEach(q->{
                            MathQuestion mathQuestion = new MathQuestion();
                            mathQuestion.setContentTypeId(q.getContent_type_id());
                            mathQuestion.setQuestionId(q.getQuestion_id());
                            mathQuestions.add(mathQuestion);
                        });
            }
            kpMathQuestionsMap.put(knowledgePoint, mathQuestions);
        }
        return  kpMathQuestionsMap;
    }

}

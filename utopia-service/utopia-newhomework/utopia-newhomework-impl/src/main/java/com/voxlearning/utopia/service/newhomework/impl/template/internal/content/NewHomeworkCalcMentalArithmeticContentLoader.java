package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.athena.api.cuotizhenduan.entity.CalculationIntelligentDiagnosisPak;
import com.voxlearning.athena.api.cuotizhenduan.entity.MentalArithmeticQuestion;
import com.voxlearning.utopia.service.newhomework.impl.athena.WrongQuestionDiagnosisLoaderClient;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.ObjectiveConfig;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;
import com.voxlearning.utopia.service.question.consumer.IntelDiagnosisClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
public class NewHomeworkCalcMentalArithmeticContentLoader extends NewHomeworkContentLoaderTemplate {

    @Inject
    private WrongQuestionDiagnosisLoaderClient wrongQuestionDiagnosisLoaderClient;
    @Inject
    private IntelDiagnosisClient intelDiagnosisClient;

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.CALC_INTELLIGENT_TEACHING;
    }

    @Override
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> content = new ArrayList<>();
        try {
            String unitId = mapper.getUnitId();
            String bookId = mapper.getBookId();
            return processContent(content, unitId, bookId);
        } catch (Exception e) {
            logger.error("Failed to load NewHomeworkCalcMentalArithmeticContentLoader, mapper:{}", mapper, e);
            return content;
        }
    }

    /**
     * 组装Content
     */
    private List<Map<String, Object>> processContent(List<Map<String, Object>> content, String unitId, String bookId) {
        List<CalculationIntelligentDiagnosisPak> cidpList = null;
        try {
            cidpList = wrongQuestionDiagnosisLoaderClient.getCuotizhenduanLoader().loadCalculationIntelligentDiagnosisPaks(unitId);
        } catch (Exception e) {
            logger.error("NewHomeworkCalcMentalArithmeticContentLoader call athena error:", e);
        }
        if (cidpList != null) {
            List<String> questionDocIds = new ArrayList<>();
            List<String> courseIds = new ArrayList<>();
            for (CalculationIntelligentDiagnosisPak cidp : cidpList) {
                List<MentalArithmeticQuestion> maqList = cidp.getQuestions();
                for (MentalArithmeticQuestion maq : maqList) {
                    questionDocIds.addAll(maq.getDocIds());
                    questionDocIds.addAll(maq.getPostTestDocIds());
                    courseIds.add(maq.getCourseId());
                }
            }

            Map<String, NewQuestion> questionMap = questionLoaderClient.loadLatestQuestionByDocIds(questionDocIds);
            //课程信息
            Map<String, IntelDiagnosisCourse> allIntelDiagnosisCourseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(courseIds);
            for(CalculationIntelligentDiagnosisPak cidp : cidpList){
                Map<String, Object> section = new HashMap<>();
                section.put("sectionId", cidp.getSectionId());
                section.put("pakId", cidp.getPakId());
                section.put("pakName", cidp.getPakName());
                Integer questionCount = 0;
                Long questionSeconds = 0L;
                List<MentalArithmeticQuestion> maqList = cidp.getQuestions();
                List<Map<String, Object>> sectionQuestions = new ArrayList<>();
                for (MentalArithmeticQuestion maq : maqList) {
                    Map<String, Object> kpMap = new LinkedHashMap<>();
                    String kpId = maq.getKpId();
                    IntelDiagnosisCourse course = allIntelDiagnosisCourseMap.get(maq.getCourseId());
                    if(course != null){
                        kpMap.put("courseId", course.getId());
                        kpMap.put("courseName", course.getName());
                        List<String> postQuestions = new ArrayList<>();
                        for(String ptId : maq.getPostTestDocIds()){
                            NewQuestion newQuestion = questionMap.get(ptId);
                            if(newQuestion != null){
                                postQuestions.add(newQuestion.getId());
                            }
                        }
                        kpMap.put("postQuestions", postQuestions);
                    }
                    kpMap.put("kpId", kpId);
                    kpMap.put("kpName", maq.getKpName());
                    List<Map<String, Object>> questions = new ArrayList<>();
                    for (String qdocId : maq.getDocIds()) {
                        NewQuestion newQuestion = questionMap.get(qdocId);
                        if (newQuestion != null) {
                            questionCount++;
                            questionSeconds += newQuestion.getSeconds();
                            questions.add(MapUtils.m(
                                    "questionId", newQuestion.getId(),
                                    "seconds", newQuestion.getSeconds(),
                                    "knowledgePoint", kpId
                            ));
                        }
                    }
                    kpMap.put("questionCount", questions.size());
                    kpMap.put("questions", questions);
                    kpMap.put("book", MapUtils.m(
                            "bookId", bookId,
                            "unitId", unitId,
                            "sectionId", cidp.getSectionId()
                    ));
                    sectionQuestions.add(kpMap);
                }
                if (CollectionUtils.isNotEmpty(sectionQuestions)) {
                    section.put("sectionQuestions", sectionQuestions);
                    section.put("questionCount", questionCount);
                    section.put("questionSeconds", questionSeconds);
                    content.add(section);
                }
            }
        }
        return content;
    }

    @Override
    public Map<String, Object> loadWaterfallContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> contentList = loadContent(mapper);
        ObjectiveConfig objectiveConfig = mapper.getObjectiveConfig();
        if (CollectionUtils.isNotEmpty(contentList)) {
            return MapUtils.m(
                    "objectiveConfigId", objectiveConfig.getId(),
                    "type", getObjectiveConfigType().name(),
                    "typeName", getObjectiveConfigType().getValue(),
                    "name", objectiveConfig.getName(),
                    "packages", contentList
            );
        }
        return Collections.emptyMap();
    }
}

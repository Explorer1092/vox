package com.voxlearning.utopia.service.newhomework.impl.template.internal;


import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.api.constant.ScoreLevel;
import com.voxlearning.utopia.api.constant.VoiceEngineType;
import com.voxlearning.utopia.core.helper.VoiceEngineTypeUtils;
import com.voxlearning.utopia.service.content.api.constant.PracticeCategory;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalogAncestor;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.PracticeLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.report.BasicAppUnitPart;
import com.voxlearning.utopia.service.newhomework.impl.template.VacationInternalProcessHomeworkAnswerTemple;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;


import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;

@Named
public class VacationInternalProcessHomeworkAnswerForBasicAppTemple extends VacationInternalProcessHomeworkAnswerTemple {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.BASIC_APP;
    }

    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    @Inject
    private PracticeLoaderClient practiceLoaderClient;


    public void internalProcessHomeworkAnswer(Map<ObjectiveConfigType, Object> resultMap,
                                              Map<String, VacationHomeworkProcessResult> allProcessResultMap,
                                              Map<Integer, NewContentType> contentTypeMap,
                                              Map<String, NewQuestion> allQuestionMap,
                                              VacationHomework vacationHomework,
                                              VacationHomeworkResult vacationHomeworkResult,
                                              ObjectiveConfigType type) {
        NewHomeworkPracticeContent newHomeworkPracticeContent = vacationHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (newHomeworkPracticeContent == null)
            return;
        List<NewHomeworkApp> apps = newHomeworkPracticeContent.getApps();
        if (apps == null)
            return;
        Set<String> lessonIds = new LinkedHashSet<>();
        for (NewHomeworkApp app : apps) {
            lessonIds.add(app.getLessonId());
        }
        Map<String, NewBookCatalog> lessonNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        Map<String, String> lessonIdToUnitIdMap = new LinkedHashMap<>();
        Set<String> unitIds = new LinkedHashSet<>();
        for (NewBookCatalog newBookCatalog : lessonNewBookCatalogMap.values()) {
            List<NewBookCatalogAncestor> ancestors = newBookCatalog.getAncestors();
            if (ancestors == null)
                continue;
            NewBookCatalogAncestor target = null;
            for (NewBookCatalogAncestor newBookCatalogAncestor : ancestors) {
                if (Objects.equals(newBookCatalogAncestor.getNodeType(), "UNIT")) {
                    target = newBookCatalogAncestor;
                    break;
                }
            }
            if (target != null) {
                lessonIdToUnitIdMap.put(newBookCatalog.getId(), target.getId());
                unitIds.add(target.getId());
            }
        }
        Map<String, NewBookCatalog> unitNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(unitIds);
        List<BasicAppUnitPart> unitParts = new LinkedList<>();
        Map<String, BasicAppUnitPart> unitPartMap = new LinkedHashMap<>();
        for (NewHomeworkApp app : apps) {
            PracticeType practiceType = practiceLoaderClient.loadPractice(app.getPracticeId());
            if (practiceType == null)
                continue;
            if (!lessonNewBookCatalogMap.containsKey(app.getLessonId()))
                continue;
            if (!lessonIdToUnitIdMap.containsKey(app.getLessonId()))
                continue;
            String unitId = lessonIdToUnitIdMap.get(app.getLessonId());
            if (!unitNewBookCatalogMap.containsKey(unitId))
                continue;
            NewHomeworkResultAppAnswer appAnswer = vacationHomeworkResult
                    .getPractices()
                    .get(type)
                    .getAppAnswers()
                    .get(app.getCategoryId() + "-" + app.getLessonId());
            if (appAnswer == null)
                continue;
            //单元
            BasicAppUnitPart unitPart;
            if (unitPartMap.containsKey(unitId)) {
                unitPart = unitPartMap.get(unitId);
            } else {
                NewBookCatalog newBookCatalog = unitNewBookCatalogMap.get(unitId);
                unitPart = new BasicAppUnitPart();
                unitPart.setUnitId(unitId);
                unitParts.add(unitPart);
                unitPart.setUnitName(newBookCatalog.getAlias());
                unitPartMap.put(unitId, unitPart);
            }
            //lesson
            BasicAppUnitPart.BasicAppLessonPart basicAppLessonPart;
            if (unitPart.getLessonPartMap().containsKey(app.getLessonId())) {
                basicAppLessonPart = unitPart.getLessonPartMap().get(app.getLessonId());
            } else {
                NewBookCatalog newBookCatalog = lessonNewBookCatalogMap.get(app.getLessonId());
                basicAppLessonPart = new BasicAppUnitPart.BasicAppLessonPart();
                basicAppLessonPart.setLessonName(newBookCatalog.getAlias());
                basicAppLessonPart.setLessonId(app.getLessonId());
                unitPart.getLessonPartMap().put(app.getLessonId(), basicAppLessonPart);
                unitPart.getLessons().add(basicAppLessonPart);
            }
            //category
            List<String> voiceUrls = new LinkedList<>();
            String voiceScoringMode = "";
            if (practiceType.getNeedRecord()) {
                if (MapUtils.isNotEmpty(appAnswer.getAnswers())) {
                    LinkedHashMap<String, String> answers = appAnswer.getAnswers();
                    for (String newHomeworkProcessId : answers.values()) {
                        VacationHomeworkProcessResult n = allProcessResultMap.get(newHomeworkProcessId);
                        if (n != null) {
                            String voiceUrl = CollectionUtils.isEmpty(n.getOralDetails()) ||
                                    CollectionUtils.isEmpty(n.getOralDetails().get(0)) ?
                                    null :
                                    n.getOralDetails().get(0).get(0).getAudio();
                            if (StringUtils.isNotBlank(voiceUrl)) {
                                VoiceEngineType voiceEngineType = n.getVoiceEngineType();
                                voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                                voiceUrls.add(voiceUrl);
                            }
                            if (StringUtils.isBlank(voiceScoringMode)) {
                                voiceScoringMode = n.getVoiceScoringMode();
                            }
                        }
                    }
                }

            }
            BasicAppUnitPart.BasicAppCategoryPart categoryPart = new BasicAppUnitPart.BasicAppCategoryPart();
            int averageScore = Objects.isNull(appAnswer.getScore()) ?
                    null : new BigDecimal(appAnswer.getScore())
                    .setScale(0, BigDecimal.ROUND_HALF_UP)
                    .intValue();
            categoryPart.setCategoryName(practiceType.getCategoryName());
            categoryPart.setVoiceUrls(voiceUrls);
            categoryPart.setVoiceScoringMode(voiceScoringMode);
            categoryPart.setAverageScore(averageScore);
            categoryPart.setAverageScoreLevel(ScoreLevel.processLevel(averageScore).getLevel());
            categoryPart.setCategoryId(app.getCategoryId());
            categoryPart.setPracticeCategory(PracticeCategory.icon(practiceType.getCategoryName()));
            basicAppLessonPart.getCategories().add(categoryPart);
        }
        resultMap.put(type, unitParts);
    }
}

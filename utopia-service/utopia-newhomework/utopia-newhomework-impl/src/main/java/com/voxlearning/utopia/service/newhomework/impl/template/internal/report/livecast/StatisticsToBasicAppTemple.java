package com.voxlearning.utopia.service.newhomework.impl.template.internal.report.livecast;


import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.utopia.service.content.api.constant.PracticeCategory;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalogAncestor;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.BasicAppInformation;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveHomeworkReport;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveHomeworkReportContext;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.template.StatisticsToObjectiveConfigTypeTemple;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class StatisticsToBasicAppTemple extends StatisticsToObjectiveConfigTypeTemple {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.BASIC_APP;
    }

    //1、调用公用方法commonHandler统计数据
    //2、统计基础练习数据
    @Override
    public LiveHomeworkReport.StatisticsToObjectiveConfigType statisticsToObjectiveConfigType(LiveHomeworkReportContext liveHomeworkReportContext, ObjectiveConfigType type) {
        LiveHomeworkReport.StatisticsToObjectiveConfigType statisticsToObjectiveConfigType = new LiveHomeworkReport.StatisticsToObjectiveConfigType();
        commonHandler(statisticsToObjectiveConfigType, liveHomeworkReportContext, type);
        Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMap = liveHomeworkReportContext.getLiveCastHomeworkResultMap();
        LiveCastHomework liveCastHomework = liveHomeworkReportContext.getLiveCastHomework();
        statisticsToObjectiveConfigType.setBasicAppInformation(processForBaseApp(type, liveCastHomework, liveCastHomeworkResultMap));
        return statisticsToObjectiveConfigType;
    }


    //1、对category数据初始化
    //2、对LiveCastHomeworkResult进行循环统计数据
    //3、对基础数据进行子到父的tree组织数据
    private List<BasicAppInformation> processForBaseApp(ObjectiveConfigType type, LiveCastHomework liveCastHomework, Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMap) {
        List<BasicAppInformation> basicAppInformationList = new LinkedList<>();
        Map<String, LiveCastHomeworkResult> newHomeworkResultMapToObjectiveConfigType = liveCastHomeworkResultMap
                .values()
                .stream()
                .filter(o -> o.isFinishedOfObjectiveConfigType(type))
                .collect(Collectors.toMap(LiveCastHomeworkResult::getId, Function.identity()));
        if (MapUtils.isNotEmpty(newHomeworkResultMapToObjectiveConfigType)) {
            Map<String, Map<String, Object>> clMap;
            List<NewHomeworkApp> apps;
            NewHomeworkPracticeContent newHomeworkPracticeContent = liveCastHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
            if (Objects.nonNull(newHomeworkPracticeContent) && newHomeworkPracticeContent.getApps() != null) {
                apps = newHomeworkPracticeContent.getApps();
                clMap = newHomeworkPracticeContent
                        .getApps()
                        .stream()
                        .map(o -> SafeConverter.toString(o.getCategoryId(), "") + "-" + SafeConverter.toString(o.getLessonId(), ""))
                        .collect(Collectors
                                .toMap(o -> o,
                                        o -> MapUtils.m(
                                                "score", 0.0,
                                                "num", 0)));
            } else {
                clMap = new LinkedHashMap<>();
                apps = new LinkedList<>();
            }
            //key : categoryId-lessonId-practiceId value:
            newHomeworkResultMapToObjectiveConfigType.forEach((k, value) -> {
                LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> o_map_answer = value.getPractices();
                NewHomeworkResultAnswer answer = o_map_answer.get(type);
                if (answer != null && answer.getAppAnswers() != null) {
                    for (Map.Entry<String, NewHomeworkResultAppAnswer> entry : answer.getAppAnswers().entrySet()) {
                        if (clMap.containsKey(entry.getKey())) {
                            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = entry.getValue();
                            if (newHomeworkResultAppAnswer != null) {
                                Map<String, Object> m = clMap.get(entry.getKey());
                                if (m != null) {
                                    m.put("num", (SafeConverter.toInt(m.get("num")) + 1));
                                    double score = SafeConverter.toDouble(newHomeworkResultAppAnswer.getScore());
                                    m.put("score", (SafeConverter.toDouble(m.get("score")) + score));
                                }
                            }
                        }
                    }
                }
            });
            Map<String, List<BasicAppInformation.Category>> lessonData = new LinkedHashMap<>();
            for (NewHomeworkApp app : apps) {
                Long practiceId = app.getPracticeId();
                PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(practiceId);
                if (practiceType == null) continue;
                Integer categoryId = app.getCategoryId();
                String lessonId = app.getLessonId();
                String k = SafeConverter.toString(categoryId, "") + "-" + SafeConverter.toString(lessonId, "");
                Map<String, Object> map = clMap.get(k);
                double score = SafeConverter.toDouble(map.get("score"));
                int num = SafeConverter.toInt(map.get("num"));
                BasicAppInformation.Category category = new BasicAppInformation.Category();
                if (num != 0) {
                    category.setAverageScore(new BigDecimal(SafeConverter.toDouble(score))
                            .divide(new BigDecimal(num), 0, BigDecimal.ROUND_HALF_UP)
                            .intValue());
                }
                category.setCategoryId(categoryId);
                category.setCategoryName(practiceType.getCategoryName());
                category.setPracticeCategory(PracticeCategory.icon(practiceType.getCategoryName()));
                if (lessonData.containsKey(lessonId)) {
                    lessonData.get(lessonId).add(category);
                } else {
                    List<BasicAppInformation.Category> categories = new LinkedList<>();
                    categories.add(category);
                    lessonData.put(lessonId, categories);
                }
            }

            Map<String, NewBookCatalog> ms = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonData.keySet());
            Map<String, String> lineData = NewHomeworkUtils.handleLessonIdToUnitId(ms);
            Map<String, List<BasicAppInformation.Lesson>> unitData = new LinkedHashMap<>();
            for (String lessonId : lessonData.keySet()) {
                if (ms.get(lessonId) == null) continue;
                BasicAppInformation.Lesson lesson = new BasicAppInformation.Lesson();
                lesson.setLessonId(lessonId);
                lesson.setLessonName(ms.get(lessonId).getAlias());
                lesson.setCategories(lessonData.get(lessonId));
                List<BasicAppInformation.Lesson> lessons;
                if (unitData.containsKey(lineData.get(lessonId))) {
                    lessons = unitData.get(ms.get(lessonId).getParentId());
                    lessons.add(lesson);
                } else {
                    lessons = new LinkedList<>();
                    lessons.add(lesson);
                    unitData.put(lineData.get(lessonId), lessons);
                }
            }
            ms = newContentLoaderClient.loadBookCatalogByCatalogIds(unitData.keySet());
            for (String ky : unitData.keySet()) {
                BasicAppInformation basicAppInformation = new BasicAppInformation();
                basicAppInformation.setUnitId(ky);
                basicAppInformation.setUnitName(ms.get(ky).getAlias());
                basicAppInformation.setLessons(unitData.get(ky));
                basicAppInformationList.add(basicAppInformation);

            }
        }
        return basicAppInformationList;
    }
}

package com.voxlearning.utopia.service.newhomework.impl.template.report.template;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.content.api.constant.PracticeCategory;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.TeacherReportParameter;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class BaseAppObjectiveConfigTypeProcessorTemplate extends CommonObjectiveConfigTypeProcessorTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.BASIC_APP;
    }


    @Override
    public void handleBaseAppType(TeacherReportParameter teacherReportParameter, boolean isPcWay) {
        ObjectiveConfigType type = teacherReportParameter.getType();
        NewHomework newHomework = teacherReportParameter.getNewHomework();
        Map<String, NewHomeworkResult> newHomeworkResultMap = teacherReportParameter.getNewHomeworkResultMap();
        Map<ObjectiveConfigType, List<Map<String, Object>>> baseAppType = teacherReportParameter.getBaseAppType();

        baseAppType.put(type, processForBaseApp(type, newHomework, newHomeworkResultMap));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> processForBaseApp(ObjectiveConfigType type, NewHomework newHomework, Map<String, NewHomeworkResult> newHomeworkResultMap) {
        List<Map<String, Object>> basicAppInformation = new LinkedList<>();

        Map<String, NewHomeworkResult> newHomeworkResultMapToObjectiveConfigType = newHomeworkResultMap
                .values()
                .stream()
                .filter(o -> o.isFinishedOfObjectiveConfigType(type))
                .collect(Collectors
                        .toMap(NewHomeworkResult::getId, Function.identity()));
        if (MapUtils.isNotEmpty(newHomeworkResultMapToObjectiveConfigType)) {
            Map<String, Map<String, Object>> clMap;
            NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
            List<NewHomeworkApp> apps;
            if (Objects.nonNull(newHomeworkPracticeContent)) {
                apps = newHomeworkPracticeContent.getApps();
                clMap = newHomeworkPracticeContent
                        .getApps()
                        .stream()
                        .map(o -> SafeConverter.toString(o.getCategoryId(), "") + "-" + SafeConverter.toString(o.getLessonId(), ""))
                        .collect(Collectors
                                .toMap(o -> o,
                                        o -> MapUtils.m(
                                                "score", 0,
                                                "num", 0)));
            } else {
                clMap = new LinkedHashMap<>();
                apps = new LinkedList<>();
            }
            //key : categoryId-lessonId-practiceId value:
            newHomeworkResultMapToObjectiveConfigType.forEach((k, value) -> {
                LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> o_map_answer = value.getPractices();
                if (MapUtils.isNotEmpty(o_map_answer) &&
                        Objects.nonNull(o_map_answer.get(type))) {
                    NewHomeworkResultAnswer answer = o_map_answer.get(type);
                    clMap.keySet()
                            .stream()
                            .filter(key_to_answer -> {
                                String[] s = StringUtils.split(key_to_answer, "-");
                                return MapUtils.isNotEmpty(answer.getAppAnswers()) &&
                                        Objects.nonNull(answer.getAppAnswers().get(s[0] + "-" + s[1]));
                            })
                            .forEach(key_to_answer -> {
                                String[] s = StringUtils.split(key_to_answer, "-");
                                if (s.length >= 2) {
                                    Map<String, Object> m = clMap.get(key_to_answer);

                                    m.put("num", (SafeConverter.toInt(m.get("num")) + 1));
                                    double score = SafeConverter.toDouble(answer.getAppAnswers().get(s[0] + "-" + s[1]).getScore());
                                    m.put("score", (SafeConverter.toDouble(m.get("score")) + score));
                                }
                            });
                }
            });
            Map<String, Map<String, Object>> lessonData = new LinkedHashMap<>();
            for (NewHomeworkApp app : apps) {
                Integer categoryId = app.getCategoryId();
                String lessonId = app.getLessonId();
                Long practiceId = app.getPracticeId();
                String k = Objects.isNull(categoryId) ? "" : categoryId + "-" + (Objects.isNull(lessonId) ? "" : lessonId);
                Map<String, Object> map = clMap.get(k);
                Object value = map.get("score");
                int num = SafeConverter.toInt(map.get("num"));
                PracticeType practiceType = practiceLoaderClient.loadPractice(practiceId);
                Map<String, Object> m = MapUtils.m(
                        "averageScore", num == 0 ? 0 : new BigDecimal(SafeConverter.toDouble(value))
                                .divide(new BigDecimal(num), 0, BigDecimal.ROUND_HALF_UP)
                                .intValue(),
                        "categoryId", categoryId,
                        "categoryName", practiceType.getCategoryName(),
                        "practiceCategory", PracticeCategory.icon(practiceType.getCategoryName())
                );
                List<Map<String, Object>> l;
                if (lessonData.containsKey(lessonId)) {
                    l = (List<Map<String, Object>>) lessonData.get(lessonId).get("categories");
                    l.add(m);
                } else {
                    l = new LinkedList<>();
                    l.add(m);
                    lessonData.put(
                            lessonId, MapUtils.m(
                                    "lessons", lessonId,
                                    "categories", l));
                }
            }
            Map<String, NewBookCatalog> ms = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonData.keySet());
            Map<String, String> lineData = NewHomeworkUtils.handleLessonIdToUnitId(ms);
            Map<String, List<Map<String, Object>>> unitData = new LinkedHashMap<>();
            for (String lessonId : lessonData.keySet()) {
                if (!lessonData.containsKey(lessonId)) {
                    continue;
                }
                Map<String, Object> m = MapUtils.m(
                        "lessonId", lessonId,
                        "lessonName", ms.get(lessonId).getAlias(),
                        "categories", lessonData.get(lessonId).get("categories")
                );
                if (unitData.containsKey(lineData.get(lessonId))) {
                    List<Map<String, Object>> l = unitData.get(ms.get(lessonId).getParentId());
                    l.add(m);
                } else {
                    List<Map<String, Object>> l = new LinkedList<>();
                    l.add(m);
                    unitData.put(lineData.get(lessonId), l);
                }
            }
            ms = newContentLoaderClient.loadBookCatalogByCatalogIds(unitData.keySet());
            for (String ky : unitData.keySet()) {
                basicAppInformation.add(
                        MapUtils.m(
                                "unitName", ms.containsKey(ky) ? ms.get(ky).getAlias() : "单元",
                                "unitId", ky,
                                "lessons", unitData.get(ky)
                        ));
            }
        }

        return basicAppInformation;
    }


}

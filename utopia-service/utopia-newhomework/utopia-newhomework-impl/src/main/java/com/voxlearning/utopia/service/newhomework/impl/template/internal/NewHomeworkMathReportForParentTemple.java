package com.voxlearning.utopia.service.newhomework.impl.template.internal;


import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePoint;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class NewHomeworkMathReportForParentTemple extends AbstractNewHomeworkBaseReportForParentTemple {
    @Override
    public Subject getSubject() {
        return Subject.MATH;
    }

    @Override
    protected Set<String> handleHomeworkContent(List<Map<String, Object>> bookInfo) {
        Set<String> homeworkContent = new HashSet<>();
        for (Map infoMap : bookInfo) {
            String sectionName = SafeConverter.toString(infoMap.get("sectionName"), "");
            String unitName = SafeConverter.toString(infoMap.get("unitName"), "");
            if (StringUtils.isNotBlank(unitName)) {
                homeworkContent.add(unitName);
            } else if (StringUtils.isNotBlank(sectionName)) {
                homeworkContent.add(sectionName);
            }
        }
        return homeworkContent;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<String> handlePracticeList(List<Map<String, Object>> newHomeworkInfo, NewHomework newHomework) {
        List<String> practiceContentList = new ArrayList<>();
        for (Map contentMap : newHomeworkInfo) {
            String objectiveConfigTypeName = SafeConverter.toString(contentMap.get("objectiveConfigTypeName"));
            ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeName);
            String newKnowledgePointName;
            //数学只有口算有objectiveConfigTypeName这个字段
            if (objectiveConfigType == ObjectiveConfigType.MENTAL || objectiveConfigType == ObjectiveConfigType.MENTAL_ARITHMETIC) {
                String mentalContent = "口算练习 ";
                List<Map<String, Object>> newKnowledgePoints = new ArrayList<>();
                if (contentMap.get("newKnowledgePoints") != null) {
                    newKnowledgePoints = (List<Map<String, Object>>) contentMap.get("newKnowledgePoints");
                }
                for (Map newKnowledgePoint : newKnowledgePoints) {
                    newKnowledgePointName = SafeConverter.toString(newKnowledgePoint.get("newKnowledgePointName"));
                    if (StringUtils.isNotBlank(newKnowledgePointName)) {
                        mentalContent += newKnowledgePointName;
                    }
                    practiceContentList.add(mentalContent);
                }
            } else if(objectiveConfigType == ObjectiveConfigType.OCR_MENTAL_ARITHMETIC) {
                practiceContentList.add(objectiveConfigType.getValue());
            } else {
                List<Map<String, Object>> newKnowledgePointInformations = new ArrayList<>();
                if (contentMap.get("newKnowledgePointInformation") != null) {
                    newKnowledgePointInformations = (List<Map<String, Object>>) contentMap.get("newKnowledgePointInformation");
                }
                for (Map infoMap : newKnowledgePointInformations) {
                    newKnowledgePointName = SafeConverter.toString(infoMap.get("newKnowledgePointName"));
                    if (StringUtils.isNotBlank(newKnowledgePointName)) {
                        practiceContentList.add(newKnowledgePointName);
                    }
                }
            }
        }
        return practiceContentList;
    }

    @Override
    protected List<Map<String, Object>> handlerNewHomework(NewHomework newHomework) {
        List<Map<String, Object>> result = new LinkedList<>();
        List<NewHomeworkPracticeContent> newHomeworkPracticeContents = newHomework.getPractices();
        Set<String> questionIds = new HashSet<>();
        for (NewHomeworkPracticeContent p : newHomeworkPracticeContents) {
            if (p.getType() == ObjectiveConfigType.MENTAL || p.getType() == ObjectiveConfigType.MENTAL_ARITHMETIC ) {
                Map<String, Object> contentData = new LinkedHashMap<>();
                result.add(contentData);
                contentData.put("objectiveConfigTypeName", p.getType().name());
                Set<String> knowledgePointIds = newHomework.findNewHomeworkQuestions(p.getType())
                        .stream()
                        .map(NewHomeworkQuestion::getKnowledgePointId)
                        .collect(Collectors.toSet());
                Map<String, NewKnowledgePoint> newKnowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePoints(knowledgePointIds);
                List<Map<String, Object>> newKnowledgePoints = newKnowledgePointMap.values()
                        .stream()
                        .map(newKnowledgePoint -> MapUtils.m("newKnowledgePointName", newKnowledgePoint.getName()))
                        .collect(Collectors.toList());
                contentData.put("newKnowledgePoints", newKnowledgePoints);
            } else if (p.getType() == ObjectiveConfigType.OCR_MENTAL_ARITHMETIC) {
                Map<String, Object> contentData = new LinkedHashMap<>();
                contentData.put("objectiveConfigTypeName", p.getType().name());
                result.add(contentData);
            } else {
                newHomework.findNewHomeworkQuestions(p.getType())
                        .stream()
                        .map(NewHomeworkQuestion::getQuestionId)
                        .filter(Objects::nonNull)
                        .forEach(questionIds::add);
            }
        }
        //除口算外的知识点的处理
        Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
        Set<String> knowledgeIds = newQuestionMap.values()
                .stream()
                .filter(newQuestion -> newQuestion.mainNewKnowledgePointList() != null)
                .map(NewQuestion::mainNewKnowledgePointList)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, NewKnowledgePoint> newKnowledgePointMap = newKnowledgePointLoaderClient.loadKnowledgePoints(knowledgeIds);
        List<Map<String, Object>> newKnowledgePointInformation = newKnowledgePointMap.values()
                .stream()
                .map(newKnowledgePoint -> MapUtils.m(
                        "newKnowledgePointId", newKnowledgePoint.getId(),
                        "newKnowledgePointName", newKnowledgePoint.getName()))
                .collect(Collectors.toList());
        result.add(MapUtils.m(
                "newKnowledgePointInformation", newKnowledgePointInformation
        ));
        return result;
    }


}

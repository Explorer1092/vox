package com.voxlearning.utopia.service.newhomework.impl.template.internal;


import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.*;

@Named
public class NewHomeworkChineseReportForParentTemple extends AbstractNewHomeworkBaseReportForParentTemple {
    @Override
    public Subject getSubject() {
        return Subject.CHINESE;
    }

    @Override
    protected Set<String> handleHomeworkContent(List<Map<String, Object>> bookInfo) {
        Set<String> homeworkContent = new HashSet<>();
        for (Map infoMap : bookInfo) {
            String sectionName = SafeConverter.toString(infoMap.get("sectionName"), "");
            String unitName = SafeConverter.toString(infoMap.get("unitName"), "");
            String content = "";
            if (StringUtils.isNotBlank(unitName)) {
                content += unitName + " ";
            }
            if (StringUtils.isNotBlank(sectionName)) {
                content += sectionName;
            }
            if (StringUtils.isNotBlank(content)) {
                homeworkContent.add(content);
            }
        }
        return homeworkContent;
    }

    @Override
    protected List<String> handlePracticeList(List<Map<String, Object>> newHomeworkInfo, NewHomework newHomework) {
        List<String> practiceContentList = new ArrayList<>();
        for (Map contentMap : newHomeworkInfo) {
            String content = "";
            String objectiveConfigTypeName = SafeConverter.toString(contentMap.get("objectiveConfigTypeName"));
            ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeName);
            if (objectiveConfigType != null) {
                int questionNum = SafeConverter.toInt(contentMap.get("questionNum"));
                if (objectiveConfigType == ObjectiveConfigType.READ_RECITE_WITH_SCORE ||  objectiveConfigType == ObjectiveConfigType.WORD_RECOGNITION_AND_READING) {
                    content += objectiveConfigType.getValue() + "(共" + questionNum + "篇)";
                } else {
                    content += objectiveConfigType.getValue() + "(共" + questionNum + "题)";
                }
                practiceContentList.add(content);
            }
        }
        return practiceContentList;
    }

    @Override
    protected List<Map<String, Object>> handlerNewHomework(NewHomework newHomework) {
        List<Map<String, Object>> result = new LinkedList<>();
        List<NewHomeworkPracticeContent> newHomeworkPracticeContents = newHomework.getPractices();
        if (newHomeworkPracticeContents != null && !newHomeworkPracticeContents.isEmpty()) {
            for (NewHomeworkPracticeContent content : newHomeworkPracticeContents){
                Map<String,Object> typeContent = new LinkedHashMap<>();
                typeContent.put("objectiveConfigTypeName",content.getType().name());
                if (content.getType() == ObjectiveConfigType.READ_RECITE_WITH_SCORE || content.getType() == ObjectiveConfigType.WORD_RECOGNITION_AND_READING) {
                    typeContent.put("questionNum", CollectionUtils.isNotEmpty(content.getApps())?content.getApps().size():0);
                } else {
                    typeContent.put("questionNum", content.processNewHomeworkQuestion(false).size());
                }
                result.add(typeContent);
            }
        }
        return result;
    }


}
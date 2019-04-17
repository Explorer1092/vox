package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.*;

@Named
public class NewHomeworkContentProcessOcrMentalArithmetic extends NewHomeworkContentProcessTemplate {

    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkContentTemp() {
        return NewHomeworkContentProcessTemp.OCR_MENTAL_ARITHMETIC;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AssignHomeworkContext processHomeworkContent(AssignHomeworkContext context, Map<String, Object> practice, ObjectiveConfigType objectiveConfigType) {
        List<Map> apps = JsonUtils.fromJsonToList(JsonUtils.toJson(practice.get("apps")), Map.class);
        if (CollectionUtils.isNotEmpty(apps)) {
            if (NewHomeworkType.OCR == context.getNewHomeworkType()) {
                List<String> bookIdList = new ArrayList<>();
                List<String> bookNameList = new ArrayList<>();
                List<String> homeworkDetailList = new ArrayList<>();
                for (Map app : apps) {
                    String bookId = SafeConverter.toString(app.get("bookId"));
                    String bookName = SafeConverter.toString(app.get("bookName"));
                    String homeworkDetail = SafeConverter.toString(app.get("homeworkDetail"));
                    if (StringUtils.isEmpty(bookId) || StringUtils.isEmpty(bookName) || StringUtils.isEmpty(homeworkDetail)) {
                        return contentError(context, objectiveConfigType);
                    }
                    bookIdList.add(bookId);
                    bookNameList.add(bookName);
                    homeworkDetailList.add(homeworkDetail);
                }
                NewHomeworkPracticeContent practiceContent = new NewHomeworkPracticeContent();
                practiceContent.setWorkBookId(StringUtils.join(bookIdList, NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_SEPARATOR));
                practiceContent.setWorkBookName(StringUtils.join(bookNameList, NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_SEPARATOR));
                practiceContent.setHomeworkDetail(StringUtils.join(homeworkDetailList, NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_SEPARATOR));
                practiceContent.setType(objectiveConfigType);
                for (Long groupId : context.getGroupIds()) {
                    List<NewHomeworkPracticeContent> newHomeworkPracticeContents = context.getGroupPractices().computeIfAbsent(groupId, k -> new ArrayList<>());
                    newHomeworkPracticeContents.add(practiceContent);
                }
            } else {
                Map app = apps.iterator().next();
                String workBookId = SafeConverter.toString(app.get("workBookId"));
                String workBookName = SafeConverter.toString(app.get("workBookName"));
                String homeworkDetail = SafeConverter.toString(app.get("homeworkDetail"));

                if (StringUtils.isEmpty(workBookName) || StringUtils.isEmpty(homeworkDetail)) {
                    return contentError(context, objectiveConfigType);
                }

                String bookId = "";
                String unitId = "";
                if (app.containsKey("book")) {
                    Map<String, Object> bookMap = (Map) app.get("book");
                    if (bookMap != null) {
                        bookId = SafeConverter.toString(bookMap.get("bookId"));
                        unitId = SafeConverter.toString(bookMap.get("unitId"));
                    }
                }
                String objectiveId = SafeConverter.toString(app.get("objectiveId"));

                NewHomeworkPracticeContent practiceContent = new NewHomeworkPracticeContent();
                practiceContent.setType(objectiveConfigType);
                practiceContent.setWorkBookId(workBookId);
                practiceContent.setWorkBookName(workBookName);
                practiceContent.setHomeworkDetail(homeworkDetail);
                for (Long groupId : context.getGroupIds()) {
                    List<NewHomeworkPracticeContent> newHomeworkPracticeContents = context.getGroupPractices().computeIfAbsent(groupId, k -> new ArrayList<>());
                    newHomeworkPracticeContents.add(practiceContent);

                    if (app.containsKey("book")) {
                        NewHomeworkBookInfo newHomeworkBookInfo = new NewHomeworkBookInfo();
                        newHomeworkBookInfo.setBookId(bookId);
                        newHomeworkBookInfo.setUnitId(unitId);
                        newHomeworkBookInfo.setObjectiveId(objectiveId);
                        LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> practiceBooksMap = context.getGroupPracticesBooksMap().computeIfAbsent(groupId, k -> new LinkedHashMap<>());
                        practiceBooksMap.put(objectiveConfigType, Collections.singletonList(newHomeworkBookInfo));
                    }
                }
            }
        }
        return context;
    }
}

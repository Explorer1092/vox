package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.OralCommunicationContentType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.BookInfoMapper;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.stone.data.StoneBufferedData;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class NewHomeworkContentProcessOralCommunication extends NewHomeworkContentProcessTemplate {

    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkContentTemp() {
        return NewHomeworkContentProcessTemp.ORAL_COMMUNICATION;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AssignHomeworkContext processHomeworkContent(AssignHomeworkContext context, Map<String, Object> practice, ObjectiveConfigType objectiveConfigType) {
        List<Map> apps = JsonUtils.fromJsonToList(JsonUtils.toJson(practice.get("apps")), Map.class);
        if (CollectionUtils.isNotEmpty(apps)) {
            List<NewHomeworkApp> newHomeworkApps = new ArrayList<>();
            Map<BookInfoMapper, List<String>> bookStoneIdsMap = new LinkedHashMap<>();
            for (Map app : apps) {
                NewHomeworkApp nha = new NewHomeworkApp();
                String stoneId = SafeConverter.toString(app.get("oralCommunicationId"));
                if (StringUtils.isBlank(stoneId)) {
                    return contentError(context, objectiveConfigType);
                }

                List<StoneBufferedData> stoneBufferedDataList = stoneDataLoaderClient.getStoneBufferedDataList(Collections.singleton(stoneId));
                if (CollectionUtils.isEmpty(stoneBufferedDataList) || stoneBufferedDataList.get(0) == null) {
                    return contentError(context, objectiveConfigType);
                }
                StoneBufferedData stoneBufferedData = stoneBufferedDataList.get(0);
                OralCommunicationContentType oralCommunicationContentType = null;
                if (stoneBufferedData.getOralPracticeConversion() != null) {
                    oralCommunicationContentType = OralCommunicationContentType.INTERACTIVE_CONVERSATION;
                } else if (stoneBufferedData.getInteractiveVideo() != null) {
                    oralCommunicationContentType = OralCommunicationContentType.INTERACTIVE_VIDEO;
                } else if (stoneBufferedData.getInteractivePictureBook() != null) {
                    oralCommunicationContentType = OralCommunicationContentType.INTERACTIVE_PICTURE_BOOK;
                }
                nha.setOralCommunicationContentType(oralCommunicationContentType);

                if (app.containsKey("book")) {
                    Map<String, Object> book = (Map<String, Object>) app.get("book");
                    if (book != null) {
                        BookInfoMapper bookInfoMapper = new BookInfoMapper();
                        bookInfoMapper.setBookId(SafeConverter.toString(book.get("bookId"), ""));
                        bookInfoMapper.setUnitId(SafeConverter.toString(book.get("unitId"), ""));
                        bookInfoMapper.setObjectiveId(SafeConverter.toString(app.get("objectiveId"), ""));
                        bookStoneIdsMap.computeIfAbsent(bookInfoMapper, k -> new ArrayList<>()).add(stoneId);
                    }
                }

                nha.setStoneDataId(stoneId);
                newHomeworkApps.add(nha);
            }
            if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
                NewHomeworkPracticeContent nhpr = new NewHomeworkPracticeContent();
                nhpr.setType(objectiveConfigType);
                nhpr.setApps(newHomeworkApps);
                for (Long groupId : context.getGroupIds()) {
                    context.getGroupPractices().computeIfAbsent(groupId, k -> new ArrayList<>()).add(nhpr);
                }
            }

            List<NewHomeworkBookInfo> newHomeworkBookInfoList = bookStoneIdsMap.entrySet()
                    .stream()
                    .map(entry -> {
                        NewHomeworkBookInfo newHomeworkBookInfo = new NewHomeworkBookInfo();
                        BookInfoMapper bookInfoMapper = entry.getKey();
                        newHomeworkBookInfo.setBookId(bookInfoMapper.getBookId());
                        newHomeworkBookInfo.setUnitId(bookInfoMapper.getUnitId());
                        newHomeworkBookInfo.setObjectiveId(bookInfoMapper.getObjectiveId());
                        newHomeworkBookInfo.setStoneIds(entry.getValue());
                        return newHomeworkBookInfo;
                    })
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(newHomeworkBookInfoList)) {
                for (Long groupId : context.getGroupIds()) {
                    context.getGroupPracticesBooksMap().computeIfAbsent(groupId, k -> new LinkedHashMap<>()).put(objectiveConfigType, newHomeworkBookInfoList);
                }
            }
        }
        return context;
    }
}

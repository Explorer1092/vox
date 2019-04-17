package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.callback;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.content.api.entity.NewClazzBookRef;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostAssignHomework;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tanguohong
 * @version 0.1
 * @since 2016/1/28
 */
@Named
public class PostAssignNewHomeworkUpdateClazzBook extends NewHomeworkSpringBean implements PostAssignHomework {
    @Override
    public void afterHomeworkAssigned(Teacher teacher, AssignHomeworkContext context) {

        Map<Long, GroupMapper> groupMapperMap = groupLoaderClient.loadGroups(context.getGroupIds(), false);
        for (NewHomework newHomework : context.getAssignedGroupHomework().values()) {
            GroupMapper group = groupMapperMap.get(newHomework.getClazzGroupId());
            if (group != null && group.getClazzId() != null) {
                LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> practicesBooksMap = context.getGroupPracticesBooksMap().get(newHomework.getClazzGroupId());
                String bookId = null;
                String unitId = null;
                String lessonId = null;
                String sectionId = null;
                if (MapUtils.isNotEmpty(practicesBooksMap)) {
                    for (ObjectiveConfigType type : practicesBooksMap.keySet()) {
                        for (NewHomeworkBookInfo bookInfo : practicesBooksMap.get(type)) {
                            bookId = bookInfo.getBookId();
                            unitId = bookInfo.getUnitId();
                            lessonId = bookInfo.getLessonId();
                            sectionId = bookInfo.getSectionId();
                        }
                    }
                }
                if (bookId != null) {
                    NewClazzBookRef.Location id = new NewClazzBookRef.Location(newHomework.getClazzGroupId(), group.getClazzId(), bookId, context.getTeacher().getSubject().name());
                    newClazzBookServiceClient.updateClazzBook(id, unitId, lessonId, sectionId);
                } else if (NewHomeworkType.MothersDay != newHomework.getType() && NewHomeworkType.Activity != newHomework.getType()) {
                    logger.warn("homework bookId is null, homeworkSource : {}", JsonUtils.toJson(context.getSource()));
                }
            }
        }
    }
}
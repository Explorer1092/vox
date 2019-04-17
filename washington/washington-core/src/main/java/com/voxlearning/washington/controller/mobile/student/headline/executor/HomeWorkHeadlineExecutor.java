package com.voxlearning.washington.controller.mobile.student.headline.executor;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkResultLoaderClient;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.washington.controller.mobile.student.headline.helper.AbstractHeadlineExecutor;
import com.voxlearning.washington.controller.mobile.student.headline.helper.HeadlineMapperContext;
import com.voxlearning.washington.mapper.studentheadline.StudentHeadlineMapper;
import com.voxlearning.washington.mapper.studentheadline.StudentHomeworkHeadlineMapper;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/1/6
 * Time: 14:28
 */
@Named
public class HomeWorkHeadlineExecutor extends AbstractHeadlineExecutor {

    @Inject private NewHomeworkResultLoaderClient newHomeworkResultLoaderClient;
    @Inject private NewHomeworkLoaderClient newHomeworkLoaderClient;

    @Override
    public List<ClazzJournalType> journalTypes() {
        return Collections.singletonList(ClazzJournalType.HOMEWORK_HEADLINE);
    }

    @Override
    public StudentHeadlineMapper generateMapper(ClazzJournal clazzJournal, HeadlineMapperContext context) {
        Set<Long> groupIds = context.getGroupIds();
        if (CollectionUtils.isEmpty(groupIds)) {
            groupIds = mobileStudentClazzHelper.getCurrentUserGroupIds(context.getCurrentUserId());
            context.setGroupIds(groupIds);
        }

        //上下文缓存一次
        Integer studentCount = context.getStudentCount();
        if (studentCount == null) {
            studentCount = mobileStudentClazzHelper.getClassStudentCount(context.getCurrentUserId(), clazzJournal.getClazzId());
            context.setStudentCount(studentCount);
        }

        StudentHomeworkHeadlineMapper studentHomeworkHeadlineMapper = fillHomeworkHeadline(clazzJournal, groupIds);
        if (null != studentHomeworkHeadlineMapper) {
            studentHomeworkHeadlineMapper.setTotalCount(studentCount);
            Boolean timeLimit = false;
            String homeworkId = studentHomeworkHeadlineMapper.getHomeworkId();
            if (StringUtils.isNotBlank(homeworkId)) {
                NewHomework newhomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
                if (newhomework != null) {
                    List<NewHomeworkPracticeContent> practices = newhomework.getPractices();
                    if (!CollectionUtils.isEmpty(practices)) {
                        for (NewHomeworkPracticeContent practiceContent : practices) {
                            if (ObjectiveConfigType.MENTAL_ARITHMETIC.equals(practiceContent.getType())
                                    && practiceContent.getTimeLimit() != null
                                    && practiceContent.getTimeLimit().getTime() != 0) {
                                timeLimit = true;
                                break;
                            }
                        }
                    }
                }
            }
            studentHomeworkHeadlineMapper.setTimeLimit(timeLimit);
        }
        return studentHomeworkHeadlineMapper;
    }

    private StudentHomeworkHeadlineMapper fillHomeworkHeadline(ClazzJournal clazzJournal, Set<Long> groupIds) {
        StudentHomeworkHeadlineMapper mapper = new StudentHomeworkHeadlineMapper();
        mapper.setType(ClazzJournalType.HOMEWORK_HEADLINE.name());
        mapper.setJournalId(clazzJournal.getId());
        mapper.initDateTime(clazzJournal.getCreateDatetime());

        Map<String, Object> extInfo = JsonUtils.fromJson(clazzJournal.getJournalJson());

        if (!extInfo.containsKey("homeworkId") || extInfo.get("homeworkId") == null || !extInfo.containsKey("subject")) {
            return null;
        }

        String homeworkId = SafeConverter.toString(extInfo.get("homeworkId"));
        mapper.setHomeworkId(homeworkId);

        mapper.setSubject(SafeConverter.toString(extInfo.get("subject")));
        Subject subject = Subject.ofWithUnknown(mapper.getSubject());
        if (Subject.UNKNOWN != subject) {
            mapper.setSubjectName(subject.getValue());
        }

        //查询作业完成人数,缓存10分钟
        Integer finishCount = 0;
        String cacheKey = "HOMEWORK_HEADLINE_FINISH_COUNT_" + homeworkId;
        CacheObject<Integer> finishCountCache = washingtonCacheSystem.CBS.flushable.get(cacheKey);
        if (null == finishCountCache || null == finishCountCache.getValue()) {
            NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
            if (null == newHomework) return null;
            if (!groupIds.contains(newHomework.getClazzGroupId())) return null; //作业必须是当前用户所在的group的

            Map<String, NewHomeworkResult> homeworkResultMap = newHomeworkResultLoaderClient.findByHomeworkForReport(newHomework);
            if (MapUtils.isNotEmpty(homeworkResultMap)) {
                List<NewHomeworkResult> results = homeworkResultMap.values().stream().filter(r -> null != r.getFinishAt()).collect(Collectors.toList());
                finishCount = results.size();
            }

            washingtonCacheSystem.CBS.flushable.set(cacheKey, 10 * 60, finishCount);
        } else {
            finishCount = finishCountCache.getValue();
        }
        mapper.setFinishCount(finishCount);

        return mapper;
    }
}

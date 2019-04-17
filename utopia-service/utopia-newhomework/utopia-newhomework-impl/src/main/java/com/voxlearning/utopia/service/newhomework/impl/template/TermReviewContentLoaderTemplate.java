package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.newhomework.api.constant.BasicReviewContentType;
import com.voxlearning.utopia.service.newhomework.api.constant.TermReviewContentType;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.entity.TermReview;
import com.voxlearning.utopia.service.question.api.mapper.review.EnglishReview;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/11/21
 */
abstract public class TermReviewContentLoaderTemplate extends NewHomeworkSpringBean {

    @Inject private RaikouSDK raikouSDK;

    abstract public TermReviewContentType getTermReviewContentType();

    abstract public MapMessage loadNewContent(Teacher teacher,
                                              List<Long> groupIds,
                                              String bookId,
                                              TermReviewContentType termReviewContentType);

    protected Subject getBookSubject(Teacher teacher, String bookId) {
        NewBookProfile newBookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
        if (newBookProfile == null || newBookProfile.getSubjectId() == null) {
            return teacher.getSubject();
        }
        return Subject.fromSubjectId(newBookProfile.getSubjectId());
    }

    protected MapMessage loadBasicContent(Teacher teacher,
                                          List<Long> groupIds,
                                          String bookId) {
        Map<Long, GroupMapper> groupMap = groupLoaderClient.loadGroups(groupIds, false);
        Set<Long> clazzIds = groupMap.values().stream().map(GroupMapper::getClazzId).collect(Collectors.toSet());
        Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        Map<Long, List<BasicReviewHomeworkPackage>> packageMap = basicReviewHomeworkPackageDao.loadBasicReviewHomeworkPackageByClazzGroupIds(groupIds);
        List<Map<String, Object>> groupStatus = new ArrayList<>();
        for (Long groupId : groupIds) {
            String clazzName = "";
            GroupMapper groupMapper = groupMap.get(groupId);
            if (groupMapper != null) {
                Clazz clazz = clazzMap.get(groupMapper.getClazzId());
                if (clazz != null) {
                    clazzName = clazz.formalizeClazzName();
                }
            }
            boolean assigned = CollectionUtils.isNotEmpty(packageMap.get(groupId));
            groupStatus.add(MapUtils.m(
                    "groupId", groupId,
                    "clazzName", clazzName,
                    "assigned", assigned,
                    "isSelect", !assigned
            ));
        }
        Subject subject = getBookSubject(teacher, bookId);
        List<BasicReviewContentType> basicReviewContentTypes = new ArrayList<>();
        if (subject == Subject.ENGLISH) {
            basicReviewContentTypes.add(BasicReviewContentType.WORD);
            // 重点句可能存在没内容的情况
            List<EnglishReview> englishReviews = termReviewLoaderClient.getTermReviewLoader().loadEnglishReviews(bookId, TermReview.EnglishType.SENTENCE_ONLY);
            if (CollectionUtils.isNotEmpty(englishReviews)) {
                basicReviewContentTypes.add(BasicReviewContentType.SENTENCE);
            }
        } else if (subject == Subject.MATH) {
            basicReviewContentTypes.add(BasicReviewContentType.CALCULATION);
        } else {
            basicReviewContentTypes.add(BasicReviewContentType.READ_RECITE_WITH_SCORE);
        }
        List<Map<String, Object>> homeworkContent = basicReviewContentTypes.stream()
                .map(basicReviewContentType -> MapUtils.m(
                        "contentType", basicReviewContentType,
                        "contentTypeName", basicReviewContentType.getName(),
                        "contentTypeDescription", basicReviewContentType.getDescription(),
                        "isSelect", true
                ))
                .collect(Collectors.toList());
        List<Map<String, Object>> homeworkDays = new ArrayList<>();
        homeworkDays.add(MapUtils.m("day", 7, "minutes", 7, "wordCount", 28, "sentenceCount", 7, "isSelect", false));
        homeworkDays.add(MapUtils.m("day", 10, "minutes", 5, "wordCount", 20, "sentenceCount", 5, "isSelect", true));
        if (subject != Subject.CHINESE) {
            homeworkDays.add(MapUtils.m("day", 15, "minutes", 3, "wordCount", 12, "sentenceCount", 3, "isSelect", false));
        }
        Map<String, Object> basicMap = new LinkedHashMap<>();
        basicMap.put("groupStatus", groupStatus);
        basicMap.put("homeworkContent", homeworkContent);
        basicMap.put("homeworkDays", homeworkDays);
        return MapMessage.successMessage().add("basic", basicMap);
    }
}

/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.alps.spi.common.JsonStringDeserializer;
import com.voxlearning.utopia.service.user.api.constants.SystemRobot;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.*;
import com.voxlearning.utopia.service.zone.api.mapper.JournalPagination;
import com.voxlearning.utopia.service.zone.api.mapper.PopularityPagination;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.zone.api.constant.ZoneConstants.IMG_COMMENT;

@ServiceVersion(version = "20150820")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface ClazzJournalLoader extends IPingable {

    @Idempotent
    default List<LikeDetail> loadJournalLikeDetails(Long journalId) {
        if (journalId == null) {
            return Collections.emptyList();
        }
        return loadJournalLikeDetails(Collections.singleton(journalId))
                .getOrDefault(journalId, Collections.emptyList());
    }

    @Idempotent
    Map<Long, List<LikeDetail>> loadJournalLikeDetails(Collection<Long> journalIds);

    @Idempotent
    List<LikeDetail> loadUserJournalLikeDetails(Long journalOwnerId);

    @Idempotent
    default List<ClazzZoneComment> loadJournalComments(Long journalId) {
        if (journalId == null) {
            return Collections.emptyList();
        }
        return loadJournalComments(Collections.singleton(journalId))
                .getOrDefault(journalId, Collections.emptyList());
    }

    @Idempotent
    Map<Long, List<ClazzZoneComment>> loadJournalComments(Collection<Long> journalIds);

    @Idempotent
    List<ClazzZoneComment> loadUserJournalComments(Long journalOwnerId);

    @Idempotent
    default ClazzJournal loadClazzJournal(Long id) {
        if (id == null) {
            return null;
        }
        return loadClazzJournals(Collections.singleton(id)).get(id);
    }

    @Idempotent
    Map<Long, ClazzJournal> loadClazzJournals(Collection<Long> ids);

    @Idempotent
    Set<ClazzJournal.ComplexID> __queryByClazzId(Long clazzId);

    @Idempotent
    Set<ClazzJournal.ComplexID> __queryByUserId(Long userId);

    @Idempotent
    Set<ClazzJournal.ComplexID> __queryByJournalType(ClazzJournalType journalType);

    @Idempotent
    JournalPagination getClazzJournals(Long userId, Long clazzId,
                                       int page, int size,
                                       ClazzJournalCategory category,
                                       Collection<Long> groupIds);

    @Idempotent
    JournalPagination getClazzJournals(Long studentId, int page, int size);

    @Idempotent
    JournalPagination getClazzJournals(Long studentId, ClazzJournalType journalType, int page, int size);

    default void __fillJournalInfo(ClazzJournal journal, JournalPagination.JournalMapper mapper) {
        mapper.setJournalId(journal.getId());
        mapper.setClazzId(journal.getClazzId());
        mapper.setJournalType(journal.getJournalType());
        mapper.setDate(DateUtils.dateToString(journal.getUpdateDatetime(), "yyyy-MM-dd HH:mm"));
        mapper.setRelevantUserId(journal.getRelevantUserId());
        mapper.getParam().putAll(JsonStringDeserializer.getInstance().deserialize(journal.getJournalJson()));
    }

    default void __fillJournalInfo(ClazzJournal journal, JournalPagination.JournalMapper mapper,
                                   Map<Long, User> relevantUsers, Map<Long, StudentInfo> relevantStudentInfos) {
        mapper.setJournalId(journal.getId());
        mapper.setClazzId(journal.getClazzId());
        mapper.setJournalType(journal.getJournalType());
        mapper.setDate(DateUtils.dateToString(journal.getCreateDatetime(), "yyyy-MM-dd HH:mm"));

        SystemRobot robot = SystemRobot.getInstance();
        if (robot.getId().equals(journal.getRelevantUserId())) {
            mapper.setRelevantUserId(robot.getId());
            mapper.setRelevantUserName(robot.fetchRealname());
            mapper.setRelevantUserImg(robot.fetchImageUrl());
            mapper.setRelevantUserType(robot.fetchUserType());
        } else {
            User user = relevantUsers.get(journal.getRelevantUserId());
            if (user != null) {
                mapper.setRelevantUserId(user.getId());
                mapper.setRelevantUserName(user.fetchRealname());
                mapper.setRelevantUserImg(user.fetchImageUrl());
                mapper.setRelevantUserType(user.fetchUserType());
            }
        }
        StudentInfo info = relevantStudentInfos.get(journal.getRelevantUserId());
        mapper.setBubble(info == null ? ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE : info.getBubbleId());
        mapper.setLikeCount(journal.getLikeCount());
        mapper.getParam().putAll(JsonStringDeserializer.getInstance().deserialize(journal.getJournalJson()));
    }

    default void __fillLikeDetail(List<LikeDetail> likeDetails, JournalPagination.JournalMapper mapper, Long userId) {
        List<String> names = new ArrayList<>();
        List<Long> likeUserIds = new ArrayList<>();
        for (LikeDetail detail : likeDetails) {
            names.add(detail.getUserName());
            likeUserIds.add(detail.getUserId());
        }
        mapper.setCanLike(!likeUserIds.contains(userId));
        mapper.setNames(names);
    }

    default void __fillComment(List<ClazzZoneComment> comments, JournalPagination.JournalMapper mapper, Long userId) {
        List<Long> commentUserIds = new ArrayList<>();
        Map<Long, List<ClazzZoneComment>> categoryByCommentImg = new HashMap<>();   // 按表情分组
        for (ClazzZoneComment comment : comments) {
            commentUserIds.add(comment.getUserId());
            if (categoryByCommentImg.containsKey(comment.getImgComment())) {
                categoryByCommentImg.get(comment.getImgComment()).add(comment);
            } else {
                List<ClazzZoneComment> list = new ArrayList<>();
                list.add(comment);
                categoryByCommentImg.put(comment.getImgComment(), list);
            }
        }
        mapper.setCanComment(commentUserIds.contains(userId));

        List<Map<String, Object>> commentStat = new ArrayList<>();
        for (Long imgId : IMG_COMMENT) {
            Map<String, Object> map = new HashMap<>();
            map.put("imgId", imgId);
            if (categoryByCommentImg.keySet().contains(imgId)) {
                map.put("count", categoryByCommentImg.get(imgId).size());
                List<String> names = categoryByCommentImg.get(imgId).stream()
                        .map(ClazzZoneComment::getUserName).collect(Collectors.toList());
                map.put("names", names);
            } else {
                map.put("count", 0);
                map.put("names", new ArrayList<>());
            }
            commentStat.add(map);
        }
        mapper.setComments(commentStat);
    }

    default PopularityPagination getUserPopularity(Long studentId, int page, int size) {
        if (studentId == null || page < 0 || size <= 0) {
            return new PopularityPagination();
        }

        // 获取该学生的所有赞和评论
        Map<Long, LikeDetail> likes = loadUserJournalLikeDetails(studentId).stream()
                .filter(e -> !Objects.equals(e.getUserId(), e.getJournalOwnerId()))
                .collect(Collectors.toMap(LikeDetail::getId, Function.<LikeDetail>identity()));
        Map<Long, ClazzZoneComment> comments = loadUserJournalComments(studentId).stream()
                .filter(e -> !Objects.equals(e.getUserId(), e.getJournalOwnerId()))
                .collect(Collectors.toMap(ClazzZoneComment::getId, Function.<ClazzZoneComment>identity()));

        // 混合排序
        List<Map<String, Object>> list = likes.values()
                .stream()
                .map(like -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", like.getId());
                    m.put("ts", like.getCreateDatetime().getTime());
                    m.put("entity", LikeDetail.class.getSimpleName());
                    m.put("jid", like.getJournalId());
                    return m;
                })
                .collect(Collectors.toList());
        list.addAll(comments.values()
                .stream()
                .map(comment -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", comment.getId());
                    m.put("ts", comment.getCreateDatetime().getTime());
                    m.put("entity", ClazzZoneComment.class.getSimpleName());
                    m.put("jid", comment.getJournalId());
                    return m;
                })
                .collect(Collectors.toList()));
        Collections.sort(list, ((o1, o2) -> {
            long l1 = SafeConverter.toLong(o1.get("ts"));
            long l2 = SafeConverter.toLong(o2.get("ts"));
            return Long.compare(l2, l1);
        }));

        // 分页
        Pageable pageable = new PageRequest(page, size);
        Page<Map<String, Object>> idPage = PageableUtils.listToPage(list, pageable);

        // 获取该分页中的动态id
        Set<Long> candidateIds = idPage.getContent().stream()
                .map(e -> SafeConverter.toLong(e.get("jid")))
                .collect(Collectors.toSet());
        Map<Long, ClazzJournal> journals = loadClazzJournals(candidateIds);

        // 返回值
        List<PopularityPagination.PopularityMapper> mappers = new ArrayList<>();
        for (Map<String, Object> each : idPage.getContent()) {
            ClazzJournal journal = journals.get(SafeConverter.toLong(each.get("jid")));
            if (journal == null) continue;

            Long id = SafeConverter.toLong(each.get("id"));
            Long timestamp = SafeConverter.toLong(each.get("ts"));
            String entityName = SafeConverter.toString(each.get("entity"));

            PopularityPagination.PopularityMapper mapper = new PopularityPagination.PopularityMapper();
            mapper.setJournalId(journal.getId());
            mapper.setJournalType(journal.getJournalType());
            mapper.setDate(DateUtils.dateToString(new Date(timestamp), "yyyy-MM-dd HH:mm"));
            mapper.getParam().putAll(JsonStringDeserializer.getInstance().deserialize(journal.getJournalJson()));
            if (StringUtils.equals(entityName, LikeDetail.class.getSimpleName())) {
                LikeDetail like = likes.get(id);
                if (like == null) continue;
                mapper.setUserId(like.getUserId());
                mapper.setUserName(like.getUserName());
                mapper.setUserImg(like.getUserImg());
                mapper.setType("LIKE");
            } else if (StringUtils.equals(entityName, ClazzZoneComment.class.getSimpleName())) {
                ClazzZoneComment comment = comments.get(id);
                if (comment == null) continue;
                mapper.setUserId(comment.getUserId());
                mapper.setUserName(comment.getUserName());
                mapper.setUserImg(comment.getUserImg());
                mapper.setCommentImg(comment.getImgComment());
                mapper.setType("COMMENT");
            } else {
                continue;
            }
            mappers.add(mapper);
        }

        return new PopularityPagination(mappers, pageable, idPage.getTotalElements());
    }
}

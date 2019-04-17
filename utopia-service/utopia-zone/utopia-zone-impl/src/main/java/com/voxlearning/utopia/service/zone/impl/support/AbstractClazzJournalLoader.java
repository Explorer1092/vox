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

package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.user.api.UserLoader;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.ClazzJournalLoader;
import com.voxlearning.utopia.service.zone.api.PersonalZoneLoader;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneComment;
import com.voxlearning.utopia.service.zone.api.entity.LikeDetail;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;
import com.voxlearning.utopia.service.zone.api.mapper.JournalPagination;

import java.util.*;
import java.util.stream.Collectors;

abstract public class AbstractClazzJournalLoader extends SpringContainerSupport implements ClazzJournalLoader {

    abstract protected PersonalZoneLoader getPersonalZoneLoader();

    protected JournalPagination $getClazzJournals(Long userId, Long clazzId,
                                                  int page, int size,
                                                  ClazzJournalCategory category,
                                                  Collection<Long> groupIds,
                                                  UserLoader userLoader) {
        if (userId == null || clazzId == null || page < 0 || size <= 0) {
            return new JournalPagination();
        }
        Set<Long> gids = CollectionUtils.toLinkedHashSet(groupIds);

        Set<ClazzJournal.ComplexID> set = __queryByClazzId(clazzId);
        if (category != null) {
            // JOURNAL_CATEGORY=?
            set = set.stream()
                    .filter(t -> t.getCategory() == category.getId())
                    .collect(Collectors.toSet());
        }else{
            //这种全查的情况要把APPLICATION_STD过滤掉
            set = set.stream().filter(t->t.getCategory() != ClazzJournalCategory.APPLICATION_STD.getId()).collect(Collectors.toSet());
        }

        if (!groupIds.isEmpty()) {
            // CLAZZ_GROUP_ID IS NULL OR CLAZZ_GROUP_ID IN (?)
            set = set.stream()
                    .filter(t -> t.getGroupId() == 0 || gids.contains(t.getGroupId()))
                    .collect(Collectors.toSet());
        }

        // SORT BY CREATE_DATETIME DESC
        // 等价于 SORT BY ID DESC
        TreeSet<ClazzJournal.ComplexID> sorted = new TreeSet<>((o1, o2) -> Long.compare(o2.getId(), o1.getId()));
        sorted.addAll(set);

        // 直接在这里就可以完成分页的动作了
        Pageable pageable = new PageRequest(page, size);
        Page<ClazzJournal.ComplexID> idPage = PageableUtils.listToPage(new ArrayList<>(sorted), pageable);

        // candidateIds就是本页需要读取的clazzJournal的ids，都捞过来吧
        // 因为loadClazzJournals返回的结果是乱序，因此还需要再排序一次
        Set<Long> candidateIds = idPage.getContent().stream()
                .map(ClazzJournal.ComplexID::getId)
                .collect(Collectors.toSet());
        List<ClazzJournal> content = loadClazzJournals(candidateIds).values().stream()
                .sorted((o1, o2) -> Long.compare(o2.getId(), o1.getId()))
                .collect(Collectors.toList());

        Page<ClazzJournal> journalPage = new PageImpl<>(content, pageable, idPage.getTotalElements());

        candidateIds = journalPage.getContent().stream()
                .map(ClazzJournal::getId)
                .collect(Collectors.toSet());

        // 查询此次分页中所有新鲜事对应的likeDetail和clazzZoneComment
        Map<Long, List<LikeDetail>> detailMap = loadJournalLikeDetails(candidateIds);
        Map<Long, List<ClazzZoneComment>> commentMap = loadJournalComments(candidateIds);

        // 查询此次分页中所有的相关用户
        Set<Long> relevantUserIds = journalPage.getContent().stream()
                .map(ClazzJournal::getRelevantUserId)
                .collect(Collectors.toSet());
        Map<Long, User> relevantUsers = userLoader.loadUsers(relevantUserIds);

        // 查StudentInfo只查学生即可
        Set<Long> studentIds = journalPage.getContent().stream()
                .filter(t -> t.getRelevantUserType() == UserType.STUDENT)
                .map(ClazzJournal::getRelevantUserId)
                .collect(Collectors.toSet());
        Map<Long, StudentInfo> relevantStudentInfos = getPersonalZoneLoader().loadStudentInfos(studentIds);


        List<JournalPagination.JournalMapper> mappers = new ArrayList<>();
        for (ClazzJournal journal : journalPage) {
            JournalPagination.JournalMapper mapper = new JournalPagination.JournalMapper();
            __fillJournalInfo(journal, mapper, relevantUsers, relevantStudentInfos);

            List<LikeDetail> likeDetails = detailMap.get(journal.getId()) == null ? Collections.<LikeDetail>emptyList() : detailMap.get(journal.getId());
            __fillLikeDetail(likeDetails, mapper, userId);

            List<ClazzZoneComment> comments = commentMap.get(journal.getId()) == null ? Collections.<ClazzZoneComment>emptyList() : commentMap.get(journal.getId());
            __fillComment(comments, mapper, userId);

            mappers.add(mapper);
        }

        return new JournalPagination(mappers, pageable, journalPage.getTotalElements());
    }

    protected JournalPagination $getClazzJournals(Long studentId, int page, int size, UserLoader userLoader) {
        if (studentId == null || page < 0 || size <= 0) {
            return new JournalPagination();
        }
        Set<ClazzJournal.ComplexID> set = __queryByUserId(studentId);
        //这种全查的情况要把APPLICATION_STD过滤掉
        set = set.stream().filter(t->t.getCategory() != ClazzJournalCategory.APPLICATION_STD.getId()).collect(Collectors.toSet());


        // SORT BY CREATE_DATETIME DESC
        // 等价于 SORT BY ID DESC
        TreeSet<ClazzJournal.ComplexID> sorted = new TreeSet<>((o1, o2) -> Long.compare(o2.getId(), o1.getId()));
        sorted.addAll(set);

        // 直接在这里就可以完成分页的动作了
        Pageable pageable = new PageRequest(page, size);
        Page<ClazzJournal.ComplexID> idPage = PageableUtils.listToPage(new ArrayList<>(sorted), pageable);

        // candidateIds就是本页需要读取的clazzJournal的ids，都捞过来吧
        // 因为loadClazzJournals返回的结果是乱序，因此还需要再排序一次
        Set<Long> candidateIds = idPage.getContent().stream()
                .map(ClazzJournal.ComplexID::getId)
                .collect(Collectors.toSet());
        List<ClazzJournal> content = loadClazzJournals(candidateIds).values().stream()
                .sorted((o1, o2) -> Long.compare(o2.getId(), o1.getId()))
                .collect(Collectors.toList());

        Page<ClazzJournal> journalPage = new PageImpl<>(content, pageable, idPage.getTotalElements());

        candidateIds = journalPage.getContent().stream()
                .map(ClazzJournal::getId)
                .collect(Collectors.toSet());

        // 查询此次分页中所有新鲜事对应的likeDetail和clazzZoneComment
        Map<Long, List<LikeDetail>> detailMap = loadJournalLikeDetails(candidateIds);
        Map<Long, List<ClazzZoneComment>> commentMap = loadJournalComments(candidateIds);

        // 查询此次分页中所有的相关用户
        Set<Long> relevantUserIds = journalPage.getContent().stream()
                .map(ClazzJournal::getRelevantUserId)
                .collect(Collectors.toSet());
        Map<Long, User> relevantUsers = userLoader.loadUsers(relevantUserIds);

        // 查StudentInfo只查学生即可
        Set<Long> studentIds = journalPage.getContent().stream()
                .filter(t -> t.getRelevantUserType() == UserType.STUDENT)
                .map(ClazzJournal::getRelevantUserId)
                .collect(Collectors.toSet());
        Map<Long, StudentInfo> relevantStudentInfos = getPersonalZoneLoader().loadStudentInfos(studentIds);

        List<JournalPagination.JournalMapper> mappers = new ArrayList<>();
        for (ClazzJournal journal : journalPage) {
            JournalPagination.JournalMapper mapper = new JournalPagination.JournalMapper();
            __fillJournalInfo(journal, mapper, relevantUsers, relevantStudentInfos);

            List<LikeDetail> likeDetails = detailMap.get(journal.getId()) == null ? Collections.<LikeDetail>emptyList() : detailMap.get(journal.getId());
            __fillLikeDetail(likeDetails, mapper, studentId);

            List<ClazzZoneComment> comments = commentMap.get(journal.getId()) == null ? Collections.<ClazzZoneComment>emptyList() : commentMap.get(journal.getId());
            __fillComment(comments, mapper, studentId);

            mappers.add(mapper);
        }

        return new JournalPagination(mappers, pageable, journalPage.getTotalElements());
    }

    @Override
    public JournalPagination getClazzJournals(Long studentId, ClazzJournalType journalType, int page, int size) {
        if (journalType == null || page < 0 || size <= 0) {
            return new JournalPagination();
        }
        Set<ClazzJournal.ComplexID> set;
        long userId = SafeConverter.toLong(studentId);
        if (userId != 0) {
            set = __queryByUserId(userId).stream()
                    .filter(t -> t.getType() == journalType.getId())
                    .collect(Collectors.toSet());
        } else {
            set = __queryByJournalType(journalType);
        }

        // SORT BY CREATE_DATETIME DESC
        // 等价于 SORT BY ID DESC
        TreeSet<ClazzJournal.ComplexID> sorted = new TreeSet<>((o1, o2) -> Long.compare(o2.getId(), o1.getId()));
        sorted.addAll(set);

        // 直接在这里就可以完成分页的动作了
        Pageable pageable = new PageRequest(page, size);
        Page<ClazzJournal.ComplexID> idPage = PageableUtils.listToPage(new ArrayList<>(sorted), pageable);

        // candidateIds就是本页需要读取的clazzJournal的ids，都捞过来吧
        // 因为loadClazzJournals返回的结果是乱序，因此还需要再排序一次
        Set<Long> candidateIds = idPage.getContent().stream()
                .map(ClazzJournal.ComplexID::getId)
                .collect(Collectors.toSet());
        List<ClazzJournal> content = loadClazzJournals(candidateIds).values().stream()
                .sorted((o1, o2) -> Long.compare(o2.getId(), o1.getId()))
                .collect(Collectors.toList());

        Page<ClazzJournal> journalPage = new PageImpl<>(content, pageable, idPage.getTotalElements());

        List<JournalPagination.JournalMapper> mappers = new ArrayList<>();
        for (ClazzJournal journal : journalPage) {
            JournalPagination.JournalMapper mapper = new JournalPagination.JournalMapper();
            __fillJournalInfo(journal, mapper);
            mappers.add(mapper);
        }

        return new JournalPagination(mappers, pageable, journalPage.getTotalElements());
    }
}

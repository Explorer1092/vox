/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.afenti.impl.util;

import com.voxlearning.alps.annotation.meta.BookStatus;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiLearningPlanUnitRankManagerPersistence;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author vincent
 * @since 14-8-26
 */
@Named
public class FetchRecommendedBook extends UtopiaAfentiSpringBean {

    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject private AfentiLearningPlanUnitRankManagerPersistence afentiLearningPlanUnitRankManagerPersistence;
    @Inject private NewContentLoaderClient newContentLoaderClient;

    public NewBookProfile fetchRecommendedBookForSystemClazz(Subject subject, Clazz clazz) {
        if (clazz == null) {
            return null;
        }

        final ClazzLevel clazzLevel;
        // 学前的默认1年级
        int level = ClazzLevel.getLevel(clazz.getClazzLevel());
        if (level > 54) {
            clazzLevel = ClazzLevel.SIXTH_GRADE;
        } else if (level >= 51 && level <= 54) {
            clazzLevel = ClazzLevel.FIRST_GRADE;
        } else if (level > 6 && level < 51 && subject != Subject.ENGLISH) {
            clazzLevel = ClazzLevel.SIXTH_GRADE;
        } else {
            clazzLevel = clazz.getClazzLevel();
        }

        List<String> haveAfentiRankBookIds = afentiLearningPlanUnitRankManagerPersistence.findAllBookIds();
        // 处理教材前缀
        List<String> bookIds = new ArrayList<>();
        for (String b : haveAfentiRankBookIds) {
            String bookId = AfentiUtils.getBookIdByNewBookId(b);
            if (!bookIds.contains(bookId)) {
                bookIds.add(bookId);
            }
        }


        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(clazz.getSchoolId())
                .getUninterruptibly();
        String newBookId = newContentLoaderClient.initializeClazzBookForAfenti(subject, clazz.getClazzLevel(), school.getRegionCode());
        NewBookProfile newBookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singletonList(newBookId)).get(newBookId);

        if (newBookProfile != null && BookStatus.ONLINE.name().equals(newBookProfile.getStatus())) {
            if (bookIds.contains(newBookId)) {
                return newBookProfile;
            }
        }

        Map<String, NewBookProfile> bookMap = newContentLoaderClient.loadBookProfilesIncludeDisabled(bookIds);
        for (NewBookProfile book : bookMap.values()) {
            Subject bookSubject = Subject.fromSubjectId(book.getSubjectId());
            if ((bookSubject.equals(subject) || (clazzLevel.getLevel() > 6 && clazzLevel.getLevel() < 10 && subject == Subject.ENGLISH && bookSubject == Subject.JENGLISH)) //兼容初中英语
                    && book.getClazzLevel() == clazzLevel.getLevel()
                    && BookStatus.ONLINE.name().equals(book.getStatus())) {
                return book;
            }
        }
        NewBookProfile delBook = null;
        for (NewBookProfile book : bookMap.values()) {
            if (Subject.fromSubjectId(book.getSubjectId()).equals(subject)) {
                delBook = book;
                break;
            }
        }
        return delBook;
    }

    public NewBookProfile fetchRecommendedBookForSystemClazz(Subject subject, Clazz clazz, AfentiLearningType type) {
        if (clazz == null || type == null) {
            return null;
        }
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(clazz.getSchoolId()).getUninterruptibly();

        //得到当前预习的年级
        ClazzLevel level = clazz.getClazzLevel();

        Term term = SchoolYear.newInstance().currentTerm();
        if (type == AfentiLearningType.preparation) {
            term = Term.上学期;
        }

        NewBookProfile newBookProfile = newContentLoaderClient.initializeClazzBookForAfenti(subject, level, term, school.getRegionCode());

        List<String> haveAfentiRankBookIds = new ArrayList<>();
        switch (type) {
            case preparation:
                haveAfentiRankBookIds = afentiLearningPlanUnitRankManagerPersistence.findAllBookIdsForPreparation();
                break;
            case review:
                haveAfentiRankBookIds = afentiLearningPlanUnitRankManagerPersistence.findAllBookIdsForReview();
                break;
        }

        if (newBookProfile != null && BookStatus.ONLINE.name().equals(newBookProfile.getStatus())) {
            if (haveAfentiRankBookIds.contains(newBookProfile.getId())) {
                return newBookProfile;
            }
        }

        Map<String, NewBookProfile> bookMap = newContentLoaderClient.loadBookProfilesIncludeDisabled(haveAfentiRankBookIds);
        for (NewBookProfile book : bookMap.values()) {
            if (Subject.fromSubjectId(book.getSubjectId()) == subject && book.getClazzLevel() == level.getLevel()) {
                return book;
            }
        }

        NewBookProfile delBook = null;
        for (NewBookProfile book : bookMap.values()) {
            if (Subject.fromSubjectId(book.getSubjectId()).equals(subject)) {
                delBook = book;
                break;
            }
        }
        return delBook;
    }

    public NewBookProfile fetchRecommendedBookByOldBook(NewBookProfile oldBook) {
        if (oldBook == null) {
            return null;
        }
        NewBookProfile res = newContentLoaderClient.loadBooksBySubjectId(oldBook.getSubjectId()).stream()
                .filter(e -> !e.isDeletedTrue())
                .filter(e -> e.isOnline())
                .filter(e -> e.getLatestVersion() != null && e.getLatestVersion() == 1)
                .filter(e -> e.getSeriesId() != null && Objects.equals(e.getSeriesId(), oldBook.getSeriesId()))
                .filter(e -> e.getClazzLevel() != null && Objects.equals(e.getClazzLevel(), oldBook.getClazzLevel()))
                .filter(e -> e.getTermType() != null && Objects.equals(e.getTermType(), oldBook.getTermType()))
                .findFirst()
                .orElse(null);
        if (res != null) {
            List<String> haveAfentiRankBookIds = afentiLearningPlanUnitRankManagerPersistence.findAllBookIds();
            if (CollectionUtils.isNotEmpty(haveAfentiRankBookIds) && haveAfentiRankBookIds.contains(res.getId())) {
                return res;
            }
            logger.warn("The book has no ranks data. book:{}", res.getId());
            res = null;
        }

        return res;
    }
}

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

package com.voxlearning.utopia.service.afenti.impl.service.processor.book;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.FetchGradeBookContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/7/13
 */
@Named
public class FGB_LoadBooks extends SpringContainerSupport implements IAfentiTask<FetchGradeBookContext> {
    @Inject protected NewContentLoaderClient newContentLoaderClient;

//    private boolean isChnBookAvailable(NewBookProfile book) {
//        if (book.getClazzLevel() == null) return false;
//        if (Arrays.asList(2, 3, 4, 5, 6).contains(book.getClazzLevel()) && book.getlate ) return true;
//        return book.getClazzLevel() == 1 && (book.getTermType() == 1 || book.getLatestVersion() == 1);
//    }

    @Override
    public void execute(FetchGradeBookContext context) {
        // 获取指定学科，指定年级的已经生成阿分题学习城堡关卡的教材
        Subject subject = context.getSubject();
        if (context.getClazzLevel().getLevel() > 6 && context.getClazzLevel().getLevel() < 10 && context.getSubject() == Subject.ENGLISH) {  //中学英语
            subject = Subject.JENGLISH;
        }

        List<NewBookProfile> books = newContentLoaderClient.loadBooks(subject)
                .stream()
                .filter(NewBookProfile::isOnline)
                .filter(b -> ClazzLevel.parse(b.getClazzLevel()) == context.getClazzLevel())
                .filter(b -> b.getLatestVersion() == 1)
                .filter(b -> context.getCandidates().contains(b.getId()))
                .collect(Collectors.toList());

        for (NewBookProfile book : books) {
            String bookName = StringUtils.defaultString(book.getName());
            if (UtopiaAfentiConstants.AFENTI_BOOK_BLACK_LIST.contains(book.getId())) {
                bookName = StringUtils.replace(bookName, "外研版-新标准", "阿分题教材");
                bookName = StringUtils.replace(bookName, "新概念英语1(培训用)", "阿分题教材");
            }
            book.setName(bookName);
        }

        context.getBooks().addAll(books);
    }
}

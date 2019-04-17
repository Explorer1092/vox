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

package com.voxlearning.utopia.service.business.impl.service.clazz;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.Sets;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.utopia.mapper.DisplayBookOfClazzMapper;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.content.api.entity.Book;
import com.voxlearning.utopia.service.content.api.entity.ClazzBookRef;
import com.voxlearning.utopia.service.content.api.entity.MathBook;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For finding clazz with same books with specified clazz.
 *
 * @author Xiaohai Zhang
 * @since 2013-05-16 15:35
 */
@Named
@Slf4j
@NoArgsConstructor
public class SameBookClazzFinder extends BusinessServiceSpringBean {

    public List<DisplayBookOfClazzMapper> find(Long teacherId, Long clazzId, Subject subject) {
        Validate.notNull(teacherId);
        if (teacherId == null || clazzId == null) {
            return Collections.emptyList();
        }
        List<Clazz> clazzList = new ArrayList<>(deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherId));

        // get teacher groups
        List<Long> clazzIds = clazzList.stream().map(Clazz::getId).collect(Collectors.toList());
        Map<Long, GroupMapper> groups = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzIds(teacherId, clazzIds, false);

        // locate specified clazz from teacher's all public clazz list
        Clazz specifiedClazz = null;
        for (Iterator<Clazz> it = clazzList.iterator(); it.hasNext(); ) {
            Clazz clazz = it.next();
            if (clazz.getId().equals(clazzId)) {
                specifiedClazz = clazz;
                it.remove();
            }
        }
        if (specifiedClazz == null) {
            return Collections.emptyList();
        }

        List<DisplayBookOfClazzMapper> mappers = new ArrayList<>();

        Long groupId = groups.containsKey(specifiedClazz.getId()) ? groups.get(specifiedClazz.getId()).getId() : null;
        DisplayBookOfClazzMapperWrapper target = generate(specifiedClazz, subject, groupId);
        if (target.getBookIds().isEmpty()) {
            return Collections.emptyList();
        }
        //mappers.add(target.getMapper());

        for (Clazz clazz : clazzList) {
            groupId = groups.containsKey(clazz.getId()) ? groups.get(clazz.getId()).getId() : null;
            DisplayBookOfClazzMapperWrapper wrapper = generate(clazz, subject, groupId);
            if (Sets.__difference_PleaseMakeSureYouKnowWhatAreYouDoing(target.getBookIds(), wrapper.getBookIds()).isEmpty()) {
                mappers.add(wrapper.getMapper());
            }
        }

        return mappers;
    }

    private DisplayBookOfClazzMapperWrapper generate(Clazz clazz, Subject subject, Long groupId) {
        List<ClazzBookRef> refs = clazzBookLoaderClient.loadGroupBookRefs(groupId)
                .toList()
                .stream()
                .sorted((o1, o2) -> Long.compare(o2.fetchUpdateTimestamp(), o1.fetchUpdateTimestamp()))
                .collect(Collectors.toList());
        DisplayBookOfClazzMapperWrapper wrapper = new DisplayBookOfClazzMapperWrapper();
        for (ClazzBookRef ref : refs) {
            Long bookId = ref.getBookId();
            if (subject == Subject.ENGLISH) {
                Book book = englishContentLoaderClient.loadEnglishBook(bookId);
                if (book == null) {
                    continue;
                }
            } else if (subject == Subject.MATH) {
                MathBook mathBook = mathContentLoaderClient.loadMathBook(bookId);
                if (mathBook == null) {
                    continue;
                }
            } else {
                continue;
            }
            wrapper.getBookIds().add(bookId);
        }

        DisplayBookOfClazzMapper mapper = new DisplayBookOfClazzMapper();
        mapper.setClazzId(clazz.getId());
        try {
            mapper.setClazzLevel(Integer.parseInt(clazz.getClassLevel()));
        } catch (Exception ex) {
            // ignore
        }
        mapper.setClazzName(clazz.formalizeClazzName());
        mapper.setCountBooks(Integer.toString(wrapper.getBookIds().size()));
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        mapper.setBookInfo(StringUtils.join(wrapper.getBookIds().toArray(new Long[0]), ","));
        wrapper.setMapper(mapper);

        return wrapper;
    }

    @Data
    private static class DisplayBookOfClazzMapperWrapper implements Serializable {
        private static final long serialVersionUID = -8928430664443100093L;
        private DisplayBookOfClazzMapper mapper;
        private SortedSet<Long> bookIds = new TreeSet<Long>();
    }
}

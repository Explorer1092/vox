/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.dao;

import com.mongodb.ReadPreference;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.business.api.entity.TtsListeningPaper;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author Junjie Zhang
 * @since 2014-08-12
 */
@Named
public class TtsListeningPaperDao extends StaticMongoDao<TtsListeningPaper, String> {

    @Override
    protected void calculateCacheDimensions(TtsListeningPaper source, Collection<String> dimensions) {
    }

    @Override
    protected void preprocessEntity(TtsListeningPaper entity) {
        super.preprocessEntity(entity);
        if (entity.getShare() == null) entity.setShare(0);
        if (entity.getBookId() == null) entity.setBookId(0L);
        if (entity.getFormat() == null) entity.setFormat(0);
    }

    public void save(TtsListeningPaper entity) {
        if (entity == null) {
            return;
        }
        ensureTimestampTouched(entity);
        preprocessEntity(entity);
        ensureIdNotNull(entity);
        Bson filter = filterFromId(entity.getId());
        BsonDocument replacement = transform(entity);
        FindOneAndReplaceOptions options = new FindOneAndReplaceOptions().upsert(true);
        createMongoConnection().collection.findOneAndReplace(filter, replacement, options);
    }

    public Page<TtsListeningPaper> getListeningPaperPageByUserId(Long userId, Pageable pageable, String title) {
        return getListenPaperPageByUidAndBid(userId, null, pageable, title);
    }

    public Page<TtsListeningPaper> getListenPaperPageByUidAndBid(Long userId, Long bookId,
                                                                 Pageable pageable, String title) {
        Filter filter = filterBuilder.where("author").is(userId);
        if (bookId != null) {
            filter = filter.and("bookId").is(bookId);
        }
        if (StringUtils.isNotBlank(title)) {
            title = title.trim();
            filter = filter.and("title").regex(title, "i");  //不区分大小写
        }
        Sort sort = new Sort(Sort.Direction.DESC, "updateDatetime");
        pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return __pageFind_OTF(filter.toBsonDocument(), pageable, ReadPreference.primary());
    }

    public Page<TtsListeningPaper> getSharedListeningPaperPage(Pageable pageable, Long bookId, Integer classLevel, Ktwelve ktwelve) {
        Filter filter = filterBuilder.where("share").is(1);
        if (bookId != null && bookId > 0) {
            filter = filter.and("bookId").is(bookId);
        }
        if (classLevel != null && classLevel > 0) {
            filter = filter.and("classLevel").is(classLevel);
        }
        else if (ktwelve == Ktwelve.JUNIOR_SCHOOL || ktwelve == Ktwelve.SENIOR_SCHOOL){
            filter = filter.and("classLevel").gte(7);
        }
        else {
            filter = filter.and("classLevel").lte(6);
        }

        Sort sort = new Sort(Sort.Direction.DESC, "updateDatetime");
        pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return __pageFind_OTF(filter.toBsonDocument(), pageable, ReadPreference.primary());
    }

    public long countSharedListeningPaper(Long userId) {
        Filter filter = filterBuilder.where("share").is(1);
        if (userId != null && userId > 0) {
            filter = filter.and("author").is(userId);
        }
        Find find = Find.find(filter);
        return __count_OTF(find, ReadPreference.primary());
    }
}

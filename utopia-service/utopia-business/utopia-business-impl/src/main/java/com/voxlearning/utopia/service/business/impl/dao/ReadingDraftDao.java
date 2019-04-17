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

package com.voxlearning.utopia.service.business.impl.dao;

import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.business.api.entity.ReadingDraft;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by tanguohong on 14-7-2.
 */
@Named
public class ReadingDraftDao extends StaticMongoDao<ReadingDraft, String> {
    @Override
    protected void calculateCacheDimensions(ReadingDraft source, Collection<String> dimensions) {
    }

    public void save(ReadingDraft draft) {
        ensureTimestampTouched(draft);
        preprocessEntity(draft);
        ensureIdNotNull(draft);

        Bson filter = filterFromId(draft.getId());
        BsonDocument replacement = transform(draft);
        FindOneAndReplaceOptions options = new FindOneAndReplaceOptions().upsert(true);
        createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndReplace(filter, replacement, options);
    }

    public List<ReadingDraft> getReadingDraftByUserId(Long userId, String... fields) {
        Filter filter = filterBuilder.where("ugcAuthor").is(userId);
        Find find = Find.find(filter);
        if (ArrayUtils.isNotEmpty(fields)) {
            find.field().includes(fields);
        }
        find.with(new Sort(Sort.Direction.DESC, "updateDatetime"));
        return __find_OTF(find, ReadPreference.primary());
    }

    public void verifyDraft(String draftid, String status) {
        ReadingDraft inst = new ReadingDraft();
        inst.setStatus(status);
        update(draftid, inst);
    }

    /**
     * @deprecated 只有上帝才能明白你要查什么
     */
    @Deprecated
    public MapMessage __ogu_findPageReading(List<Long> points, String style, Integer difficultyLevel, String status, Long ugcAuthor, String ename, Integer page, Integer pageSize) {
        Filter filter = filterBuilder.build();
        if (points != null && points.size() > 0) {
            filter.and("points").in(points);
        }
        if (StringUtils.isNotBlank(style)) {
            filter.and("style").is(style);
        }
        if (difficultyLevel != null) {
            filter.and("difficultyLevel").is(difficultyLevel);
        }
        if (StringUtils.isNotBlank(status)) {
            filter.and("status").is(status);
        } else {
            filter.and("status").ne("draft");
        }
        if (ugcAuthor != null) {
            filter.and("ugcAuthor").is(ugcAuthor);
        }
        if (StringUtils.isNotBlank(ename)) {
            filter.and("ename").regex(ename, "i");//options="i" 不区分大小写
        }

        Find find = Find.find(filter);
        find.with(new Sort(Sort.Direction.DESC, "updateDatetime"));
        int totalRows = (int) __count_OTF(find, ReadPreference.primary());
        if (totalRows > 0) {
            // 计算总页数
            int totalPages = (int) Math.ceil(totalRows * 1.0 / pageSize);
            // 分页
            int skip = (page - 1) * pageSize;
            if (skip < 0) skip = 0;
            find.skip(skip).limit(pageSize);
            find.field().excludes("content");
            // 查询结果集
            List<ReadingDraft> list = __find_OTF(find, ReadPreference.primary());

            MapMessage message = new MapMessage();
            message.set("totalRows", totalRows);
            message.set("totalPages", totalPages);
            message.set("_List_", list);
            return message;

        } else {
            MapMessage message = new MapMessage();
            message.set("totalRows", 0);
            message.set("totalPages", 0);
            message.set("_List_", new ArrayList<>());
            return message;
        }
    }
}

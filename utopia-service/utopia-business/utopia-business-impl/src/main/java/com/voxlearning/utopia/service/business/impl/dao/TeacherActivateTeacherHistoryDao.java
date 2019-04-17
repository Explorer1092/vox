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
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.CacheObjectLoader;
import com.voxlearning.utopia.api.constant.ActivationType;
import com.voxlearning.utopia.service.business.api.entity.TeacherActivateTeacherHistory;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mongo dao implementation of {@link TeacherActivateTeacherHistory}.
 *
 * @author RuiBao
 * @author Xiaohai Zhang
 * @version 0.1
 * @since 13-11-27
 */
@Named
@UtopiaCacheSupport(TeacherActivateTeacherHistory.class)
public class TeacherActivateTeacherHistoryDao extends StaticMongoDao<TeacherActivateTeacherHistory, String> {

    @Override
    protected void calculateCacheDimensions(TeacherActivateTeacherHistory source, Collection<String> dimensions) {
        dimensions.add(TeacherActivateTeacherHistory.ck_id(source.getId()));
        dimensions.add(TeacherActivateTeacherHistory.ck_inviterId(source.getInviterId()));
        dimensions.add(TeacherActivateTeacherHistory.ck_inviteeId(source.getInviteeId()));
    }

    @Override
    protected void preprocessEntity(TeacherActivateTeacherHistory entity) {
        super.preprocessEntity(entity);
        if (entity.getSuccess() == null) entity.setSuccess(Boolean.FALSE);
        if (entity.getOver() == null) entity.setOver(Boolean.FALSE);
    }

    public Map<Long, List<TeacherActivateTeacherHistory>> findByInviterIds(Collection<Long> inviterIds) {
        CacheObjectLoader.Loader<Long, List<TeacherActivateTeacherHistory>> loader = getCache()
                .getCacheObjectLoader()
                .createLoader(TeacherActivateTeacherHistory::ck_inviterId);
        return loader.loads(CollectionUtils.toLinkedHashSet(inviterIds))
                .loadsMissed(this::__findByInviterIds)
                .writeAsList(entityCacheExpirationInSeconds())
                .getResult();
    }

    public Map<Long, List<TeacherActivateTeacherHistory>> findByInviteeIds(Collection<Long> inviteeIds) {
        CacheObjectLoader.Loader<Long, List<TeacherActivateTeacherHistory>> loader = getCache()
                .getCacheObjectLoader()
                .createLoader(TeacherActivateTeacherHistory::ck_inviteeId);
        return loader.loads(CollectionUtils.toLinkedHashSet(inviteeIds))
                .loadsMissed(this::__findByInviteeIds)
                .writeAsList(entityCacheExpirationInSeconds())
                .getResult();
    }

    public void updateActivationType(String id, ActivationType type) {
        TeacherActivateTeacherHistory inst = new TeacherActivateTeacherHistory();
        inst.setActivationType(type);
        update(id, inst);
    }

    public void updateOver(String id) {
        TeacherActivateTeacherHistory inst = new TeacherActivateTeacherHistory();
        inst.setOver(true);
        update(id, inst);
    }

    public void updateExtensionAttributes(String id, Map<String, Object> attr) {
        TeacherActivateTeacherHistory inst = new TeacherActivateTeacherHistory();
        inst.setExtensionAttributes(attr);
        update(id, inst);
    }

    // ========================================================================
    // Private methods
    // ========================================================================

    // 加上过期逻辑
    private Map<Long, List<TeacherActivateTeacherHistory>> __findByInviterIds(Collection<Long> inviterIds) {
        Filter filter = filterBuilder.where("inviterId").in(inviterIds);
        Find find = Find.find(filter);

        List<TeacherActivateTeacherHistory> histories = __find_OTF(find, ReadPreference.primary());

        // 处理过期数据
        histories = filterExpiredHistories(histories);
        return histories.stream().collect(Collectors.groupingBy(TeacherActivateTeacherHistory::getInviterId));
    }

    private Map<Long, List<TeacherActivateTeacherHistory>> __findByInviteeIds(Collection<Long> inviteeIds) {
        Filter filter = filterBuilder.where("inviteeId").in(inviteeIds);
        Find find = Find.find(filter);

        List<TeacherActivateTeacherHistory> histories = __find_OTF(find, ReadPreference.primary());
        histories = filterExpiredHistories(histories);
        return histories.stream().collect(Collectors.groupingBy(TeacherActivateTeacherHistory::getInviteeId));
    }

    private List<TeacherActivateTeacherHistory> filterExpiredHistories(List<TeacherActivateTeacherHistory> dataList) {
        if (CollectionUtils.isEmpty(dataList)) {
            return dataList;
        }

        Date fromDate = DateUtils.calculateDateDay(new Date(), -180);

        List<TeacherActivateTeacherHistory> retList = new ArrayList<>();

        for (TeacherActivateTeacherHistory item : dataList) {
            if (item.getCreateTime().before(fromDate)) {
                if (!SafeConverter.toBoolean(item.getOver())) {
                    updateOver(item.getId());
                }
            } else {
                retList.add(item);
            }
        }

        return retList;
    }
}

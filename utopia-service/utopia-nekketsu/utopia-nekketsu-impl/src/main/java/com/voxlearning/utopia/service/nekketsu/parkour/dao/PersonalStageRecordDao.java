/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.nekketsu.parkour.dao;

import com.mongodb.ReadPreference;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.alps.lang.util.Sets;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.PersonalStageRecord;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.WordPuzzle;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Sadi.Wan on 2014/8/19.
 */
@Named
@UtopiaCacheSupport(PersonalStageRecord.class)
public class PersonalStageRecordDao extends StaticMongoDao<PersonalStageRecord, String> {

    @Override
    protected void calculateCacheDimensions(PersonalStageRecord source, Collection<String> dimensions) {
        dimensions.add(PersonalStageRecord.ck_id(source.getId()));
    }

    @Override
    protected void preprocessEntity(PersonalStageRecord entity) {
        super.preprocessEntity(entity);
        if (entity.getPersonalBest() == null) entity.setPersonalBest(0);
        if (entity.getStarBest() == null) entity.setStarBest(0);
        if (entity.getCorrectRate() == null) entity.setCorrectRate(0D);
        if (entity.getTimePerQuestion() == null) entity.setTimePerQuestion(0);
    }

    public Map<Integer, PersonalStageRecord> getsUserMultiStageRecord(long userId, Collection<Integer> stageIdList) {
        stageIdList = Sets.iterableToSetExcludeNull(stageIdList);
        if (stageIdList.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<String> ids = stageIdList.stream()
                .map(t -> userId + "_" + t)
                .collect(Collectors.toSet());
        List<PersonalStageRecord> getsList = loads(ids).values()
                .stream()
                .collect(Collectors.toList());
        Map<Integer, PersonalStageRecord> rtn = new HashMap<>();
        for (PersonalStageRecord psr : getsList) {
            rtn.put(psr.getStageId(), psr);
        }
        return rtn;
    }

    public Map<Long, PersonalStageRecord> getMultiUserStageRecord(Collection<Long> useridCollect, int stageId) {
        useridCollect = Sets.iterableToSetExcludeNull(useridCollect);
        if (useridCollect.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<String> ids = useridCollect.stream()
                .map(t -> t + "_" + stageId)
                .collect(Collectors.toSet());

        List<PersonalStageRecord> getsSet = loads(ids).values()
                .stream()
                .collect(Collectors.toList());
        Map<Long, PersonalStageRecord> rtn = new HashMap<>();
        for (PersonalStageRecord psr : getsSet) {
            rtn.put(psr.getRoleId(), psr);
        }
        return rtn;
    }

    public static final int randomPickCount = 10;
    public static final String NEKKETSU_PARKOUR_RECENT_PLAY_USER_STAGE_ = "_new_NEKKETSU_PARKOUR_RECENT_PLAY_USER_STAGE_";

    public Set<Long> getRecentPlayedStageRoleId(int stageId) {
        String rtnCacheKey = NEKKETSU_PARKOUR_RECENT_PLAY_USER_STAGE_ + stageId;
        List<Long> rtn = getCache().load(rtnCacheKey);
        if (null != rtn && rtn.size() > 0) {//chache里有数据，返回
            return new HashSet<>(rtn);
        }
        //从db取
        Filter filter = filterBuilder.where("stageId").is(stageId);
        Find find = Find.find(filter).limit(randomPickCount)
                .with(new Sort(Sort.Direction.DESC, "latestPlayTime"));
        List<PersonalStageRecord> fromDb = __find_OTF(find, ReadPreference.primary());

        rtn = fromDb.stream()
                .filter(t -> t.getRoleId() != null)
                .limit(randomPickCount)
                .map(PersonalStageRecord::getRoleId)
                .collect(Collectors.toList());
        getCache().add(rtnCacheKey, entityCacheExpirationInSeconds(), rtn);
        return new HashSet<>(rtn);
    }

    public PersonalStageRecord findOne(long userId, int stageId) {
        String id = userId + "_" + stageId;
        return load(id);
    }

    public PersonalStageRecord updateFields(String id, Map<String, Object> fieldMap, WordPuzzle puzzle) {
        Update update = updateBuilder.build();
        for (Map.Entry<String, Object> entry : fieldMap.entrySet()) {
            update.set(entry.getKey(), entry.getValue());
        }
        update.addToSet("achievedPuzzle", puzzle);
        return update(id, update);
    }

    public void appendRecentPlay(int stageId, long userId) {
        String rtnCacheKey = NEKKETSU_PARKOUR_RECENT_PLAY_USER_STAGE_ + stageId;
        List<Long> list = getCache().load(rtnCacheKey);
        if (null == list) {
            list = new ArrayList<>();
        }
        if (list.contains(userId)) {
            list.remove(userId);
            list.add(userId);
        } else {
            list = list.stream().limit(randomPickCount).collect(Collectors.toList());
            list.add(userId);
        }
        getCache().set(rtnCacheKey, entityCacheExpirationInSeconds(), list);
    }
}

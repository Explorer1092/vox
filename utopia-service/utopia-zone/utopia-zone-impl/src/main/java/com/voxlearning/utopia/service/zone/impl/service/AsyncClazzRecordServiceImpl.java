package com.voxlearning.utopia.service.zone.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.ValueWrapper;
import com.voxlearning.alps.spi.common.JsonStringSerializer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.zone.api.AsyncClazzRecordService;
import com.voxlearning.utopia.service.zone.api.constant.ClazzRecordCardStatusEnum;
import com.voxlearning.utopia.service.zone.api.constant.ClazzRecordTypeEnum;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.ClazzRecordCardMapper;
import com.voxlearning.utopia.service.zone.cache.ZoneCache;
import com.voxlearning.utopia.service.zone.impl.support.InternalClazzRecordService;
import com.voxlearning.utopia.service.zone.support.ClazzRecordCacheKeyGenerator;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Async clazz record service impl
 * Created ex on 2017/3/1.
 */

@Named
@ExposeServices({
        @ExposeService(interfaceClass = AsyncClazzRecordService.class, version = @ServiceVersion(version = "20170309")),
        @ExposeService(interfaceClass = AsyncClazzRecordService.class, version = @ServiceVersion(version = "20170428"))
})
public class AsyncClazzRecordServiceImpl extends SpringContainerSupport implements AsyncClazzRecordService {

    @Inject private InternalClazzRecordService internalClazzRecordService;

    @Override
    public AlpsFuture<ClazzRecordCardMapper> loadStudyMasterCard(Long userId, NewHomework.Location homework) {
        // parameter check
        if (homework == null) {
            return new ValueWrapperFuture<>(null);
        }

        // load from cache first, cached by homework
        String ck = ClazzRecordCacheKeyGenerator.ck_sm_card(homework.getId());
        ClazzRecordCardMapper cachedObj = ZoneCache.getCache().load(ck);
        if (cachedObj != null) {
            refreshHwPart(cachedObj, String.valueOf(userId));
            return new ValueWrapperFuture<>(cachedObj);
        }

        ClazzRecordCardMapper mapper = internalClazzRecordService.internalLoadStudyMasterCard(userId, homework);
        if (mapper != null) {
            ZoneCache.getCache().add(ck, 1800, mapper);
        }

        return new ValueWrapperFuture<>(mapper);
    }

    @Override
    public AlpsFuture<ClazzRecordCardMapper> loadFocusCard(Long userId, NewHomework.Location homework) {
        // parameter check
        if (homework == null) {
            return new ValueWrapperFuture<>(null);
        }

        // load from cache first, cached by homework
        String ck = ClazzRecordCacheKeyGenerator.ck_focus_card(homework.getId());
        ClazzRecordCardMapper cachedObj = ZoneCache.getCache().load(ck);
        if (cachedObj != null) {
            refreshHwPart(cachedObj, String.valueOf(userId));
            return new ValueWrapperFuture<>(cachedObj);
        }

        ClazzRecordCardMapper mapper = internalClazzRecordService.internalLoadFocusCard(userId, homework);
        if (mapper != null) {
            ZoneCache.getCache().add(ck, 1800, mapper);
        }

        return new ValueWrapperFuture<>(mapper);
    }

    @Override
    public AlpsFuture<ClazzRecordCardMapper> loadFashionCard(Long clazzId, List<Long> classmates) {
        if (clazzId == null || clazzId <= 0 || CollectionUtils.isEmpty(classmates)) {
            return new ValueWrapperFuture<>(null);
        }

        // sort the classmates and use the hash value as part of the sharp key
        List<Long> sorted = classmates.stream().sorted((o1, o2) -> Long.compare(o2, o1)).collect(Collectors.toList());
        String fashionKey = StringUtils.join(clazzId, "_", JsonUtils.toJson(sorted).hashCode());
        String ck = ClazzRecordCacheKeyGenerator.ck_fashion_card(fashionKey);
        ClazzRecordCardMapper cachedObj = ZoneCache.getCache().load(ck);
        if (cachedObj != null) {
            refreshNonHwPart(cachedObj, clazzId, classmates);
            return new ValueWrapperFuture<>(cachedObj);
        }

        ClazzRecordCardMapper mapper = internalClazzRecordService.internalLoadFashionCard(clazzId, classmates);
        if (mapper != null) {
            ZoneCache.getCache().add(ck, 1800, mapper);
        }

        return new ValueWrapperFuture<>(mapper);
    }

    @Override
    public AlpsFuture<List<ClazzRecordCardMapper>> loadTop3FashionList(Long clazzId, List<Long> classmates) {
        if (clazzId == null || clazzId <= 0 || CollectionUtils.isEmpty(classmates)) {
            return new ValueWrapperFuture<>(Collections.emptyList());
        }

        // sort the classmates and use the hash value as part of the fashion key
        List<Long> sorted = classmates.stream().sorted((o1, o2) -> Long.compare(o2, o1)).collect(Collectors.toList());
        String fashionKey = StringUtils.join(clazzId, "_", JsonStringSerializer.getInstance().serialize(sorted).hashCode());
        String cacheKey = ClazzRecordCacheKeyGenerator.ck_fashion_top3(fashionKey);

        List<ClazzRecordCardMapper> result = ZoneCache.getCache().load(cacheKey);
        if (result == null) {
            result = internalClazzRecordService.internalLoadTop3FashionMapper(clazzId, classmates);
            ZoneCache.getCache().add(cacheKey, 1800, result);
        }
        return new ValueWrapperFuture<>(result);
    }
    @Override
    public AlpsFuture<ClazzRecordCardMapper> loadWeekTopFashionCard(Long clazzId, List<Long> classmates) {
        if (clazzId == null || clazzId <= 0 || CollectionUtils.isEmpty(classmates)) {
            return new ValueWrapperFuture<>(null);
        }

        // sort the classmates and use the hash value as part of the fashion key
        List<Long> sorted = classmates.stream().sorted((o1, o2) -> Long.compare(o2, o1)).collect(Collectors.toList());
        String fashionKey = StringUtils.join(clazzId, "_", JsonStringSerializer.getInstance().serialize(sorted).hashCode());
        String cacheKey = ClazzRecordCacheKeyGenerator.ck_fashion_week(fashionKey);
        CacheObject<ValueWrapper.SerializableValueWrapper> cacheObject = ZoneCache.getCache().get(cacheKey);
        if (cacheObject != null && cacheObject.getValue() != null) {
            return new ValueWrapperFuture<>((ClazzRecordCardMapper) cacheObject.getValue().get());
        }
        ClazzRecordCardMapper mapper = internalClazzRecordService.internalLoadWeekTopFashionMapper(clazzId, classmates);
        ZoneCache.getCache().add(cacheKey, 1800, new ValueWrapper.SerializableValueWrapper(mapper));
        return new ValueWrapperFuture<>(mapper);
    }

    @Override
    public AlpsFuture<ClazzRecordCardMapper> loadFullMarksCard(Collection<Long> groupIds, Long clazzId, List<Long> classmates) {
        if (CollectionUtils.isEmpty(groupIds) || clazzId == null || clazzId <= 0) {
            ClazzRecordCardMapper cardMapper = new ClazzRecordCardMapper();
            cardMapper.setRecordTypeEnum(ClazzRecordTypeEnum.FULLMARKS_STAR);
            cardMapper.setStatusEnum(ClazzRecordCardStatusEnum.UNLOCK);
            return new ValueWrapperFuture<>(cardMapper);
        }

        // sort the group ids and use the hash value as part of the fullmarks key
        List<Long> sorted = groupIds.stream().sorted((o1, o2) -> Long.compare(o2, o1)).collect(Collectors.toList());
        String fullMarksKey = StringUtils.join(clazzId, "_", JsonUtils.toJson(sorted).hashCode());
        String ck = ClazzRecordCacheKeyGenerator.ck_fullmarks_card(fullMarksKey);
        ClazzRecordCardMapper cachedObj = ZoneCache.getCache().load(ck);
        if (cachedObj != null) {
            refreshNonHwPart(cachedObj, clazzId, classmates);
            return new ValueWrapperFuture<>(cachedObj);
        }

        ClazzRecordCardMapper cardMapper = internalClazzRecordService.internalLoadFullMarksCard(groupIds, clazzId, classmates);
        if (cardMapper != null) {
            ZoneCache.getCache().set(ck, 1800, cardMapper);
        }
        return new ValueWrapperFuture<>(cardMapper);

    }

    @Override
    public AlpsFuture<ClazzRecordCardMapper> loadFriendshipCard(Long clazzId, List<Long> classmates) {
        if (CollectionUtils.isEmpty(classmates) || clazzId == null || clazzId <= 0) {
            ClazzRecordCardMapper cardMapper = new ClazzRecordCardMapper(ClazzRecordTypeEnum.FRIENDSHIP_STAR);
            return new ValueWrapperFuture<>(cardMapper);
        }

        // sort the classmates and use the hash value as part of the fullmarks key
        List<Long> sorted = classmates.stream().sorted((o1, o2) -> Long.compare(o2, o1)).collect(Collectors.toList());
        String friendShipKey = StringUtils.join(clazzId, "_", JsonUtils.toJson(sorted).hashCode());
        String ck = ClazzRecordCacheKeyGenerator.ck_friendship_card(friendShipKey);
        ClazzRecordCardMapper cachedObj = ZoneCache.getCache().load(ck);
        if (cachedObj != null) {
            refreshNonHwPart(cachedObj, clazzId, classmates);
            return new ValueWrapperFuture<>(cachedObj);
        }

        ClazzRecordCardMapper cardMapper = internalClazzRecordService.internalLoadFriendShipCard(clazzId, classmates);
        if (cardMapper != null) {
            ZoneCache.getCache().set(ck, 1800, cardMapper);
        }
        return new ValueWrapperFuture<>(cardMapper);
    }

    /**
     * 作业类卡片（学霸、专注）
     * 更新 完成状态 和 点赞数
     */
    private void refreshHwPart(ClazzRecordCardMapper mapper, String userId) {
        mapper.updateSelfFinished(userId);
        internalClazzRecordService.updateLikeCount(mapper, 0L, Collections.emptyList());

    }

    /**
     * 非作业卡片
     * 更新 点赞数
     */
    private void refreshNonHwPart(ClazzRecordCardMapper mapper, Long clazzId, List<Long> classmates) {
        internalClazzRecordService.updateLikeCount(mapper, clazzId, classmates);
    }

}

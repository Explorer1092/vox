package com.voxlearning.utopia.service.zone.impl.manager;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.zone.api.constant.ClazzRecordTypeEnum;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.RecordLikeMapper;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/3/7
 * Time: 19:28
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class ClazzRecordNonHwLikeCacheManager extends PojoCacheObject<ClazzRecordNonHwLikeCacheManager.GenerateClazzRecordNonHwLikedKey, List<RecordLikeMapper>> {

    public ClazzRecordNonHwLikeCacheManager(UtopiaCache cache) {
        super(cache);
    }


    public void clickLiked(RecordLikeMapper recordLikeMapper) {
        if (recordLikeMapper == null) {
            return;
        }
        ClazzRecordTypeEnum typeEnum = ClazzRecordTypeEnum.safeParse(recordLikeMapper.getRecordTypeEnumName());
        if (typeEnum == null) return;
        String cacheKey = cacheKey(new ClazzRecordNonHwLikeCacheManager.GenerateClazzRecordNonHwLikedKey(typeEnum, recordLikeMapper.getClazzId()));

        CacheObject<List<RecordLikeMapper>> object = getCache().get(cacheKey);
        if (object != null && object.getValue() != null) {
            getCache().cas(cacheKey, expirationInSeconds(), object, currentValue -> {
                currentValue = new ArrayList<RecordLikeMapper>(currentValue);
                currentValue.add(0, recordLikeMapper);
                return currentValue;
            });
        } else {
            getCache().add(cacheKey, expirationInSeconds(), Collections.singletonList(recordLikeMapper));
        }
        /**
         ChangeCacheObject<List<RecordLikeMapper>> modifier = likeMappers -> {
         if (likeMappers == null) likeMappers = new LinkedList<>();
         RecordLikeMapper exist = likeMappers.stream()
         .filter(p -> Objects.equals(p.getRecordTypeEnumName(), recordLikeMapper.getRecordTypeEnumName()))
         .filter(p -> Objects.equals(p.getClazzId(), recordLikeMapper.getClazzId()))
         .filter(p -> Objects.equals(p.getUserId(), recordLikeMapper.getUserId()))
         .findFirst().orElse(null);
         if (exist == null) {
         likeMappers.add(0, recordLikeMapper);
         }
         return likeMappers;
         };

         CacheValueModifierExecutor<List<RecordLikeMapper>> executor = getCache().createCacheValueModifier();
         executor.key(cacheKey)
         .expiration(expirationInSeconds())
         .modifier(modifier)
         .execute();
         */
    }

    public List<RecordLikeMapper> loadTodayClickLikedList(RecordLikeMapper recordLikeMapper) {
        if (recordLikeMapper == null) {
            return new ArrayList<>();
        }
        ClazzRecordTypeEnum typeEnum = ClazzRecordTypeEnum.safeParse(recordLikeMapper.getRecordTypeEnumName());

        if (typeEnum == null) {
            return new ArrayList<>();
        }
        return load(new ClazzRecordNonHwLikeCacheManager.GenerateClazzRecordNonHwLikedKey(typeEnum, recordLikeMapper.getClazzId()));

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"recordType", "clazzId"})
    class GenerateClazzRecordNonHwLikedKey {
        private ClazzRecordTypeEnum recordType;
        private Long clazzId;

        @Override
        public String toString() {
            return "RT=" + recordType + ",CID=" + clazzId;
        }
    }

}

package com.voxlearning.utopia.service.zone.impl.manager;

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
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/3/7
 * Time: 19:28
 */
@UtopiaCacheExpiration(86400 * 7)
public class ClazzRecordHwLikeCacheManager extends PojoCacheObject<ClazzRecordHwLikeCacheManager.GenerateClazzRecordHwLikedKey, List<RecordLikeMapper>> {

    public ClazzRecordHwLikeCacheManager(UtopiaCache cache) {
        super(cache);
    }


    public void clickLiked(RecordLikeMapper recordLikeMapper) {
        if (recordLikeMapper == null) {
            return;
        }
        ClazzRecordTypeEnum typeEnum = ClazzRecordTypeEnum.safeParse(recordLikeMapper.getRecordTypeEnumName());
        if (typeEnum == null) return;
        String cacheKey = cacheKey(new ClazzRecordHwLikeCacheManager.GenerateClazzRecordHwLikedKey(typeEnum, recordLikeMapper.getHomeworkId()));


        CacheObject<List<RecordLikeMapper>> object = getCache().get(cacheKey);
        if (object != null && object.getValue() != null) {
            //过滤超过7天的记录
            getCache().cas(cacheKey, expirationInSeconds(), object, currentValue -> {
                currentValue = new ArrayList<>(currentValue);
                List<RecordLikeMapper> temp = currentValue
                        .stream()
                        .filter(r -> (System.currentTimeMillis() - r.getCreateTime()) / 1000 < expirationInSeconds())
                        .collect(Collectors.toList());

                temp.add(0, recordLikeMapper);
                return temp;
            });
        } else {
            getCache().add(cacheKey, expirationInSeconds(), Collections.singletonList(recordLikeMapper));
        }
        /** 小海指导： 这个是修改操作，如果对象没有更改不做任何操作
         ChangeCacheObject<List<RecordLikeMapper>> modifier = likeMappers -> {
         if (likeMappers == null) likeMappers = new LinkedList<>();
         RecordLikeMapper exist = likeMappers.stream()
         .filter(p -> Objects.equals(p.getRecordTypeEnumName(), recordLikeMapper.getRecordTypeEnumName()))
         .filter(p -> Objects.equals(p.getHomeworkId(), recordLikeMapper.getHomeworkId()))
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


    public List<RecordLikeMapper> loadClickLikedList(RecordLikeMapper recordLikeMapper) {
        if (recordLikeMapper == null) {
            return new ArrayList<>();
        }
        ClazzRecordTypeEnum typeEnum = ClazzRecordTypeEnum.safeParse(recordLikeMapper.getRecordTypeEnumName());
        if (typeEnum == null) {
            return new ArrayList<>();
        }
        return load(new ClazzRecordHwLikeCacheManager.GenerateClazzRecordHwLikedKey(typeEnum, recordLikeMapper.getHomeworkId()));
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"recordType", "homeworkId"})
    class GenerateClazzRecordHwLikedKey {
        private ClazzRecordTypeEnum recordType;
        private String homeworkId;

        @Override
        public String toString() {
            return "RT=" + recordType.name() + ",HW=" + homeworkId;
        }
    }

}

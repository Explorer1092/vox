package com.voxlearning.utopia.service.zone.impl.manager;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.CacheValueModifierExecutor;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.RecordSoundShareMapper;
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
 * 语音分享
 */
@UtopiaCacheExpiration(86400 * 3)
public class ClazzRecordSoundShareManager extends PojoCacheObject<ClazzRecordSoundShareManager.GenerateClazzRecordKey, List<RecordSoundShareMapper>> {

    public ClazzRecordSoundShareManager(UtopiaCache cache) {
        super(cache);
    }


    public void share(RecordSoundShareMapper recordLikeMapper) {
        if (recordLikeMapper == null) {
            return;
        }

        String cacheKey = cacheKey(new ClazzRecordSoundShareManager.GenerateClazzRecordKey(recordLikeMapper.getClazzId()));

        CacheObject<List<RecordSoundShareMapper>> object = getCache().get(cacheKey);
        if (object != null && object.getValue() != null) {
            getCache().cas(cacheKey, expirationInSeconds(), object, currentValue -> {
                List<RecordSoundShareMapper> temp = currentValue
                        .stream()
                        .filter(r -> (System.currentTimeMillis() - r.getCreateTime()) / 1000 < expirationInSeconds())
                        .collect(Collectors.toList());

                temp.add(0, recordLikeMapper);
                return temp;
            });
        } else {
            getCache().add(cacheKey, expirationInSeconds(), Collections.singletonList(recordLikeMapper));
        }

        LogCollector.info("zone_clazzrecord_soundshare_history", MapUtils.map(
                "userId", SafeConverter.toString(recordLikeMapper.getUserId()),
                "recordType", recordLikeMapper.getRecordTypeEnumName(),
                "hwId", recordLikeMapper.getHwId(),
                "uri", recordLikeMapper.getUri(),
                "time", SafeConverter.toString(recordLikeMapper.getTime())
        ));
    }


    public List<RecordSoundShareMapper> loadSharedList(Long clazzId) {
        if (clazzId == null || clazzId <= 0) {
            return new ArrayList<>();
        }
        return load(new ClazzRecordSoundShareManager.GenerateClazzRecordKey(clazzId));

    }

    public void deleteRecord(Long clazzId, Long userId, String uri) {
        if (clazzId == null || userId == null) {
            return;
        }
        ChangeCacheObject<List<RecordSoundShareMapper>> modifier;
        if (StringUtils.isBlank(uri)) {
            modifier = currentValue -> {
                currentValue.removeIf(record -> userId.equals(record.getUserId()));
                return currentValue;
            };
        } else {
            modifier = currentValue -> {
                currentValue.removeIf(record -> userId.equals(record.getUserId()) && uri.equals(record.getUri()));
                return currentValue;
            };
        }
        String cacheKey = cacheKey(new ClazzRecordSoundShareManager.GenerateClazzRecordKey(clazzId));
        CacheValueModifierExecutor<List<RecordSoundShareMapper>> executor = getCache().createCacheValueModifier();
        executor.key(cacheKey)
                .expiration(expirationInSeconds())
                .modifier(modifier)
                .execute();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"clazzId"})
    class GenerateClazzRecordKey {
        private Long clazzId;

        @Override
        public String toString() {
            return "CLAZZID=" + clazzId;
        }
    }

}

package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;

import java.util.*;

/**
 * 缓存基类　用于原子增加值
 *
 * @author peng.zhang.a
 * @since 16-9-1
 */
abstract public class CacheBaseManager<ID, K, V> extends PojoCacheObject<ID, K> {
    protected CacheBaseManager(UtopiaCache cache) {
        super(cache);
    }

    public boolean casAddSet(String cacheKey, V p) {
        CacheObject<Set<V>> cacheObject = getCache().get(cacheKey);
        if (cacheObject == null) return false;

        if (cacheObject.getValue() == null
                && getCache().add(cacheKey, expirationInSeconds(), new HashSet<>(Collections.singletonList(p)))) {
            return true;
        }

        return getCache().cas(cacheKey, expirationInSeconds(), cacheObject, currentValue -> {
            currentValue = new HashSet<V>(currentValue);
            currentValue.add(p);
            return currentValue;
        });
    }

    public boolean casAddList(String cacheKey, V p) {
        CacheObject<List<V>> cacheObject = getCache().get(cacheKey);
        if (cacheObject == null) {
            return false;
        }

        if (cacheObject.getValue() == null
                && getCache().add(cacheKey, expirationInSeconds(), new ArrayList<>(Collections.singletonList(p)))) {
            return true;
        }
        return getCache().cas(cacheKey, expirationInSeconds(), cacheObject, currentValue -> {
            currentValue = new ArrayList<>(currentValue);
            currentValue.add(p);
            return currentValue;
        });
    }
}
package com.voxlearning.utopia.service.zone.impl.support;

import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chensn
 * @date 2018-10-23 15:33
 */
@UtopiaCachePrefix(prefix = "class_zone_halloween2018UsersCache")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_month)
public class Halloween2018UsersCache extends PojoCacheObject <String, Long> {

  public static final String KEY = "clothing_type_";

  protected Halloween2018UsersCache(UtopiaCache cache) {
    super(cache);
  }

  public void increase(Integer type) {
    String key = cacheKey(KEY + type);
    cache.incr(key, 1, 1, expirationInSeconds());
  }

  public Map <String, Long> loadLikedByTypeCounts() {
    Map <String, Long> sortMap = new LinkedHashMap <>();
    List <String> stringList = Lists.newArrayList();
    for (int i = 0; i < 8; i++) {
      stringList.add(cacheKey(KEY + i));
    }
    Map <String, Long> map = cache.loads(stringList);

    map.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue()
            .reversed()).forEachOrdered(e -> sortMap.put(e.getKey(), e.getValue()));
    return sortMap;
  }

  public void deleteKey(String key) {
    cache.delete(key);
  }
  public String loadByKey(String key) {
    return cache.load(key);
  }


}

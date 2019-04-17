package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.zone.api.entity.SignRecord;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chensn
 * @date 2018-10-23 15:33
 */
@UtopiaCachePrefix(prefix = "class_circle_modify_sign")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
public class ClassCircleModifySignUserCountCache extends PojoCacheObject <String, List<SignRecord>> {

  public static final String KEY = "count_";

  protected ClassCircleModifySignUserCountCache(UtopiaCache cache) {
    super(cache);
  }

  public void setKey(Long userId, List<SignRecord> list) {
    String key = KEY + userId;
    List<SignRecord> signRecords = load(key);
    if(CollectionUtils.isEmpty(signRecords)){
      signRecords = new ArrayList <>();
    }
    SignRecord signRecord = new SignRecord();
    signRecord.setFinished(false);
    signRecord.setSignType(list.get(0).getSignType());
    signRecords.add(signRecord);
    set(key, signRecords);
  }

  public List<SignRecord> loadByKey(String userId) {
    String key = KEY + userId;
    return load(key);
  }
  public void updateKey(Long userId, Integer type) {
    String key = KEY + userId;
    List<SignRecord> signRecords = load(key);
    for (SignRecord signRecord: signRecords) {
      if(signRecord.getSignType().equals(type)){
        signRecord.setFinished(true);
      }
    }
    set(key, signRecords);
  }
}

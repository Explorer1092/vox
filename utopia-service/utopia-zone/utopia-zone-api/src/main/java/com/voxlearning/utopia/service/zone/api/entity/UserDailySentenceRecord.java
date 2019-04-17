package com.voxlearning.utopia.service.zone.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @Author yulong.ma
 * @Date 2018/10/23 1508
 * @Version1.0
 **/
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_class_circle_user_sign")
@UtopiaCacheExpiration(policy=CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181024")
public class UserDailySentenceRecord implements Serializable {

  @DocumentId
  private String id;
  private Long userId;

  private Integer year;

  private List<Integer> records;

  public static String  generateId(Integer year,Long userId){
    return year+"_"+userId;
  }
  public static String ck_id(String id) {
    return CacheKeyGenerator.generateCacheKey(UserDailySentenceRecord.class, id);
  }
}

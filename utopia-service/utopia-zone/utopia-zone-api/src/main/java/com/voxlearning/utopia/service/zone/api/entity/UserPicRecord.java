package com.voxlearning.utopia.service.zone.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.random.RandomUtils;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @Author yulong.ma
 * @Date 2018/10/23 1454
 * @Version1.0
 **/
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_class_circle_user_pic_record_{}",dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
@UtopiaCacheRevision("20181231")
public class UserPicRecord implements Serializable {

  @DocumentId
  private String id;
  /**用户id**/
  private Long userId;
  /**年**/
  private Integer year;
  /**年中的第几周**/
  private Integer week;

  private List<Boolean> weekRecord;

  private String picPath;

  /**每日一句周Id**/
  private String weekDailySentenceId;
  @DocumentCreateTimestamp
  private Date createAt;
  @DocumentUpdateTimestamp
  private Date updateAt;

  public static String generateId(Long userId, Integer year, Integer week) {
    return userId + "_" + year + "_" + week;
  }
}

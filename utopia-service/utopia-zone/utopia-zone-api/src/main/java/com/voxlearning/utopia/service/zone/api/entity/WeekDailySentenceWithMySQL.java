package com.voxlearning.utopia.service.zone.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.random.RandomUtils;
import java.io.Serializable;
import java.util.Date;
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
@DocumentConnection(configName = "main")
@DocumentTable(table = "VOX_CLASS_CIRCLE_WEEK_SENTENCE")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181024")

public class WeekDailySentenceWithMySQL implements Serializable {

  @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
  @DocumentField("ID")
  private Long id;
  @DocumentField("WEEK_DAILY_SENTENCE")
  private String weekDailySentence;
  @DocumentField("CREATE_DATETIME")
  private Date createDateTime;
  @DocumentField("UPDATE_DATETIME")
  private Date updateDateTime;
  public static String ck_rankListToClient() {
    return CacheKeyGenerator.generateCacheKey(WeekDailySentenceWithMySQL.class, "WeekDailySentence");
  }
}

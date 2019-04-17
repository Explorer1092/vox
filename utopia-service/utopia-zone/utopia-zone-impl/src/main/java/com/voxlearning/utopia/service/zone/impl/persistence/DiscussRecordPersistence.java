package com.voxlearning.utopia.service.zone.impl.persistence;

import com.mongodb.MongoNamespace;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.connection.IMongoConnection;
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.zone.api.entity.DiscussZoneUserRecord;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author chensn
 * @date 2018-10-23 14:23
 */
@Repository
public class DiscussRecordPersistence extends DynamicMongoShardPersistence<DiscussZoneUserRecord, String> {
  private static final String ID_SEP = "_";
  @Override
  protected String calculateDatabase(String template, DiscussZoneUserRecord document) {
    return null;
  }

  @Override
  protected String calculateCollection(String template, DiscussZoneUserRecord document) {
    Objects.requireNonNull(document);
    Objects.requireNonNull(document.getId());
    String[] ids = document.getId().split("_");
    return StringUtils.formatMessage(template, SafeConverter.toInt(ids[0]));
  }

  @Override
  protected void calculateCacheDimensions(DiscussZoneUserRecord document, Collection<String> dimensions) {

  }

  protected List<IMongoConnection> calculateMongoConnection(Integer discussId) {
    String mockId = discussId + "_9999l_00000l";
    MongoNamespace namespace = calculateIdMongoNamespace(mockId);
    return createMongoConnection(namespace);
  }

  public List<DiscussZoneUserRecord> patternId(Integer discussId, Long clazzId){
    StringBuilder sbl = new StringBuilder();
    sbl.append("^").append(discussId).append(ID_SEP).append(clazzId).append(ID_SEP);
    Pattern pattern = Pattern.compile(sbl.toString());
    Criteria criteria = Criteria.where("_id").regex(pattern);
    List<DiscussZoneUserRecord> result = $executeQuery(calculateMongoConnection(discussId), Query.query(criteria)).getUninterruptibly();
    return result;
  }


}

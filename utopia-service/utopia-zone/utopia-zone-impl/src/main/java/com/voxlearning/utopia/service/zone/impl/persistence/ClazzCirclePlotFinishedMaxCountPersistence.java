package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.zone.api.entity.ClazzCirclePlotFinishedMaxCount;
import com.voxlearning.utopia.service.zone.api.entity.ClazzCirclePlotQuestion;
import java.util.Collection;
import org.springframework.stereotype.Repository;

/**
 * @author yulong.ma
 * @date 2018-11-09
 */
@Repository
public class ClazzCirclePlotFinishedMaxCountPersistence extends StaticMongoShardPersistence<ClazzCirclePlotFinishedMaxCount, Integer> {


  @Override
  protected void calculateCacheDimensions(ClazzCirclePlotFinishedMaxCount document,
      Collection <String> dimensions) {

  }
}

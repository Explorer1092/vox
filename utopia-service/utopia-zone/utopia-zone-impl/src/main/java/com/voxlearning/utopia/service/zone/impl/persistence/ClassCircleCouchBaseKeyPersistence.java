package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.zone.api.entity.ClassCircleCouchBaseKey;
import com.voxlearning.utopia.service.zone.api.entity.ClassCircleCouchBaseRecord;
import java.util.Collection;
import org.springframework.stereotype.Repository;

/**
 * @author chensn
 * @date 2018-10-30
 */
@Repository
public class ClassCircleCouchBaseKeyPersistence extends StaticMongoShardPersistence<ClassCircleCouchBaseKey, String> {

  @Override
  protected void calculateCacheDimensions(ClassCircleCouchBaseKey document,
      Collection <String> dimensions) {

  }
}

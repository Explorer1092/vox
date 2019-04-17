package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.zone.api.entity.ClassCircleCouchBaseRecord;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivity;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @author chensn
 * @date 2018-10-30
 */
@Repository
public class ClassCircleCouchBasePersistence extends StaticMongoShardPersistence<ClassCircleCouchBaseRecord, String> {

  @Override
  protected void calculateCacheDimensions(ClassCircleCouchBaseRecord document,
      Collection <String> dimensions) {

  }
}

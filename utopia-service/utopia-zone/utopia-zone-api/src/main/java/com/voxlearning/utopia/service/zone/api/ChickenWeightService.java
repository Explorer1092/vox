package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.zone.api.entity.giving.ClassCircleChickenWeight;
import java.util.concurrent.TimeUnit;

/**
 * @Author yulong.ma
 * @Date 2018/11/16 1512
 * @Version1.0
 **/
@ServiceVersion(version = "20181116")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ChickenWeightService {

  public void upsert(ClassCircleChickenWeight classCircleChickenWeight);

  public ClassCircleChickenWeight queryClassCircleChickenWeight(String id);


  public int  weightType();
}

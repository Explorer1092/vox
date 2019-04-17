package com.voxlearning.utopia.service.zone.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.zone.api.ChickenWeightService;
import com.voxlearning.utopia.service.zone.api.entity.giving.ClassCircleChickenWeight;
import com.voxlearning.utopia.service.zone.impl.persistence.giving.ChickenWeightPersistence;
import com.voxlearning.utopia.service.zone.impl.service.plot.WeightObj;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author yulong.ma
 * @Date 2018/11/16 1514
 * @Version1.0
 **/

@Named("com.voxlearning.utopia.service.zone.impl.service.ChickenWeightServiceImpl")
@ExposeService(interfaceClass = ChickenWeightService.class, version = @ServiceVersion(version = "20181105"))
@Slf4j
public class ChickenWeightServiceImpl implements ChickenWeightService {

  @Inject
  private ChickenWeightPersistence chickenWeightPersistence;
  @Override
  public void upsert(ClassCircleChickenWeight classCircleChickenWeight) {
    chickenWeightPersistence.upsert(classCircleChickenWeight);
  }

  @Override
  public ClassCircleChickenWeight queryClassCircleChickenWeight(String id) {
    return  chickenWeightPersistence.load(id);
  }


  @Override
  public int  weightType(){
    List<WeightObj> weightObjList = new ArrayList<>();
    ClassCircleChickenWeight classCircleChickenWeight = chickenWeightPersistence.load("1");
    Map<String,Integer> map =classCircleChickenWeight.getConfigs();
    for (Map.Entry<String, Integer> entry : map.entrySet()) {
      WeightObj weightObj = new WeightObj();
      if(entry.getKey().equals("4")){
        continue;
      }
      weightObj.setIndex(entry.getKey());
      weightObj.setWeight(entry.getValue());
      weightObjList.add(weightObj);
    }
    Integer weightSum = 0;
    for (WeightObj wc : weightObjList) {
      weightSum += wc.getWeight();
    }
    Integer n = RandomUtils.nextInt(weightSum);
    Integer m = 0;
    for (WeightObj wc : weightObjList) {
      if (m <= n && n < m + wc.getWeight()) {
        return Integer.valueOf(wc.getIndex()) ;
      }
      m += wc.getWeight();
    }
    return 1;
  }

}

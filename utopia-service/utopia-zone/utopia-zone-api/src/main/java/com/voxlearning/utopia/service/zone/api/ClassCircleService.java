package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.zone.api.entity.*;
import com.voxlearning.utopia.service.zone.api.entity.plot.DailySertenceModifySignConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181023")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ClassCircleService {


  public WeekDailySentence getDailySentence(Long userId);

  public List<WeekDailySentence> queryAllWeekDailySentence();


  public String saveWeekDailySenate(List<DailySentence> dailySentences, String token, String wholePic, Integer beginWeek);

  public MapMessage saveUserSignIn(Long userId);

  public List <DiscussZone> findUsedDiscuss();

  public void addOrUpdate(DiscussZone discussZone);

  public UserDailySentenceRecord getRecordByUserId(Long userId);

  public void saveOrUpdateDiscussRecord(Long userId, Long clazzId, Integer discussId);

  public List <DiscussZoneUserRecord> getDiscussRecord(Long clazzId, Integer discussId);

  public void updateWeekDailySenate(List<DailySentence> dailySentences, String id, Integer beginWeek, String wholePic);

  public void deleteDiscussCache();

  void modifySign(Long userId,Integer signType);

  void saveOrUpdateModifySignType(DailySertenceModifySignConfig dailySertenceModifySignConfig);

  List<DailySertenceModifySignConfig> queryDailySertenceModifySignConfigList(Long userId);

  //补签统计
  public List<SignRecord> loadModifySignUserCountCache(Long userId) ;
  //更新弹窗
  public void updateModifySign(Long userId, Integer type);

  public List<DiscussZone> getById(Integer id);
}

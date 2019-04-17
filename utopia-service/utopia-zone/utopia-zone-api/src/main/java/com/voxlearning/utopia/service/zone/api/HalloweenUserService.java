package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.zone.api.entity.DailySentence;
import com.voxlearning.utopia.service.zone.api.entity.DiscussZone;
import com.voxlearning.utopia.service.zone.api.entity.DiscussZoneUserRecord;
import com.voxlearning.utopia.service.zone.api.entity.UserDailySentenceRecord;
import com.voxlearning.utopia.service.zone.api.entity.WeekDailySentence;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181030")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface HalloweenUserService {

  /**年级维度**/
  public Object queryUsers(Long grade,Long userId);
}

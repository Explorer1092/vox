package com.voxlearning.utopia.service.afenti.api;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Ruib
 * @since 2017/2/10
 */
@ServiceVersion(version = "20170210")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface DPAfentiService {

    List<NewBookProfile> fetchGradeBookList(ClazzLevel clazzLevel, Subject subject);

    // 获取afenti 星星数
    Map<String, Object> loadUserAfentiStar(Long userId, Subject subject);
}

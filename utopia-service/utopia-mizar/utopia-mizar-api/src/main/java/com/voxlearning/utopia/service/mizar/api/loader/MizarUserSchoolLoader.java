package com.voxlearning.utopia.service.mizar.api.loader;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserSchool;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by yaguang.wang
 * on 2017/6/22.
 */
@ServiceVersion(version = "20170622")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MizarUserSchoolLoader extends IPingable {

    /**
     * 按照ID去查
     * @param schoolId
     * @return
     */
    MizarUserSchool loadBySchoolId(Long schoolId);

    List<MizarUserSchool> loadByUserId(String userId);

    /**
     * 查询所有非disable的数据
     * @return
     */
    List<MizarUserSchool> loadAll();
}

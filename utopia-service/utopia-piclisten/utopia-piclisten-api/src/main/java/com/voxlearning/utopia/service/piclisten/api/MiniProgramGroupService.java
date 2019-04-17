package com.voxlearning.utopia.service.piclisten.api;


import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;

import java.util.concurrent.TimeUnit;

@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
@ServiceVersion(version = "20180521")
public interface MiniProgramGroupService extends IPingable {


    MapMessage loadTotalGroupRank(Long uid,Long pid, String gid);

    MapMessage loadWeekGroupRank(Long uid, Long pid,String gid);
}

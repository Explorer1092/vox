package com.voxlearning.utopia.service.crm.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.entity.crm.schoolrecord.SchoolServiceRecord;

import java.util.concurrent.TimeUnit;

/**
 * 学校的
 * Created by yaguang.wang
 * on 2017/4/19.
 */
@ServiceVersion(version = "20171024")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface SchoolServiceRecordService extends IPingable {

    SchoolServiceRecord addSchoolServiceRecord(SchoolServiceRecord record);
}

package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/3/16
 */
@ServiceVersion(version = "1.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ParentBabyEagleChinaCultureService {


    Map<Long, List<String>> getShareRecordsByStudentIds(Collection<Long> studentIds);


    void insertShareRecordByStudentIds(Long studentId, String courseId);
}

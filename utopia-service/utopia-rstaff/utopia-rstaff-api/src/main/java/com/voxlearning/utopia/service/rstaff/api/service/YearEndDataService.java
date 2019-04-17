package com.voxlearning.utopia.service.rstaff.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "1.0.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface YearEndDataService {

    /**
     * 2018年年终盘点，老师数据接口
     * @param teacherId
     * @return
     */
    public Map<String,Object> loadTeacherYearData(Long teacherId);

    /**
     * 2018年年终盘点，学生数据接口
     * @param studentId
     * @return
     */
    public Map<String,Object> loadStudentYearData(Long studentId);

}

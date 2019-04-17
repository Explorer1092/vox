package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20180402")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface ReadingReportLoader extends IPingable {


    //班级能力分析
    @Idempotent
    MapMessage fetchPictureSemesterReport(Long gid);


    //班级能力分析：（大数据接口部分）
    //test
    @Idempotent
    MapMessage fetchPictureSemesterReportFromBigData(Long gid);

    //学生能力分析：
    @Idempotent
    MapMessage fetchAbilityAnalysis(Long gid);

    //学生能力分析：（大数据接口部分）
    //test
    @Idempotent
    MapMessage fetchAbilityAnalysisFromBigData(Long gid);


    //获取作业绘本报告
    @Idempotent
    MapMessage fetchPictureInfo(Teacher teacher, String hid);


    //获取推荐配音
    @Idempotent
    MapMessage fetchRecommend(String hid, ObjectiveConfigType type, String pictureId);


    //临时接口：用于测试获取学生信息
    //test
    @Idempotent
    MapMessage fetchUserInfo(Long gid);


    @Idempotent
    MapMessage fetchPictureWordCnt(List<String> hids);
}

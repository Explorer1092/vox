package com.voxlearning.utopia.service.newexam.api.service;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newexam.api.entity.AvengerNewExam;
import com.voxlearning.utopia.service.question.api.entity.NewExam;

import java.util.concurrent.TimeUnit;

/**
 * @Description: 考试数据上报
 * @author: Mr_VanGogh
 * @date: 2019/4/10 下午4:54
 */
@ServiceVersion(version = "20190410")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface AsyncAvengerNewExamService extends IPingable {

    @Async
    AlpsFuture<AvengerNewExam> informNewExamToBigData(NewExam newExam);

}

package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReading;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.mapper.avenger.AvengerHomework;

import java.util.concurrent.TimeUnit;

/**
 * 用于给大数据实时上报作业数据到kafka，希望有机会能独立出去
 *
 * @author xuesong.zhang
 * @since 2017/7/5
 */
@ServiceVersion(version = "20181210")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface AsyncAvengerHomeworkService extends IPingable {

    @Async
    AlpsFuture<AvengerHomework> informHomeworkToBigData(NewHomework newHomework, NewHomeworkBook newHomeworkBook);

    @Async
    AlpsFuture<AvengerHomework> informBasicReviewHomeworkToBigData(NewHomework newHomework);

    AlpsFuture<AvengerHomework> informOutsideReadingToBigData(OutsideReading outsideReading);
}

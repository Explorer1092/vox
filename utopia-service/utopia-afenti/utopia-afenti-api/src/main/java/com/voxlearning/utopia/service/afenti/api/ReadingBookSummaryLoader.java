package com.voxlearning.utopia.service.afenti.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.afenti.api.entity.PicBookOrderStat;
import com.voxlearning.utopia.service.afenti.api.entity.ReadingBookSummary;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20180313")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ReadingBookSummaryLoader {

    /**
     * 获得绘本的统计信息(阅读量)
     * @return
     */
    List<ReadingBookSummary> loadReadingBookSummaries(String seriesName, Date start, Date end);

    /**
     * 获得某个系列下面绘本的订单量
     * @param seriesName
     * @param start
     * @param end
     * @return
     */
    List<PicBookOrderStat> loadPicBookOrderStat(String seriesName, Date start, Date end);
}

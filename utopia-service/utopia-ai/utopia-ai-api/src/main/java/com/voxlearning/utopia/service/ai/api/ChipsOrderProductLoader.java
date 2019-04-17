package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190328")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsOrderProductLoader extends IPingable {
    MapMessage loadOnSaleShortLevelProductInfo(Long parentId);

    MapMessage loadOnSaleShortLevelProductInfo();

    MapMessage loadOfficialProductInfoByType(String productInfoTypeName, Long userId);

    /**
     * 检查用户已经购买的教材和将要购买的教材是否是互斥的
     *
     * @param userBooks
     * @param targetBooks
     * @return
     */
    Boolean checkBookBoughtMutex(List<String> userBooks, List<String> targetBooks);

    /**
     * 查询短期课产品信息
     *
     * @param parentId
     * @param studentId
     * @param checkStudent 是否检查学生
     * @return
     */
    MapMessage loadOnSaleShortLevelProductInfo(Long parentId, Long studentId, Boolean checkStudent);

    String loadShortProductAdPath(Long userId);

    MapMessage loadOnSaleShortLevelProductInfo(Long parentId, Long studentId, Boolean checkStudent, String type);
}

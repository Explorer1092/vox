package com.voxlearning.utopia.service.mizar.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GoodsCategory;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GrouponGoods;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author xiang.lv
 * @date 2016/9/21   11:32
 */
@ServiceVersion(version = "20160815")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface GrouponGoodsService extends IPingable {

    GoodsCategory saveGoodsCategory(final GoodsCategory goodsCategory);

    GrouponGoods saveGrouponGoods(final GrouponGoods grouponGoods);

}

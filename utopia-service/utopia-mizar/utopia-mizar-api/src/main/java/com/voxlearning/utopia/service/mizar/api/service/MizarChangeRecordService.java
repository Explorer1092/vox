package com.voxlearning.utopia.service.mizar.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarBrand;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopGoods;

import java.util.concurrent.TimeUnit;

/**
 * 信息变更申请相关
 *
 * @author yuechen.wang
 * @date 2016/10/09
 */
@ServiceVersion(version = "20161009")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MizarChangeRecordService extends IPingable {

    /**
     * 保存变更信息
     */
    MapMessage saveChangeRecord(MizarEntityChangeRecord record);

    /**
     * 审核通过品牌申请
     */
    MapMessage approve(MizarEntityChangeRecord record, MizarBrand brand);

    /**
     * 审核通过机构申请
     */
    MapMessage approve(MizarEntityChangeRecord record, MizarShop shop);

    /**
     * 审核通过课程申请
     */
    MapMessage approve(MizarEntityChangeRecord record, MizarShopGoods goods);

    /**
     * 驳回申请
     */
    MapMessage reject(MizarEntityChangeRecord record);

}

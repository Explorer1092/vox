package com.voxlearning.utopia.service.mizar.consumer.service;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarBrand;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopGoods;
import com.voxlearning.utopia.service.mizar.api.service.MizarChangeRecordService;
import lombok.Getter;

/**
 * Created by yuechen.wang on 2016/10/10.
 */
public class MizarChangeRecordServiceClient {
    @Getter
    @ImportService(interfaceClass = MizarChangeRecordService.class)
    private MizarChangeRecordService remoteReference;

    public MapMessage saveChangeRecord(MizarEntityChangeRecord record) {
        if (record == null) {
            return MapMessage.errorMessage("参数无效");
        }
        return remoteReference.saveChangeRecord(record);
    }

    public MapMessage approve(MizarEntityChangeRecord record, MizarShopGoods goods) {
        if (record == null || goods == null) {
            return MapMessage.errorMessage("参数无效");
        }
        return remoteReference.approve(record, goods);

    }

    public MapMessage approve(MizarEntityChangeRecord record, MizarShop shop) {
        if (record == null || shop == null) {
            return MapMessage.errorMessage("参数无效");
        }
        return remoteReference.approve(record, shop);

    }

    public MapMessage approve(MizarEntityChangeRecord record, MizarBrand mizarBrand) {
        if (record == null || mizarBrand == null) {
            return MapMessage.errorMessage("参数无效");
        }
        return remoteReference.approve(record, mizarBrand);

    }

    public MapMessage reject(MizarEntityChangeRecord record) {
        if (record == null) {
            return MapMessage.errorMessage("参数无效");
        }
        return remoteReference.reject(record);

    }

}

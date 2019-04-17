package com.voxlearning.utopia.mizar.audit;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.service.mizar.api.constants.MizarAuditStatus;
import com.voxlearning.utopia.service.mizar.api.constants.MizarEntityType;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarBrand;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopGoods;

/**
 * 获取变更申请的实体
 * Created by Yuechen.Wang on 2016/12/01.
 */
public class MizarAuditEntityFactory {

    private static <E> MizarEntityChangeRecord newInstance(MizarAuthUser user, MizarEntityType entityType, E entity, String desc) {
        if (user == null || entityType == null || entity == null || StringUtils.isBlank(desc)) {
            throw new UtopiaRuntimeException("Mizar平台生成变更审核实体失败, 参数不能为空!");
        }
        MizarEntityChangeRecord record = new MizarEntityChangeRecord();
        record.setAuditStatus(MizarAuditStatus.PENDING.name());
        record.setEntityType(entityType.getCode());
        record.setApplicant(user.getRealName());
        record.setApplicantId(user.getUserId());
        record.setContent(JSON.toJSONString(entity));
        record.setDesc(desc);
        return record;
    }

    public static MizarEntityChangeRecord newBrandInstance(MizarAuthUser user, MizarBrand brand, String desc) {
        return newInstance(user, MizarEntityType.BRAND, brand, desc);
    }

    public static MizarEntityChangeRecord newShopInstance(MizarAuthUser user, MizarShop shop, String desc) {
        return newInstance(user, MizarEntityType.SHOP, shop, desc);
    }

    public static MizarEntityChangeRecord newGoodsInstance(MizarAuthUser user, MizarShopGoods goods, String desc) {
        return newInstance(user, MizarEntityType.GOODS, goods, desc);
    }

    public static MizarEntityChangeRecord newActivityInstance(MizarAuthUser user, MizarShopGoods activity, String desc) {
        return newInstance(user, MizarEntityType.FAMILY_ACTIVITY, activity, desc);
    }

}

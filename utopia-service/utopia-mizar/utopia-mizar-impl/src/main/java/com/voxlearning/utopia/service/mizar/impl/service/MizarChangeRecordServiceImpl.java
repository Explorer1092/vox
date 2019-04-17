package com.voxlearning.utopia.service.mizar.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.mizar.api.constants.MizarAuditStatus;
import com.voxlearning.utopia.service.mizar.api.constants.MizarShopStatusType;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarBrand;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopGoods;
import com.voxlearning.utopia.service.mizar.api.service.MizarChangeRecordService;
import com.voxlearning.utopia.service.mizar.impl.dao.change.MizarEntityChangeRecordDao;
import com.voxlearning.utopia.service.mizar.impl.dao.shop.MizarBrandDao;
import com.voxlearning.utopia.service.mizar.impl.dao.shop.MizarShopDao;
import com.voxlearning.utopia.service.mizar.impl.dao.shop.MizarShopGoodsDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

/**
 * Created by yuechen.wang on 2016/10/10.
 */
@Named
@Service(interfaceClass = MizarChangeRecordService.class)
@ExposeService(interfaceClass = MizarChangeRecordService.class)
public class MizarChangeRecordServiceImpl extends SpringContainerSupport implements MizarChangeRecordService {

    @Inject private MizarBrandDao mizarBrandDao;
    @Inject private MizarShopDao mizarShopDao;
    @Inject private MizarShopGoodsDao mizarShopGoodsDao;
    @Inject private MizarEntityChangeRecordDao mizarEntityChangeRecordDao;

    @Override
    public MapMessage saveChangeRecord(MizarEntityChangeRecord record) {
        if (record == null) {
            return MapMessage.errorMessage("参数无效");
        }
        try {
            MizarEntityChangeRecord upsert = mizarEntityChangeRecordDao.upsert(record);
            return MapMessage.successMessage().add("rid", upsert.getId());
        } catch (Exception ex) {
            logger.error("Failed save MizarChangeRecord!", ex);
            return MapMessage.errorMessage("保存变更失败！");
        }
    }

    @Override
    public MapMessage approve(MizarEntityChangeRecord record, MizarBrand brand) {
        if (record == null || brand == null) {
            return MapMessage.errorMessage("参数无效");
        }
        try {
            // 首先, 变更信息
            MizarBrand upsert = mizarBrandDao.upsert(brand);
            if (upsert == null) {
                return MapMessage.errorMessage("记录更新失败");
            }
            // 然后变更申请记录
            record.setTargetId(upsert.getId());
            record.setAuditStatus(MizarAuditStatus.APPROVE.name());
            record.setAuditTime(new Date());
            mizarEntityChangeRecordDao.upsert(record);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed save MizarChangeRecord!", ex);
            return MapMessage.errorMessage("变更审核失败！");
        }
    }

    @Override
    public MapMessage approve(MizarEntityChangeRecord record, MizarShop shop) {
        if (record == null || shop == null) {
            return MapMessage.errorMessage("参数无效");
        }
        try {
            // 首先, 变更信息
            // 机构审核通过了直接上线
            shop.setShopStatus(MizarShopStatusType.ONLINE.name());
            MizarShop upsert = mizarShopDao.upsert(shop);
            if (upsert == null) {
                return MapMessage.errorMessage("记录更新失败");
            }
            // 然后变更申请记录
            record.setTargetId(upsert.getId());
            record.setAuditStatus(MizarAuditStatus.APPROVE.name());
            record.setAuditTime(new Date());
            mizarEntityChangeRecordDao.upsert(record);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed save MizarChangeRecord!", ex);
            return MapMessage.errorMessage("课程变更审核失败！");
        }
    }

    @Override
    public MapMessage approve(MizarEntityChangeRecord record, MizarShopGoods goods) {
        if (record == null || goods == null) {
            return MapMessage.errorMessage("参数无效");
        }
        try {
            // 首先, 变更信息
            MizarShopGoods upsert = mizarShopGoodsDao.upsert(goods);
            if (upsert == null) {
                return MapMessage.errorMessage("记录更新失败");
            }
            // 然后变更申请记录
            record.setTargetId(upsert.getId());
            record.setAuditStatus(MizarAuditStatus.APPROVE.name());
            record.setAuditTime(new Date());
            mizarEntityChangeRecordDao.upsert(record);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed save MizarChangeRecord!", ex);
            return MapMessage.errorMessage("课程变更审核失败！");
        }
    }

    @Override
    public MapMessage reject(MizarEntityChangeRecord record) {
        if (record == null) {
            return MapMessage.errorMessage("参数无效");
        }
        try {
            // 变更申请记录
            record.setAuditStatus(MizarAuditStatus.REJECT.name());
            record.setAuditTime(new Date());
            mizarEntityChangeRecordDao.upsert(record);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed save MizarChangeRecord!", ex);
            return MapMessage.errorMessage("变更审核失败！");
        }
    }
}

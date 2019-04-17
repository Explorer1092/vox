package com.voxlearning.utopia.mizar.audit;

import com.voxlearning.utopia.service.mizar.api.constants.MizarEntityType;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Yuechen.Wang on 2016/10/12.
 */
@Named
public class MizarAuditProcessorFactory {

    private final static MizarAuditProcessorFactory instance = new MizarAuditProcessorFactory();

    public static MizarAuditProcessorFactory getInstance() {
        return instance;
    }

    @Inject private MizarBrandAuditProcessor mizarBrandAuditProcessor;
    @Inject private MizarShopAuditProcessor mizarShopAuditProcessor;
    @Inject private MizarGoodsAuditProcessor mizarGoodsAuditProcessor;


    public MizarAuditProcessor getProcessor(MizarAuditContext context) {
        MizarEntityType entityType = context.fetchEntityType();
        switch (entityType) {
            case BRAND:
                mizarBrandAuditProcessor.setContext(context);
                return mizarBrandAuditProcessor;
            case SHOP:
                mizarShopAuditProcessor.setContext(context);
                return mizarShopAuditProcessor;
            case GOODS:
            case FAMILY_ACTIVITY: // 亲子活动跟课程使用同一实体
                mizarGoodsAuditProcessor.setContext(context);
                return mizarGoodsAuditProcessor;
            default:
                return null;
        }
    }

}

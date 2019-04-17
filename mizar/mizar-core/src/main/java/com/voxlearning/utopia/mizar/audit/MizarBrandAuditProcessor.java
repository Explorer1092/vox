package com.voxlearning.utopia.mizar.audit;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.mizar.utils.BeanUtils;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarBrand;

import javax.inject.Named;

/**
 * Created by Yuechen.Wang on 2016/10/12.
 */
@Named("mizarBrandAuditProcessor")
public class MizarBrandAuditProcessor extends MizarAuditProcessor {

    @Override
    public MapMessage approve() {
        if (!checkBeforeAudit()) {
            return MapMessage.errorMessage("参数异常");
        }
        MizarEntityChangeRecord record = getContext().getRecord();
        MizarAuthUser currentUser = getContext().getCurrentUser();
        record.setAuditorId(currentUser.getUserId());
        record.setAuditor(currentUser.getRealName());
        MizarBrand change = JSON.parseObject(record.getContent(), MizarBrand.class);
        // 新增模式的审核通过之后直接生成新的实体咯
        MizarBrand old;
        if (StringUtils.isNotBlank(record.getTargetId())) {
            old = mizarLoaderClient.loadBrandById(record.getTargetId());
            BeanUtils.getInstance().copyDiff(old, change);
        } else {
            old = change;
        }
        old.setPoints(change.getPoints());
        return mizarChangeRecordServiceClient.approve(record, old);
    }

    @Override
    /**这个方法可以放到**/
    public MapMessage reject() {
        return defaultReject();
    }

}

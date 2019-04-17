package com.voxlearning.utopia.mizar.audit;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.mizar.utils.BeanUtils;
import com.voxlearning.utopia.service.mizar.api.constants.MizarShopStatusType;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;

import javax.inject.Named;

/**
 * Created by Yuechen.Wang on 2016/10/12.
 */
@Named
public class MizarShopAuditProcessor extends MizarAuditProcessor {
    @Override
    public MapMessage approve() {
        if (!checkBeforeAudit()) {
            return MapMessage.errorMessage("参数异常");
        }
        MizarEntityChangeRecord record = getContext().getRecord();

        MizarAuthUser currentUser = getContext().getCurrentUser();
        record.setAuditorId(currentUser.getUserId());
        record.setAuditor(currentUser.getRealName());

        MizarShop change = JSON.parseObject(record.getContent(), MizarShop.class);
        // 新增模式的审核通过之后直接生成新的实体咯
        MizarShop old;
        if (StringUtils.isNotBlank(record.getTargetId())) {
            old = mizarLoaderClient.loadShopById(record.getTargetId());
            BeanUtils.getInstance().copyDiff(old, change);
            old.setVip(change.getVip());
            old.setBaiduGps(change.getBaiduGps());
            old.setCooperator(change.getCooperator());
        } else {
            old = change;
        }
        MapMessage retMsg = mizarChangeRecordServiceClient.approve(record, old);
        if (!retMsg.isSuccess()) {
            return retMsg;
        }
//        // 准备发送消息
//        Map<String, String> params = new HashMap<>();
//        params.put("user", SafeConverter.toString(currentUser.getRealName(), "--"));
//        params.put("time", DateUtils.nowToString());
//        params.put("role", MizarUserRoleType.of(currentUser.getRoleList().get(0)).getRoleName());
//        params.put("id", record.getId());
//        MizarNotify notify = MizarNotifyPostOffice.writeNotify(MizarNotifyTemplate.SHOP_APPROVE, params);
//
//        // FIXME 如果发送消息有异常，暂时忽略
//        mizarNotifyServiceClient.sendNotify(notify, Collections.singletonList(record.getApplicantId()));
        return MapMessage.successMessage();
    }

    /**
     * 机构驳回的话，如果是上线的机构，需要改状态为下线
     */
    @Override
    public MapMessage reject() {
        // 记录申请信息
        MapMessage rejectMsg = defaultReject();
        if (!rejectMsg.isSuccess()) {
            return rejectMsg;
        }
        MizarEntityChangeRecord record = getContext().getRecord();
//        MizarAuthUser currentUser = getContext().getCurrentUser();
//        // 准备发送消息
//        Map<String, String> params = new HashMap<>();
//        params.put("user", SafeConverter.toString(currentUser.getRealName(), "--"));
//        params.put("time", DateUtils.nowToString());
//        params.put("role", MizarUserRoleType.of(currentUser.getRoleList().get(0)).getRoleName());
//        params.put("reason", SafeConverter.toString(getContext().getProcessNotes()));
//        params.put("id", record.getId());
//        MizarNotify notify = MizarNotifyPostOffice.writeNotify(MizarNotifyTemplate.SHOP_REJECT, params);
//        // FIXME 如果发送消息有异常，暂时忽略
//        mizarNotifyServiceClient.sendNotify(notify, Collections.singletonList(record.getApplicantId()));
        if (StringUtils.isBlank(record.getTargetId())) {
            return rejectMsg;
        }
        MizarShop shop = mizarLoaderClient.loadShopById(record.getTargetId());
        shop.setShopStatus(MizarShopStatusType.OFFLINE.getName());
        return mizarServiceClient.saveMizarShop(shop);
    }
}

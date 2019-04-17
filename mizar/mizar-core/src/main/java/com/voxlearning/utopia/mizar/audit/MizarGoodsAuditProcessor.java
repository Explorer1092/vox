package com.voxlearning.utopia.mizar.audit;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.mizar.utils.BeanUtils;
import com.voxlearning.utopia.service.mizar.api.constants.MizarGoodsStatus;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopGoods;

import javax.inject.Named;

/**
 * Created by Yuechen.Wang on 2016/10/12.
 */
@Named
public class MizarGoodsAuditProcessor extends MizarAuditProcessor {

    @Override
    public MapMessage approve() {
        if (!checkBeforeAudit()) {
            return MapMessage.errorMessage("参数异常");
        }
        MizarEntityChangeRecord record = getContext().getRecord();

        MizarAuthUser currentUser = getContext().getCurrentUser();
        record.setAuditorId(currentUser.getUserId());
        record.setAuditor(currentUser.getRealName());

        MizarShopGoods change = JSON.parseObject(record.getContent(), MizarShopGoods.class);
        // 新增模式的审核通过之后直接生成新的实体咯
        MizarShopGoods goods;
        if (StringUtils.isNotBlank(record.getTargetId())) {
            goods = mizarLoaderClient.loadShopGoodsById(record.getTargetId());
            if (goods == null) {
                return MapMessage.errorMessage("无效的课程/活动信息!");
            }
//            if (goods.getStatus() == MizarGoodsStatus.ONLINE) {
//                return MapMessage.errorMessage("课程/活动已经上线，不允许变更！");
//            }
            BeanUtils.getInstance().copyDiff(goods, change);
        } else {
            goods = change;
        }
        return mizarChangeRecordServiceClient.approve(record, goods);
    }

    /**
     * 课程驳回的话，如果是上线的课程，需要改状态为下线
     */
    @Override
    public MapMessage reject() {
        // 记录申请信息
        MapMessage rejectMsg = defaultReject();
        if (!rejectMsg.isSuccess()) {
            return rejectMsg;
        }
        MizarEntityChangeRecord record = getContext().getRecord();
        if (StringUtils.isBlank(record.getTargetId())) {
            return rejectMsg;
        }
        MizarShopGoods goods = mizarLoaderClient.loadShopGoodsById(record.getTargetId());
        goods.setStatus(MizarGoodsStatus.OFFLINE);
        return mizarServiceClient.saveMizarShopGoods(goods);
    }
}

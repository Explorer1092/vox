package com.voxlearning.utopia.service.business.impl.support.order.userOrder.filter;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.utopia.service.business.impl.support.order.FilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilter;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilterContext;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarReserveRecord;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopGoods;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarServiceClient;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.api.entities.SmsMessage;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Objects;

/**
 * describe:
 * 临时使用
 * @author yong.liu
 * @date 2019/03/08
 */
@Slf4j
@Named
public class ThirdPartySendMessageFilter  extends UserOrderFilter {

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;
    @Inject
    private MizarLoaderClient mizarLoaderClient;
    @Inject
    private MizarServiceClient mizarServiceClient;

    @Override
    public void doFilter(UserOrderFilterContext context, FilterChain chain) {
        UserOrder order = context.getOrder();

        if(Objects.equals(order.getProductId(),"5c8110b64bd04e3e259ab5c0")){
            try {
                String device = "付款" + order.getOrderPrice().setScale(2, RoundingMode.HALF_UP) + "元钱";
                UtopiaSql utopiaSql = utopiaSqlFactory.getUtopiaSql("hs_misc");
                String sql = "UPDATE THIRD_PARTY_USER_INFO SET device='" + device + "' WHERE order_id='" + order.genUserOrderId() + "' AND third_party_type_id=9";
                int update = utopiaSql.withSql(sql).executeUpdate();
            }catch (Exception e){
                log.info("人教老师活动更新付款状态失败");
            }

            Long userId = order.getUserId();
            String mobile = sensitiveUserDataServiceClient.loadUserMobile(userId);
            SmsMessage smsMessage = new SmsMessage();
            smsMessage.setMobile(mobile);
            smsMessage.setType(SmsType.MIZAR_SMS_NOTIFY.name());
            String smsText = "恭喜您成功报名《小学语文》统编教材优课观摩活动，活动将于5月31日-6月2日在杭州召开，请定期查阅“人教期刊一起教研活动”公众号内信息。";
            if (StringUtils.isNotBlank(smsText) && StringUtils.isNotBlank(mobile)) {
                smsMessage.setSmsContent(smsText);
                smsServiceClient.getSmsService().sendSms(smsMessage);
            }
        }
        MizarReserveRecord mizarReserveRecord = mizarLoaderClient.loadShopReserveByParentId(order.getUserId()).stream().filter(r -> Objects.equals(r.getOrderId(), order.genUserOrderId())).findFirst().orElse(null);
        if (mizarReserveRecord == null) {
            return;
        }

        MizarShopGoods mizarShopGoods = mizarLoaderClient.loadShopGoodsById(mizarReserveRecord.getShopGoodsId());
        if (mizarShopGoods == null) {
            return;
        }
        // 发送短信
        String mobile = sensitiveUserDataServiceClient.loadUserMobile(order.getUserId());
        SmsMessage smsMessage = new SmsMessage();
        smsMessage.setMobile(mobile);
        smsMessage.setType(SmsType.HONEY_COMB_FREE.name());
        String smsText = mizarShopGoods.getSmsMessage();
        if (StringUtils.isNotBlank(smsText) && StringUtils.isNotBlank(mobile)) {
            smsMessage.setSmsContent(smsText);
            smsServiceClient.getSmsService().sendSms(smsMessage);
        }
        mizarLoaderClient.incrSellCount(mizarShopGoods.getId());
        mizarLoaderClient.incrDaySellCount(mizarShopGoods.getId());
        // 修改报名状态为已支付
        mizarServiceClient.updateReservationStatus(Collections.singleton(mizarReserveRecord.getId()), MizarReserveRecord.Status.Payment);
    }
}

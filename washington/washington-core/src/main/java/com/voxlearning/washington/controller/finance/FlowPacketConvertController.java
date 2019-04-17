package com.voxlearning.washington.controller.finance;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.campaign.FlowPacketConvert;
import com.voxlearning.utopia.service.finance.client.FlowPacketConvertServiceClient;
import com.voxlearning.utopia.service.reward.consumer.RewardServiceClient;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 流量包充值回调接口
 */
@RequestMapping(value = "/finance/flowpacket")
@Controller
public class FlowPacketConvertController extends AbstractController {
    // 安信捷回调错误码映射
    private static final Map<String, String> AXJ_ERROR_MAP = new HashMap<>();
    private static final String AXJ_SUCCESS_CODE = "4";
    private static final String AXJ_FAILURE_CODE = "5";
    private static final String AXJ_SUCCESS_SMS = "您在奖品中心兑换的{}已到帐，有效期限本月，感谢使用。";
    private static final String AXJ_FAILED_SMS = "您在奖品中心兑换的{}充值失败，学豆/园丁豆已退还至您的账户，您可在奖品中心重新尝试订购。";

    static {
//        ERROR_MAP.put("0001", "交易代码为空");
//        ERROR_MAP.put("0002", "非法卡品");
//        ERROR_MAP.put("0003", "非法手机号(位长不够,非移动号码)");
//        ERROR_MAP.put("0004", "");
//        ERROR_MAP.put("0005", "非法渠道（渠道不存在、渠道终止合作、渠道状态无效）");
//        ERROR_MAP.put("0006", "库存不足");
//        ERROR_MAP.put("0007", "请求包体为空");
//        ERROR_MAP.put("0008", "平台鉴权失败");
//        ERROR_MAP.put("0009", "重复的交易流水号");
//        ERROR_MAP.put("0010", "渠道账户余额不够");
        // 文档更新 2017-06-06
        // FIXME 以下代码也是仅供参考，具体还是以实际情况为准
        AXJ_ERROR_MAP.put("1", "参数不合法 ");
        AXJ_ERROR_MAP.put("5", "该笔订单已完成订购");
        AXJ_ERROR_MAP.put("10", "用户认证失败 ");
        AXJ_ERROR_MAP.put("11", "IP 或域名认证失败  网关问题");
        AXJ_ERROR_MAP.put("12", "余额不足,不能下发本次流量");
        AXJ_ERROR_MAP.put("13", "无效流量网关类型 ");
        AXJ_ERROR_MAP.put("15", "提交的手机号超量 ");
        AXJ_ERROR_MAP.put("20", "提交参数不正确(比如:必填参数为空) ");
        AXJ_ERROR_MAP.put("21", "签名错误! ");
        AXJ_ERROR_MAP.put("22", "提交的号码为空(比如:该业务不支持的手机号,手机号不合格,手机号与产 品不匹配,手机号在黑名单中) ");
        AXJ_ERROR_MAP.put("24", "产品参数不正确 ");
        AXJ_ERROR_MAP.put("25", "无该产品订购权限 ");
        AXJ_ERROR_MAP.put("26", "无效的订单号；产品不允许重复订购");
        AXJ_ERROR_MAP.put("27", "查询日期范围不能超过近三个月");
        AXJ_ERROR_MAP.put("29", "请先关闭推送配置! ");
        AXJ_ERROR_MAP.put("30", "调用过于频繁,请稍后再试! ");
        AXJ_ERROR_MAP.put("50", "系统繁忙,稍后再试! ");
        AXJ_ERROR_MAP.put("77", "是贵司催促手动撤销");
        AXJ_ERROR_MAP.put("4013", "存在在途订单不允许再做变更操作");
        AXJ_ERROR_MAP.put("4012", "非实名违章单停、非实名违章双停状态下，只能受理实名");
        AXJ_ERROR_MAP.put("4011", "欠费单停");
        AXJ_ERROR_MAP.put("4019", "套餐互斥");
    }

    @Inject private FlowPacketConvertServiceClient flowPacketConvertServiceClient;
    @Inject private RewardServiceClient rewardServiceClient;

    @RequestMapping(value = "/callback.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    String callback(String result, String msg, String transactionId, String pass) {
//        logger.info("Flow packet charging callback invoked! {},{},{},{}", result, msg, transactionId, pass);
        try {
            // 验证订单数据是否一致
            FlowPacketConvert chargingItem = flowPacketConvertServiceClient.getFlowPacketConvertService()
                    .loadFlowPacketConvert(pass)
                    .getUninterruptibly();
            if (chargingItem == null) {
                logger.warn("Monitor:FlowPacketConvertController.callback chargingItem is null result={}, msg={}, transactionId={}, pass={}", result, msg, transactionId, pass);
                return "贵公司回调传过来的pass和订单提交时传过去的pass不一致";
            }
            if (!Objects.equals(chargingItem.getTransactionId(), transactionId)) {
                logger.warn("Monitor:FlowPacketConvertController.callback chargingItem.getTransactionId()={},transactionId={}", chargingItem.getTransactionId(), transactionId);
                return "贵公司回调传过来的transactionId和订单提交时给返回的不一致";
            }

            // 更新订单状态
            if ("0".equals(result)) {
                flowPacketConvertServiceClient.getFlowPacketConvertService()
                        .updateChargingSuccess(chargingItem.getId(), chargingItem.getTransactionId(), FlowPacketConvert.FlowPacketVendor.JJLL)
                        .getUninterruptibly();
            } else {
                flowPacketConvertServiceClient.getFlowPacketConvertService()
                        .updateChargingFailed(chargingItem.getId(), result, msg, FlowPacketConvert.FlowPacketVendor.JJLL)
                        .awaitUninterruptibly();
                // 告诉奖品中心-订单 流量充值失败
                rewardServiceClient.cancelFlowPacketOrder(Long.valueOf(chargingItem.getOrderNo()), "充值失败");
            }
        } catch (Exception e) {
            logger.error("Flow packet charging callback error result={}, msg={}, transactionId={}, pass={}", result, msg, transactionId, pass, e);
        }
        return "success";
    }

    @RequestMapping(value = "/deliver.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    MapMessage deliver() {
        String seqnum = getRequestString("seqnum");
        String rcode = getRequestString("rcode");
        try {
            // 验证订单数据是否一致
            FlowPacketConvert chargingItem = flowPacketConvertServiceClient.getFlowPacketConvertService()
                    .loadFlowPacketConvert(seqnum)
                    .getUninterruptibly();
            if (chargingItem == null) {
                logger.warn("Monitor:FlowPacketConvertController.deliver chargingItem is null seqnum={}, rcode={}", seqnum, rcode);
                return MapMessage.errorMessage("充值订单号 [{}] 异常", seqnum);
            }
            Long orderId = SafeConverter.toLong(chargingItem.getOrderNo());
            RewardOrder rewardOrder = rewardLoaderClient.loadRewardOrders(Collections.singleton(orderId)).get(orderId);
            // 更新订单状态
            if (AXJ_SUCCESS_CODE.equals(rcode)) {
                flowPacketConvertServiceClient.getFlowPacketConvertService()
                        .updateChargingSuccess(chargingItem.getId(), chargingItem.getTransactionId(), FlowPacketConvert.FlowPacketVendor.AXJ)
                        .getUninterruptibly();
                // 给用户发送充值成功短信通知
                if (rewardOrder != null) {
                    String mobile = sensitiveUserDataServiceClient.loadUserMobile(chargingItem.getUserId());
                    String content = StringUtils.formatMessage(AXJ_SUCCESS_SMS, rewardOrder.getProductName());
                    smsServiceClient.createSmsMessage(mobile).content(content).type(SmsType.FLOW_PACKET_REWARD_NOTIFY.name()).send();
                }
                return MapMessage.successMessage("状态更新成功");
            } else {
                String rcodeDetail = getRequestString("rcode_detail");
                String errorInfo = "";
                if (AXJ_FAILURE_CODE.equals(rcode)) {
                    errorInfo = AXJ_ERROR_MAP.get(rcodeDetail);
                }
                if (StringUtils.isBlank(errorInfo)) errorInfo = "未知错误";
                flowPacketConvertServiceClient.getFlowPacketConvertService()
                        .updateChargingFailed(chargingItem.getId(), rcode, rcodeDetail + ":" + errorInfo, FlowPacketConvert.FlowPacketVendor.AXJ)
                        .awaitUninterruptibly();
                if (rewardOrder != null) {
                    // 告诉奖品中心-订单 流量充值失败
                    rewardServiceClient.cancelFlowPacketOrder(orderId, "充值失败");
                    // 给用户发送短信通知充值失败
                    String mobile = sensitiveUserDataServiceClient.loadUserMobile(chargingItem.getUserId());
                    String content = StringUtils.formatMessage(AXJ_FAILED_SMS, rewardOrder.getProductName());
                    smsServiceClient.createSmsMessage(mobile).content(content).type(SmsType.FLOW_PACKET_REWARD_NOTIFY.name()).send();
                }
                return MapMessage.errorMessage("订单 [{}] 充值失败", seqnum);
            }
        } catch (Exception ex) {
            logger.error("Axj flow packet charging callback error seqnum={}, rcode={}", seqnum, rcode, ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

}

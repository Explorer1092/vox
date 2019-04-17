package com.voxlearning.utopia.mizar.controller.crm;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.extension.sensitive.codec.SensitiveLib;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.mizar.service.reserve.MizarReserveService;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarReserveRecord;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopGoods;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderAmortizeHistory;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderRefundServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 客户管理-预约信息 相关
 * Created by Yuechen.Wang on 2016/9/7.
 */
@Controller
@RequestMapping(value = "/crm/reserve")
public class ReservationManageController extends AbstractMizarController {

    @Inject private MizarReserveService mizarReserveService;
    private static final int RESERVE_PAGE_SIZE = 10;

    @Inject
    private UserOrderRefundServiceClient userOrderRefundServiceClient;
    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @AlpsPubsubPublisher(topic = "mizar.honeycomb.order.activate.tpc")
    private MessagePublisher mizarHoneycombOrderPublisher;

    // 预约信息查询
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String reserveIndex(Model model) {
        String selectedShop = getRequestString("selectedShop"); // 选择门店
        String studentName = getRequestString("studentName"); // 客户名称
        String mobile = getRequestString("mobile"); // 联系电话
        String status = getRequestString("status"); // 客户状态
        Integer page = getRequestInt("page", 1); // 当前页
        model.addAttribute("selectedShop", selectedShop);
        model.addAttribute("studentName", studentName);
        model.addAttribute("mobile", mobile);
        model.addAttribute("status", status);
        model.addAttribute("page", page);

        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("studentName", studentName);
        filterMap.put("mobile", mobile);
        filterMap.put("status", status);

        List<String> userShop = currentUserShop();
        // 生效门店
        List<Map<String, Object>> shopList = mizarLoaderClient.loadShopByIds(userShop).values().stream()
                .map(MizarShop::simpleInfo).collect(Collectors.toList());
        List<String> shopIds;
        if (StringUtils.isNotBlank(selectedShop) && !"all".equals(selectedShop)) {
            shopIds = Collections.singletonList(selectedShop);
        } else {
            shopIds = userShop;
        }
        Map<String, MizarShop> userShopMap = mizarLoaderClient.loadShopByIds(shopIds);
        List<Map<String, Object>> reserveInfo = mizarReserveService.loadShopReservations(userShopMap.values(), filterMap);
        model.addAttribute("reservePage", splitList(reserveInfo, RESERVE_PAGE_SIZE));
        model.addAllAttributes(filterMap);

        model.addAttribute("shop", userShop);
        model.addAttribute("shopList", shopList);
        return "crm/reservemanage";
    }

    // 变更备注
    @RequestMapping(value = "takenote.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateNotes() {
        Long reserveId = requestLong("reserveId"); // 选中的记录
        String notes = getRequestString("notes").replaceAll("\\s", ""); // 备注
        if (StringUtils.isBlank(notes)) {
            return MapMessage.errorMessage("请填写备注");
        }
        try {
            MizarReserveRecord record = mizarLoaderClient.loadReservations(Collections.singleton(reserveId)).get(reserveId);
            if (record == null) {
                return MapMessage.errorMessage("无效的预约信息");
            }
            record.setNotes(notes);
            return mizarServiceClient.saveMizarReserve(record);
        } catch (Exception ex) {
            logger.error("Failed change reserve note, id={}", reserveId, ex);
            return MapMessage.errorMessage("备注更改失败：" + ex.getMessage());
        }
    }

    // 变更状态
    @RequestMapping(value = "changestatus.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeStatus() {
        Set<Long> reserveIdSet = requestLongSet("reservations"); // 选中的记录
        String pre = getRequestString("pre").replaceAll("\\s", ""); // 记录变更之前状态
        String post = getRequestString("post").replaceAll("\\s", ""); // 记录变更之后的状态
        try {
            MizarReserveRecord.Status preStatus = MizarReserveRecord.parseStatus(pre);
            MizarReserveRecord.Status postStatus = MizarReserveRecord.parseStatus(post);
            if (preStatus == null || postStatus == null) {
                return MapMessage.errorMessage("无效的状态：({},{})", pre, post);
            }
            Map<Long, MizarReserveRecord> reservations = mizarLoaderClient.loadReservations(reserveIdSet);
            Set<Long> records = reservations.values().stream()
                    .filter(record -> preStatus.equals(record.getStatus()))
                    .map(MizarReserveRecord::getId)
                    .collect(Collectors.toSet());

            if (CollectionUtils.isEmpty(records)) {
                Map<MizarReserveRecord.Status, String> statusMap = MizarReserveRecord.fetchStatusMap();
                return MapMessage.errorMessage("状态[{}->{}]变更失败", statusMap.get(preStatus), statusMap.get(postStatus));
            }
            MapMessage mapMessage = mizarServiceClient.updateReservationStatus(records, postStatus);
            if (!mapMessage.isSuccess()) {
                return mapMessage;
            }
            Map<String, MizarShopGoods> shopGoodsMap = mizarLoaderClient.loadShopGoodsByIds(reservations.values().stream().map(MizarReserveRecord::getShopGoodsId).collect(Collectors.toSet()));
            for (MizarReserveRecord record : reservations.values()) {
                boolean fengChao = false;
                MizarShopGoods mizarShopGoods = shopGoodsMap.get(record.getShopGoodsId());
                if (mizarShopGoods == null || mizarShopGoods.featureDealSuccess() == 0) {
                    continue;
                }
                if (postStatus == MizarReserveRecord.Status.Access) {
                    fengChao = true;
                    // 需要退费
                    if (mizarShopGoods.featureDealSuccess() == 2) {
                        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(record.getOrderId());
                        // 只针对大于0的订单
                        if (userOrder.getOrderPrice().compareTo(BigDecimal.ZERO) > 0) {
                            Map<String, BigDecimal> items = new HashMap<>();
                            userOrderLoaderClient.loadOrderAmortizeHistory(record.getOrderId()).stream()
                                    .filter(h -> h.getPaymentStatus() == PaymentStatus.Paid).forEach(history -> items.put(history.getProductItemId(), history.getPayAmount()));
                            // 到店退款
                            MapMessage mapMessageRefund = userOrderRefundServiceClient.backCashRefund(record.getParentId(), record.getOrderId().split("_")[0], items, "mizar", "fengchao");
                            if (!mapMessageRefund.isSuccess()) {
                                logger.error("退款失败");
                                fengChao = false;
                            }
                        }
                    }
                }
                // 给蜂巢发激活消息
                if (fengChao) {
                    mizarHoneycombOrderPublisher.publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(MapUtils.m("orderId", record.getOrderId(), "userId", record.getParentId()))));
                }
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed change status, shop=[{}], {}->{}", getRequestString("reservations"), pre, post, ex);
            return MapMessage.errorMessage("状态变更失败：" + ex.getMessage());
        }
    }

    /**
     * 批量到课处理
     * @return
     */
    @RequestMapping(value = "access_by_mobiles.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage accessByMobiles() {
        Set<String> mobiles = Arrays.stream(getRequestString("mobiles").split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        if (mobiles.isEmpty()) {
            return MapMessage.errorMessage("请填写手机号");
        }
        String goodsId = getRequestString("goodsId");
        if (StringUtils.isBlank(goodsId)) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            MizarShopGoods shopGoods = mizarLoaderClient.loadShopGoodsById(goodsId);
            if (shopGoods == null) {
                return MapMessage.errorMessage("课程不存在");
            }
            List<MizarReserveRecord> goodsReserves = mizarLoaderClient.loadGoodsRecords(goodsId);
            Set<Long> reserveSet = new HashSet<>();
            for (MizarReserveRecord record : goodsReserves) {
                // 忽略已经到课的
                if (record.getStatus() == MizarReserveRecord.Status.Access) {
                    continue;
                }
                String decodeMobile = SensitiveLib.decodeMobile(record.getMobile());
                if (!mobiles.contains(decodeMobile)) {
                    continue;
                }
                reserveSet.add(record.getId());
                // 需要退费
                if (shopGoods.featureDealSuccess() == 2) {
                    UserOrder userOrder = userOrderLoaderClient.loadUserOrder(record.getOrderId());
                    // 只针对大于0的订单
                    if (userOrder.getOrderPrice().compareTo(BigDecimal.ZERO) > 0) {
                        Map<String, BigDecimal> items = new HashMap<>();
                        userOrderLoaderClient.loadOrderAmortizeHistory(record.getOrderId()).stream()
                                .filter(h -> h.getPaymentStatus() == PaymentStatus.Paid).forEach(history -> items.put(history.getProductItemId(), history.getPayAmount()));
                        // 到店退款
                        MapMessage mapMessageRefund = userOrderRefundServiceClient.backCashRefund(record.getParentId(), record.getOrderId().split("_")[0], items, "mizar", "fengchao");
                        if (!mapMessageRefund.isSuccess()) {
                            logger.error("退款失败");
                        }
                    }
                }
                // 给蜂巢发激活消息
                mizarHoneycombOrderPublisher.publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(MapUtils.m("orderId", record.getOrderId(), "userId", record.getParentId()))));
            }
            return mizarServiceClient.updateReservationStatus(reserveSet, MizarReserveRecord.Status.Access);
        } catch (Exception ex) {
            return MapMessage.errorMessage("状态变更失败：" + ex.getMessage());
        }
    }

}

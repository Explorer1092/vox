package com.voxlearning.washington.controller.ai;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.ai.api.ChipsOrderProductLoader;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chips/user")
public class ChipsUserController extends AbstractAiController {

    @ImportService(interfaceClass = ChipsOrderProductLoader.class)
    private ChipsOrderProductLoader chipsOrderProductLoader;


    @RequestMapping(value = "short/be.vpage", method = {RequestMethod.GET})
    public String shortBe() {
        try {
            User curUser = currentUser();
            if (curUser == null || !curUser.isStudent()) {
                return "redirect:/view/mobile/parent/parent_ai/short_be_in_student";
            }

            MapMessage mapMessage = parentServiceClient.loadOrRegisteDefaultParentUserByStudentId(curUser.getId());
            if (!mapMessage.isSuccess() || mapMessage.get("parent") == null || !(mapMessage.get("parent") instanceof User)) {
                return "redirect:/view/mobile/parent/parent_ai/short_be_in_student";
            }
            User user =  (User) mapMessage.get("parent");

            MapMessage message = chipsOrderProductLoader.loadOnSaleShortLevelProductInfo(user.getId(), curUser.getId(), true);
            if (!message.isSuccess()) {
                return "redirect:/view/mobile/parent/parent_ai/short_be_in_student";
            }

            String productId = SafeConverter.toString(message.get("productId"));
            String status = SafeConverter.toString(message.get("status"));
            if (StringUtils.isAnyBlank(productId, status) || PaymentStatus.Unpaid.name().equals(status)) {
                return "redirect:/view/mobile/parent/parent_ai/short_be_in_student";
            }

            //学生端需要订单id回跳到引导页
            List<OrderProductItem> orderProductItem = userOrderLoaderClient.loadProductItemsByProductId(productId);
            Set<String> itemIdSet = orderProductItem.stream().map(OrderProductItem::getId).collect(Collectors.toSet());
            String orderId = Optional.ofNullable(userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.ChipsEnglish.name(), user.getId()))
                    .map(e -> {
                        UserOrder userOrder = e.stream().filter(or -> productId.equals(or.getProductId())).findFirst().orElse(null);
                        if (userOrder != null) {//先找相同productId的订单
                            return userOrder;
                        }
                        //找有重合的item的订单
                        List<String> userProductIdList = e.stream().map(UserOrder::getProductId).collect(Collectors.toList());
                        Map<String, List<OrderProductItem>> userOrderProductItemMap = userOrderLoaderClient.loadProductItemsByProductIds(userProductIdList);
                        userOrder = e.stream().filter(or -> {
                            List<OrderProductItem> orderProductItemList = userOrderProductItemMap.get(or.getProductId());
                            if (CollectionUtils.isEmpty(orderProductItemList)) {
                                return false;
                            }
                            Set<String> itemId = orderProductItemList.stream().map(OrderProductItem::getId).collect(Collectors.toSet());
                            Set<String> allItem = new HashSet<>();
                            allItem.addAll(itemId);
                            allItem.addAll(itemIdSet);
                            return allItem.size() != (itemId.size() + itemIdSet.size()) ||
                                    Boolean.TRUE.equals(chipsOrderProductLoader.checkBookBoughtMutex(orderProductItemList.stream().map(OrderProductItem::getAppItemId).collect(Collectors.toList()), orderProductItem.stream().map(OrderProductItem::getAppItemId).collect(Collectors.toList())));
                        }).findFirst().orElse(null);
                        return userOrder;
                    })
                    .map(UserOrder::genUserOrderId)
                    .orElse("");

            if (StringUtils.isNotBlank(orderId)) {
                return "redirect:/view/mobile/parent/parent_ai/payment_success_in_student?oid=" + orderId;
            } else {
                return "redirect:/view/mobile/parent/parent_ai/short_be_in_student";
            }
        } catch (Exception e) {
            logger.error("shortBe error.", e);
            return "redirect:/view/mobile/parent/parent_ai/short_be_in_student";
        }
    }
}

package com.voxlearning.utopia.service.business.impl.support.order.userOrder.filter;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.business.impl.support.order.FilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilter;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilterContext;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.client.CampaignServiceClient;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.client.AsyncOrderCacheServiceClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.piclisten.api.ParentSelfStudyService;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.piclisten.client.TextBookManagementLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Summer on 2017/5/22.
 */
@Slf4j
@Named
public class PicListenOrderFilter extends UserOrderFilter {
    @Inject
    private CampaignServiceClient campaignServiceClient;
    @Inject
    private AsyncOrderCacheServiceClient asyncOrderCacheServiceClient;
    @Inject
    private TextBookManagementLoaderClient textBookManagementLoaderClient;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    @ImportService(interfaceClass = ParentSelfStudyService.class)
    private ParentSelfStudyService parentSelfStudyService;

    @Override
    public void doFilter(UserOrderFilterContext context, FilterChain chain) {
        UserOrder order = context.getOrder();
        OrderProduct product = userOrderLoaderClient.loadOrderProductById(order.getProductId());
        List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(order.getProductId());

        if (order.getOrderType() == OrderType.pic_listen && StringUtils.isNotEmpty(order.getOrderProductServiceType()) &&
                OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.PicListenBook) {
            Long userId = order.getUserId();
            Boolean isRenjiao = false;
            Boolean isWaiyan = false;
            //如果是打包教材，打包教材肯定是人教或者外研
            if (product.fetchAttribute("piclisten_package_id") != null) {
                isRenjiao = SafeConverter.toBoolean(product.fetchAttribute("is_renjiao"), true);
                //如果是人教版的包
                if (isRenjiao) {
                    //增加抽奖次数
                    campaignServiceClient.getCampaignService().addLotteryFreeChance(CampaignType.PICLISTENBOOK_ORDER_LOTTERY, order.getUserId(), 1);
                }
                isWaiyan = !isRenjiao;
            }else { //如果不是打包教材，需要限定是不是人教或者外研教材
                OrderProductItem orderProductItem = orderProductItems.stream().findFirst().orElse(null);
                if (orderProductItem != null){
                    TextBookManagement textBook = textBookManagementLoaderClient.getTextBook(orderProductItem.getAppItemId());
                    if (textBook != null){
                        isWaiyan = textBook.getShortPublisherName().equalsIgnoreCase("外研版");
                        isRenjiao = textBook.getShortPublisherName().equals("人教版");
                    }
                }
            }
            if (isRenjiao || isWaiyan) {
                String showName = "";
                List<StudentParentRef> refList = parentLoaderClient.loadParentStudentRefs(userId);
                if (CollectionUtils.isNotEmpty(refList)) {
                    User stu = userLoaderClient.loadUser(refList.get(0).getStudentId());
                    String firstName = "杨";
                    if (stu != null && StringUtils.isNotBlank(stu.fetchRealname())) {
                        firstName = StringUtils.substring(stu.fetchRealname(), 0, 1);
                    }
                    showName = firstName + "**" + refList.get(0).getCallName();
                }

                Map<String, Object> data = new HashMap<>();
                data.put("showName", showName);
                data.put("productName", product.getName());
                if (isRenjiao)
                    asyncOrderCacheServiceClient.getAsyncOrderCacheService().PicListenBookPurchaseCacheManager_add(data);
                else
                    asyncOrderCacheServiceClient.getAsyncOrderCacheService().PicListenBookPurchaseCacheManager_waiyanAdd(data);

                asyncOrderCacheServiceClient.getAsyncOrderCacheService().PicListenBookPurchaseCacheManager_addBuyOne();
            }

            //点读机购买成功,把该教材添加到用户书架上。#47598
            orderProductItems.forEach(t -> {
                parentSelfStudyService.addBook2PicListenShelf(userId, t.getAppItemId());
            });
        }
        // K捆绑处理点读机部分
        if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.GroupProduct && CollectionUtils.isNotEmpty(orderProductItems)) {
            List<OrderProductItem> picItems = orderProductItems.stream().filter(i -> OrderProductServiceType.safeParse(i.getProductType()) == OrderProductServiceType.PicListenBook)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(picItems)) {
                orderProductItems.forEach(t -> {
                    parentSelfStudyService.addBook2PicListenShelf(SafeConverter.toLong(order.getExtAttributes()), t.getAppItemId());
                });
            }
        }
        chain.doFilter(context);
    }
}

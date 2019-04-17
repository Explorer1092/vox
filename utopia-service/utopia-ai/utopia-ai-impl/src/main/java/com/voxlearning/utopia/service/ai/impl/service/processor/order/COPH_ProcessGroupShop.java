package com.voxlearning.utopia.service.ai.impl.service.processor.order;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.service.ai.entity.ChipsGroupShopping;
import com.voxlearning.utopia.service.ai.entity.ChipsUserCourse;
import com.voxlearning.utopia.service.ai.entity.ChipsUserOrderExt;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsOrderPostContext;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsGroupShoppingPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsUserOrderExtDao;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.ai.util.StringExtUntil;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderProductRef;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
public class COPH_ProcessGroupShop extends AbstractAiSupport implements IAITask<ChipsOrderPostContext> {

    @Inject
    private ChipsGroupShoppingPersistence chipsGroupShoppingPersistence;

    @Inject
    private ChipsUserOrderExtDao chipsUserOrderExtDao;

    @AlpsQueueProducer(queue = "utopia.chips.group.to.success.queue")
    private MessageProducer groupToSuccessProducer;

    @Override
    public void execute(ChipsOrderPostContext context) {
        ChipsUserOrderExt chipsUserOrderExt = context.getOrderExt();
        if (chipsUserOrderExt == null || StringUtils.isBlank(chipsUserOrderExt.getGroupShoppingCode()) || chipsUserOrderExt.getSponsor() == null) {
            return;
        }
        if (!chipsUserOrderExt.getSponsor()) {
            boolean res = chipsGroupShoppingPersistence.updateNumber(chipsUserOrderExt.getGroupShoppingCode(), 1, 2);
            if (res) {
                Map<String, Object> message = new HashMap<>();
                message.put("C", chipsUserOrderExt.getGroupShoppingCode());
                message.put("N", 1);
                groupToSuccessProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
                return;
            }
            List<ChipsGroupShopping> chipsGroupShoppingList = chipsGroupShoppingPersistence.loadInGroupingRecord();
            String newGroupCode = "";
            for(ChipsGroupShopping groupShopping : chipsGroupShoppingList) {
                res = chipsGroupShoppingPersistence.updateNumber(groupShopping.getCode(), 1, 2);
                if (res) {
                    newGroupCode = groupShopping.getCode();
                    break;
                }
            }

            if (StringUtils.isBlank(newGroupCode)) {
                processUserGroupCourse(context.getUserOrder());

                newGroupCode = StringExtUntil.md5(context.getUserOrder().getId() + context.getUserId().toString());
                chipsGroupShoppingPersistence.insertOrUpdate(context.getUserId(), context.getUserOrder().getId(), newGroupCode, 1);

                chipsUserOrderExt.setNewGroupCode(newGroupCode);
                chipsUserOrderExt.setStatus(ChipsUserOrderExt.OrderStatus.PAYED);
                chipsUserOrderExt.setSponsor(true);
                chipsUserOrderExt.setUpdateDate(new Date());
                chipsUserOrderExtDao.upsert(chipsUserOrderExt);
                context.terminateTask();
                return;
            }

            chipsUserOrderExt.setNewGroupCode(newGroupCode);

            Map<String, Object> message = new HashMap<>();
            message.put("C", newGroupCode);
            message.put("N", 1);
            groupToSuccessProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
        } else {
            processUserGroupCourse(context.getUserOrder());

            chipsGroupShoppingPersistence.updateNumber(chipsUserOrderExt.getGroupShoppingCode(), 0, 1);

            chipsUserOrderExt.setStatus(ChipsUserOrderExt.OrderStatus.PAYED);
            chipsUserOrderExt.setUpdateDate(new Date());
            chipsUserOrderExtDao.upsert(chipsUserOrderExt);
            context.terminateTask();
        }
    }

    private void processUserGroupCourse(UserOrder order) {
        Long userId = order.getUserId();
        List<UserOrderProductRef> userOrderProductRefs = userOrderLoaderClient.loadOrderProducts(userId, order.getId());
        if (CollectionUtils.isEmpty(userOrderProductRefs)) {
            List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductId(order.getProductId());
            if (CollectionUtils.isEmpty(itemList)) {
                return;
            }
            Map<String, UserActivatedProduct> userActiveMap = chipsUserService.getUserActiveProduct(userId);
            for(OrderProductItem item : itemList) {
                UserActivatedProduct userActivatedProduct = userActiveMap.get(item.getId());
                if (userActivatedProduct == null) {
                    continue;
                }
                ChipsUserCourse userCourse = ChipsUserCourse.initNewCourse(userId, order.getId(), order.getProductId(), item.getId(), userActivatedProduct.getServiceStartTime(), userActivatedProduct.getServiceEndTime());
                userCourse.setActive(false);
                chipsUserCoursePersistence.insertOrUpdate(userCourse);
            }
        } else {
            Map<String, UserActivatedProduct> userActiveMap = chipsUserService.getUserActiveProduct(userId);
            for(UserOrderProductRef userOrderProductRef : userOrderProductRefs) {
                String productId = userOrderProductRef.getProductId();
                List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductId(productId);
                if (CollectionUtils.isEmpty(itemList)) {
                    continue;
                }

                for(OrderProductItem item : itemList) {
                    UserActivatedProduct userActivatedProduct = userActiveMap.get(item.getId());
                    if (userActivatedProduct == null) {
                        continue;
                    }
                    ChipsUserCourse userCourse = ChipsUserCourse.initNewCourse(userId, order.getId(), productId, item.getId(), userActivatedProduct.getServiceStartTime(), userActivatedProduct.getServiceEndTime());
                    userCourse.setActive(false);
                    chipsUserCoursePersistence.insertOrUpdate(userCourse);
                }
            }
        }
    }
}

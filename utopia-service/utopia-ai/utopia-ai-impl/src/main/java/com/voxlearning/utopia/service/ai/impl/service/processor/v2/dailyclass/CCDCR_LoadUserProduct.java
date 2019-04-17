package com.voxlearning.utopia.service.ai.impl.service.processor.v2.dailyclass;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishContentService;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonBookRef;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.ai.entity.ChipsUserCourse;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsContentDailyClassContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class CCDCR_LoadUserProduct extends AbstractAiSupport implements IAITask<ChipsContentDailyClassContext> {

    @Inject
    private ChipsEnglishContentService chipsEnglishContentService;

    @Override
    public void execute(ChipsContentDailyClassContext context) {
        List<ChipsUserCourse> chipsUserCourses = chipsUserService.loadUserEffectiveCourse(context.getUser().getId());
        if (CollectionUtils.isEmpty(chipsUserCourses)) {
            initNotBuyContext(context);
            return;
        }

        Set<String> itemIds = chipsUserCourses.stream().map(ChipsUserCourse::getProductItemId).collect(Collectors.toSet());
        Map<String, OrderProductItem> itemMap = userOrderLoaderClient.loadOrderProductItems(itemIds);
        if (MapUtils.isEmpty(itemMap)) {
            initNotBuyContext(context);
            return;
        }

        Set<String> productIds = chipsUserCourses.stream().map(ChipsUserCourse::getProductId).collect(Collectors.toSet());
        AIUserLessonBookRef aiUserLessonBookRef = aiUserLessonBookSupport.fetchUserCurrentBook(context.getUser().getId());
        if (aiUserLessonBookRef == null || !productIds.contains(aiUserLessonBookRef.getProductId())) {
            Map<String, List<OrderProductItem>> orderProductItemMap = new HashMap<>();
            chipsUserCourses.stream().collect(Collectors.groupingBy(ChipsUserCourse::getProductId)).forEach((k, v) -> {
                List<OrderProductItem> itemList = new ArrayList<>();
                v.stream().map(ChipsUserCourse::getProductItemId).forEach(e -> itemList.add(itemMap.get(e)));
                orderProductItemMap.put(k, itemList);
            });
            aiUserLessonBookRef = aiUserLessonBookSupport.initNewUserBookV2(productIds, orderProductItemMap, context.getUser().getId());
        }

        OrderProduct product = userOrderLoaderClient.loadOrderProductById(aiUserLessonBookRef.getProductId());
        if (product == null) {
            initNotBuyContext(context);
            return;
        }

        ChipsEnglishProductTimetable timetable = chipsEnglishProductTimetableDao.load(aiUserLessonBookRef.getProductId());
        Date beginDate = Optional.ofNullable(timetable).map(ChipsEnglishProductTimetable::getBeginDate).orElse(new Date());
        Date endDate = Optional.ofNullable(timetable).map(ChipsEnglishProductTimetable::getEndDate).orElse(new Date());
        context.setBeginDate(beginDate);
        context.setEndDate(endDate);
        context.setBookRef(aiUserLessonBookRef);
        context.setClassName(product.getName());
        context.setTimetable(timetable);
        Set<String> bookIds = itemMap.values().stream().map(OrderProductItem::getAppItemId).collect(Collectors.toSet());
        context.getUserBoughtBooks().addAll(bookIds);
        context.setMapUrl(chipsContentService.fetchBookMapUrl(aiUserLessonBookRef.getBookId()));
        context.getExtMap().put("added",getUserRecipientinfo(context.getUser().getId(),aiUserLessonBookRef.getProductId()));
    }

    private void initNotBuyContext(ChipsContentDailyClassContext context) {
        context.getExtMap().put("data", "401");
        context.getExtMap().put("message", "无购买");
        context.getExtMap().put("redirect", ProductConfig.getMainSiteBaseUrl() + chipsContentService.shortProductPath(context.getUser().getId()));
        context.terminateTask();
    }


    /**
     * Return 0=> all not add,1 => only recipient,2 => only wx,3 => all
     * @param userId
     */
    private int getUserRecipientinfo(long userId,String productId) {
        int mod=0b00;

        if(chipsEnglishContentService.existUserWxInfo(userId,productId)){
            mod=mod | 0b10;
        }
        if(chipsEnglishContentService.existUserRecipientInfo(userId,productId)){
            mod=mod | 0b01;
        }
        return mod;
    }

}

package com.voxlearning.utopia.service.ai.impl.service.processor.drawingtask;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.ai.cache.manager.UserPageVisitCacheManager;
import com.voxlearning.utopia.service.ai.data.DrawingTabConfig;
import com.voxlearning.utopia.service.ai.entity.ChipsUserCourse;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsDrawingTaskLoadContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.ai.impl.support.ConstantSupport;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class CDTL_LoadLabel extends AbstractAiSupport implements IAITask<ChipsDrawingTaskLoadContext> {

    @Inject
    private UserPageVisitCacheManager userPageVisitCacheManager;

    private static String SHORT_LABLE = "short";

    @Override
    public void execute(ChipsDrawingTaskLoadContext context) {
        userPageVisitCacheManager.addRecord(context.getUserId(), ConstantSupport.DRAWING_TASK_FIRST_PAGE_CACHE_KEY, 90);

        List<DrawingTabConfig> cfglist = chipsContentService.loadUserDrawingTab(context.getUserId());
        if (CollectionUtils.isEmpty(cfglist)) {
            context.terminateTask();
            return;
        }

        List<Map<String, String>> labelList = new ArrayList<>();
        cfglist.forEach(cfg -> {
            Map<String, String> map = new HashMap<>();
            map.put("code", cfg.getLabelCode());
            map.put("name", cfg.getLabelName());
            labelList.add(map);
        });
        context.setLabelList(labelList);

        Set<String> userBooks = Optional.ofNullable(chipsUserService.loadUserEffectiveCourse(context.getUserId()))
                .map(list -> list.stream().map(ChipsUserCourse::getProductItemId).collect(Collectors.toSet()))
                .filter(CollectionUtils::isNotEmpty)
                .map(items -> userOrderLoaderClient.loadOrderProductItems(items))
                .filter(MapUtils::isNotEmpty)
                .map(itemsMap -> itemsMap.values().stream().map(OrderProductItem::getAppItemId).collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
        List<String> bookList = cfglist.stream()
                .filter(e -> StringUtils.isBlank(context.getLabelCode()) || e.getLabelCode().equals(context.getLabelCode()))
                .findFirst()
                .map(cfg -> {
                    if (cfg.getLabelCode().equals(SHORT_LABLE)) {
                        return cfg.getBooks().stream().filter(e -> userBooks.contains(e)).collect(Collectors.toList());
                    }
                    return cfg.getBooks();
                })
                .orElse(Collections.emptyList());
        context.setBookList(bookList);
    }
}

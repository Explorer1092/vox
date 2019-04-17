package com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.filterList;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.VendorAppFilterContext;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 按照给的排序阀值排序
 *
 * @author peng.zhang.a
 * @since 16-10-12
 */
@Named
public class Filter_SortVendorApp extends FilterBase {
    @Override
    public void execute(VendorAppFilterContext context) {

        List<VendorApps> collect = CollectionUtils.isEmpty(context.getResultVendorApps())
                ? Collections.emptyList()
                : context.getResultVendorApps()
                .stream()
                .sorted(((o1, o2) -> Integer.compare(o2.getRank(), o1.getRank())))
                .collect(Collectors.toList());

        // 新逻辑begin xuesong
//        VendorApps tempApps = vendorLoaderClient.loadVendor(OrderProductServiceType.WrongTopic.name());
//        if (CollectionUtils.isNotEmpty(collect)) {
//            // 默认把错题本扔进去
//            List<String> tempList = collect.stream()
//                    .map(VendorApps::getAppKey)
//                    .filter(o -> StringUtils.equalsIgnoreCase(OrderProductServiceType.WrongTopic.name(), o))
//                    .collect(Collectors.toList());
//
//            // 避免重复的错题本
//            if (CollectionUtils.isEmpty(tempList)) {
//                collect.add(tempApps);
//            }
//        } else {
//            collect.add(tempApps);
//        }
        // 新逻辑end xuesong

        context.setResultVendorApps(collect);
    }
}

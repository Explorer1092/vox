package com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.filterList;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.VendorAppFilterContext;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 加载产品时过滤可用的产品，过滤条件：
 * 1　没有被disabled的app
 * 2　在当前环境下可以使用的app
 *
 * @author peng.zhang.a
 * @since 16-10-12
 */
@Named
public class Filter_LoadVendorAppsAndOrder extends FilterBase {
    @Override
    public void execute(VendorAppFilterContext context) {
        Map<String, VendorApps> vendorAppsMap = vendorLoaderClient.loadVendorAppsIncludeDisabled()
                .values().stream()
                .filter(t -> !SafeConverter.toBoolean(t.getDisabled(), true))
                .filter(t -> t.isVisible(RuntimeMode.current().getLevel()))
                .collect(Collectors.toMap(VendorApps::getAppKey, Function.identity(),
                        (u, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", u));
                        },
                        LinkedHashMap::new));

        context.setVendorApps(vendorAppsMap);
        context.setResultVendorApps(new ArrayList<>(vendorAppsMap.values()));

        List<String> appKeys = vendorAppsMap.values()
                .stream()
                .map(VendorApps::getAppKey)
                .collect(Collectors.toList());

        context.setAppPayStatus(userOrderLoaderClient.getUserAppPaidStatus(appKeys, context.getStudentDetail().getId(), false));

    }
}

package com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.filterList;

import com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.VendorAppFilterContext;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;

import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author peng.zhang.a
 * @since 16-10-12
 */
@Named
public class Filter_ByParentChain extends FilterBase {
    @Override
    public void execute(VendorAppFilterContext context) {
        List<VendorApps> collect = context.getResultVendorApps()
                .stream()
                .filter(t -> t.getWechatBuyFlag())
                .collect(Collectors.toList());
        context.setResultVendorApps(collect);
    }
}

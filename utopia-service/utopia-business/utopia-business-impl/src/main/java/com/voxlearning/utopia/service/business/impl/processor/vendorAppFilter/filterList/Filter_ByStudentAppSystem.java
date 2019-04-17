package com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.filterList;

import com.voxlearning.utopia.api.constant.AppSystemType;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.VendorAppFilterContext;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;

import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生app版本号过滤，低版本不显示app
 * @author peng.zhang.a
 * @since 16-10-12
 */
@Named
public class Filter_ByStudentAppSystem extends FilterBase {
    @Override
    public void execute(VendorAppFilterContext context) {
        List<VendorApps> collect = context.getResultVendorApps()
                .stream()
                .filter(t -> {
                    if (context.getSourceType() == OperationSourceType.app
                            && AppSystemType.IOS == context.getAppSystemType()) {
                        return VersionUtil.compareVersion(context.getAppVersion(), t.getVersion()) >= 0;
                    } else if (context.getSourceType() == OperationSourceType.app
                            && AppSystemType.ANDROID == context.getAppSystemType()) {
                        return VersionUtil.compareVersion(context.getAppVersion(), t.getVersionAndroid()) >= 0;
                    }
                    return true;
                }).collect(Collectors.toList()
                );
        context.setResultVendorApps(collect);
    }
}

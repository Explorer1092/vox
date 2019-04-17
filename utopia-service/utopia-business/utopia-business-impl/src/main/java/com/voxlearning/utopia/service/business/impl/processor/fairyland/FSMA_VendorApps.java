package com.voxlearning.utopia.service.business.impl.processor.fairyland;

import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.service.region.api.buffer.RaikouRegionBufferDelegator;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.AppSystemType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.business.impl.processor.AbstractExecuteTask;
import com.voxlearning.utopia.service.region.util.RegionGrayUtils;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.client.VendorAppsServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.region.api.constant.RegionConstants.TAG_GRAY_REGION_SUFFIX;

/**
 * 1. disabled，offline，runtime mode
 * 2. 年级
 * 3. 客户端版本号
 * 4. 产品灰度区域
 *
 * @author Ruib
 * @since 2019/1/3
 */
@Named
public class FSMA_VendorApps extends AbstractExecuteTask<FetchStudentAppContext> {

    @Inject private RaikouSystem raikouSystem;
    @Inject private VendorAppsServiceClient vasc;

    @Override
    public void execute(FetchStudentAppContext context) {
        RaikouRegionBufferDelegator buffer = new RaikouRegionBufferDelegator(raikouSystem.getRegionBuffer());
        int code = context.getStudent().getStudentSchoolRegionCode();

        List<VendorApps> vas = vasc.getVendorAppsBuffer().loadVendorAppsList()
                .stream()
                .filter(va -> va.isVisible(RuntimeMode.current().getLevel()))
                .filter(va -> va.validateClazzLevel(context.getStudent().getClazzLevel().getLevel()))
                .filter(va -> {
                    if (AppSystemType.IOS == context.getAst()) {
                        return VersionUtil.compareVersion(context.getVersion(), va.getVersion()) >= 0;
                    } else if (AppSystemType.ANDROID == context.getAst()) {
                        return VersionUtil.compareVersion(context.getVersion(), va.getVersionAndroid()) >= 0;
                    }
                    return true;
                })
                .filter(va -> RegionGrayUtils.checkRegionGrayStatus(code, va.getAppKey() + TAG_GRAY_REGION_SUFFIX, buffer))
                .collect(Collectors.toList());

        context.setVas(vas);
        context.setVam(vas.stream().collect(Collectors.toMap(VendorApps::getAppKey, Function.identity(),
                (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                },
                LinkedHashMap::new)));
    }
}

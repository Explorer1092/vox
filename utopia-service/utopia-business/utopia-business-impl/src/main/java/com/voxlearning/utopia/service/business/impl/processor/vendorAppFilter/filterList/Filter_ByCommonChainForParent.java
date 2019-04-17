package com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.filterList;

import com.voxlearning.raikou.service.region.api.buffer.RaikouRegionBufferDelegator;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.VendorAppFilterContext;
import com.voxlearning.utopia.service.region.api.constant.RegionConstants;
import com.voxlearning.utopia.service.region.util.RegionGrayUtils;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;

/**
 * 公共过滤方法：
 * １　有传入的使用平台参数，并且app可以在此平台上使用
 * ２　是否在应用灰度地区或者用户有购买的订单
 * ３　符合应用的使用年级并且有购买的订单
 * ４　洛亚走美无订单则不显示
 *
 * @author peng.zhang.a
 * @since 16-10-17
 */
@Named
public class Filter_ByCommonChainForParent extends FilterBase {

    @Inject private RaikouSystem raikouSystem;

    @Override
    public void execute(VendorAppFilterContext context) {
        List<VendorApps> collect = context.getResultVendorApps()
                .stream()
                .filter(t -> context.getSourceType() == null || t.matchPlaySources(context.getSourceType()))
                .filter(t -> checkGrayTag(context.getStudentDetail(), t.getAppKey())
                        || context.hasPaidBefore(t.getAppKey()))
                .filter(t -> {
                    Integer clazzLevel = context.getStudentDetail().getClazzLevelAsInteger();
                    if (clazzLevel == null) {
                        if (isAfenti(t.getAppKey())) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                    return t.validateClazzLevel(context.getStudentDetail().getClazzLevel().getLevel())
                            || context.hasPaidBefore(t.getAppKey());
                })
                .filter(t -> {
                    //错题精讲 只有当前有效期内的用户才可以看到入口
                    if (FeeCourse.isEqual(t.getAppKey())) {
                        if (context.getAppPayStatus() == null
                                || context.getAppPayStatus().get(t.getAppKey()) == null
                                || !context.getAppPayStatus().get(t.getAppKey()).isActive()) {
                            return false;
                        }
                    }

                    // 如果是洛亚传说或者三国 走美 宠物，如果用户没有购买过（包括周期类和道具类），直接就不显示了
                    if (A17ZYSPG.isEqual(t.getAppKey())
                            || SanguoDmz.isEqual(t.getAppKey())
                            || TravelAmerica.isEqual(t.getAppKey())
                            || PetsWar.isEqual(t.getAppKey())) {
                        if (context.getAppPayStatus() == null
                                || context.getAppPayStatus().get(t.getAppKey()) == null
                                || context.getAppPayStatus().get(t.getAppKey()).unpaid()) {
                            return false;
                        }
                    }
                    return true;

                })
                .collect(Collectors.toList());
        context.setResultVendorApps(collect);
    }


    /**
     * 产品灰度判断
     */
    private boolean checkGrayTag(StudentDetail studentDetail, String appKey) {
        if (studentDetail == null) return false;
        if (isAfenti(appKey)) {
            return true;
        }
        Integer regionCodes = studentDetail.getStudentSchoolRegionCode();
        String grayTagName = appKey + RegionConstants.TAG_GRAY_REGION_SUFFIX;
        RaikouRegionBufferDelegator buffer = new RaikouRegionBufferDelegator(raikouSystem.getRegionBuffer());
        return RegionGrayUtils.checkRegionGrayStatus(regionCodes, grayTagName, buffer);
    }
}

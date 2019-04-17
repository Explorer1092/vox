package com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.api.constant.AppSystemType;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.business.api.context.AbstractContext;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;

/**
 * @author peng.zhang.a
 * @since 16-10-12
 */
@Getter
@Setter
@NoArgsConstructor
public class VendorAppFilterContext extends AbstractContext<VendorAppFilterContext> {

    private static final long serialVersionUID = 4133944938199567375L;
    //in
    private StudentDetail studentDetail;
    private OperationSourceType sourceType;
    private User parent;
    private String appVersion;
    private AppSystemType appSystemType;


    //mid
    private Map<String, VendorApps> vendorApps;
    private Map<String, AppPayMapper> appPayStatus;
    private Set<String> studentBlackUsers;
    private Set<String> parentBlackUsers;
    private Boolean inWhiteList;


    //out
    private List<VendorApps> resultVendorApps;

    public static VendorAppFilterContext newInstance(StudentDetail studentDetail,
                                                     User parent,
                                                     OperationSourceType sourceType,
                                                     String appVersion,
                                                     AppSystemType systemType) {
        VendorAppFilterContext vendorAppFilterContext = new VendorAppFilterContext();
        vendorAppFilterContext.setSourceType(sourceType);
        vendorAppFilterContext.setStudentDetail(studentDetail);
        vendorAppFilterContext.setParent(parent);
        vendorAppFilterContext.setAppVersion(appVersion);
        vendorAppFilterContext.setAppSystemType(systemType);
        return vendorAppFilterContext;
    }

    public void setResultVendorApps(List<VendorApps> list) {
        this.resultVendorApps = list;
        if (CollectionUtils.isEmpty(list)) {
            this.terminateTask();
        }
    }

    public boolean hasPaidBefore(String appKey) {
        boolean result = appPayStatus != null && appPayStatus.containsKey(appKey)
                && appPayStatus.get(appKey).hasPaid();
        switch (OrderProductServiceType.safeParse(appKey)) {
            case AfentiExam:
                return result || hasPaidBefore(AfentiExamImproved.name());
            case AfentiMath:
                return result || hasPaidBefore(AfentiMathImproved.name());
            case AfentiChinese:
                return result || hasPaidBefore(AfentiChineseImproved.name());
            default:
                return result;
        }
        
    }

    //付过费但已过期
    public boolean hasPaidAndExpired(String appKey) {
        return hasPaidBefore(appKey) &&
                appPayStatus != null &&
                appPayStatus.containsKey(appKey) &&
                hasExpired(appKey);
    }

    public boolean hasExpired(String appKey) {
        boolean result = appPayStatus != null && appPayStatus.containsKey(appKey)
                && !appPayStatus.get(appKey).isActive();
        switch (OrderProductServiceType.safeParse(appKey)) {
            case AfentiExam:
                return result && hasExpired(AfentiExamImproved.name());
            case AfentiMath:
                return result && hasExpired(AfentiMathImproved.name());
            case AfentiChinese:
                return result && hasExpired(AfentiChineseImproved.name());
            default:
                return result;
        }
    }
}
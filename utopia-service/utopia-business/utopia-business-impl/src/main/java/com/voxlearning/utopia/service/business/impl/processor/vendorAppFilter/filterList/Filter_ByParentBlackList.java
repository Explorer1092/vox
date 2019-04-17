package com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.filterList;

import com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.VendorAppFilterContext;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 家长端黑名单过滤
 * @author peng.zhang.a
 * @since 16-10-12
 */
@Named
public class Filter_ByParentBlackList extends FilterBase {
    @Override
    public void execute(VendorAppFilterContext context) {

        if (context.getParent() == null || context.getStudentDetail() == null) {
            context.setResultVendorApps(Collections.emptyList());
            context.terminateTask();
        }
        //个人黑名单直接返回为空
        if (context.getParentBlackUsers().contains(String.valueOf(context.getParent().getId()))) {
            context.setResultVendorApps(Collections.emptyList());
            context.terminateTask();
        }

        //灰度黑名单
        boolean isBlack = userBlacklistServiceClient.isInBlackListByParent(context.getParent(), context.getStudentDetail());
        //1：过滤掉没有订单并且在黑名单中的用户
        //2：app设置灰度，显示在灰度中的app
        List<VendorApps> collect = context.getResultVendorApps()
                .stream()
                .filter(t -> !isBlack || context.hasPaidBefore(t.getAppKey()))
                .collect(Collectors.toList());

        context.setResultVendorApps(collect);
    }
}

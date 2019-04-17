package com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.filterList;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.VendorAppFilterContext;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fugui.chang
 * @since 2018/11/2.
 * 过滤掉 付过费 且 已过期 且retainApps不包含的
 */
@Named
public class Filter_PaidAndExpired extends FilterBase {

    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;
    private static final List<String> retainApps = Arrays.asList(
            "AfentiExamVideo",
            "AfentiMathVideo",
            "AfentiChineseVideo",
            "AfentiExam",
            "AfentiMath",
            "AfentiChinese",
            "AfentiExamImproved",
            "AfentiMathImproved",
            "AfentiChineseImproved",
            "ListenWorld",
            "WordBuilder",
            "ChinesePilot",
            "MathGarden",
            "WrongTopic",
            "ELevelReading",
            "CLevelReading");

    @Override
    public void execute(VendorAppFilterContext context) {

        //如果配置true,则不走这个过滤逻辑
        try {
            String flag = commonConfigServiceClient.getCommonConfigBuffer()
                    .loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_STUDENT.name(), "app_filter_not_used_flag");
            if (SafeConverter.toBoolean(flag)) {
                return;
            }
        } catch (Exception ignore) {
        }

        List<VendorApps> result = context.getResultVendorApps()
                .stream()
                .filter(t -> !(context.hasPaidAndExpired(t.getAppKey())))
                .collect(Collectors.toList());


        context.setResultVendorApps(result);
    }
}

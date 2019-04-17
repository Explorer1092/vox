package com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.filterList;

import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.VendorAppFilterContext;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.wonderland.api.entity.WonderlandTimesCard;
import com.voxlearning.utopia.service.wonderland.client.WonderlandLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;

/**
 * 学生黑名单过滤
 *
 * @author peng.zhang.a
 * @since 16-10-12
 */
@Named
public class Filter_ByStudentBlackList extends FilterBase {
    @Inject private WonderlandLoaderClient wonderlandLoaderClient;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    @Override
    public void execute(VendorAppFilterContext context) {

        //个人黑名单直接返回为空
        if (context.getStudentBlackUsers().contains(String.valueOf(context.getStudentDetail().getId()))) {
            context.setResultVendorApps(Collections.emptyList());
            context.terminateTask();
        }

        //是否黑名单用户，针对学校于地区立即生效
        boolean isBlack;

        // 紧急开关true，默认全中地区黑名单
        boolean emergency_all_close_true = SafeConverter.toBoolean(
                commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "EMERGENCY_ALL_TRUE")
                , true
        );

        if (emergency_all_close_true) {
            isBlack = true;
        } else {
            isBlack = userBlacklistServiceClient.isInBlackListByStudent(Collections.singletonList(context.getStudentDetail()))
                    .getOrDefault(context.getStudentDetail().getId(), false);
        }

        //错题宝单独需要处理,购买不同学科的只需要显示一个Feature #48431
        //过滤app规则
        //1：保留免费的app产品
        //2：学生在黑名单中，有支付并且在使用中的app
        //3：阿分题视频，开通任何一学科，状态就算开通
        //4：小鹰学堂，ValueAddedLiveTimesCard（小鹰学堂听课卡，小鹰国学堂体验卡，小鹰国学堂精品课vip）其中一种开通都显示

        Set<String> afentiVedios = new HashSet<>(Arrays.asList(AfentiExamVideo.name(), AfentiMathVideo.name(), AfentiChineseVideo.name()));
        boolean afentiVedioPaid = afentiVedios.stream()
                .filter(context::hasPaidBefore)
                .findFirst()
                .orElse(null) != null;
        boolean finalIsBlack = isBlack;
        List<VendorApps> collect = context.getResultVendorApps().stream()
                .filter(t -> !finalIsBlack
                        || context.hasPaidBefore(t.getAppKey())
                        || afentiVedios.contains(t.getAppKey())
                        || ValueAddedLiveTimesCard.name().equals(t.getAppKey())
                )// 小U学堂的特殊处理
                .collect(Collectors.toList());

        if (isBlack && !afentiVedioPaid) {
            collect = collect.stream().filter(p -> !afentiVedios.contains(p.getAppKey())).collect(Collectors.toList());
        }

        //购买听课卡，体验卷的用户　黑名单要显示出小鹰国学堂
        if (isBlack && !displayEagletSinologyClassRoom(context.getStudentDetail().getId())) {
            collect = collect.stream().filter(p -> !ValueAddedLiveTimesCard.name().equals(p.getAppKey())).collect(Collectors.toList());
        }
        context.setResultVendorApps(collect);
    }

    private boolean displayEagletSinologyClassRoom(Long studentId) {
        //获取听课卡的数量
        AlpsFuture<WonderlandTimesCard> wonderlandTimesCardAlps = wonderlandLoaderClient.
                getWonderlandLoader().loadWonderlandTimesCard(studentId, OrderProductServiceType.ValueAddedLiveTimesCard.name());

        //获取国学体验券卡的数量
        AlpsFuture<WonderlandTimesCard> wonderlandSinologyCardAlps = wonderlandLoaderClient.getWonderlandLoader()
                .loadWonderlandTimesCard(studentId, OrderProductServiceType.EagletSinologyExperienceCard.name());
        WonderlandTimesCard wonderlandTimesCard = wonderlandTimesCardAlps.getUninterruptibly();
        WonderlandTimesCard wonderlandSinologyCard = wonderlandSinologyCardAlps.getUninterruptibly();

        long timesCard = wonderlandTimesCard == null || wonderlandTimesCard.getTimes() == null ? 0 : wonderlandTimesCard.getTimes();
        long sinologyCardCount = wonderlandSinologyCard == null || wonderlandSinologyCard.getTimes() == null ? 0 : wonderlandSinologyCard.getTimes();


        // 获取学生购买过的产品
        Set<OrderProductServiceType> orderProductServiceTypes = userOrderLoaderClient.loadUserActivatedProductList(studentId)
                .stream()
                .filter(p -> p.getServiceEndTime() != null && p.getServiceEndTime().getTime() > System.currentTimeMillis())
                .map(o -> OrderProductServiceType.safeParse(o.getProductServiceType()))
                .collect(Collectors.toSet());
        // 是否开通国学精品课程
        boolean isSinologyVip = orderProductServiceTypes.contains(OrderProductServiceType.EagletSinologyClassRoom);

        return (timesCard + sinologyCardCount) > 0 || isSinologyVip;
    }


}

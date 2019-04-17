package com.voxlearning.utopia.service.reward.impl.internal;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.privilege.client.BlackWhiteListManagerClient;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;
import com.voxlearning.utopia.service.reward.entity.RewardSku;
import com.voxlearning.utopia.service.reward.impl.loader.RewardLoaderImpl;
import com.voxlearning.utopia.service.reward.impl.service.RewardServiceImpl;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserAggregationLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Created by alex on 2018/9/26.
 */
@Named
@Slf4j
public class InternalRewardOrderService {
    @Inject
    private UserAggregationLoaderClient userAggregationLoaderClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    BlackWhiteListManagerClient blackWhiteListManagerClient;
    @Inject
    private RewardLoaderImpl rewardLoader;
    @Inject
    private RewardServiceImpl rewardServiceImpl;
    @ImportService(interfaceClass = CRMRewardService.class)
    private CRMRewardService crmRewardService;

    // 抓一抓创建订单处理
    // 实物的时候只看库存
    public MapMessage createRewardOrder(User user, long productId, boolean validateShippingAddress, RewardOrder.Source source) {
        int quantity = 1;

        RewardProduct rewardProduct = rewardLoader.getRewardProductBuffer().loadRewardProductMap().get(productId);
        if (rewardProduct == null) {
            log.error("Reward center prize claw config error, product {} is not found", productId);
            return MapMessage.errorMessage("未找到对应的奖品");
        }

        // 抓一抓, 如果商品是实物需要验证库存
        RewardSku sku = null;
        if (rewardProduct.isShiwu()) {
            List<RewardSku> skuList = crmRewardService.$findRewardSkusByProductId(productId);
            if (CollectionUtils.isEmpty(skuList)) {
                log.warn("Reward center prize claw item {} is out of sku", productId);
                return MapMessage.errorMessage("奖品未配置库存");
            }

            sku = skuList.stream().filter(p -> p.getInventorySellable() >= quantity).findAny().orElse(null);
            if (sku == null) {
                return MapMessage.errorMessage("奖品数量不足！");
            }

            if (validateShippingAddress) {
                // 去除毕业班学生
                StudentDetail detail = studentLoaderClient.loadStudentDetail(user.getId());
                if (detail == null || rewardLoader.isGraduate(detail)) {
                    return MapMessage.errorMessage("毕业班不能兑换");
                }

                TeacherDetail teacherDetail = userAggregationLoaderClient.loadStudentTeacherForRewardSending(user.getId());
                if (teacherDetail == null) {
                    return MapMessage.errorMessage("未找到收货老师");
                }
            }
        }

        return rewardServiceImpl.createLagacyOrder(user, rewardProduct, sku, quantity, source);
    }
}

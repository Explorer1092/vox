package com.voxlearning.utopia.service.campaign.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.DPCampaignService;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.impl.lottery.AbstractCampaignWrapper;
import com.voxlearning.utopia.service.campaign.impl.lottery.CampaignWrapperFactory;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Xiaochao.Wei
 * @since 2018/3/2
 */
@Named
@Service(interfaceClass = DPCampaignService.class)
@ExposeService(interfaceClass = DPCampaignService.class)
public class DPCampaignServiceImpl implements DPCampaignService {
    @Inject
    private CampaignWrapperFactory campaignWrapperFactory;

    @Override
    public MapMessage addArrangeHomeworkLotteryChance(Long userId, Integer delta) {
        if (userId == null || delta == null) {
            return MapMessage.errorMessage();
        }
        AbstractCampaignWrapper wrapper = campaignWrapperFactory.get(CampaignType.JUNIOR_ARRANGE_HOMEWORK_LOTTERY);
        if (wrapper == null) {
            return MapMessage.errorMessage();
        }

        Long result = wrapper.addLotteryChance(userId, delta);
        return MapMessage.successMessage().add("result", result);
    }
}

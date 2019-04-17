package com.voxlearning.utopia.service.business.impl.listener.handler;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.business.BusinessEvent;
import com.voxlearning.utopia.business.BusinessEventType;
import com.voxlearning.utopia.service.business.impl.listener.BusinessEventHandler;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.client.CampaignServiceClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Summer on 2017/3/30.
 */
@Named
public class TeacherScholarShipAddLotteryChance implements BusinessEventHandler {
    @Inject private CampaignServiceClient campaignServiceClient;

    @Override
    public BusinessEventType getEventType() {
        return BusinessEventType.TEACHER_SCHOLARSHIP_ADD_LOTTERY_CHANCE;
    }

    @Override
    public void handle(BusinessEvent event) {
//        if (event == null || event.getAttributes() == null) {
//            return;
//        }
//        long teacherId = SafeConverter.toLong(event.getAttributes().get("teacherId"));
//        if (teacherId == 0) {
//            return;
//        }
//        int addChance = SafeConverter.toInt(event.getAttributes().get("addChance"));
//        if (addChance == 0) {
//            return;
//        }
//        campaignServiceClient.getCampaignService().addLotteryFreeChance(CampaignType.TEACHER_SCHOLARSHIP_GOLD_LOTTERY, teacherId, addChance);
    }

}

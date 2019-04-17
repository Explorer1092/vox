/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.campaign.impl.loader;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.campaign.api.CampaignLoader;
import com.voxlearning.utopia.service.campaign.api.document.*;
import com.voxlearning.utopia.service.campaign.impl.dao.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Default {@link CampaignLoader} implementation.
 *
 * @author Xiaohai Zhang
 * @since Aug 5, 2016
 */
@Named
@ExposeServices({
        @ExposeService(interfaceClass = CampaignLoader.class,version = @ServiceVersion(version = "2017.12.15")),
        @ExposeService(interfaceClass = CampaignLoader.class,version = @ServiceVersion(version = "2018.05.24"))
})
public class CampaignLoaderImpl implements CampaignLoader {

    @Inject private CampaignAwardDao campaignAwardDao;
    @Inject private CampaignLotteryDao campaignLotteryDao;
    @Inject private CampaignLotteryHistoryDao campaignLotteryHistoryDao;
    @Inject private CampaignLotteryBigHistoryDao campaignLotteryBigHistoryDao;
    @Inject private CampaignLotteryFragmentHistoryDao campaignLotteryFragmentHistoryDao;
    @Inject private CampaignLotterySendHistoryDao campaignLotterySendHistoryDao;

    @Override
    public List<CampaignAward> findCampaignAwards(Integer campaignId, Long userId) {
        if (campaignId == null || userId == null) return Collections.emptyList();
        return campaignAwardDao.findCampaignAwards(campaignId, userId);
    }

    @Override
    public CampaignLottery findCampaignLotterie(Long id) {
        if (id == null) return null;
        return campaignLotteryDao.load(id);
    }

    @Override
    public List<CampaignLottery> findCampaignLotteries(Integer campaignId) {
        if (campaignId == null) return Collections.emptyList();
        return campaignLotteryDao.findByCampaignId(campaignId);
    }

    @Override
    public CampaignLottery findCampaignLottery(Integer campaignId, Integer awardId) {
        if (campaignId == null || awardId == null) return null;
        return campaignLotteryDao.findByCampaignId(campaignId).stream()
                .filter(p -> Objects.equals(awardId, p.getAwardId())).findFirst().orElse(null);
    }

    @Override
    public List<CampaignLotteryHistory> findCampaignLotteryHistories(Integer campaignId, Long userId) {
        if (campaignId == null || userId == null) return Collections.emptyList();
        return campaignLotteryHistoryDao.findCampaignLotteryHistories(campaignId, userId);
    }

    @Override
    public List<CampaignLotteryBigHistory> findCampaignLotteryBigHistories(Integer campaignId) {
        if (campaignId == null) return Collections.emptyList();
        return campaignLotteryBigHistoryDao.loadByCampaignId(campaignId);
    }

    @Override
    public List<CampaignLotteryBigHistory> findCampaignLotteryBigHistories(Integer campaignId, Integer awardId) {
        if (campaignId == null || awardId == null) return Collections.emptyList();
        return campaignLotteryBigHistoryDao.loadByCampaignIdAndAwardId(campaignId, awardId);
    }

    @Override
    public List<CampaignLotteryBigHistory> findCampaignLotteryBigHistoriesUnderUser(Long userId) {
        return campaignLotteryBigHistoryDao.loadByUserId(userId);
    }

    @Override
    public List<CampaignLotterySendHistory> findCampaignLotterySendHistories(Integer campaignId, Long receiverId) {
        if (campaignId == null || receiverId == null) return Collections.emptyList();
        return campaignLotterySendHistoryDao.loadByCampaignIdAndReceiverId(campaignId, receiverId);
    }

    @Override
    public List<CampaignLotteryFragmentHistory> findCampaignLotteryFragmentHistories(Integer campaignId, Long userId) {
        if (campaignId == null || userId == null) return Collections.emptyList();
        List<CampaignLotteryFragmentHistory> list = new ArrayList<>();
        if (new Date().before(DateUtils.stringToDate("2016-09-26 23:59:59"))) {
            // 对历史数据做个兼容 查询4次活动的总和
            List<CampaignLotteryFragmentHistory> list41 = campaignLotteryFragmentHistoryDao.loadByCampaignIdAndUserId(41, userId);
            List<CampaignLotteryFragmentHistory> list42 = campaignLotteryFragmentHistoryDao.loadByCampaignIdAndUserId(42, userId);
            List<CampaignLotteryFragmentHistory> list43 = campaignLotteryFragmentHistoryDao.loadByCampaignIdAndUserId(43, userId);
            if (CollectionUtils.isNotEmpty(list41)) {
                list.addAll(list41);
            }
            if (CollectionUtils.isNotEmpty(list42)) {
                list.addAll(list42);
            }
            if (CollectionUtils.isNotEmpty(list43)) {
                list.addAll(list43);
            }
            Collections.sort(list, (o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime()));
        } else {
            list = campaignLotteryFragmentHistoryDao.loadByCampaignIdAndUserId(campaignId, userId);
        }
        return list;
    }

    @Override
    public List<CampaignLotteryBigHistory> loadAllCampaignLotteryBigHistorys() {
        return campaignLotteryBigHistoryDao.loadAllHistorys();
    }
}

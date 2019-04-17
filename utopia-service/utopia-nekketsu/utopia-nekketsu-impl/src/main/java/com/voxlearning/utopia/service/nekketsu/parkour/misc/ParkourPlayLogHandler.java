/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.nekketsu.parkour.misc;

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.nekketsu.impl.queue.ParkourQueueSender;
import com.voxlearning.utopia.service.nekketsu.parkour.dao.ParkourRegionRankDao;
import com.voxlearning.utopia.service.nekketsu.parkour.dao.PersonalStageRecordDao;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.ParkourPlayLog;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.ParkourRankDetail;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.ParkourRegionRank;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Parkour play log handler.
 * Copied from orinigal implementation.
 *
 * @author Sadi Wan
 * @author Xiaohai Zhang
 * @since Jan 6, 2015
 */
@Named
public class ParkourPlayLogHandler extends SpringContainerSupport {

    @Inject private PersonalStageRecordDao personalStageRecordDao;
    @Inject private ParkourRegionRankDao parkourRegionRankDao;
    @Inject private ParkourQueueSender parkourQueueSender;

    public void handle(final ParkourPlayLog log, final StudentDetail student) {
        if (log == null) {
            return;
        }
        AlpsThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    internalHandle(log, student);
                } catch (Exception ex) {
                    logger.warn("Failed to handle parkour play log", ex);
                }
            }
        });
    }

    private void internalHandle(ParkourPlayLog log, StudentDetail student) throws Exception {

        parkourQueueSender.saveParkourPlayLog(log);

        if ("AI".equals(log.getPlayType()) && log.isWinTrue()) {//打通关模式需要计算排行
            if (student != null) {
                LinkedHashMap<Integer, KeyValuePair<String, String>> rankRegionMap = ParkouMiscUtil.getStudentRankRegion(student);
                List<Integer> regionCodeLst = new ArrayList<>(rankRegionMap.keySet());
                //取全国 全省 全市排行
                Map<Integer, ParkourRegionRank> rankGet = parkourRegionRankDao.getsRegionRankBatch(regionCodeLst, log.getStageId());
                ParkourRankDetail meRankDetail = new ParkourRankDetail(log.getPlayerId(), log.getTimeCost(), log.getStar(), log.getPlayDateTime());
                List<ParkourRegionRank> updateRankList = new ArrayList<>();
                for (Integer regionCode : regionCodeLst) {
                    ParkourRegionRank rank = rankGet.get(regionCode);
                    if (null == rank) {
                        String id = regionCode + "_" + log.getStageId();
                        rank = new ParkourRegionRank();
                        rank.setStageId(log.getStageId());
                        rank.setRegionCode(regionCode);
                        rank.setId(id);
                        rank.setRankList(new ArrayList<>());
                    }
                    List<ParkourRankDetail> regionRank = rank.getRankList();

                    regionRank.add(meRankDetail);
                    if (regionRank.indexOf(meRankDetail) < regionRank.lastIndexOf(meRankDetail)) {//以前也在榜里
                        ParkourRankDetail preMeRank = regionRank.get(regionRank.indexOf(meRankDetail));
                        if (preMeRank.compareTo(meRankDetail) > 0) {//排行更新了
                            regionRank.remove(regionRank.indexOf(meRankDetail));
                            Collections.sort(regionRank);
                            updateRankList.add(rank);
                        }
                    } else {//以前不在榜里
                        Collections.sort(regionRank);
                        if (regionRank.size() > 10) {
                            regionRank.remove(regionRank.size() - 1);
                        }
                        if (regionRank.contains(meRankDetail)) {
                            updateRankList.add(rank);
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(updateRankList)) {
                    parkourRegionRankDao.inserts(updateRankList);
                }
            }
            personalStageRecordDao.appendRecentPlay(log.getStageId(), log.getPlayerId());
        }
    }
}

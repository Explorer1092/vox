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

package com.voxlearning.utopia.service.nekketsu.elf.api.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.nekketsu.elf.api.ElfLoader;
import com.voxlearning.utopia.service.nekketsu.elf.api.support.ElfApiSupport;
import com.voxlearning.utopia.service.nekketsu.elf.entity.*;
import com.voxlearning.utopia.service.nekketsu.elf.queue.InternalElfLogQueueSender;
import com.voxlearning.utopia.service.walker.elf.net.types.*;
import com.voxlearning.utopia.service.walker.elf.net.types.response.ShowNewFlag;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Created by Sadi.Wan on 2015/2/27.
 */
@Named
@Service(interfaceClass = ElfLoader.class)
@ExposeService(interfaceClass = ElfLoader.class)
public class ElfLoaderImpl extends ElfApiSupport implements ElfLoader {

    @Inject private InternalElfLogQueueSender elfLogQueueSender;

    @Override
    public MapMessage initInfo(final long userId) {
        MapMessage rtn = MapMessage.successMessage();
        ElfBubble bubble = elfBubbleDao.load(userId);
        if (null == bubble) {
            return MapMessage.errorMessage().setErrorCode("100001").setInfo("登录失败，服务器出错");
        }
        //装配气泡
        ShowNewFlag showNewFlag = new ShowNewFlag();
        showNewFlag.fillFrom(bubble);
        List<LevelPlantInfo> levelPlantInfos = buildMyLevelPlant(elfUserRecordDao.load(userId), null, null);
        for (LevelPlantInfo levelPlantInfo : levelPlantInfos) {
            for (PlantFloorInfo plantFloorInfo : levelPlantInfo.plantFloorInfoList) {
                for (PlantInfo plantInfo : plantFloorInfo.plantInfoList) {
                    if (plantInfo.composable) {
                        showNewFlag.showBookNew = true;
                        break;
                    }
                }

            }
        }
//        for (Boolean newPlant : bubble.getNewPlant().values()) {
//            if (newPlant) {
//                showNewFlag.showBookNew = true;
//                break;
//            }
//        }
        rtn.add("newFlag", showNewFlag);

        ElfUserRecord userRecord = elfUserRecordDao.load(userId);
        if (null == userRecord) {
            return MapMessage.errorMessage().setErrorCode("100001").setInfo("登录失败，服务器出错");
        }
        final boolean incDay;
        Calendar now = Calendar.getInstance();
        if (null == userRecord.getLatestLoginTime()) {//第一次登录
            incDay = true;
            userRecord = elfUserRecordDao.updateLoginTime(userId, incDay);
        } else {
            Calendar calLastLogin = Calendar.getInstance();
            calLastLogin.setTime(userRecord.getLatestLoginTime());
            incDay = (now.get(Calendar.DAY_OF_YEAR) != calLastLogin.get(Calendar.DAY_OF_YEAR)) || (now.get(Calendar.YEAR) != calLastLogin.get(Calendar.YEAR));
            if (incDay || now.getTimeInMillis() - calLastLogin.getTimeInMillis() > 60 * 1000) {//不同天登陆或距上次登录1分钟以上，才更新数据库
                userRecord = elfUserRecordDao.updateLoginTime(userId, incDay);
            }
            if (incDay && null != userRecord) {//登录天数增加了，需要判断成就
                ElfMyAchievementMap elfMyAchievementMap = elfAchvDao.load(userId);
                if (null != elfMyAchievementMap) {
                    final ElfMyAchievement elfMyAchievement = elfMyAchievementMap.getAchievementMap().get(ElfAchievementType.LOGIN_DAY);
                    if (!elfMyAchievement.isExchangable()) {
                        if (elfMyAchievement.getAchievementType().isStageExchangable(elfMyAchievement.getStage(), userRecord.getLoginDayCount())) {//登录天数新成就达成
                            ((ShowNewFlag) rtn.get("newFlag")).showAchvNew = true;
                            AlpsThreadPool.getInstance().submit(new Runnable() {
                                @Override
                                public void run() {
                                    elfAchvDao.setExchangable(userId, elfMyAchievement.getAchievementType(), elfMyAchievement.getStage());
                                    elfBubbleDao.updateByField(userId, "newAchv", true);
                                    elfLogQueueSender.saveElfPlayLog(new ElfPlayLog(null, userId, new Date(), ElfLogEnum.LOGIN_ACHV, new StringBuilder("达成累计登陆奖励").append(elfMyAchievement.getStage()).toString()));
                                }
                            });
                        }
                    }
                }
            }
        }

        if (incDay) {
            AlpsThreadPool.getInstance().submit(new Runnable() {
                @Override
                public void run() {
                    elfLogQueueSender.saveElfPlayLog(new ElfPlayLog(null, userId, new Date(), ElfLogEnum.FIRST_DAILY_LOGIN, new StringBuilder("当日首次登录").toString()));
                }
            });
        }
        if (null == userRecord) {
            return MapMessage.errorMessage().setErrorCode("100001").setInfo("登录失败，服务器出错");
        }

        List<PlantDefInfo> plantDefList = new ArrayList<>();
        for (ElfPlantDef plantDef : elfPlantDao.loadAll()) {
            PlantDefInfo plantDefInfo = new PlantDefInfo();
            plantDefInfo.name = plantDef.getName();
            plantDefInfo.star = plantDef.getStar();
            plantDefInfo.plantId = plantDef.getPlantId();
            if (null != plantDef.getMadeOf()) {
                for (Map.Entry<String, Integer> entry : plantDef.getMadeOf().entrySet()) {
                    PlantMadeOf plantMadeOf = new PlantMadeOf();
                    plantMadeOf.count = entry.getValue();
                    plantMadeOf.plantId = entry.getKey();
                    plantDefInfo.madeOfList.add(plantMadeOf);
                }
            }
            plantDefList.add(plantDefInfo);
        }
        rtn.add("plantDefList", plantDefList);
        return rtn;
    }

    @Override
    public MapMessage getLevelPlant(long userId) {
        try {
            return MapMessage.successMessage().add("levelPlantInfoList", buildMyLevelPlant(elfUserRecordDao.load(userId), null, null));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("getLevePlant failed,userId:{},Exception:{}", userId, e);
        }
        return MapMessage.errorMessage().setErrorCode("100015").setInfo("读取数据失败");
    }

    @Override
    public MapMessage loadLevelBookWithRecord(long userId, String levelId, boolean paidUser) {
        List<ElfBookDef> elfBookDefList = elfBookDao.findByLevelId(levelId);
        if (CollectionUtils.isEmpty(elfBookDefList)) {
            return MapMessage.errorMessage().setErrorCode("100014").setInfo("参数非法");
        }
        ElfUserRecord userRecord = elfUserRecordDao.load(userId);
        if (null == userRecord) {
            return MapMessage.errorMessage().setErrorCode("100015").setInfo("读取数据失败");
        }
        int counter = 0;
        int page = 0;
        List<BookBrief> bookBriefList = new ArrayList<>();
        for (ElfBookDef elfBookDef : elfBookDefList) {
            BookBrief bookBrief = new BookBrief();
            bookBrief.bookId = elfBookDef.getBookId();
            bookBrief.bookName = elfBookDef.getBookName();
            bookBrief.hasSun = userRecord.getBookRecordMap().containsKey(elfBookDef.getBookId()) && userRecord.getBookRecordMap().get(elfBookDef.getBookId()).isSunGained();

            bookBrief.open = paidUser ? true : counter < 3;
            bookBrief.cover = elfBookDef.getCover();
            if (paidUser && page < 1 && !bookBrief.hasSun) {
                page = counter / 3 + 1;
            }
            counter++;
            bookBriefList.add(bookBrief);
        }
        return MapMessage.successMessage().add("bookBriefList", bookBriefList).add("page", page);
    }

    @Override
    public ElfUserRecord loadUserRecord(long userId) {
        return elfUserRecordDao.load(userId);
    }

    @Override
    public ElfMyGiftList loadGiftList(long userId) {
        return elfMyGiftDao.load(userId);
    }

    @Override
    public MapMessage loadAchvForDisplay(final long userId) {
        AlpsThreadPool.getInstance().submit(new Runnable() {//去掉new气泡
            @Override
            public void run() {
                elfBubbleDao.updateByMap(userId, Collections.singletonMap("newAchv", false));
            }
        });
        return MapMessage.successMessage().add("achvInfoList", buildAchivementForDisplay(userId, null, null));
    }

    @Override
    public ElfMyAchievementMap loadAchv(long userId) {
        return elfAchvDao.load(userId);
    }

    @Override
    public ElfBubble loadBubble(long userId) {
        return elfBubbleDao.load(userId);
    }

}

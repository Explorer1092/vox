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
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.nekketsu.elf.api.ElfService;
import com.voxlearning.utopia.service.nekketsu.elf.api.support.ElfApiSupport;
import com.voxlearning.utopia.service.nekketsu.elf.constant.WalkerElfConstant;
import com.voxlearning.utopia.service.nekketsu.elf.entity.*;
import com.voxlearning.utopia.service.nekketsu.elf.queue.InternalElfLogQueueSender;
import com.voxlearning.utopia.service.walker.elf.net.types.LevelPlantInfo;
import com.voxlearning.utopia.service.walker.elf.net.types.PlantFloorInfo;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Sadi.Wan on 2015/2/28.
 */
@Named
@Service(interfaceClass = ElfService.class)
@ExposeService(interfaceClass = ElfService.class)
public class ElfServiceImpl extends ElfApiSupport implements ElfService {
    @Inject private InternalElfLogQueueSender elfLogQueueSender;

    @Override
    public MapMessage finishAnimate(final long userId) {
        AlpsThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                elfBubbleDao.updateByField(userId, "playAnimate", false);
            }
        });
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage unsetLevelTabNew(final long userId, final String levelId) {
        AlpsThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                elfBubbleDao.updateByField(userId, "newPlant." + levelId, false);
            }
        });
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage startReading(final long userId, final String bookId, final Set<String> paidLevels) {
        ElfUserRecord elfUserRecord = elfUserRecordDao.load(userId);
        if (null == elfUserRecord) {
            return MapMessage.errorMessage().setErrorCode("100002").setInfo("获取用户记录失败");
        }

        final ElfBookDef bookDef = elfBookDao.findByBookId(bookId);
        bookDef.getLevelId();
        if (null == bookDef) {
            return MapMessage.errorMessage().setErrorCode("100003").setInfo("读本不存在");
        }
        if (bookDef.getIndexOfLevel() > 3 && !paidLevels.contains(bookDef.getLevelId())) {
            return MapMessage.errorMessage().setErrorCode("100022").setInfo("小英雄，开通后才能继续拯救精灵王哦~");
        }
        if (!elfUserRecord.getBookRecordMap().containsKey(bookId) || !elfUserRecord.getBookRecordMap().get(bookId).isSunGained()) {//这本书还没读完过
            elfUserRecord = elfUserRecordDao.startFirstReading(userId, UserBookRecord.getDefault(bookId), new Date(System.currentTimeMillis() + bookDef.getDuration()));
            if (null == elfUserRecord) {
                return MapMessage.errorMessage().setErrorCode("100004").setInfo("不能开始阅读，数据操作失败");
            }
            AlpsThreadPool.getInstance().submit(new Runnable() {
                @Override
                public void run() {
                    elfLogQueueSender.saveElfPlayLog(new ElfPlayLog(null, userId, new Date(), ElfLogEnum.START_FIRST_READING, new StringBuilder("开始首次阅读").append(bookId).append(",预计").append(bookDef.getDuration()).append("毫秒后可完成阅读").toString()));
                }
            });
            return MapMessage.successMessage().add("hasRead", false).add("plantId", bookDef.getPlantId()).add("duration", String.valueOf(bookDef.getDuration()));
        } else {//读本已经读过
            elfUserRecord = elfUserRecordDao.startGainedReading(userId, bookId, new Date(System.currentTimeMillis() + bookDef.getDuration() / 2));
            if (null == elfUserRecord) {
                return MapMessage.errorMessage().setErrorCode("100004").setInfo("不能开始阅读，数据操作失败");
            }
            AlpsThreadPool.getInstance().submit(new Runnable() {
                @Override
                public void run() {
                    elfLogQueueSender.saveElfPlayLog(new ElfPlayLog(null, userId, new Date(), ElfLogEnum.START_GAINED_READING, new StringBuilder("开始非首次阅读").append(bookId).append(",预计").append(bookDef.getDuration()).append("毫秒后可完成阅读").toString()));
                }
            });
            return MapMessage.successMessage().add("hasRead", true).add("plantId", bookDef.getPlantId()).add("duration", String.valueOf(bookDef.getDuration()));
        }
    }

    @Override
    public MapMessage finishFirstReading(final long userId, ElfUserRecord elfUserRecord, String bookId, Date fnTime) {
        if (null == elfUserRecord) {
            return MapMessage.errorMessage().setErrorCode("100014").setInfo("参数非法");
        }
        if (!elfUserRecord.getReadingTimers().containsKey(bookId)) {
            return MapMessage.errorMessage().setErrorCode("100016").setInfo("参数错误，无法完成阅读");
        }
        final ElfBookDef elfBookDef = elfBookDao.findByBookId(bookId);
        ElfUserRecord modified = elfUserRecordDao.finishFirstReading(userId, bookId, elfBookDef.getPlantId(), fnTime);
        if (null == modified) {
            return MapMessage.errorMessage().setErrorCode("100016").setInfo("参数错误，无法完成阅读");
        }
        final ElfLevelDef levelDef = elfLevelDefDao.findByLevelId(elfBookDef.getLevelId());
        final ElfPlantDef elfPlantDef = elfPlantDao.findByPlantId(elfBookDef.getPlantId());
        final int integralPrize = WalkerElfConstant.savePlantIntegral.get(elfPlantDef.getStar() - 1);
        final ElfAchievementType levelAchievementType = ElfAchievementType.valueOf(levelDef.getId());
        final ElfMyAchievementMap elfMyAchievementMap = elfAchvDao.load(userId);
        boolean newLevelAchv = false;
        if (!elfMyAchievementMap.getAchievementMap().get(levelAchievementType).isExchangable()) {
            //当前成就可兑换了

            Set<String> starSet = new HashSet<>();

            List<LevelPlantInfo> levelPlantInfos = buildMyLevelPlant(modified, null, null);
            for (LevelPlantInfo levelPlantInfo : levelPlantInfos) {
                if (levelPlantInfo.levelId.equalsIgnoreCase(elfBookDef.getLevelId())) {
                    for (PlantFloorInfo plantFloorInfo : levelPlantInfo.plantFloorInfoList) {
                        if (plantFloorInfo.star == elfPlantDef.getStar()) {
                            starSet.addAll(plantFloorInfo.plantInfoList.stream().map(e -> e.plantId).collect(Collectors.toList()));
                        }
                    }
                }
            }
//            Map<String,ElfPlantDef> starMap = elfPlantDao.findByStar(elfPlantDef.getStar());
//            List<String> plantIdList = levelDef.getPlantStruct().get("STAR_" + elfPlantDef.getStar());
//            Set<String> levelSet = new HashSet<>(starMap.keySet());
//            if (levelAchievementType == ElfAchievementType.SAVE_PRINCE || levelAchievementType == ElfAchievementType.SAVE_QUEEN
//                    || levelAchievementType == ElfAchievementType.SAVE_KING)
//                levelSet.retainAll(plantIdList);
            if (levelAchievementType.isStageExchangable(elfPlantDef.getStar() - 1, modified.checkSavedPlant(starSet))) {
                newLevelAchv = true;
            }
        }

        //是否有新的可被拯救植物
        final Set<String> savableModified = getSavaOrComposablePlant(modified.getPlantCounter());
        savableModified.removeAll(getSavaOrComposablePlant(elfUserRecord.getPlantCounter()));
        final boolean newSavable = !savableModified.isEmpty();
        boolean newSun = false;
        ElfMyAchievement sunAchievementt = elfMyAchievementMap.getAchievementMap().get(ElfAchievementType.SUN_GET);
        if (!sunAchievementt.isExchangable()) {//本阶段太阳城就尚未达成，需要判断成就是否达成
            if (ElfAchievementType.SUN_GET.isStageExchangable(sunAchievementt.getStage(), modified.getSunCount())) {//太阳成就达成
                newSun = true;
            }
        }
        final boolean newAchvSave = newLevelAchv || newSun;
        final boolean newSunSave = newSun;
        final Map<ElfAchievementType, Integer> newAchvSetMap = new HashMap<>();
        if (newSunSave) {
            newAchvSetMap.put(ElfAchievementType.SUN_GET, sunAchievementt.getStage());
        }
        if (newLevelAchv) {
            newAchvSetMap.put(levelAchievementType, elfPlantDef.getStar() - 1);
        }
        AlpsThreadPool.getInstance().submit(new Runnable() {//异步写入礼物、成就及气泡
            @Override
            public void run() {
                elfMyGiftDao.load(userId);
                ElfMyGiftList modifiedGiftList = elfMyGiftDao.pushGift(userId, new ElfMyGift(userId, RandomUtils.nextObjectId(), ElfGiftType.INTEGRAL, integralPrize, new Date(), "拯救" + elfPlantDef.getName() + "后获得礼物"));
                Map<String, Boolean> bubbleUpdateMap = new HashMap<>();
                if (newAchvSave) {
                    elfAchvDao.batchSetExchangable(userId, newAchvSetMap);
                    bubbleUpdateMap.put("newAchv", newAchvSave);
                }
                bubbleUpdateMap.put("newPlant." + levelDef.getId(), newSavable);
                bubbleUpdateMap.put("newGift", true);
                elfBubbleDao.updateByMap(userId, bubbleUpdateMap);
                elfLogQueueSender.saveElfPlayLog(new ElfPlayLog(null, userId, new Date(), ElfLogEnum.FINISH_FIRST_READING, new StringBuilder("完成首次阅读").append(bookId).append(",获取礼物(学豆").append(integralPrize).append("个)").append(null == modifiedGiftList ? "失败" : "成功").append(",获得成就:").append(newAchvSetMap).append(",新增可拯救植物:").append(savableModified).toString()));
            }
        });
        return MapMessage.successMessage().add("showAchvNew", newAchvSave).add("showBookNew", newSavable).add("showGiftNew", true).add("plantSaved", true).add("pkGet", 0).add("integralGet", integralPrize);
    }

    @Override
    public MapMessage finishGainedReading(final long userId, ElfUserRecord elfUserRecord, String bookId, Date fnTime) {
        ElfUserRecord modified = elfUserRecordDao.finishGainedReading(userId, bookId, fnTime);
        if (null == modified) {
            return MapMessage.errorMessage().setErrorCode("100016").setInfo("参数错误，无法完成阅读");
        }
        final boolean sendPrize = RandomUtils.nextInt() % 3 == 0;//是否发奖励
        boolean sendPk = RandomUtils.nextInt() % 2 == 0;
        final ElfGiftType giftType = sendPk ? ElfGiftType.PK : ElfGiftType.INTEGRAL;
        final ElfBookDef elfBooKDef = elfBookDao.findByBookId(bookId);
        final int prizeCount = 1;

        AlpsThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                StringBuilder logSb = new StringBuilder("完成非首次阅读").append(elfBooKDef.getBookId());
                if (sendPrize) {
                    elfMyGiftDao.pushGift(userId, new ElfMyGift(userId, RandomUtils.nextObjectId(), giftType, prizeCount, new Date(), "听《" + elfBooKDef.getBookName() + "》获得礼物"));
                    logSb.append(",获取礼物").append(giftType).append(prizeCount).append("个");
                }
                elfLogQueueSender.saveElfPlayLog(new ElfPlayLog(null, userId, new Date(), ElfLogEnum.FINISH_GAINED_READING, logSb.toString()));
            }
        });

        return MapMessage.successMessage().add("showAchvNew", false).add("showBookNew", false).add("showGiftNew", sendPrize).add("plantSaved", false).add("pkGet", sendPrize && sendPk ? prizeCount : 0).add("integralGet", sendPrize && !sendPk ? prizeCount : 0);
    }

    @Override
    public MapMessage savePlant(final long userId, final String levelId, final String plantId) {
        MapMessage precheck = saveOrComposePrecheck(userId, levelId, plantId);
        if (!precheck.isSuccess()) {
            return precheck;
        }
        final ElfPlantDef plantDef = (ElfPlantDef) precheck.get("plantDef");
        ElfUserRecord elfUserRecord = (ElfUserRecord) precheck.get("elfUserRecord");
        if (elfUserRecord.getPlantCounter().containsKey(plantId)) {
            return MapMessage.errorMessage().setErrorCode("100008").setInfo("植物已被拯救过了");
        }
        ;
        final ElfUserRecord modified = elfUserRecordDao.insertSavePlant(userId, plantId, plantDef.getMadeOf());
        if (null == modified) {
            return MapMessage.errorMessage().setErrorCode("100009").setInfo("不能拯救植物，数据操作失败");
        }
        final int integralPrize = WalkerElfConstant.savePlantIntegral.get(plantDef.getStar() - 1);
        AlpsThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                ElfMyGiftList modifiedGiftList = elfMyGiftDao.pushGift(userId, new ElfMyGift(userId, RandomUtils.nextObjectId(), ElfGiftType.INTEGRAL, integralPrize, new Date(), "拯救" + plantDef.getName() + "后获得礼物"));
                elfLogQueueSender.saveElfPlayLog(new ElfPlayLog(null, userId, new Date(), ElfLogEnum.EXCHANGE_GIFT, new StringBuilder("拯救").append(plantId).append(",获取礼物(学豆").append(integralPrize).append("个)").append(null == modifiedGiftList ? "失败" : "成功").toString()));
            }
        });

        //拯救成功,算成就
        final ElfLevelDef levelDef = (ElfLevelDef) precheck.get("levelDef");
        final ElfAchievementType achievementType = ElfAchievementType.valueOf(levelDef.getId());
        ElfMyAchievementMap myAchievementMap = elfAchvDao.load(userId);
        List<LevelPlantInfo> levelPlantInfos = buildMyLevelPlant(modified, null, null);
        MapMessage rtn = MapMessage.successMessage().add("levelPlantInfoList", levelPlantInfos);
        if (null == myAchievementMap) {//成就读取失败，就当做没有达成新成就
            return rtn.add("showAchvNew", false);
        }
        final ElfMyAchievement achievement = myAchievementMap.getAchievementMap().get(achievementType);

        //当前成就在此前已经是可领取状态，则不弹气泡了
        //拯救的植物并不是当前成就的等阶，也不弹气泡
        if (achievement.isExchangable() || achievement.getStage() != plantDef.getStar() - 1) {
            return rtn.add("showAchvNew", false);
        }
        Set<String> starSet = new HashSet<>();

        for (LevelPlantInfo levelPlantInfo : levelPlantInfos) {
            if (levelPlantInfo.levelId.equalsIgnoreCase(levelId)) {
                for (PlantFloorInfo plantFloorInfo : levelPlantInfo.plantFloorInfoList) {
                    if (plantFloorInfo.star == plantDef.getStar()) {
                        starSet.addAll(plantFloorInfo.plantInfoList.stream().map(e -> e.plantId).collect(Collectors.toList()));
                    }
                }
            }
        }
//        Map<String,ElfPlantDef> starMap = elfPlantDao.findByStar(plantDef.getStar());
//        List<String> plantIdList = levelDef.getPlantStruct().get("STAR_" + achievement.getStage());
//        Set<String> levelSet = new HashSet<>(starMap.keySet());
//        if (achievementType == ElfAchievementType.SAVE_PRINCE || achievementType == ElfAchievementType.SAVE_QUEEN
//                || achievementType == ElfAchievementType.SAVE_KING)
//            levelSet.retainAll(plantIdList);
        final boolean newAchv = achievement.isExchangable() ? false : achievementType.isStageExchangable(achievement.getStage(), modified.checkSavedPlant(starSet));

        rtn.add("showAchvNew", newAchv);
        //新成就达成
        AlpsThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                StringBuilder logSb = new StringBuilder("拯救植物").append(plantId);
                if (newAchv) {
                    elfAchvDao.setExchangable(userId, achievementType, achievement.getStage());
                    elfBubbleDao.updateByField(userId, "newAchv", true);
                    logSb.append(",获得成就").append(achievementType).append(achievement.getStage());
                }
                elfLogQueueSender.saveElfPlayLog(new ElfPlayLog(null, userId, new Date(), ElfLogEnum.SAVE_PLANT, logSb.toString()));
            }
        });
        return rtn;
    }

    @Override
    public MapMessage composePlant(final long userId, String levelId, final String plantId) {
        MapMessage precheck = saveOrComposePrecheck(userId, levelId, plantId);
        if (!precheck.isSuccess()) {
            return precheck;
        }
        ElfPlantDef plantDef = (ElfPlantDef) precheck.get("plantDef");
        ElfUserRecord elfUserRecord = (ElfUserRecord) precheck.get("elfUserRecord");
        if (!elfUserRecord.getPlantCounter().containsKey(plantId)) {
            return MapMessage.errorMessage().setErrorCode("100010").setInfo("未被拯救的植物不能合成");
        }
        final ElfUserRecord modified = elfUserRecordDao.incComposePlant(userId, plantId, plantDef.getMadeOf());
        if (null == modified) {
            return MapMessage.errorMessage().setErrorCode("100011").setInfo("不能合成植物，数据操作失败");
        }
        final ElfAchievementType elfAchievementType = ElfAchievementType.COMPOSE;
        ElfMyAchievementMap elfMyAchievementMap = elfAchvDao.load(userId);
        if (null == elfMyAchievementMap) {
            return MapMessage.errorMessage().setErrorCode("100002").setInfo("获取用户记录失败");
        }
        final ElfMyAchievement elfMyAchievement = elfMyAchievementMap.getAchievementMap().get(elfAchievementType);
        final boolean newAchv = elfMyAchievement.isExchangable() ? false : elfAchievementType.isStageExchangable(elfMyAchievement.getStage(), modified.getComposeCount());
        List<LevelPlantInfo> levelPlantInfos = buildMyLevelPlant(modified, null, null);
        MapMessage rtn = MapMessage.successMessage().add("levelPlantInfoList", levelPlantInfos);
        rtn.add("showAchvNew", newAchv);
        //新成就达成
        AlpsThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                StringBuilder logSb = new StringBuilder("合成植物").append(plantId);
                if (newAchv) {
                    elfAchvDao.setExchangable(userId, elfAchievementType, elfMyAchievement.getStage());
                    elfBubbleDao.updateByField(userId, "newAchv", true);
                    logSb.append(",获得成就").append(elfAchievementType).append(elfMyAchievement.getStage());
                }
                elfLogQueueSender.saveElfPlayLog(new ElfPlayLog(null, userId, new Date(), ElfLogEnum.COMPOSE_PLANT, logSb.toString()));
            }
        });
        return rtn;
    }

    private MapMessage saveOrComposePrecheck(final long userId, String levelId, String plantId) {
        ElfPlantDef plantDef = elfPlantDao.findByPlantId(plantId);
        if (null == plantDef) {
            return MapMessage.errorMessage().setErrorCode("100005").setInfo("不存在的植物");
        }
        ElfUserRecord elfUserRecord = elfUserRecordDao.load(userId);
        if (null == elfUserRecord) {
            return MapMessage.errorMessage().setErrorCode("100002").setInfo("获取用户记录失败");
        }
        if (null == plantDef.getMadeOf()) {//植物只能被阅读获得，并不能被合成拯救
            return MapMessage.errorMessage().setErrorCode("100006").setInfo("植物不可被合成拯救");
        }
        ElfLevelDef levelDef = elfLevelDefDao.findByLevelId(levelId);
        if (null == levelDef) {
            return MapMessage.errorMessage().setErrorCode("100012").setInfo("参数非法，不存在的关卡");
        }
        for (Map.Entry<String, Integer> entry : plantDef.getMadeOf().entrySet()) {
            if (!elfUserRecord.getPlantCounter().containsKey(entry.getKey()) || elfUserRecord.getPlantCounter().get(entry.getKey()) < entry.getValue()) {//合成材料不足
                return MapMessage.errorMessage().setErrorCode("100007").setInfo("合成材料不足");
            }
        }
        return MapMessage.successMessage().add("plantDef", plantDef).add("elfUserRecord", elfUserRecord).add("levelDef", levelDef);
    }

    @Override
    public MapMessage unsetAchvBubble(final long userId) {
        AlpsThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                elfBubbleDao.updateByField(userId, "newAchv", false);
            }
        });
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage unsetGiftBubble(final long userId) {
        AlpsThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                elfBubbleDao.updateByField(userId, "newGift", false);
            }
        });
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage exchangeGift(final long userId, final ElfMyGift gift) {
        ElfMyGiftList modified = elfMyGiftDao.removeGift(userId, gift.getGiftId());
        if (null == modified) {
            return MapMessage.errorMessage().setErrorCode("100015").setInfo("读取数据失败");
        }
        AlpsThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                elfLogQueueSender.saveElfPlayLog(new ElfPlayLog(null, userId, new Date(), ElfLogEnum.EXCHANGE_GIFT, new StringBuilder("领取礼物").append("(giftId:").append(gift.getGiftId()).append(")成功.礼物内容:").append(gift.getGiftType()).append(gift.getCount()).append("个").toString()));
            }
        });
        return MapMessage.successMessage().add("modified", modified);
    }

    @Override
    public MapMessage exchangeAchv(final long userId, final ElfAchievementType elfAchievementType, final ElfMyAchievementMap elfMyAchievementMap) {
        if (null == elfMyAchievementMap) {
            return MapMessage.errorMessage().setErrorCode("100015").setInfo("读取数据失败");
        }
        final ElfMyAchievement elfMyAchievement = elfMyAchievementMap.getAchievementMap().get(elfAchievementType);
        if (!elfMyAchievement.isExchangable()) {
            return MapMessage.errorMessage().setErrorCode("100021").setInfo("成就不可兑换");
        }
        int nextStage = elfMyAchievement.getStage() + 1;
        boolean nextExchangable = false;//下一等级成就是否可以领取
        if (nextStage >= elfAchievementType.getIncInt().length && elfAchievementType.getInc() == 0) {//有限等级的成就，如果此次领取的是最高级成就，则将stage置-1
            nextStage = -1;
        }
        ElfUserRecord elfUserRecord = elfUserRecordDao.load(userId);
        if (null == elfUserRecord) {
            return MapMessage.errorMessage().setErrorCode("100015").setInfo("读取数据失败");
        }
        if (nextStage > 0) {
            if (elfAchievementType == ElfAchievementType.LOGIN_DAY) {
                nextExchangable = elfAchievementType.isStageExchangable(nextStage, elfUserRecord.getLoginDayCount());
            } else if (elfAchievementType == ElfAchievementType.SUN_GET) {
                nextExchangable = elfAchievementType.isStageExchangable(nextStage, elfUserRecord.getSunCount());
            } else if (elfAchievementType == ElfAchievementType.COMPOSE) {
                nextExchangable = elfAchievementType.isStageExchangable(nextStage, elfUserRecord.getComposeCount());
            } else
                nextExchangable = elfAchievementType.isStageExchangable(nextStage, getAchvFz(elfAchievementType, elfUserRecord, nextStage));
        }
        final boolean nextExchangableFinal = nextExchangable;
        ElfMyAchievementMap modified = elfAchvDao.setExchanged(userId, elfAchievementType, elfMyAchievement.getStage(), nextStage, nextExchangable);
        if (null == modified) {
            return MapMessage.errorMessage().setErrorCode("100015").setInfo("读取数据失败");
        }
        AlpsThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                elfLogQueueSender.saveElfPlayLog(new ElfPlayLog(null, userId, new Date(), ElfLogEnum.EXCHANGE_ACHV, new StringBuilder("领取成就").append(elfAchievementType).append(elfMyAchievement.getStage()).append(nextExchangableFinal ? "后续成就变为可领取" : "").toString()));
            }
        });
        return MapMessage.successMessage().add("achvInfo", buildAchivementForDisplay(userId, elfUserRecord, modified).get(elfAchievementType.ordinal()));
    }

    @Override
    public MapMessage importPlantDef(Collection<ElfPlantDef> plantDefs) {
        return MapMessage.successMessage().add("data", elfPlantDao.replaceAll(plantDefs));
    }

    @Override
    public MapMessage importLevel(Collection<ElfLevelDef> levelDefs) {
        return MapMessage.successMessage().add("data", elfLevelDefDao.replaceAll(levelDefs));
    }

    @Override
    public MapMessage importBookDef(Collection<ElfBookDef> bookDefs) {
        return MapMessage.successMessage().add("data", elfBookDao.replaceAll(bookDefs));
    }
}

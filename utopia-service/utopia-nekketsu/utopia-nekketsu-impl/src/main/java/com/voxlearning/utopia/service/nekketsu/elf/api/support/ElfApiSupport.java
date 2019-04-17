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

package com.voxlearning.utopia.service.nekketsu.elf.api.support;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.nekketsu.elf.constant.WalkerElfConstant;
import com.voxlearning.utopia.service.nekketsu.elf.dao.*;
import com.voxlearning.utopia.service.nekketsu.elf.entity.*;
import com.voxlearning.utopia.service.walker.elf.net.types.AchvShowInfo;
import com.voxlearning.utopia.service.walker.elf.net.types.LevelPlantInfo;
import com.voxlearning.utopia.service.walker.elf.net.types.PlantFloorInfo;
import com.voxlearning.utopia.service.walker.elf.net.types.PlantInfo;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by Sadi.Wan on 2015/2/27.
 */
public abstract class ElfApiSupport extends SpringContainerSupport {
    @Inject
    protected ElfAchvDao elfAchvDao;

    @Inject
    protected ElfBookDao elfBookDao;

    @Inject
    protected ElfBubbleDao elfBubbleDao;

    @Inject
    protected ElfLevelDefDao elfLevelDefDao;

    @Inject
    protected ElfMyGiftDao elfMyGiftDao;

    @Inject
    protected ElfPlantDao elfPlantDao;

    @Inject
    protected ElfUserRecordDao elfUserRecordDao;

    protected List<LevelPlantInfo> buildMyLevelPlant(ElfUserRecord elfUserRecord, Collection<ElfLevelDef> levelDefList, ElfBubble elfBubble) {
        if (null == levelDefList) {
            levelDefList = elfLevelDefDao.listAll();
        }
        if (null == elfBubble) {
            elfBubble = elfBubbleDao.load(elfUserRecord.getUserId());
        }
        List<LevelPlantInfo> levelPlantInfoList = new ArrayList<>(levelDefList.size());

        for (ElfLevelDef elfLevelDef : levelDefList) {
            LevelPlantInfo levelPlantInfo = new LevelPlantInfo();
            levelPlantInfo.levelId = elfLevelDef.getId();
            levelPlantInfo.showNew = elfBubble.getNewPlant().get(elfLevelDef.getId());
            int star = 1;
            for (List<String> startFloorList : elfLevelDef.getPlantStruct().values()) {
                PlantFloorInfo plantFloorInfo = new PlantFloorInfo();
                plantFloorInfo.integralCount = WalkerElfConstant.savePlantIntegral.get(star - 1);
                plantFloorInfo.star = star++;
                for (String plantId : startFloorList) {
                    PlantInfo plantInfo = new PlantInfo();
                    ElfPlantDef plantDef = elfPlantDao.findByPlantId(plantId);
                    if (elfUserRecord.getPlantCounter().containsKey(plantId)) {
                        plantInfo.count = elfUserRecord.getPlantCounter().get(plantId);
                        plantInfo.name = plantDef.getName();
                        plantInfo.locked = false;
                        plantInfo.plantId = plantId;
                        plantInfo.savable = false;
                        plantInfo.composable = true;
                        if (null != plantDef.getMadeOf()) {
                            for (Map.Entry<String, Integer> entry : plantDef.getMadeOf().entrySet()) {
                                if (!elfUserRecord.getPlantCounter().containsKey(entry.getKey()) || elfUserRecord.getPlantCounter().get(entry.getKey()) < entry.getValue()) {//合成材料不足
                                    plantInfo.composable = false;
                                    break;
                                }
                            }
                        } else {
                            plantInfo.composable = false;
                        }
                    } else {
                        plantInfo.count = 0;
                        plantInfo.name = plantDef.getName();
                        plantInfo.locked = true;
                        plantInfo.plantId = plantId;
                        plantInfo.composable = false;
                        plantInfo.savable = true;
                        if (null != plantDef.getMadeOf()) {
                            for (Map.Entry<String, Integer> entry : plantDef.getMadeOf().entrySet()) {
                                if (!elfUserRecord.getPlantCounter().containsKey(entry.getKey()) || elfUserRecord.getPlantCounter().get(entry.getKey()) < entry.getValue()) {//合成材料不足
                                    plantInfo.savable = false;
                                    break;
                                }
                            }
                        } else {
                            plantInfo.savable = false;
                        }
                    }
                    plantFloorInfo.plantInfoList.add(plantInfo);
                }
                levelPlantInfo.plantFloorInfoList.add(plantFloorInfo);
            }
            levelPlantInfoList.add(levelPlantInfo);
        }
        return levelPlantInfoList;
    }

    protected Set<String> getSavaOrComposablePlant(Map<String, Integer> plantCounter) {
        Collection<ElfPlantDef> elfPlantDefList = elfPlantDao.loadAll();
        Set<String> rtn = new HashSet<>();
        for (ElfPlantDef elfPlantDef : elfPlantDefList) {
            boolean composable = true;
            if (null == elfPlantDef.getMadeOf()) {
                continue;
            }
            for (Map.Entry<String, Integer> entry : elfPlantDef.getMadeOf().entrySet()) {
                if (!plantCounter.containsKey(entry.getKey()) || plantCounter.get(entry.getKey()) < entry.getValue()) {
                    composable = false;
                    break;
                }

            }
            if (composable) {
                rtn.add(elfPlantDef.getPlantId());
            }
        }
        return rtn;
    }

    protected List<AchvShowInfo> buildAchivementForDisplay(long userId, ElfUserRecord elfUserRecord, ElfMyAchievementMap elfMyAchievementMap) {
        if (null == elfUserRecord) {
            elfUserRecord = elfUserRecordDao.load(userId);
        }
        if (null == elfMyAchievementMap) {
            elfMyAchievementMap = elfAchvDao.load(userId);
        }
        List<AchvShowInfo> rtn = new ArrayList<>();
        for (ElfAchievementType elfAchievementType : ElfAchievementType.values()) {
            ElfMyAchievement elfMyAchievement = elfMyAchievementMap.getAchievementMap().get(elfAchievementType);
            AchvShowInfo achvShowInfo = new AchvShowInfo();
            achvShowInfo.achvId = elfAchievementType.name();
            achvShowInfo.exchangable = elfMyAchievement.isExchangable();
            achvShowInfo.integralCount = elfAchievementType.getIntegralCount();
            achvShowInfo.pkCount = elfAchievementType.getPkCount();
            achvShowInfo.fmNum = elfAchievementType.calcFm(elfMyAchievement.getStage());
            int calcFzInput = getAchvFz(elfAchievementType, elfUserRecord, elfMyAchievement.getStage());
            achvShowInfo.fzNum = elfMyAchievement.isExchangable() ? achvShowInfo.fmNum : calcFzInput;
            if (-1 == elfMyAchievement.getStage()) {//有限成就已到顶
                achvShowInfo.desc = elfAchievementType.getTxtTemplate().replace("?", elfAchievementType.getIncString()[elfAchievementType.getIncString().length - 1]);
                achvShowInfo.exchangable = false;
                achvShowInfo.fmNum = elfAchievementType.getIncInt()[elfAchievementType.getIncInt().length - 1];
                achvShowInfo.fzNum = achvShowInfo.fmNum;
            } else if (null != elfAchievementType.getIncString()) {
                achvShowInfo.desc = elfAchievementType.getTxtTemplate().replace("?", elfAchievementType.getIncString()[elfMyAchievement.getStage()]);
            } else {
                String replace;
                if (elfAchievementType.getIncInt().length <= elfMyAchievement.getStage()) {
                    replace = String.valueOf(elfAchievementType.getIncInt()[elfAchievementType.getIncInt().length - 1] + elfAchievementType.getInc() * (elfMyAchievement.getStage() - elfAchievementType.getIncInt().length + 1));
                } else {
                    replace = String.valueOf(elfAchievementType.getIncInt()[elfMyAchievement.getStage()]);
                }
                achvShowInfo.desc = elfAchievementType.getTxtTemplate().replace("?", replace);
            }

            rtn.add(achvShowInfo);
        }
        return rtn;
    }

    protected int getAchvFz(ElfAchievementType elfAchievementType, ElfUserRecord elfUserRecord, int targetStage) {
        int calcFzInput;
        switch (elfAchievementType) {
            case SAVE_KING:
            case SAVE_PRINCE:
            case SAVE_QUEEN:
                ElfLevelDef elfLevelDef = elfLevelDefDao.findByLevelId(elfAchievementType.name());
                if (elfLevelDef.getPlantStruct().containsKey("STAR_" + String.valueOf(targetStage + 1)))
                    calcFzInput = elfUserRecord.checkSavedPlant(new HashSet<>(elfLevelDef.getPlantStruct().get("STAR_" + String.valueOf(targetStage + 1))));
                else
                    calcFzInput = 0;
                break;
            case SUN_GET:
                calcFzInput = elfUserRecord.getSunCount() - elfAchievementType.getStageStart(targetStage);
                break;
            case LOGIN_DAY:
                calcFzInput = elfUserRecord.getLoginDayCount() - elfAchievementType.getStageStart(targetStage);
                break;
            case COMPOSE:
                calcFzInput = SafeConverter.toInt(elfUserRecord.getComposeCount(), 0) - elfAchievementType.getStageStart(targetStage);
                break;
            default:
                calcFzInput = 0;
        }
        return calcFzInput;
    }
}

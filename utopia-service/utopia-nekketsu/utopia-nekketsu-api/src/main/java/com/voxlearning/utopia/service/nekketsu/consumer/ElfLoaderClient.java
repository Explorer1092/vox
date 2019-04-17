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

package com.voxlearning.utopia.service.nekketsu.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.service.nekketsu.elf.api.ElfLoader;
import com.voxlearning.utopia.service.nekketsu.elf.entity.ElfBubble;
import com.voxlearning.utopia.service.nekketsu.elf.entity.ElfMyAchievementMap;
import com.voxlearning.utopia.service.nekketsu.elf.entity.ElfMyGiftList;
import com.voxlearning.utopia.service.nekketsu.elf.entity.ElfUserRecord;
import lombok.Getter;
import org.slf4j.Logger;

/**
 * Created by Sadi.Wan on 2015/2/28.
 */
public class ElfLoaderClient {
    private static final Logger logger = LoggerFactory.getLogger(ElfLoaderClient.class);

    @Getter
    @ImportService(interfaceClass = ElfLoader.class)
    private ElfLoader remoteReference;

    public MapMessage initInfo(long userId) {
        try {
            return remoteReference.initInfo(userId);
        } catch (Exception e) {
            logger.error("ELF_LOGIN_DUBBLE_FAILED:userid {} ,Excepiton e", userId, e);
        }
        return MapMessage.errorMessage().setErrorCode("100013").setInfo("登录失败，连接出错");
    }

    public ElfBubble loadBubble(long userId) {
        return remoteReference.loadBubble(userId);
    }

    public ElfMyGiftList loadGiftList(long userId) {
        return remoteReference.loadGiftList(userId);
    }

    public ElfMyAchievementMap loadAchv(long userId) {
        return remoteReference.loadAchv(userId);
    }

    public ElfUserRecord loadUserRecord(long userId) {
        return remoteReference.loadUserRecord(userId);
    }
}

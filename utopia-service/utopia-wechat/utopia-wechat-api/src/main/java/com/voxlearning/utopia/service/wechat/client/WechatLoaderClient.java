/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.wechat.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.wechat.api.WechatLoader;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.data.WechatNoticeSnapshot;
import com.voxlearning.utopia.service.wechat.api.entities.*;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WechatLoaderClient {

    @Getter
    @ImportService(interfaceClass = WechatLoader.class)
    private WechatLoader wechatLoader;

    public WechatFaqCatalog loadWechatFaqCatalog(Long id) {
        if (id == null) {
            return null;
        }
        return wechatLoader.loadWechatFaqCatalog(id);
    }

    public List<WechatFaqCatalog> loadWechatFaqCatalogs(WechatType type) {
        if (type == null) {
            return Collections.emptyList();
        }
        return wechatLoader.loadWechatFaqCatalogs(type.getType());
    }

    public WechatFaq loadWechatFaq(Long id) {
        if (id == null) {
            return null;
        }
        return wechatLoader.loadWechatFaq(id);
    }

    public List<WechatFaq> loadCatalogWechatFaqs(Long catalogId, WechatType type) {
        if (catalogId == null || type == null) {
            return Collections.emptyList();
        }
        return wechatLoader.loadCatalogWechatFaqs(catalogId, type);
    }

    public List<WechatFaq> matchWechatFaq(String keyword, WechatType type) {
        return wechatLoader.matchWechatFaq(keyword, type);
    }

    public WechatFaceInviteRecord loadWechatFaceInviteLastRecord(String openId) {
        return wechatLoader.loadWechatFaceInviteLastRecord(openId);
    }

    public List<WechatNoticeSnapshot> loadWechatNoticeSnapshotByUserId(Long userId, Boolean isHistory) {
        return wechatLoader.loadWechatNoticeSnapshotByUserId(userId, isHistory);
    }

    @Deprecated
    public List<Map<String, Object>> loadNoSubmitLittleChampionChildren(List<User> students) {
        return wechatLoader.loadNoSubmitLittleChampionChildren(students);
    }

    @Deprecated
    public WechatLittleChampion loadWechatLittleChampionById(Long id) {
        return wechatLoader.loadWechatLittleChampionById(id);
    }

    @Deprecated
    public boolean hasSubmitWechatLittleChampion(Long studentId) {
        return wechatLoader.hasSubmitWechatLittleChampion(studentId);
    }


    @Deprecated
    public List<WechatLittleChampion> loadWechatLittleChampionByTeacherId(Long teacherId) {
        return wechatLoader.loadWechatLittleChampionByTeacherId(teacherId);
    }

    public Map<Long, List<UserWechatRef>> loadUserWechatRefs(Collection<Long> userIds, WechatType type) {
        return wechatLoader.loadUserWechatRefs(userIds, type);
    }


    public Map<Long, List<UserWechatRef>> loadUserWechatRefs(Collection<Long> userIds) {
        return wechatLoader.loadUserWechatRefs(userIds);
    }


    public User loadWechatUser(String openId) {
        return wechatLoader.loadWechatUser(openId);
    }

    public UserMiniProgramRef loadMiniProgramUserRef(String openId, MiniProgramType miniProgramType) {
        return wechatLoader.loadMiniProgramUserRef(openId, miniProgramType.getType());
    }

    public UserMiniProgramRef loadMiniProgramUserRef(Long userId, MiniProgramType miniProgramType) {
        return wechatLoader.loadMiniProgramUserRef(userId, miniProgramType.getType());
    }

    public User loadWechatUser(String openId, Long userId) {
        return wechatLoader.loadWechatUser(openId, userId);
    }


    public boolean haveBindBefore(Long userId, Integer wechatType) {
        return wechatLoader.haveBindBefore(userId, wechatType);
    }


    public boolean isBinding(Long userId, Integer wechatType) {
        return wechatLoader.isBinding(userId, wechatType);
    }


    public boolean isStudentBinding(Long studentId) {
        return wechatLoader.isStudentBinding(studentId);
    }

    public UserWechatRef loadUserWechatRefByUserIdAndWechatType(Long userId, Integer type) {
        return wechatLoader.loadUserWechatRefByUserIdAndWechatType(userId, type);
    }
}

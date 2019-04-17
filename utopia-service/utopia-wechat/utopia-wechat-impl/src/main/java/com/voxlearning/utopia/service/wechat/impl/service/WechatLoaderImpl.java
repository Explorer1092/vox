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

package com.voxlearning.utopia.service.wechat.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.wechat.api.WechatLoader;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTicketType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.data.WechatNoticeSnapshot;
import com.voxlearning.utopia.service.wechat.api.entities.*;
import com.voxlearning.utopia.service.wechat.impl.dao.*;
import com.voxlearning.utopia.service.wechat.impl.service.wechat.WechatNoticeHistoryService;
import com.voxlearning.utopia.service.wechat.impl.service.wechat.WechatNoticeService;
import org.springframework.beans.BeanUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Default {@link WechatLoader} implementation.
 *
 * @author Xiaohai Zhang
 * @since Jan 19, 2015
 */
@Named("com.voxlearning.utopia.service.wechat.impl.service.WechatLoaderImpl")
@ExposeServices({
        @ExposeService(interfaceClass = WechatLoader.class, version = @ServiceVersion(version = "2.0.DEV")),
        @ExposeService(interfaceClass = WechatLoader.class, version = @ServiceVersion(version = "20180605"))
}
)
public class WechatLoaderImpl extends SpringContainerSupport implements WechatLoader {

    @Inject private ParentLoaderClient parentLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserWechatRefPersistence userWechatRefPersistence;
    @Inject private WechatFaceInviteRecordPersistence wechatFaceInviteRecordPersistence;
    @Inject private WechatFaqCatalogPersistence wechatFaqCatalogPersistence;
    @Inject private WechatFaqPersistence wechatFaqPersistence;
    @Inject private WechatLittleChampionPersistence wechatLittleChampionPersistence;
    @Inject private WechatNoticeHistoryService wechatNoticeHistoryService;
    @Inject private WechatNoticeService wechatNoticeService;
    @Inject private WechatSignPersistence wechatSignPersistence;
    @Inject private WechatSupportUserPersistence wechatSupportUserPersistence;
    @Inject private WechatTicketPersistence wechatTicketPersistence;
    @Inject private UserMiniProgramRefPersistence userMiniProgramRefPersistence;

    @Override
    public WechatFaqCatalog loadWechatFaqCatalog(Long id) {
        return wechatFaqCatalogPersistence.load(id);
    }

    @Override
    public List<WechatFaqCatalog> loadWechatFaqCatalogs(Integer type) {
        if (type == null) {
            return Collections.emptyList();
        }
        return wechatFaqCatalogPersistence.findAllCatalogsByType(type);
    }

    @Override
    public WechatFaq loadWechatFaq(Long id) {
        return wechatFaqPersistence.load(id);
    }

    @Override
    public List<WechatFaq> loadCatalogWechatFaqs(Long catalogId, WechatType wechatType) {
        if (catalogId == null || wechatType == null) {
            return Collections.emptyList();
        }
        return wechatFaqPersistence.findByCatalog(catalogId, wechatType.getType());
    }

    @Override
    @Deprecated
    public List<WechatFaq> loadWechatFaqForCrm(String sql, Map<String, Object> parameters) {
        // 需要的话重新写，这都叫啥实现
        throw new UnsupportedOperationException();
    }

    @Override
    public List<WechatFaq> matchWechatFaq(String keyword, WechatType wechatType) {
        if (StringUtils.isEmpty(keyword) || wechatType == null) {
            return Collections.emptyList();
        }

        String[] keywords = keyword.split("\\s");
        Map<Long, WechatFaq> result = new HashMap<>();
        for (String k : keywords) {
            if (k.trim().length() == 0) {
                continue;
            }
            List<WechatFaq> lst = wechatFaqPersistence.findByTitleLike(k, wechatType.getType());
            for (WechatFaq faq : lst) {
                result.put(faq.getId(), faq);
            }
        }
        return new ArrayList<>(result.values());
    }

    @Override
    public WechatSupportUser loadWechatSupportUser(String openId) {
        return wechatSupportUserPersistence.getSupportUserByOpenId(openId);
    }

    @Override
    public WechatSign loadUserWechatSign(Long userId, String signDate) {
        return wechatSignPersistence.findByUserIdAndSignDate(userId, signDate);
    }

    @Override
    public Map<Long, List<UserWechatRef>> loadUserWechatRefs(Collection<Long> userIds, WechatType wechatType) {
        if (wechatType == null) {
            return Collections.emptyMap();
        }
        return userWechatRefPersistence.findByUserIds(userIds, wechatType.getType());
    }

    //FIXME 这个方法会查出所有微信端的用户 请自行过滤 调用之前找 xiaopeng.yang
    @Override
    public Map<Long, List<UserWechatRef>> loadUserWechatRefs(Collection<Long> userIds) {
        return userWechatRefPersistence.findByUserIdsFromCache(userIds);
    }

    @Override
    public User loadWechatUser(String openId) {
        UserWechatRef ref = userWechatRefPersistence.findByOpenId(openId);
        if (null == ref) {
            return null;
        }
        return userLoaderClient.loadUser(ref.getUserId());
    }

    @Override
    public User loadWechatUserByUnionId(String unionId) {
        UserWechatRef ref = userWechatRefPersistence.findByUnionId(unionId);
        if (ref == null) {
            return null;
        }
        return userLoaderClient.loadUser(ref.getUserId());
    }

    @Override
    public List<UserWechatRef> loadUserWechatRefsIncludeDisabled(Long userId, Integer wechatType) {
        return userWechatRefPersistence.findByUserIdInAnyCase(userId, wechatType);
    }

    @Override
    public UserMiniProgramRef loadMiniProgramUserRef(String openId, Integer miniProgramType) {
        if (StringUtils.isBlank(openId) || miniProgramType == null)
            return null;
        return userMiniProgramRefPersistence.findByOpenId(openId, miniProgramType);
    }

    @Override
    public UserMiniProgramRef loadMiniProgramUserRef(Long userId, Integer miniProgramType) {
        if (userId == null || miniProgramType == null)
            return null;
        return userMiniProgramRefPersistence.findByUserId(userId, miniProgramType);
    }

    @Override
    public User loadWechatUser(String openId, Long userId) {
        UserWechatRef ref = userWechatRefPersistence.findByOpenIdAndUserId(openId, userId);
        if (null == ref) {
            return null;
        }
        return userLoaderClient.loadUser(ref.getUserId());
    }

    @Override
    public boolean haveBindBefore(Long userId, Integer wechatType) {
        //老方法 兼容以前的家长微信和老师微信
        List<UserWechatRef> refs = userWechatRefPersistence.findByUserIdInAnyCase(userId, wechatType);
        return CollectionUtils.isNotEmpty(refs);
    }

    @Override
    public boolean isBinding(Long userId, Integer wechatType) {
        List<UserWechatRef> refs = userWechatRefPersistence.findByUserId(userId, wechatType);
        return CollectionUtils.isNotEmpty(refs);
    }

    @Override
    public boolean isStudentBinding(Long studentId) {
        if (studentId == null) return false;
        List<StudentParent> parents = parentLoaderClient.loadStudentParents(studentId);
        if (CollectionUtils.isEmpty(parents)) return false;

        Set<Long> parentIds = parents.stream().map(e -> e.getParentUser().getId()).collect(Collectors.toSet());
        Map<Long, List<UserWechatRef>> map = loadUserWechatRefs(parentIds, WechatType.PARENT);
        long count = map.values().stream()
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(List::stream)
                .filter(e -> e != null)
                .count();
        return count > 0;
    }

    @Override
    public WechatTicket loadWechatTicket(Long teacherId, WechatTicketType wechatTicketType) {
        return wechatTicketPersistence.loadByUserIdAndTicketType(teacherId, wechatTicketType.name());
    }

    @Override
    public WechatFaceInviteRecord loadWechatFaceInviteLastRecord(String openId) {
        return wechatFaceInviteRecordPersistence.loadByOpenId(openId).stream().findFirst().orElse(null);
    }

    @Override
    public List<WechatNoticeSnapshot> loadWechatNoticeSnapshotByUserId(Long userId, Boolean isHistory) {
        if (userId == null) {
            return null;
        }

        List<WechatNoticeSnapshot> noticeSnaps = null;
        if (isHistory != null && isHistory) {
            List<WechatNoticeHistory> histories = wechatNoticeHistoryService.loadAllByUserId(userId);
            if (histories != null && !histories.isEmpty()) {
                noticeSnaps = new ArrayList<>(histories.size());
                for (WechatNoticeHistory e : histories) {
                    WechatNoticeSnapshot snap = new WechatNoticeSnapshot();
                    BeanUtils.copyProperties(e, snap);
                    snap.setCreateTime(e.getCreateDatetime());
                    WechatNoticeType type = WechatNoticeType.of(e.getMessageType());
                    snap.setTypeDesc(type == null ? String.valueOf(e.getMessageType()) : type.getDescription());
                    WechatNoticeState state = WechatNoticeState.of(e.getState());
                    snap.setStateDesc(state == null ? String.valueOf(e.getState()) : state.getDescription());
                    noticeSnaps.add(snap);
                }
            }
        } else {
            List<WechatNotice> notices = wechatNoticeService.loadAllByUserId(userId);
            if (notices != null && !notices.isEmpty()) {
                noticeSnaps = new ArrayList<>(notices.size());
                for (WechatNotice e : notices) {
                    WechatNoticeSnapshot snap = new WechatNoticeSnapshot();
                    BeanUtils.copyProperties(e, snap);
                    snap.setCreateTime(e.getCreateDatetime());
                    WechatNoticeType type = WechatNoticeType.of(e.getMessageType());
                    snap.setTypeDesc(type == null ? String.valueOf(e.getMessageType()) : type.getDescription());
                    WechatNoticeState state = WechatNoticeState.of(e.getState());
                    snap.setStateDesc(state == null ? String.valueOf(e.getState()) : state.getDescription());
                    noticeSnaps.add(snap);
                }
            }
        }
        return noticeSnaps;
    }

    @Override
    @Deprecated
    public List<Map<String, Object>> loadNoSubmitLittleChampionChildren(List<User> students) {
        if (CollectionUtils.isEmpty(students)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> results = new ArrayList<>();
        for (User student : students) {
            WechatLittleChampion champion = wechatLittleChampionPersistence.findByStudentId(student.getId());
            if (champion == null) {
                Map<String, Object> studentMap = new HashMap<>();
                studentMap.put("studentId", student.getId());
                studentMap.put("studentName", student.fetchRealname());
                results.add(studentMap);
            }
        }
        return results;
    }

    @Override
    @Deprecated
    public WechatLittleChampion loadWechatLittleChampionById(Long id) {
        return wechatLittleChampionPersistence.load(id);
    }

    @Override
    @Deprecated
    public boolean hasSubmitWechatLittleChampion(Long studentId) {
        return wechatLittleChampionPersistence.findByStudentId(studentId) != null;
    }

    @Override
    @Deprecated
    public List<WechatLittleChampion> loadWechatLittleChampionByTeacherId(Long teacherId) {
        return wechatLittleChampionPersistence.findByTeacherId(teacherId);
    }

    @Override
    public UserWechatRef loadUserWechatRefByUserIdAndWechatType(Long userId, Integer type) {
        List<UserWechatRef> refs = userWechatRefPersistence.findByUserId(userId, type);
        if (CollectionUtils.isEmpty(refs)) {
            return null;
        }
        return refs.stream().min((o1, o2) -> o2.getUpdateDatetime().compareTo(o1.getUpdateDatetime())).orElse(null);
    }
}

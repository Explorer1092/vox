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

package com.voxlearning.utopia.service.zone.impl.loader;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.privilege.client.PrivilegeLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.zone.api.PersonalZoneLoader;
import com.voxlearning.utopia.service.zone.api.constant.ClazzZoneProductSubspecies;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneBag;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneProduct;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;
import com.voxlearning.utopia.service.zone.api.mapper.BubbleMapper;
import com.voxlearning.utopia.service.zone.buffer.ClazzZoneProductBuffer;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzZoneBagPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.StudentInfoPersistence;
import com.voxlearning.utopia.service.zone.impl.service.ZoneBagServiceImpl;
import com.voxlearning.utopia.service.zone.impl.service.ZoneProductServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
@ExposeService(interfaceClass = PersonalZoneLoader.class)
public class PersonalZoneLoaderImpl extends SpringContainerSupport implements PersonalZoneLoader {

    @Inject private ClazzZoneBagPersistence clazzZoneBagPersistence;
    @Inject private StudentInfoPersistence studentInfoPersistence;
    @Inject private ZoneBagServiceImpl zoneBagService;
    @Inject private ZoneProductServiceImpl zoneProductService;
    @Inject private PrivilegeLoaderClient privilegeLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;

    @Override
    public StudentInfo loadStudentInfo(Long studentId) {
        if (studentId == null) {
            return null;
        }

        StudentInfo studentInfo = studentInfoPersistence.load(studentId);
        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentId);

        if (studentInfo == null && studentExtAttribute == null) {
            return null;
        } else if (studentInfo == null) {
            studentInfo = new StudentInfo();
            studentInfo.setStudentId(studentId);
            studentInfo.setBubbleId(studentExtAttribute.getBubbleId());
            studentInfo.setHeadWearId(studentExtAttribute.getHeadWearId());

            studentInfo.setLikeCount(0);
            studentInfo.setSignInCount(0);
            studentInfo.setStudyMasterCount(0);
        } else if (studentExtAttribute == null) {
            studentInfo.setBubbleId(0L);
            studentInfo.setHeadWearId(null);
        } else {
            studentInfo.setBubbleId(studentExtAttribute.getBubbleId());
            studentInfo.setHeadWearId(studentExtAttribute.getHeadWearId());
        }

        return studentInfo;
    }

    @Override
    public Map<Long, StudentInfo> loadStudentInfos(Collection<Long> studentIds) {
        Map<Long, StudentInfo> studentInfoMap = studentInfoPersistence.loads(CollectionUtils.toLinkedHashSet(studentIds));
        Map<Long, StudentExtAttribute> studentExtAttributeMap = studentLoaderClient.loadStudentExtAttributes(studentIds);

        if (MapUtils.isEmpty(studentInfoMap) && MapUtils.isEmpty(studentExtAttributeMap)) {
            return Collections.emptyMap();
        }

        // case A: studentInfo 比 studentExtAttribute 多的情况
        for (Map.Entry<Long, StudentInfo> entry : studentInfoMap.entrySet()) {
            Long studentId = entry.getKey();
            StudentInfo studentInfo = entry.getValue();

            StudentExtAttribute studentExtAttribute = studentExtAttributeMap.get(studentId);
            if (studentExtAttribute != null) {
                studentInfo.setBubbleId(studentExtAttribute.getBubbleId());
                studentInfo.setHeadWearId(studentExtAttribute.getHeadWearId());
            } else {
                studentInfo.setBubbleId(0L);
                studentInfo.setHeadWearId(null);
            }
        }

        // case B: studentInfo 比 studentExtAttribute 少的情况
        Collection<Long> subtract = CollectionUtils.subtract(studentExtAttributeMap.keySet(), studentInfoMap.keySet());
        for (Long studentId : subtract) {
            StudentExtAttribute studentExtAttribute = studentExtAttributeMap.get(studentId);
            StudentInfo studentInfo = new StudentInfo();
            studentInfo.setStudentId(studentId);
            studentInfo.setBubbleId(studentExtAttribute.getBubbleId());
            studentInfo.setHeadWearId(studentExtAttribute.getHeadWearId());

            studentInfo.setLikeCount(0);
            studentInfo.setSignInCount(0);
            studentInfo.setStudyMasterCount(0);
            studentInfoMap.put(studentId, studentInfo);
        }

        return studentInfoMap;
    }

    @Override
    public void __deleteBubbles(Collection<Long> bagIds, Long userId) {
        bagIds = CollectionUtils.toLinkedHashSet(bagIds);
        if (bagIds.isEmpty() || userId == null) {
            return;
        }
        bagIds.forEach(bagId -> clazzZoneBagPersistence.delete(bagId, userId));
    }

    @Override
    public boolean hasBubble(Long studentId, Long bubbleId) {
        return zoneBagService.findClazzZoneBagList(studentId)
                .getUninterruptibly()
                .stream()
                .filter(t -> Objects.equals(t.getProductId(), bubbleId))
                .filter(ClazzZoneBag::isAvailable)
                .count() > 0;
    }

    @Override
    public MapMessage showBubbles(Long studentId) {
        return $showBubbles(studentId, zoneProductService.getClazzZoneProductBuffer());
    }

    private MapMessage $showBubbles(Long studentId, ClazzZoneProductBuffer zoneProductBuffer) {
        if (studentId == null) {
            return MapMessage.successMessage();
        }
        // 查询自己拥有的付费气泡
        Set<Long> ownedBubbleIds = new HashSet<>();
        Set<Long> expiredBagIds = new HashSet<>();
        for (ClazzZoneBag bubble : zoneBagService.findClazzZoneBagList(studentId).getUninterruptibly()) {
            if (bubble.isAvailable()) {
                // 如果没有过期，放入拥有气泡的id列表
                ownedBubbleIds.add(bubble.getProductId());
            } else {
                // 如果过期了，直接删除
                expiredBagIds.add(bubble.getId());
            }
        }
        if (!expiredBagIds.isEmpty()) {
            __deleteBubbles(expiredBagIds, studentId);
        }
        // 自己当前使用的气泡
        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentId);
        Long currentUsingBubbleId = ClazzZoneProduct.CLAZZ_ZONE_DEFAULT_BUBBLE;
        if (studentExtAttribute != null && studentExtAttribute.getBubbleId() != null) {
            currentUsingBubbleId = studentExtAttribute.getBubbleId();
        }
        // 查询所有气泡
        List<BubbleMapper> bubbles = new ArrayList<>();
        for (ClazzZoneProduct bubble : zoneProductBuffer.loadBubbles()) {
            if (bubble.fetchSubspecies() == ClazzZoneProductSubspecies.AFENTI_BASIC
                    || bubble.fetchSubspecies() == ClazzZoneProductSubspecies.TALENT
                    || bubble.fetchSubspecies() == ClazzZoneProductSubspecies.AFENTI_EXAM) {
                continue;
            }

            BubbleMapper mapper = new BubbleMapper();
            mapper.setBubbleId(bubble.getId());
            mapper.setName(bubble.getName());
            mapper.setCategory(bubble.getSubspecies());
            mapper.setPeriodOfValidity(bubble.getPeriodOfValidity());
            mapper.setPrice(bubble.getPrice());
            mapper.setCurrentUsing(bubble.getId().equals(currentUsingBubbleId));
            mapper.setOwned(ownedBubbleIds.contains(bubble.getId()));
            bubbles.add(mapper);
        }
        return MapMessage.successMessage().add("bubbles", bubbles);
    }
}

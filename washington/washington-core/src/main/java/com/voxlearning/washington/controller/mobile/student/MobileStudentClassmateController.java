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

package com.voxlearning.washington.controller.mobile.student;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.spi.common.JsonStringSerializer;
import com.voxlearning.utopia.core.cdn.url2.config.CdnConfig;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.action.api.support.PrivilegeType;
import com.voxlearning.utopia.service.clazz.cache.ClazzCache;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import com.voxlearning.washington.controller.mobile.student.headline.helper.MobileStudentClazzHelper;
import com.voxlearning.washington.mapper.Classmate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 班级空间-同班同学数据接口
 *
 * @author yuechen.wang
 * @since 2017-04-27
 */
@Controller
@RequestMapping(value = "/studentMobile/clazz")
public class MobileStudentClassmateController extends AbstractMobileController {

    @Inject private MobileStudentClazzHelper mobileStudentClazzHelper;
    @Inject private PrivilegeBufferServiceClient privilegeBufferServiceClient;

    @RequestMapping(value = "/classmate.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage classmate() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        if (currentStudentDetail().getClazz() == null) {
            return MapMessage.errorMessage("您还没有加入班级");
        }

        Long userId = currentUserId();
        Long clazzId = currentStudentDetail().getClazzId();

        int pageNum = Integer.max(1, getRequestInt("page", 1));
        int pageSize = getRequestInt("size", 7);

        try {
            // 排好顺序的同班同学
            List<Classmate> classmates = getClassmatesOrderByUserId(userId, clazzId);

            // 分页
            Page<Classmate> classmatePage = PageableUtils.listToPage(classmates, new PageRequest(pageNum - 1, pageSize));

            // 分页取完没有数据啦
            if (CollectionUtils.isEmpty(classmatePage.getContent())) {
                return MapMessage.successMessage().add("classmates", Collections.emptyList());
            }

            return MapMessage.successMessage()
                    .add("classmates", classmatePage.getContent())
                    .add("myFairy", false)
                    .add("cdnDomain", CdnConfig.getAvatarDomain().getValue());
        } catch (Exception ex) {
            logger.error("Failed load student classmate profile data. student={}", userId, ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    private List<Classmate> getClassmatesOrderByUserId(Long userId, Long clazzId) {
        List<User> classmates = mobileStudentClazzHelper.getCacheClassmates(userId, clazzId);
        if (CollectionUtils.isEmpty(classmates)) {
            return Collections.emptyList();
        }

        // 缓存一份排好顺序的数据
        List<Long> classmateIds = classmates.stream().map(User::getId).sorted(Long::compare).collect(Collectors.toList());
        String cacheKey = StringUtils.join("StudentAppClassmates_v3_", clazzId, "_", JsonStringSerializer.getInstance().serialize(classmateIds).hashCode());
        // 从缓存读取
        List<Classmate> classmateList = ClazzCache.getClazzCache().load(cacheKey);
        if (classmateList == null) {
            Map<String, Privilege> allHeadWear = new HashMap<>();
            Map<Long, StudentInfo> studentInfos = new HashMap<>();
            try {
                // 所有头饰, 忽略重复的头饰
                allHeadWear = privilegeBufferServiceClient.getPrivilegeBuffer()
                        .dump()
                        .getData()
                        .stream()
                        .filter(p -> PrivilegeType.Head_Wear == p.getType())
                        .collect(Collectors.toMap(Privilege::getId, Function.identity(), (u, v) -> {
                            logger.error("Duplicate privilege found, please check ASAP. Privilege={}", u.getName());
                            return u;
                        }, LinkedHashMap::new));

                studentInfos = personalZoneLoaderClient.getPersonalZoneLoader().loadStudentInfos(classmateIds);
            } catch (Exception ex) {
                logger.warn("Failed invoke classmate info. userId={}", currentUserId(), ex);
            }
            classmateList = new LinkedList<>();
            // 包装数据
            classmates.sort(Comparator.comparing(User::getId));  // 按照学生ID排一个顺序
            for (User classmate : classmates) {
                Long classmateId = classmate.getId();
                classmateList.add(new Classmate(classmate).withHeadWear(studentInfos.get(classmateId), allHeadWear));
            }

            // 排序完毕缓存半个小时
            ClazzCache.getClazzCache().set(cacheKey, 1800, classmateList);
        }

        return classmateList.stream().filter(c -> !userId.equals(c.getUserId())).collect(Collectors.toList());
    }

}

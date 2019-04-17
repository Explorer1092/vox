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

package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.user.api.entities.ChannelCUserAttribute;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.vendor.api.SelfStudyConfigLoader;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.mapper.JournalPagination;
import com.voxlearning.utopia.service.zone.client.ClazzJournalLoaderClient;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2016/8/3.
 */
@Controller
@RequestMapping(value = "/parentMobile/learning")
@Slf4j
public class MobileParentLearningCycleController extends AbstractMobileController {
    //只显示在pc上可使用的app的用户
    private static final Set<Long> onlyShowPcVendorAppUser = new HashSet<>(Collections.singletonList(20001L));
    //需要屏蔽的app
    private static final Set<String> notShowAppKeyForBlackUser = new HashSet<>(Arrays.asList("UsaAdventure", "AfentiMath", "AfentiExam", "GreatAdventure"));

    @Inject private RaikouSystem raikouSystem;

    @Inject private ClazzJournalLoaderClient clazzJournalLoaderClient;

    @ImportService(interfaceClass = SelfStudyConfigLoader.class)
    private SelfStudyConfigLoader selfStudyConfigLoader;

    /**
     * 学习圈入口
     */
    @RequestMapping(value = "/index.vpage", method = {RequestMethod.GET})
    public String index(Model model) {
        Integer clazzLevel = 0;
        User parent = currentParent();
        // 登录了， 看有没有孩子
        Long studentId = getRequestLong("sid");
        if (studentId != 0L) {
            ChannelCUserAttribute attribute = studentLoaderClient.loadStudentChannelCAttribute(studentId);
            if (attribute == null) {
                // 不是C端孩子
                Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
                if (clazz != null && clazz.getClazzLevel() != null) {
                    clazzLevel = clazz.getClazzLevel().getLevel();
                    // 毕业班
                    model.addAttribute("isGraduate", clazz.isTerminalClazz());
                }

                // 获取趣味学习应用列表(过滤掉没有背景图片的应用)
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                List<FairylandProduct> productList = businessVendorServiceClient.getParentAvailableFairylandProducts(parent, studentDetail,
                        FairyLandPlatform.PARENT_APP, FairylandProductType.APPS)
                        .stream()
                        .filter(p -> StringUtils.isNotBlank(p.getBackgroundImage()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(productList)) {
                    // 拼装数据
                    List<Map<String, Object>> products = new ArrayList<>();
                    for (FairylandProduct fairylandProduct : productList) {
                        if (parent != null && onlyShowPcVendorAppUser.contains(parent.getId())) {
                            continue;
                        }
                        Map<String, Object> appInfo = new HashMap<>();
                        appInfo.put("appKey", fairylandProduct.getAppKey());
                        appInfo.put("productName", fairylandProduct.getProductName());
                        appInfo.put("productDesc", fairylandProduct.getProductDesc());
                        appInfo.put("backgroundImage", fairylandProduct.getBackgroundImage());
                        appInfo.put("operationMessage", fairylandProduct.getOperationMessage());
                        appInfo.put("rank", fairylandProduct.getRank());
                        products.add(appInfo);
                    }
                    Collections.sort(products, ((o1, o2) -> Integer.compare((int) o2.get("rank"), (int) o1.get("rank"))));
                    model.addAttribute("productList", products);
                }
            } else {
                // C端孩子
                ChannelCUserAttribute.ClazzCLevel clazzCLevel = ChannelCUserAttribute.getClazzCLevelByClazzJie(attribute.getClazzJie());
                if (clazzCLevel != null) {
                    clazzLevel = clazzCLevel.getLevel();
                }
            }
        }
        // 获取工具栏
        MapMessage configMessage = selfStudyConfigLoader.loadSelfStudyShowConfigByClazzLevel(clazzLevel);
        if (configMessage.isSuccess()) {
            model.addAttribute("toolList", configMessage.get("result"));
        }
        return "parentmobile/learning/index";
    }

    // 自学动态 获取数据
    @RequestMapping(value = "cyclejournal.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage cycleJournal() {
        Long studentId = getRequestLong("sid");
        if (studentId == 0) {
            return MapMessage.errorMessage("学生ID为空");
        }
        StudentDetail student = studentLoaderClient.loadStudentDetail(studentId);
        if (student == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        Clazz clazz = student.getClazz();
        if (null == clazz) {
            return MapMessage.errorMessage("班级为空");
        }
        int currentPage = ConversionUtils.toInt(getRequest().getParameter("currentPage"), 1);
        int page = currentPage - 1;
        int size = 10;
        Set<Long> groupIds = deprecatedGroupLoaderClient.loadStudentGroups(student.getId(), false).stream()
                .map(GroupMapper::getId)
                .collect(Collectors.toSet());
        JournalPagination journalPagination = clazzJournalLoaderClient.getClazzJournalLoader().getClazzJournals(
                student.getId(), clazz.getId(), page, size, ClazzJournalCategory.LEARNING_CYCLE, groupIds);
        return MapMessage.successMessage().add("journalPage", journalPagination);
    }

    /**
     * 学习圈点赞
     */
    @RequestMapping(value = "like.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage like() {
        Long journalId = getRequestLong("journalId");
        Long clazzId = getRequestLong("clazzId");
        Long relevantUserId = getRequestLong("relevantUserId");
        Long studentId = getRequestLong("studentId");

        User user = raikouSystem.loadUser(studentId);
        User relevantUser = raikouSystem.loadUser(relevantUserId);
        if (user == null || relevantUser == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        if (Objects.equals(user.getId(), relevantUserId)) {
            return MapMessage.errorMessage("不允许给自己点赞");
        }
        // 自定义点赞人姓名
        String userName = user.fetchRealname() + "家长";
        return clazzJournalServiceClient.likeLearningCycle(user, userName, journalId, relevantUser, clazzId);
    }
}

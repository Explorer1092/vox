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

package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.utopia.admin.persist.entity.AdminDict;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author Longlong Yu
 * @since 下午6:20,13-10-16.
 */
@Controller
@RequestMapping("/site/dict")
public class SiteDictController extends SiteAbstractController {

    @RequestMapping(value = "dicthomepage.vpage", method = RequestMethod.GET)
    public String dictHomepage(Model model) {

        List<String> adminDictGroupNameList = adminDictPersistence.findALlGroupName();

        model.addAttribute("adminDictGroupNameList", adminDictGroupNameList);
        return "site/dict/dicthomepage";
    }

    @RequestMapping(value = "addadmindict.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addAdminDict(@RequestParam String selectedGroupName,
                                   @RequestParam String writtenGroupName,
                                   @RequestParam String groupMember,
                                   @RequestParam String description) {

        // 验证
        writtenGroupName = writtenGroupName.replaceAll("\\s", "");
        groupMember = groupMember.replaceAll("\\s", "");
        description = description.trim();
        try {
            Validate.isTrue(StringUtils.isNotBlank(writtenGroupName) || StringUtils.isNotBlank(selectedGroupName), "请选择一个group name,或者手动输入一个group name");
            Validate.isTrue(StringUtils.isNotBlank(writtenGroupName) || StringUtils.isBlank(selectedGroupName) || writtenGroupName.equals(selectedGroupName),
                    "请选择一个group name: [" + writtenGroupName + "," + selectedGroupName + "]");
            Validate.notEmpty(groupMember, "请输入group member");
            Validate.notEmpty(description, "请输入description");
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }

        String groupName = StringUtils.isEmpty(writtenGroupName) ? selectedGroupName : writtenGroupName;
        boolean newGroupName = !adminDictPersistence.findALlGroupName().contains(groupName);
        if (!newGroupName) {
            List<AdminDict> adminDictList = adminDictPersistence.findByGroupName(groupName);

            AdminDict existAdminDict = null;
            for (AdminDict it : adminDictList) {
                if (!it.getGroupMember().equals(groupMember)) continue;
                existAdminDict = it;
                break;
            }

            if (existAdminDict != null)
                return MapMessage.errorMessage("增加失败，已存在'" + groupName + "'-'" + groupMember + "'");
        }

        AdminDict adminDict = new AdminDict();
        adminDict.setGroupName(groupName);
        adminDict.setGroupMember(groupMember);
        adminDict.setDescription(description);
        adminDictPersistence.persist(adminDict);

        // 记录日志
        addAdminLog("adminDict:增加'" + groupName + "'-'" + groupMember + "'");

        MapMessage mapMessage = MapMessage.successMessage("增加'" + groupName + "'-'" + groupMember + "'成功");
        if (newGroupName)
            mapMessage.add("groupName", groupName);

        return mapMessage;
    }

    @RequestMapping(value = "dictlistchip.vpage", method = RequestMethod.POST)
    public String dictListChip(@RequestParam String groupName, Model model) {
        model.addAttribute("adminDictList", adminDictPersistence.findByGroupName(groupName));
        return "site/dict/dictlistchip";
    }

}

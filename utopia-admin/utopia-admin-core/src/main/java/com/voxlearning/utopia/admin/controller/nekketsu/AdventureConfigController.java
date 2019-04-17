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

package com.voxlearning.utopia.admin.controller.nekketsu;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.StageAppType;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.SystemAppCategory;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.SystemApp;
import com.voxlearning.utopia.service.nekketsu.consumer.AdventureLoaderClient;
import com.voxlearning.utopia.service.nekketsu.consumer.AdventureManagementClient;
import com.voxlearning.utopia.service.nekketsu.consumer.AdventureServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;

/**
 * @author GaoJian
 * @version 0.1
 * @since 2014/9/9 10:19
 */
@Controller
@RequestMapping(value = "/appmanager/nekketsu/adventure")
public class AdventureConfigController extends AbstractAdminSystemController {

    @Inject private AdventureServiceClient adventureServiceClient;
    @Inject private AdventureLoaderClient adventureLoaderClient;
    @Inject private AdventureManagementClient adventureManagementClient;

    @RequestMapping(value = "/config.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        List<SystemApp> list = adventureLoaderClient.getAllSystemApps();
        model.addAttribute("systemApps", list);
        return "nekketsu/adventureconfig";
    }

    @RequestMapping(value = "/save.vpage", method = RequestMethod.POST)
    public String saveConfig(Long id, String type, String category, Model model) {
        if (id != null && StringUtils.isNotEmpty(type) && StringUtils.isNotEmpty(category)) {
            PracticeType practiceType = practiceLoaderClient.loadPractice(id);
            if (null != practiceType) {
                SystemApp systemApp = new SystemApp();
                systemApp.setId(id);
                systemApp.setCategoryName(practiceType.getCategoryName());
                systemApp.setFileName(practiceType.getFilename());
                systemApp.setPracticeName(practiceType.getPracticeName());
                systemApp.setSize(practiceType.getGameSize());
                systemApp.setType(StageAppType.valueOf(type));
                systemApp.setCategory(SystemAppCategory.valueOf(category));
                systemApp.setValid(true);
                adventureServiceClient.addSystemApp(systemApp);
            }
        }
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        return "redirect:index.vpage";
    }

    @RequestMapping(value = "/delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String deleteSystemApp(Long id, Model model) {
        if (id != null) {
            adventureServiceClient.deleteSystemApp(id);
            return "sucess";
        }
        return "error";
    }

    @RequestMapping(value = "/changeSystemAppValid.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String changeSystemAppValid(Long id, Model model) {
        if (id != null) {
            adventureServiceClient.changeSystemAppValid(id);
            return "sucess";
        }
        return "error";
    }

    @RequestMapping(value = "/management.vpage", method = RequestMethod.GET)
    public String management() {
        return "nekketsu/adventuremgmt";
    }

    @RequestMapping(value = "/getCache.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getCache(Long userId) {
        return adventureManagementClient.getCache(userId);
    }

    @RequestMapping(value = "/refreshAppCache.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage refreshAppCache(Long userId) {
        MapMessage response = adventureManagementClient.refreshAppCache();
        if (response.isSuccess()) {
            List<SystemApp> list = adventureLoaderClient.getAllSystemApps();
            return MapMessage.successMessage().add("SystemApp", list);
        }
        return response;
    }

    @RequestMapping(value = "/refreshCache.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage refreshCache(Long userId) {
        MapMessage response = adventureManagementClient.refreshCache(userId);
        if (response.isSuccess()) {
            adventureLoaderClient.getUserAdventureByUserId(userId);
            adventureLoaderClient.getBookStagesByUserId(userId);
            return adventureManagementClient.getCache(userId);
        }
        return response;
    }

}

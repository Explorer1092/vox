package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardLoader;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardService;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardBusinessType;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardItemType;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardSubject;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardCategory;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardItem;
import com.voxlearning.utopia.service.parentreward.api.mapper.ParentRewardItemWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author malong
 * @since 2017/7/20
 */
@Controller
@RequestMapping(value = "opmanager/parentreward")
@Slf4j
public class ParentRewardManagerController extends AbstractAdminSystemController {
    @ImportService(interfaceClass = ParentRewardLoader.class)
    private ParentRewardLoader parentRewardLoader;
    @ImportService(interfaceClass = ParentRewardService.class)
    private ParentRewardService parentRewardService;
    @StorageClientLocation(storage = "17-pmc")
    private StorageClient storageClient;

    @RequestMapping(value = "itemlist.vpage", method = RequestMethod.GET)
    public String getItemList(Model model) {
        int page = getRequestInt("page", 1);
        if (page < 0) page = 1;
        Pageable pageable = new PageRequest(page - 1, 20);
        List<ParentRewardItem> itemList = parentRewardLoader.loadParentRewardItemsFromDB().getUninterruptibly();
        List<ParentRewardItemWrapper> wrapperList = new ArrayList<>();
        itemList.stream().sorted(Comparator.comparingInt(ParentRewardItem::getRank)).forEach(e -> wrapperList.add(itemConvert2Wrapper(e)));
        Page<ParentRewardItemWrapper> itemPage = PageableUtils.listToPage(wrapperList, pageable);
        model.addAttribute("itemPage", itemPage);
        model.addAttribute("currentPage", itemPage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", itemPage.getTotalPages());
        model.addAttribute("hasPrev", itemPage.hasPrevious());
        model.addAttribute("hasNext", itemPage.hasNext());
        List<ParentRewardCategory> categoryList = parentRewardLoader.loadParentRewardCategorysFromDB().getUninterruptibly()
                .stream()
                .sorted(Comparator.comparingInt(ParentRewardCategory::getRank))
                .collect(Collectors.toList());
        Map<String, Long> categoryMap = categoryList.stream().collect(Collectors.toMap(ParentRewardCategory::getTitle, ParentRewardCategory::getId));
        model.addAttribute("categoryMap", categoryMap);

        Map<String, String> itemTypeMap = new HashMap<>();
        for (ParentRewardItemType itemType : ParentRewardItemType.values()) {
            itemTypeMap.put(itemType.name(), itemType.getDesc());
        }
        model.addAttribute("itemTypeMap", itemTypeMap);

        Map<String, String> subjectMap = new HashMap<>();
        for (ParentRewardSubject subject : ParentRewardSubject.values()) {
            subjectMap.put(subject.name(), subject.getValue());
        }
        model.addAttribute("subjectMap", subjectMap);
        Map<String, String> businessMap = new HashMap<>();
        for (ParentRewardBusinessType businessType : ParentRewardBusinessType.values()) {
            businessMap.put(businessType.name(), businessType.getValue());
        }
        model.addAttribute("businessMap", businessMap);

        return "opmanager/parentreward/itemlist";
    }

    @RequestMapping(value = "getitem.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getItem() {
        Long id = getRequestLong("itemId");
        ParentRewardItem item = parentRewardLoader.loadParentRewardItemFromDB(id).getUninterruptibly();
        if (item == null) {
            return MapMessage.errorMessage("奖励项不存在");
        }
        return MapMessage.successMessage().add("item", item);
    }

    @RequestMapping(value = "additem.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addItem() {
        Long id = getRequestLong("id");
        String key = getRequestString("key");
        Long categoryId = getRequestLong("categoryId");
        String business = getRequestString("business");
        String subject = getRequestString("subject");
        String title = getRequestString("title");
        String levelTitle = getRequestString("levelTitle");
        String type = getRequestString("type");
        Integer count = getRequestInt("count");
        int rank = getRequestInt("rank");
        String redirectUrl = getRequestString("redirectUrl");
        String secondaryPageUrl = getRequestString("secondaryPageUrl");
        Integer sendExpire = getRequestInt("sendExpireDays", 0);
        Integer receiveExpire = getRequestInt("receiveExpireDays", 0);
        Boolean sendPush = getRequestBool("sendGeneratePush");
        String color = getRequestString("color");
        String desc = getRequestString("desc");
        ParentRewardItem item = parentRewardLoader.loadParentRewardItemFromDB(id).getUninterruptibly();
        if (item == null)
            item = new ParentRewardItem();
        item.setKey(key);
        item.setCategoryId(categoryId);
        item.setBusiness(business);
        item.setSubject(subject);
        item.setTitle(title);
        item.setLevelTitle(levelTitle);
        item.setType(type);
        item.setCount(count);
        item.setRank(rank);
        item.setRedirectUrl(redirectUrl);
        item.setSecondaryPageUrl(secondaryPageUrl);
        item.setSendExpireDays(sendExpire);
        item.setReceiveExpireDays(receiveExpire);
        item.setSendGeneratePush(sendPush);
        item.setColor(color);
        item.setDescription(desc);

        try {
            ParentRewardItem upsert = parentRewardService.upsertParentRewardItem(item).getUninterruptibly();
            if (upsert == null) {
                return MapMessage.errorMessage("保存奖励项失败");
            }
            return MapMessage.successMessage().add("id", upsert.getId());
        } catch (Exception ex) {
            ex.printStackTrace();
            return MapMessage.errorMessage("保存奖励项失败");
        }
    }

    @RequestMapping(value = "categorylist.vpage", method = RequestMethod.GET)
    public String getCategoryList(Model model) {
        int page = getRequestInt("page", 1);
        if (page < 0) page = 1;
        Pageable pageable = new PageRequest(page - 1, 20);
        List<ParentRewardCategory> categoryList = parentRewardLoader.loadParentRewardCategorysFromDB().getUninterruptibly()
                .stream()
                .sorted(Comparator.comparingInt(ParentRewardCategory::getRank))
                .collect(Collectors.toList());
        Page<ParentRewardCategory> categoryPage = PageableUtils.listToPage(categoryList, pageable);
        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("currentPage", categoryPage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", categoryPage.getTotalPages());
        model.addAttribute("hasPrev", categoryPage.hasPrevious());
        model.addAttribute("hasNext", categoryPage.hasNext());

        return "opmanager/parentreward/categorylist";
    }

    @RequestMapping(value = "getcategory.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getCategory() {
        Long id = getRequestLong("categoryId");
        ParentRewardCategory category = parentRewardLoader.loadParentRewardCategoryFromDB(id).getUninterruptibly();
        if (category == null) {
            return MapMessage.errorMessage("奖励类型不存在");
        }
        return MapMessage.successMessage().add("category", category);
    }

    @RequestMapping(value = "addcategory.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addCategory() {
        Long id = getRequestLong("id");
        String key = getRequestString("key");
        String title = getRequestString("title");
        Integer rank = getRequestInt("rank");
        ParentRewardCategory category = parentRewardLoader.loadParentRewardCategoryFromDB(id).getUninterruptibly();
        if (category == null)
            category = new ParentRewardCategory();
        category.setKey(key);
        category.setTitle(title);
        category.setRank(rank);

        try {
            ParentRewardCategory upsert = parentRewardService.upsertParentRewardCategory(category).getUninterruptibly();
            if (upsert == null) {
                return MapMessage.errorMessage("添加奖励类型失败");
            }
            return MapMessage.successMessage().add("id", upsert.getId());
        } catch (Exception ex) {
            return MapMessage.errorMessage("奖励类型已经存在");
        }
    }

    private ParentRewardItemWrapper itemConvert2Wrapper(ParentRewardItem source) {
        ParentRewardItemWrapper wrapper = new ParentRewardItemWrapper();
        wrapper.setId(source.getId());
        wrapper.setKey(source.getKey());
        if (StringUtils.isNotEmpty(source.getBusiness())) {
            ParentRewardBusinessType businessType = ParentRewardBusinessType.of(source.getBusiness());
            if (businessType != null) {
                wrapper.setBusiness(businessType.getValue());
            }
        }
        if (StringUtils.isNotEmpty(source.getSubject())) {
            ParentRewardSubject subject = ParentRewardSubject.of(source.getSubject());
            if (subject != null) {
                wrapper.setSubject(subject.getValue());
            }
        }
        wrapper.setTitle(source.getTitle());
        wrapper.setLevelTitle(source.getLevelTitle());
        wrapper.setType(source.getType());
        wrapper.setCount(source.getCount());
        wrapper.setRank(source.getRank());
        wrapper.setRedirectUrl(source.getRedirectUrl());
        wrapper.setSecondaryPageUrl(source.getSecondaryPageUrl());
        wrapper.setSendExpireDays(source.getSendExpireDays());
        wrapper.setReceiveExpireDays(source.getReceiveExpireDays());
        wrapper.setSendGeneratePush(source.getSendGeneratePush());
        wrapper.setDisabled(source.getDisabled());
        wrapper.setIcon(source.getIcon());
        wrapper.setLevelIcon(source.getLevelIcon());
        wrapper.setColor(source.getColor());
        wrapper.setDescription(source.getDescription());
        ParentRewardCategory category = parentRewardLoader.loadParentRewardCategoryFromDB(source.getCategoryId()).getUninterruptibly();
        if (category != null) {
            wrapper.setCategoryTitle(category.getTitle());
        }
        return wrapper;
    }


    @RequestMapping(value = "/uploadimg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadParentRewardImg(MultipartFile inputFile) {
        Long id = getRequestLong("id");
        String type = getRequestString("type");
        if (inputFile == null) {
            return MapMessage.errorMessage("没有可上传的文件");
        }
        String suffix = StringUtils.substringAfterLast(inputFile.getOriginalFilename(), ".");
        if (StringUtils.isBlank(suffix)) {
            suffix = "jpg";
        }
        StorageMetadata storageMetadata = new StorageMetadata();
        storageMetadata.setContentLength(inputFile.getSize());
        String env = "parentreward/";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = "parentreward/test/";
        }
        String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
        String fileName = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + suffix;
        try {
            String realName = storageClient.upload(inputFile.getInputStream(), fileName, path, storageMetadata);
            String fileUrl = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + realName;
            if ("category".equals(type)) {
                ParentRewardCategory category = parentRewardLoader.loadParentRewardCategoryFromDB(id).getUninterruptibly();
                category.setIconUrl(fileUrl);
                parentRewardService.upsertParentRewardCategory(category).getUninterruptibly();
            } else if ("itemScore".equals(type)) {
                ParentRewardItem item = parentRewardLoader.loadParentRewardItemFromDB(id).getUninterruptibly();
                item.setIcon(fileUrl);
                parentRewardService.upsertParentRewardItem(item);
            } else if ("itemLevel".equals(type)) {
                ParentRewardItem item = parentRewardLoader.loadParentRewardItemFromDB(id).getUninterruptibly();
                item.setLevelIcon(fileUrl);
                parentRewardService.upsertParentRewardItem(item);
            }

        } catch (Exception ex) {
            return MapMessage.errorMessage("上传图片失败");
        }

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "/onlineitem.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage onlineItem() {
        Long itemId = getRequestLong("itemId");
        ParentRewardItem item = parentRewardLoader.loadParentRewardItemFromDB(itemId).getUninterruptibly();
        if (item == null) {
            return MapMessage.errorMessage("奖励项不存在");
        }
        item.setDisabled(Boolean.FALSE);
        try {
            parentRewardService.upsertParentRewardItem(item);
        } catch (Exception ex) {
            return MapMessage.errorMessage("上线奖励项失败");
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "/offlineitem.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage offlineItem() {
        Long itemId = getRequestLong("itemId");
        ParentRewardItem item = parentRewardLoader.loadParentRewardItemFromDB(itemId).getUninterruptibly();
        if (item == null) {
            return MapMessage.errorMessage("奖励项不存在");
        }
        item.setDisabled(Boolean.TRUE);
        try {
            parentRewardService.upsertParentRewardItem(item);
        } catch (Exception ex) {
            return MapMessage.errorMessage("下线奖励项失败");
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "/onlinecategory.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage onlineCategory() {
        Long id = getRequestLong("id");
        ParentRewardCategory category = parentRewardLoader.loadParentRewardCategoryFromDB(id).getUninterruptibly();
        if (category == null) {
            return MapMessage.errorMessage("奖励类型不存在");
        }
        category.setDisabled(Boolean.FALSE);
        try {
            parentRewardService.upsertParentRewardCategory(category);
        } catch (Exception ex) {
            return MapMessage.errorMessage("上线奖励类型失败");
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "/offlinecategory.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage offlineCategory() {
        Long id = getRequestLong("id");
        ParentRewardCategory category = parentRewardLoader.loadParentRewardCategoryFromDB(id).getUninterruptibly();
        if (category == null) {
            return MapMessage.errorMessage("奖励类型不存在");
        }
        category.setDisabled(Boolean.TRUE);
        try {
            parentRewardService.upsertParentRewardCategory(category);
        } catch (Exception ex) {
            return MapMessage.errorMessage("下线奖励类型失败");
        }
        return MapMessage.successMessage();
    }
}

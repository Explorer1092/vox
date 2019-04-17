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

package com.voxlearning.utopia.admin.controller.opmanager.advertise;


import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.utopia.admin.controller.site.CrmAdvertisementController;
import com.voxlearning.utopia.admin.data.AdQueryContext;
import com.voxlearning.utopia.admin.persist.entity.AdminLog;
import com.voxlearning.utopia.service.advertisement.cache.AdvertisementCache;
import com.voxlearning.utopia.service.config.api.constant.*;
import com.voxlearning.utopia.service.config.api.entity.*;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.user.api.constants.UserTagType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserTag;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import com.voxlearning.utopia.service.user.cache.UserCache;
import com.voxlearning.utopia.service.user.consumer.UserManagementClient;
import com.voxlearning.utopia.service.user.consumer.UserTagLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserTagQueueClient;
import com.voxlearning.utopia.service.vendor.api.CRMVendorService;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNewsTag;
import lombok.Cleanup;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.config.api.constant.AdDetailAuditStatus.PENDING_LV1;
import static com.voxlearning.utopia.service.config.api.constant.AdvertisementSlotType.*;
import static com.voxlearning.utopia.service.config.api.constant.AdvertisementTargetType.*;

/**
 * 广告管理平台后台
 * 风格尽量与 {@link CrmAdvertisementController} 统一，以便功能的切换
 * Created by Yuechen Wang on 2016-05-17.
 */
@Controller
@RequestMapping("/opmanager/advertisement")
public class AdvertiseManageController extends AbstractAdvertiseController {

    @Inject private EmailServiceClient emailServiceClient;
    @Inject private UserTagQueueClient userTagQueueClient;
    @Inject private UserTagLoaderClient userTagLoaderClient;
    @Inject private UserManagementClient userManagementClient;

    @ImportService(interfaceClass = CRMVendorService.class)
    private CRMVendorService crmVendorService;

    // 需要上传两张广告图片的广告位
    private static final List<String> doubleSourceSlot = Arrays.asList(
            "221001" // 家长APP-机构导流list页-顶部运营位
            , "220703" // 家长APP-上课了-组合广告位
            , "220107" // 家长APP-首页-热门活动1
            , "220108" // 家长APP-首页-热门活动2
            , "220109" // 家长APP-首页-热门活动3
            , "220110" // 家长APP-首页-热门活动4
            , "220111" // 家长APP端-首页-直播导流广告位
    );

    // 学生app-班级动态-运营 广告位
    private static final String CRM_ADVERTISE_SLOT_ID = "320702";

    //-----------------------------------------------------------------------------
    //--------------                广告位相关操作                      -------------
    //-----------------------------------------------------------------------------

    @RequestMapping(value = "slotindex.vpage", method = RequestMethod.GET)
    public String slotIndex(Model model) {
        String qSlotId = getRequestString("qSlotId");
        int page = Integer.max(1, getRequestInt("page", 1));
        int userType = getRequestInt("ut");
        String endpoint = getRequestString("ep");
        Pageable pageable = new PageRequest(page - 1, 10);
        List<AdvertisementSlot> slotList = new ArrayList<>();
        if (StringUtils.isBlank(qSlotId)) {
            slotList = crmConfigService.$loadAdvertisementSlots()
                    .stream()
                    .filter(ut -> userType <= 0 || ut.getUserType() == userType)
                    .filter(ep -> StringUtils.isBlank(endpoint) || endpoint.equals(ep.getEndpoint()))
                    .collect(Collectors.toList());
        } else {
            AdvertisementSlot slot = crmConfigService.$loadAdvertisementSlot(qSlotId);
            if (slot != null) slotList = Collections.singletonList(slot);
        }
        Page<AdvertisementSlot> slotPage = PageableUtils.listToPage(slotList, pageable);
        model.addAttribute("slotPage", slotPage);
        model.addAttribute("currentPage", Integer.min(page, slotPage.getTotalPages()));
        model.addAttribute("totalPage", slotPage.getTotalPages());
        model.addAttribute("hasPrev", slotPage.hasPrevious());
        model.addAttribute("hasNext", slotPage.hasNext());
        model.addAttribute("ut", userType);
        model.addAttribute("ep", endpoint);
        model.addAttribute("qSlotId", qSlotId);
        model.addAttribute("slotTypeList", Arrays.asList(AdvertisementSlotType.values()));
        return "opmanager/advertisement/slotindex";
    }

    @RequestMapping(value = "saveslot.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveSlot() {
        String mode = getRequestString("mode");
        String slotId = getRequestString("id");
        String slotName = getRequestString("name");
        int width = getRequestInt("width");
        int height = getRequestInt("height");
        // 暂不校验这两项内容
        int userType = getRequestInt("userType");
        String endpoint = getRequestString("endpoint");
        String slotType = getRequestString("type");
        int capacity = getRequestInt("capacity");
        try {
            AdvertisementSlot slot = crmConfigService.$loadAdvertisementSlot(slotId);
            if (!StringUtils.equals(mode, "edit") && !StringUtils.equals(mode, "add")) {
                return MapMessage.errorMessage("一个不可理解的错误？");
            }
            if (StringUtils.equals(mode, "edit") && slot == null) {
                return MapMessage.errorMessage("无效的广告位信息！");
            }
            if (StringUtils.equals(mode, "add")) {
                if (slot != null) {
                    return MapMessage.errorMessage("广告位：" + slotId + " 已经存在！");
                }
                slot = new AdvertisementSlot();
            }
            slot.setId(slotId);
            slot.setName(slotName);
            slot.setUserType(userType);
            slot.setEndpoint(endpoint);
            slot.setWidth(width);
            slot.setHeight(height);
            slot.setType(slotType);
            slot.setCapacity(capacity);

            MapMessage msg = validateSlot(slot);
            if (!msg.isSuccess()) {
                return msg;
            }
            slot = crmConfigService.$upsertAdvertisementSlot(slot);
            return new MapMessage().setSuccess(slot != null);
        } catch (Exception ex) {
            logger.error("Failed save slot : {}", ex.getMessage(), ex);
            return MapMessage.errorMessage("保存广告位失败：{}", ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "delslot.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteSlot(@RequestParam String slotId) {
        if (StringUtils.isBlank(slotId)) {
            return MapMessage.errorMessage("无效的广告位ID");
        }
        AdvertisementSlot slot = crmConfigService.$loadAdvertisementSlot(slotId);
        if (slot == null) {
            return MapMessage.errorMessage("无效的广告位ID");
        }
        // 删除广告位的时候不能有关联的未删除的广告,
        List<AdvertisementDetail> adList = advertisementLoaderClient.getLocalAdvertisementBuffer()
                .loadAvailableAds(slotId)
                .stream()
                .filter(AdvertisementDetail::isAvailableAdvertisement)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(adList)) {
            return MapMessage.errorMessage("该广告位还有关联的广告，请先删除广告！");
        }
        boolean ret = crmConfigService.$removeAdvertisementSlot(slotId);
        return new MapMessage().setSuccess(ret);
    }

    private MapMessage validateSlot(AdvertisementSlot slot) {
        StringBuilder msg = new StringBuilder();
        if (slot.getId().length() != 6) {
            msg.append("广告位ID输入不合要求！").append("\n");
        }
        AdvertisementSlotType slotType = AdvertisementSlotType.safeParse(slot.getType());
        if (StringUtils.isBlank(slot.getType())) {
            msg.append("类型不能为空！").append("\n");
        } else if (slotType == null) {
            msg.append("类型只能为“轮播”、“随机”、“弹窗”、“Popup消息”、“闪屏”、“纯文本”！").append("\n");
        }
        if (slotType != null && slotType != 纯文本 && slot.getWidth() * slot.getHeight() == 0) {
            msg.append("请填写正常的图片尺寸！").append("\n");
        }
        if (StringUtils.isBlank(slot.getName())) {
            msg.append("广告位名称不能为空！请妥善填写！").append("\n");
        }
        if (slot.getCapacity() <= 0) {
            msg.append("广告位容纳数量至少为1个！").append("\n");
        }
        if (msg.length() > 0) {
            return MapMessage.errorMessage(msg.toString());
        }
        return MapMessage.successMessage();
    }

    //-----------------------------------------------------------------------------
    //--------------              广告列表页相关操作                    -------------
    //-----------------------------------------------------------------------------
    @RequestMapping(value = "adindex.vpage", method = RequestMethod.GET)
    public String queryAds(Model model) {

        // login user
        String currentUser = getCurrentAdminUser().getAdminUserName();

        // 广告列表搜索项 过滤特殊广告位
        List<AdvertisementSlot> slotList = crmConfigService.$loadAdvertisementSlots();

        List<AdvertisementDetail> ad = advertisementLoaderClient.loadAllAdDetails();

        Set<String> creatorList = new HashSet<>();

        // 根据用户身份进行判断
        if (getAuditorSlotConfig().containsKey(currentUser)) { // 审核者
            // 审核者指定了广告位ID,根据广告位ID进行过滤
            String slotRegex = getAuditorSlotConfig().get(currentUser);
            slotList = slotList.stream()
                    .filter(p -> p.getId().matches(slotRegex))
                    .collect(Collectors.toList());

            Set<String> slotIds = slotList.stream().map(AdvertisementSlot::getId).collect(Collectors.toSet());
            creatorList = ad.stream()
                    .filter(p -> slotIds.contains(p.getAdSlotId()))
                    .map(AdvertisementDetail::getCreator)
                    .sorted()
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } else { // 创建者只能看到自己的提交的广告
            creatorList.add(currentUser);

            ad = ad.stream()
                    .filter(p -> StringUtils.equals(p.getCreator(), currentUser))
                    .collect(Collectors.toList());

            Set<String> slotIds = ad.stream().map(AdvertisementDetail::getAdSlotId).collect(Collectors.toSet());

            slotList = slotList.stream()
                    .filter(p -> slotIds.contains(p.getId()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("slotList", slotList);
        model.addAttribute("creatorList", creatorList);
        model.addAttribute("statusList", AdDetailStatus.toKeyValuePairs());
        model.addAttribute("categoryList", AdDetailCategory.values());
        model.addAttribute("typeList", AdDetailType.values());
        model.addAttribute("prePath", (RuntimeMode.isUsingProductionData() ? "http://www.17zuoye.com" : "http://www.test.17zuoye.net") + "/gridfs/");
        return "opmanager/advertisement/adindex";
    }

    @RequestMapping(value = "adindex.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage queryAdDetails() {
        try {
            AdQueryContext context = new AdQueryContext();
            requestFillEntity(context);
            String currentUser = getCurrentAdminUser().getAdminUserName();

            List<AdvertisementDetail> adCandidates;
            if (context.byAdId()) {
                adCandidates = Collections.singletonList(advertisementLoaderClient.loadAdDetail(context.getAdId()));
            } else {
                // 全部Load出来再filter呗
                adCandidates = advertisementLoaderClient.loadAllAdDetails();
            }

            // 做一下广告权限过滤
            if (getAuditorSlotConfig().containsKey(currentUser)) {
                String slotRegex = getAuditorSlotConfig().get(currentUser);

                Set<String> slotIds = crmConfigService.$loadAdvertisementSlots().stream()
                        .filter(p -> p.getId().matches(slotRegex))
                        .map(AdvertisementSlot::getId).collect(Collectors.toSet());

                adCandidates = adCandidates.stream()
                        .filter(p -> slotIds.contains(p.getAdSlotId()))
                        .collect(Collectors.toList());

            } else {  // 创建人
                adCandidates = adCandidates.stream()
                        .filter(p -> StringUtils.equals(p.getCreator(), currentUser))
                        .collect(Collectors.toList());
            }

            Page<AdvertisementDetail> adsPage = context.filterList(adCandidates);

            // 广告列表搜索项 过滤特殊广告位
            Map<String, AdvertisementSlot> slotMap = crmConfigService.$loadAdvertisementSlots()
                    .stream()
                    .collect(Collectors.toMap(AdvertisementSlot::getId, Function.identity(), (u, v) -> u));

            return MapMessage.successMessage()
                    .add("pageNum", context.getPage())
                    .add("adsList", generateAdInfo(adsPage.getContent(), slotMap))
                    .add("totalPage", adsPage.getTotalPages())
                    .add("hasNext", adsPage.hasNext())
                    .add("hasPre", adsPage.hasPrevious())
                    .add("currentPage", context.getPage());
        } catch (Exception ex) {
            logger.error("Failed load advertisement details in CRM", ex);
            return MapMessage.errorMessage("数据加载异常！");
        }

    }

    //-----------------------------------------------------------------------------
    //--------------              广告排期相关操作                    -------------
    //-----------------------------------------------------------------------------
    @RequestMapping(value = "adarrangement.vpage", method = RequestMethod.GET)
    public String adArrangement(Model model) {
        String queryStr = getRequestString("queryDate");
        String week = getRequestString("week");
        Date queryDate;
        if (StringUtils.isBlank(queryStr) || DateUtils.stringToDate(queryStr, "yyyy-MM-dd") == null) {
            queryDate = new Date();
        } else {
            queryDate = DateUtils.stringToDate(queryStr, "yyyy-MM-dd");
            if (StringUtils.isNotBlank(week)) {
                if ("pre".equals(week)) {
                    queryDate = DateUtils.calculateDateDay(queryDate, -7);
                } else if ("next".equals(week)) {
                    queryDate = DateUtils.calculateDateDay(queryDate, 7);
                }
            }
        }
        // 全部Load出来再filter呗
        Map<String, List<AdvertisementDetail>> adGroupBySlot = advertisementLoaderClient.getLocalAdvertisementBuffer()
                .loadAllAds().stream()
                .filter(AdvertisementDetail::isAvailableAdvertisement)
                .sorted(Comparator.comparing(a -> Integer.valueOf(a.getAdSlotId())))
                .collect(Collectors.groupingBy(AdvertisementDetail::getAdSlotId, Collectors.toList()));

        List<Map<String, Object>> result = new ArrayList<>();
        LinkedHashMap<String, String> slotMap = crmConfigService.$loadAdvertisementSlots()
                .stream()
                .collect(Collectors.toMap(
                        AdvertisementSlot::getId,
                        AdvertisementSlot::getName,
                        (u, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", u));
                        },
                        LinkedHashMap::new)
                );
        for (Map.Entry<String, String> entry : slotMap.entrySet()) {
            Map<String, Object> slotData = new LinkedHashMap<>();
            slotData.put("id", entry.getKey());
            slotData.put("title", entry.getValue());

            List<AdvertisementDetail> adDetails = adGroupBySlot.get(entry.getKey());
            List<Map<String, Object>> items = new ArrayList<>();
            // 初始化Calendar
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(queryDate);
            calendar.setFirstDayOfWeek(Calendar.SUNDAY);
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            for (int i = 0; i < 7; i++) {
                Date indexDate = calendar.getTime();
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("day", calendar.get(Calendar.DATE));
                List<Map<String, Object>> arrange = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(adDetails)) {
                    arrange = adDetails.stream()
                            .filter(ad -> isShowableAd(ad, indexDate))
                            .map(this::wrapAdvertisement)
                            .collect(Collectors.toList());
                }
                item.put("list", arrange);
                items.add(item);
                calendar.add(Calendar.DAY_OF_WEEK, 1);
            }
            slotData.put("items", items);
            result.add(slotData);
        }
        model.addAttribute("result", result);
        model.addAttribute("queryDate", queryDate);
        model.addAttribute("week", week);
        return "opmanager/advertisement/adarrangement";
    }

    private Map<String, Object> wrapAdvertisement(AdvertisementDetail ad) {
        Map<String, Object> adMap = new LinkedHashMap<>();
        adMap.put("id", ad.getId());
        adMap.put("name", ad.getName());
        return adMap;
    }

    private boolean isShowableAd(AdvertisementDetail ad, Date date) {
        if (ad == null || !ad.isAvailableAdvertisement()) {
            return false;
        }
        Long start = ad.getShowTimeStart() == null ?
                0 : Long.valueOf(DateUtils.dateToString(ad.getShowTimeStart(), "yyyyMMdd"));
        Long end = ad.getShowTimeStart() == null ?
                20501231L : Long.valueOf(DateUtils.dateToString(ad.getShowTimeEnd(), "yyyyMMdd"));
        Long index = Long.valueOf(DateUtils.dateToString(date, "yyyyMMdd"));
        return index >= start && index <= end;
    }

    //-----------------------------------------------------------------------------
    //--------------              广告详情页相关操作                    -------------
    //-----------------------------------------------------------------------------
    @RequestMapping(value = "addetail.vpage", method = RequestMethod.GET)
    public String adDetail(Model model) {
        Long adId = getRequestLong("adId");
        Map<String, String> auditorConfig = getAuditorSlotConfig();
        String currentUser = getCurrentAdminUser().getAdminUserName();
        if (adId != 0L) {
            AdvertisementDetail advertisement = crmConfigService.$loadAdvertisementDetail(adId);
            if (advertisement != null) {
                // 如果没有权限直接返回
                if (!checkUserPrivilege(advertisement.getCreator(), advertisement) && !auditorConfig.containsKey(currentUser)) {
                    return "redirect: /opmanager/advertisement/adindex.vpage";
                }
                AdvertisementSlot slot = crmConfigService.$loadAdvertisementSlot(advertisement.getAdSlotId());
                model.addAttribute("hasGif", slot != null && (AdvertisementSlotType.valueOf(slot.getType()) == AdvertisementSlotType.闪屏 || doubleSourceSlot.contains(slot.getId())));
                model.addAttribute("hasExt", slot != null && (AdvertisementSlotType.valueOf(slot.getType()) == AdvertisementSlotType.闪屏));
                model.addAttribute("ad", advertisement);
                model.addAttribute("periods", advertisement.fetchPeriod());
                model.addAttribute("editable", AdDetailStatus.of(advertisement.getStatus()) != AdDetailStatus.ONLINE);
            } else {
                adId = 0L;
            }
        }
        model.addAttribute("adId", adId);
        model.addAttribute("prePath", getPrePath());
        //白名单过滤
        List<AdvertisementSlot> adSlots = crmConfigService.$loadAdvertisementSlots();

        model.addAttribute("slotList", adSlots);
        model.addAttribute("categoryList", AdDetailCategory.values());
        model.addAttribute("typeList", AdDetailType.values());
        //广告tag，针对支持tag的广告展示，默认加载到页面 display：hidden
        List<JxtNewsTag> jxtNewsTags = crmVendorService.$loadJxtNewsTagList()
                .stream()
                .filter(e -> !e.isDisabledTrue())
                .collect(Collectors.toList());
        // 自上而下产生树
        Map<String, Object> tagTree = generateTagTree(0, "root", jxtNewsTags, true);
        //广告标签渲染
        List<AdvertisementMark> marks = crmConfigService.$loadMarksByAdId(adId);
        if (CollectionUtils.isNotEmpty(marks)) {
            List<String> markIds = marks.stream().map(AdvertisementMark::getMarkId).collect(Collectors.toList());
            model.addAttribute("markIds", JsonUtils.toJson(markIds));
        }
        if (RuntimeMode.isTest()) {
            model.addAttribute("isTest", "0");
        } else {
            model.addAttribute("isTest", "1");
        }
        model.addAttribute("displayMarkSlotId", CRM_ADVERTISE_SLOT_ID);
        model.addAttribute("tagTree", JsonUtils.toJson(tagTree));
        model.addAttribute("isAdmin", isAdmin());
        return "opmanager/advertisement/addetail";
    }

    @RequestMapping(value = "saveaddetail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveAdDetail(@RequestParam Long adId) {
        // 获取参数
        String name = getRequestString("name");
        String description = getRequestString("description");
        String slotId = getRequestString("slotId");
        String resourceUrl = StringUtils.trim(getRequestString("resourceUrl"));
        Boolean redirectWithUid = getRequestBool("redirectWithUid");
        Integer priority = getRequestInt("priority", 10);
        Date showTimeStart = DateUtils.stringToDate(getRequestString("showTimeStart"), DateUtils.FORMAT_SQL_DATETIME);
        Date showTimeEnd = DateUtils.stringToDate(getRequestString("showTimeEnd"), DateUtils.FORMAT_SQL_DATETIME);
        String adCode = getRequestString("code");
        String content = getRequestString("content");
        String btnContent = getRequestString("btnContent");
        Boolean showLogo = getRequestBool("showLogo");
        Integer duration = getRequestInt("duration");
        Integer viewQuota = getRequestInt("viewQuota");
        Integer clickQuota = getRequestInt("clickQuota");
        Integer showLimit = getRequestInt("showLimit");
        Integer clickLimit = getRequestInt("clickLimit");
        Integer userDailyShowLimit = getRequestInt("userDailyShowLimit");
        Integer dailyClickLimit = getRequestInt("dailyClickLimit");
        String periods = getRequestString("periods");
        String businessCategory = getRequestString("businessCategory");
        String type = getRequestString("type");
        String markIds = getRequestString("markIds");
        String markText = getRequestString("markText");
        Boolean logCollected = getRequestBool("logCollected");
        Boolean multiAccountSupport = getRequestBool("multiAccountSupport");
        // code不存在的话随机生成一串
        if (StringUtils.isBlank(adCode)) {
            adCode = RandomUtils.randomString(12);
        }
        try {
            AdvertisementDetail advertisement;
            if (adId == 0L) {
                advertisement = new AdvertisementDetail();
                advertisement.setCreator(getCurrentAdminUser().getAdminUserName());
                advertisement.setCreatorName(getCurrentAdminUser().getRealName());
            } else {
                advertisement = crmConfigService.$loadAdvertisementDetail(adId);
                if (advertisement == null) {
                    advertisement = new AdvertisementDetail();
                    advertisement.setCreator(getCurrentAdminUser().getAdminUserName());
                    advertisement.setCreatorName(getCurrentAdminUser().getRealName());
                }
            }
            // 校验输入信息
            advertisement.setName(name);
            advertisement.setAdCode(adCode);
            advertisement.setAdContent(content);
            advertisement.setBtnContent(btnContent);
            advertisement.setDescription(description);
            advertisement.setAdSlotId(slotId);
            // 非管理员不允许使用优先级
            if (isAdmin()) {
                advertisement.setPriority(priority);
            } else {
                advertisement.setPriority(AdvertisementPriority.DEFAULT.getLevel());
            }
            advertisement.setResourceUrl(resourceUrl);
            advertisement.setRedirectWithUid(redirectWithUid);
            advertisement.setShowTimeStart(showTimeStart);
            advertisement.setShowTimeEnd(showTimeEnd);
            advertisement.setShowLogo(showLogo);
            advertisement.setDisplayDuration(duration);
            advertisement.setClickLimit(clickLimit);
            advertisement.setUserViewQuota(viewQuota);
            advertisement.setUserClickQuota(clickQuota);
            advertisement.setUserDailyShowLimit(userDailyShowLimit);
            advertisement.setShowLimit(showLimit);
            advertisement.setDailyClickLimit(dailyClickLimit);
            advertisement.setDisplayPeriod(periods);
            advertisement.setBusinessCategory(businessCategory);
            advertisement.setType(type);
            // 保存的时候重置一下状态
            advertisement.setStatus(AdDetailStatus.DRAFT.getStatus());
            advertisement.setAuditStatus(AdDetailAuditStatus.DRAFT.getStatus());
            advertisement.setLogCollected(logCollected);
            advertisement.setMultiAccountSupport(multiAccountSupport);
            MapMessage validMsg = validateDetail(advertisement);
            if (!validMsg.isSuccess()) {
                return validMsg;
            }
            // 上传完成，保存实体
            MapMessage returnMsg;
            String op = "新建广告";
            if (adId == 0L) {
                returnMsg = advertisementServiceClient.getAdvertisementService().createAdvertisement(advertisement);
                adId = SafeConverter.toLong(returnMsg.get("id"));
            } else {
                returnMsg = advertisementServiceClient.getAdvertisementService().updateAdvertisement(adId, advertisement);
                op = "编辑广告";
            }
            returnMsg.setInfo(returnMsg.isSuccess() ? "保存成功" : "保存失败");
            saveOperationLog(adId, op, returnMsg.getInfo());
            //设置id 保存标签用到
            advertisement.setId(adId);
            saveMarks(advertisement, markIds, markText);
            return returnMsg.add("warning", validMsg.getInfo());
        } catch (Exception ex) {
            logger.error("Save advertisement error! id={}, ex={}", adId, ex.getMessage(), ex);
            return MapMessage.errorMessage("保存广告失败:{}", ex.getMessage(), ex);
        }
    }

    /**
     * 关联标签
     *
     * @param detail  广告对象
     * @param markIds 标签关联id ，多个以“，”分割
     */
    private void saveMarks(AdvertisementDetail detail, String markIds, String markText) {
        //广告id不合法
        if (detail == null || detail.getId() == null || detail.getId() <= 0) {
            return;
        }

        crmConfigService.$disabledMarks(detail.getId());
        if (StringUtils.equals(detail.getAdSlotId(), CRM_ADVERTISE_SLOT_ID)) {
            //新增标签 标签内容为空
            if (StringUtils.isBlank(markIds) || StringUtils.isBlank(markText)) {
                return;
            }
            String[] arrayMarkId = markIds.split(",");
            String[] arrayMarkTxt = markText.split(",");
            //标签id与内容长度不匹配
            if (arrayMarkId.length != arrayMarkTxt.length) {
                return;
            }
            for (int i = 0; i < arrayMarkId.length; i++) {
                AdvertisementMark advertisementMark = new AdvertisementMark();
                advertisementMark.setMarkId(arrayMarkId[i]);
                advertisementMark.setMarkText(arrayMarkTxt[i]);
                advertisementMark.setAdId(detail.getId());
                crmConfigService.$saveAdvertisementMark(detail.getId(), advertisementMark);
            }
        }
    }


    @RequestMapping(value = "deladdetail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteAdDetail(@RequestParam Long adId) {
        try {
            if (adId == 0L) {
                return MapMessage.errorMessage("无效的广告ID：0");
            }
            MapMessage returnMsg = advertisementServiceClient.getAdvertisementService().deleteAdvertisement(adId);
            saveOperationLog(adId, "删除广告", returnMsg.isSuccess() ? "删除成功" : returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("Failed to delete ad: id={}, ex={}", adId, ex.getMessage(), ex);
            return MapMessage.errorMessage("删除广告失败!(id={}, msg={})", adId, ex.getMessage());
        }
    }

    @RequestMapping(value = "uploadsrc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadSource(MultipartFile file) {
        Long adId = getRequestLong("adId");
        String type = getRequestString("type");
        if (adId == 0L || StringUtils.isBlank(type)) {
            return MapMessage.errorMessage("无效的参数！");
        }
        if (!"gif".equalsIgnoreCase(type) && !"img".equalsIgnoreCase(type)  && !"ext".equalsIgnoreCase(type)) {
            return MapMessage.errorMessage("图片参数异常！");
        }
        if (file == null || file.isEmpty()) {
            return MapMessage.errorMessage("请选择上传的素材！");
        }
        try {
            AdvertisementDetail ad = crmConfigService.$loadAdvertisementDetail(adId);
            if (ad == null || ad.isDisabledTrue()) {
                return MapMessage.errorMessage("无效的广告位!");
            }
            MapMessage validMsg = validateImg(file, ad.getAdSlotId(), type);
            if (!validMsg.isSuccess()) {
                return validMsg;
            }
            String fileName = uploadFile(file, ad.getAdSlotId());
            if (StringUtils.isBlank(fileName)) {
                return MapMessage.errorMessage("素材保存失败！");
            }
            MapMessage returnMsg = advertisementServiceClient.getAdvertisementService().uploadAdvertisementImg(adId, type, fileName);
            saveOperationLog(adId, "上传广告素材", returnMsg.isSuccess() ? "操作成功" : returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("Failed to upload img, ex={}", ex);
            return MapMessage.errorMessage("上传素材失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "clearsrc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clearSource() {
        Long adId = getRequestLong("adId");
        String type = getRequestString("type");
        if (adId == 0L || StringUtils.isBlank(type)) {
            return MapMessage.errorMessage("无效的参数！");
        }
        if (!"gif".equalsIgnoreCase(type) && !"img".equalsIgnoreCase(type)) {
            return MapMessage.errorMessage("图片参数异常！");
        }
        try {
            AdvertisementDetail ad = crmConfigService.$loadAdvertisementDetail(adId);
            if (ad == null || ad.isDisabledTrue()) {
                return MapMessage.errorMessage("无效的广告位!");
            }
            return advertisementServiceClient.getAdvertisementService().uploadAdvertisementImg(adId, type, null);
        } catch (Exception ex) {
            logger.error("Failed to clear img, ex={}", ex);
            return MapMessage.errorMessage("清除素材图片失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "adtracelog.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage adTraceLog() {
        Long adId = getRequestLong("adId");
        if (adId == 0L) {
            return MapMessage.errorMessage("无效的广告ID");
        }
        try {
            List<AdminLog> adminLogs = adminLogPersistence.withSelectFromTable(TRACE_AD_LOG_QUERY)
                    .useParamsArgs(adId).queryAll();
            return MapMessage.successMessage().add("logs", generateLog(adminLogs));
        } catch (Exception ex) {
            logger.error("Failed trace advertisement operation log, aid={}", adId, ex);
            return MapMessage.errorMessage("查询操作日志失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "adpriority.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage adjustAdPriority() {
        String currentUser = getCurrentAdminUser().getAdminUserName();
        Map<String, String> auditorConfig = getAuditorSlotConfig();
        Long adId = getRequestLong("adId");
        Integer priority = getRequestInt("priority", AdvertisementPriority.DEFAULT.getLevel());
        AdvertisementDetail advertisement = crmConfigService.$loadAdvertisementDetail(adId);
        if (advertisement == null || advertisement.isDisabledTrue()) {
            return MapMessage.errorMessage("该广告已经被删除，请刷新重新加载页面！");
        }
        // 校验身份，只有 Lv1以上 级别可以调整
        int privilege = getUserPrivilegeLevel();
        if (privilege < PRIVILEGE_LV1 && !auditorConfig.containsKey(currentUser)) {
            return MapMessage.errorMessage("您没有实时调整广告优先级的权限！");
        }
        if (Objects.equals(priority, advertisement.getPriority())) {
            return MapMessage.successMessage();
        }
        priority = AdvertisementPriority.ofLevelWithDefault(priority).getLevel();
        try {
            return advertisementServiceClient.getAdvertisementService().updateAdDetailPriority(adId, priority);
        } catch (Exception ex) {
            logger.error("Failed to adjust advertisement priority, ad={}, lv={}", adId, priority, ex);
            return MapMessage.errorMessage("调整广告优先级失败：" + ex.getMessage());
        }
    }

    private List<Map<String, Object>> generateAdInfo(List<AdvertisementDetail> allAdsList, Map<String, AdvertisementSlot> slotMap) {
        List<Map<String, Object>> results = new ArrayList<>();
        int level = getUserPrivilegeLevel();
        String currentUser = getCurrentAdminUser().getAdminUserName();
        Map<String, String> auditorConfig = getAuditorSlotConfig();
        String lineSep = "<br/>";
        for (AdvertisementDetail ad : allAdsList) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", ad.getId());
            info.put("name", ad.getName());
            info.put("adCode", ad.getAdCode());
            info.put("description", ad.getDescription());
            info.put("slot", slotMap.containsKey(ad.getAdSlotId()) ? slotMap.get(ad.getAdSlotId()).getName().replaceAll("-", lineSep) : "--");
            info.put("imgUrl", ad.getImgUrl());
            info.put("hasImg", StringUtils.isNotBlank(ad.getImgUrl()));
            info.put("gifUrl", ad.getGifUrl());
            info.put("extUrl", ad.getExtUrl());
            info.put("startTime", ad.getShowTimeStart() != null ? DateUtils.dateToString(ad.getShowTimeStart(), DateUtils.FORMAT_SQL_DATETIME).replace(" ", lineSep) : "--");
            info.put("endTime", ad.getShowTimeEnd() != null ? DateUtils.dateToString(ad.getShowTimeEnd(), DateUtils.FORMAT_SQL_DATETIME).replace(" ", lineSep) : "--");
            info.put("priority", AdvertisementPriority.ofLevel(ad.getPriority()));
            info.put("priorityVal", ad.getPriority());
            info.put("creator", ad.getCreator());
            info.put("creatorName", ad.getCreatorName());
            info.put("st", ad.getStatus());
            info.put("status", ad.getStatus());
            info.put("auditSt", ad.getAuditStatus());
            info.put("auditStatus", ad.getAuditStatus());

            boolean auditStatusValid;
            boolean statusValid;
            String auditStatusName;
            String statusName = AdDetailStatus.of(ad.getStatus()).getName();
            statusValid = Objects.equals(ad.getStatus(), AdDetailStatus.ONLINE.getStatus());
            auditStatusValid = Objects.equals(ad.getAuditStatus(), AdDetailAuditStatus.APPROVED.getStatus());
            boolean isExpired = false;
            if (ad.getAuditStatus() >= AdDetailAuditStatus.PENDING.getStatus()
                    && ad.getAuditStatus() <= AdDetailAuditStatus.PROCESSED.getStatus()) {
                auditStatusName = AdDetailAuditStatus.PENDING.getName();
            } else {
                auditStatusName = AdDetailAuditStatus.of(ad.getAuditStatus()).getName();
            }

            if (ad.getShowTimeEnd() != null && new Date().compareTo(ad.getShowTimeEnd()) >= 0) {
                isExpired = true;
            }

            info.put("isExpired", isExpired);
            info.put("statusName", statusName);
            info.put("auditStatusValid", auditStatusValid);
            info.put("auditStatusName", auditStatusName);
            info.put("statusValid", statusValid);
            info.put("hasPrivilege", level > 0 || checkUserPrivilege(ad.getCreator(), ad));
            info.put("adjust", level > 0 || auditorConfig.containsKey(currentUser));
            info.put("canOp", (ad.getAuditStatus() >= 11 && ad.getAuditStatus() <= 13)  && auditorConfig.containsKey(currentUser));
            info.put("canRaise", false);
            results.add(info);
        }
        return results;
    }

    private List<Map<String, String>> generateLog(List<AdminLog> log) {
        if (CollectionUtils.isEmpty(log)) {
            return Collections.emptyList();
        }
        List<Map<String, String>> results = new ArrayList<>();
        log.forEach(t -> {
            Map<String, String> map = new HashMap<>();
            map.put("operator", t.getAdminUserName());
            map.put("operation", SafeConverter.toString(t.getTargetStr(), "--"));
            map.put("comment", SafeConverter.toString(t.getComment(), "--"));
            map.put("createtime", DateUtils.dateToString(t.getCreateDatetime(), DateUtils.FORMAT_SQL_DATETIME));
            results.add(map);
        });
        return results;
    }

    //-----------------------------------------------------------------------------
    //--------------               广告审批相关操作                     -------------
    //-----------------------------------------------------------------------------
    @RequestMapping(value = "submitad.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage submitAdDetail(@RequestParam Long adId) {
        try {
            // 操作之前做状态检查, 防止重复操作
            AdvertisementDetail ad = crmConfigService.$loadAdvertisementDetail(adId);
            if (ad == null || ad.isDisabledTrue()) {
                return MapMessage.errorMessage("该广告已经被删除，请刷新重新加载页面！");
            }
            if (ad.getAuditStatus() > AdDetailAuditStatus.DRAFT.getStatus()) {
                return MapMessage.errorMessage("该广告已经提交审核！");
            }
            MapMessage validMsg = validateBeforeSubmit(ad);
            if (!validMsg.isSuccess()) {
                return validMsg;
            }
            // 管理员提交给上一级管理员审核
            AdDetailAuditStatus status;
            switch (getUserPrivilegeLevel()) {
                case PRIVILEGE_LV3:
                case PRIVILEGE_LV2:
                    status = AdDetailAuditStatus.PENDING_LV3;
                    break;
                case PRIVILEGE_LV1:
                    status = AdDetailAuditStatus.PENDING_LV2;
                    break;
                case ORDINARY:
                default:
                    status = PENDING_LV1;
                    break;
            }
            ad.setAuditStatus(status.getStatus());
            MapMessage returnMsg = advertisementServiceClient.getAdvertisementService().updateAdvertisement(adId, ad);
            if (!returnMsg.isSuccess()) {
                return returnMsg;
            }
            saveOperationLog(adId, "广告提交审核", returnMsg.isSuccess() ? "操作成功" : returnMsg.getInfo());
            sendAdvertiseNotify(ad, status, null);
            return returnMsg;
        } catch (Exception ex) {
            logger.error("Failed to submit advertisement:id={},ex={}", adId, ex.getMessage(), ex);
            return MapMessage.errorMessage("提交审核失败:{}", ex.getMessage(), ex);
        }
    }

    /**
     * 快速不审核提交上线
     * <p>
     * 直接将缓存中的广告信息替换为快速上线的广告
     * 此广告位上的其他广告暂时无法显示，直到缓存消失
     * </p>
     */
    @RequestMapping(value = "submitFast.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage submitFast() {
        long adId = getRequestLong("adId");
        AdvertisementDetail ad = crmConfigService.$loadAdvertisementDetail(adId);
        if (ad == null || ad.isDisabledTrue()) {
            return MapMessage.errorMessage("该广告已经被删除，请刷新重新加载页面！");
        } else if (ad.getAuditStatus() > AdDetailAuditStatus.PENDING.getStatus() + getUserPrivilegeLevel()) {
            return MapMessage.errorMessage("该广告已经转交上级审核！");
        }
        Map<Integer, List<AdvertisementTarget>> adMap = advertisementLoaderClient.loadAdTargetsGroupByType(adId);
        if (MapUtils.isEmpty(adMap)) {
            return MapMessage.errorMessage("没有配置投放策略");
        } else if (adMap.size() > 1 || !adMap.containsKey(TARGET_TYPE_USER.getType())) {
            return MapMessage.errorMessage("投放对象只能单独针对指定用户投放");
        } else if (adMap.get(TARGET_TYPE_USER.getType()).size() > 5) {
            return MapMessage.errorMessage("投放用户数量不应该超过５个用户");
        }

        ad.setAuditor(getCurrentAdminUser().getAdminUserName());
        ad.setAuditorName(getCurrentAdminUser().getRealName());
        ad.setAuditDatetime(new Date());
        ad.setAuditStatus(AdDetailAuditStatus.APPROVED.getStatus());
        ad.setStatus(AdvertisementStatus.ONLINE.getStatus());
        MapMessage returnMsg = advertisementServiceClient.getAdvertisementService().updateAdvertisement(adId, ad);
        if (!returnMsg.isSuccess()) {
            return returnMsg;
        }

        // 广告上线成功 记录指定用户
        List<AdvertisementTarget> targetList = adMap.get(TARGET_TYPE_USER.getType());
        if (CollectionUtils.isNotEmpty(targetList)) {
            // 打userTag
            for (AdvertisementTarget target : targetList) {
                User user = userLoaderClient.loadUser(SafeConverter.toLong(target.getTargetStr()));
                if (user != null) {
                    AdvertisementCache.addAdTargetUser(adId, user.getId(), DayRange.current().next().getEndDate());
                }
            }
        }
        saveOperationLog(adId, "广告快速上线", "User:" + StringUtils.join(targetList.stream().map(AdvertisementTarget::getTargetStr).collect(Collectors.toList()), ","));

        //快速上线立马生效，不会走广告投放约束条件
        List<NewAdMapper> adMappers = new ArrayList<>();
        NewAdMapper data = new NewAdMapper();
        data.setId(ad.getId());
        data.setImg(ad.getImgUrl());
        data.setDescription(ad.getDescription());
        data.setName(ad.getName());
        data.setCode(SafeConverter.toString(ad.getAdCode()));
        data.setPriority(AdvertisementPriority.TOP); // 快速上线默认置顶
        data.setHasUrl(StringUtils.isNotBlank(ad.getResourceUrl()));
        data.setContent(ad.getAdContent());
        data.setBtnContent(ad.getBtnContent());
        data.setShowStartTime(ad.getShowTimeStart() == null ? 0 : ad.getShowTimeStart().getTime());
        data.setShowEndTime(ad.getShowTimeEnd() == null ? 0 : ad.getShowTimeEnd().getTime());
        data.setGif(ad.getGifUrl());
        data.setExt(ad.getExtUrl());
        data.setShowSeconds(ad.getDisplayDuration() == null ? "" : ad.getDisplayDuration().toString());
        data.setUrl(ad.getResourceUrl());
        data.setNeedLogin(true);
        data.setShowLogo(ad.getShowLogo());
        data.setShowLimit(ad.getShowLimit());
        data.setClickLimit(ad.getClickLimit());
        data.setDailyClickLimit(ad.getDailyClickLimit());
        data.setDisplayPeriod(ad.fetchPeriod());
        adMappers.add(data);

        Map<String, List<NewAdMapper>> userAdMappers = new HashMap<>();
        adMap.get(AdvertisementTargetType.TARGET_TYPE_USER.getType())
                .forEach((adTarget) -> {
                    String key = CacheKeyGenerator.generateCacheKey("USER_LONDON_LIST:", null, new Object[]{adTarget.getTargetStr(), ad.getAdSlotId()});
                    userAdMappers.put(key, adMappers);
                });
        UserCache.getUserCache().sets(userAdMappers, 1200);

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "raiseup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage raiseUp(@RequestParam Long adId) {
        try {
            // 操作之前做状态检查, 防止重复操作
            AdvertisementDetail ad = crmConfigService.$loadAdvertisementDetail(adId);
            if (ad == null || ad.isDisabledTrue()) {
                return MapMessage.errorMessage("该广告已经被删除，请刷新重新加载页面！");
            }
            if (ad.getAuditStatus() > AdDetailAuditStatus.PENDING.getStatus() + getUserPrivilegeLevel()) {
                return MapMessage.errorMessage("该广告已经转交上级审核！");
            }
            int privilege = getUserPrivilegeLevel();
            AdDetailAuditStatus status;
            if (privilege == PRIVILEGE_LV1) {
                status = AdDetailAuditStatus.PENDING_LV2;
            } else if (privilege == PRIVILEGE_LV2) {
                status = AdDetailAuditStatus.PENDING_LV3;
            } else {
                return MapMessage.errorMessage("您没有向上级提交申请的权限！");
            }
            ad.setAuditor(getCurrentAdminUser().getAdminUserName());
            ad.setAuditorName(getCurrentAdminUser().getRealName());
            ad.setAuditDatetime(new Date());
            ad.setAuditStatus(status.getStatus());
            MapMessage returnMsg = advertisementServiceClient.getAdvertisementService().updateAdvertisement(adId, ad);
            if (!returnMsg.isSuccess()) {
                return returnMsg;
            }
            sendAdvertiseNotify(ad, status, getRequestString("comment"));
            saveOperationLog(adId, "转上级审批", "操作成功");
            return returnMsg;
        } catch (Exception ex) {
            logger.error("Failed to submit advertisement:id={},ex={}", adId, ex.getMessage(), ex);
            return MapMessage.errorMessage("提交审核失败:{}", ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "approvead.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage approveAdDetail(@RequestParam Long adId) {
        try {
            // 操作之前做状态检查, 防止重复操作
            AdvertisementDetail ad = crmConfigService.$loadAdvertisementDetail(adId);
            if (ad == null || ad.isDisabledTrue()) {
                return MapMessage.errorMessage("该广告已经被删除，请刷新重新加载页面！");
            }
            if (ad.getAuditStatus() > AdDetailAuditStatus.PROCESSED.getStatus()) {
                return MapMessage.errorMessage("该广告已经审核完成！");
            }
            ad.setAuditor(getCurrentAdminUser().getAdminUserName());
            ad.setAuditorName(getCurrentAdminUser().getRealName());
            ad.setAuditDatetime(new Date());
            ad.setAuditStatus(AdDetailAuditStatus.APPROVED.getStatus());
            MapMessage returnMsg = advertisementServiceClient.getAdvertisementService().updateAdvertisement(adId, ad);
            if (!returnMsg.isSuccess()) {
                return returnMsg;
            }
            sendAdvertiseNotify(ad, AdDetailAuditStatus.APPROVED, null);
            saveOperationLog(adId, "审核通过", "操作成功");
            return returnMsg;
        } catch (Exception ex) {
            logger.error("Failed to approve advertisement:id={},ex={}", adId, ex.getMessage(), ex);
            return MapMessage.errorMessage("批准广告失败:{}", ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "rejectad.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage rejectAdDetail(@RequestParam Long adId) {
        try {
            // 操作之前做状态检查, 防止重复操作
            AdvertisementDetail ad = crmConfigService.$loadAdvertisementDetail(adId);
            if (ad == null || ad.isDisabledTrue()) {
                return MapMessage.errorMessage("该广告已经被删除，请刷新重新加载页面！");
            }
            if (ad.getAuditStatus() > AdDetailAuditStatus.PROCESSED.getStatus()) {
                return MapMessage.errorMessage("该广告已经审核完成！");
            }
            ad.setAuditor(getCurrentAdminUser().getAdminUserName());
            ad.setAuditorName(getCurrentAdminUser().getRealName());
            ad.setAuditDatetime(new Date());
            ad.setAuditStatus(AdDetailAuditStatus.REJECTED.getStatus());
            MapMessage returnMsg = advertisementServiceClient.getAdvertisementService().updateAdvertisement(adId, ad);
            if (!returnMsg.isSuccess()) {
                return returnMsg;
            }
            sendAdvertiseNotify(ad, AdDetailAuditStatus.REJECTED, getRequestString("comment"));
            saveOperationLog(adId, "审核驳回", "操作成功");
            return returnMsg;
        } catch (Exception ex) {
            logger.error("Failed to reject advertisement:id={},ex={}", adId, ex.getMessage(), ex);
            return MapMessage.errorMessage("驳回广告失败:{}", ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "adonline.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage adDetailOnline(@RequestParam Long adId) {
        try {
            // 操作之前做状态检查, 防止重复操作
            AdvertisementDetail ad = crmConfigService.$loadAdvertisementDetail(adId);
            if (ad == null || ad.isDisabledTrue()) {
                return MapMessage.errorMessage("该广告已经被删除，请刷新重新加载页面！");
            }
            if (ad.getAuditStatus() < AdDetailAuditStatus.PROCESSED.getStatus()) {
                return MapMessage.errorMessage("该广告尚未通过审核！");
            }
            // 不是自己创建的广告不允许下线
            if (!checkUserPrivilege(ad.getCreator(), ad)) {
                return MapMessage.errorMessage("对不起，你不是该广告的创建者");
            }
            if (AdDetailStatus.ONLINE.getStatus().equals(ad.getStatus())) {
                return MapMessage.successMessage();
            }
            MapMessage returnMsg = advertisementServiceClient.getAdvertisementService().updateAdDetailStatus(adId, AdDetailStatus.ONLINE.getStatus());
            // 广告上线成功 记录指定用户
            if (!returnMsg.isSuccess()) {
                return returnMsg;
            }

            List<AdvertisementTarget> targetList = advertisementLoaderClient.loadAdTargetByType(adId, TARGET_TYPE_USER);
            if (CollectionUtils.isNotEmpty(targetList)) {
                // 打userTag
                for (AdvertisementTarget target : targetList) {
                    User user = userLoaderClient.loadUser(SafeConverter.toLong(target.getTargetStr()));
                    if (user != null) {
                        AdvertisementCache.addAdTargetUser(adId, user.getId(), ad.getShowTimeEnd());
                    }
                }
            }

            targetList = advertisementLoaderClient.loadAdTargetByType(adId, TARGET_TYPE_SCHOOL);
            if (CollectionUtils.isNotEmpty(targetList)) {
                for (AdvertisementTarget target : targetList) {
                    Long schoolId = SafeConverter.toLong(target.getTargetStr());
                    if (schoolId > 0L) {
                        AdvertisementCache.addAdTargetSchool(adId, schoolId, ad.getShowTimeEnd());
                    }
                }
            }

            targetList = advertisementLoaderClient.loadAdTargetByType(adId, TARGET_TYPE_REGION);
            if (CollectionUtils.isNotEmpty(targetList)) {
                for (AdvertisementTarget target : targetList) {
                    Integer regionCode = SafeConverter.toInt(target.getTargetStr());
                    if (regionCode > 0) {
                        AdvertisementCache.addAdTargetRegion(adId, regionCode, ad.getShowTimeEnd());
                    }
                }
            }

            // Enhancement #36120 上线广告－邮件提醒LV1的管理员
            String receiver = crmConfigService.$loadCommonConfigValue(ADVERTISE_AUDITOR_CATEGORY, ADVERTISE_AUDITOR_LV1);
            if (StringUtils.isBlank(receiver)) {
                logger.error("Please Set Advertise Lv1 Auditor!");
            } else {
                String receiveEmails = StringUtils.join(receiver.split(","), MAIL_SUFFIX).concat(MAIL_SUFFIX);
                receiveEmails = receiveEmails.substring(0, receiveEmails.length() - 1);
                Map<String, Object> content = new HashMap<>();
                content.put("name", ad.getName()); // 广告名称
                content.put("creator", ad.getCreatorName()); // 创建人
                content.put("auditor", ad.getAuditorName()); // 审核人
                content.put("operator", getCurrentAdminUser().getRealName()); // 当前操作人
                content.put("desc", ad.getDescription());  // 广告说明

                AdvertisementSlot slot = crmConfigService.$loadAdvertisementSlot(ad.getAdSlotId());
                content.put("slot", slot == null ? null : StringUtils.formatMessage("{}({})", slot.getName(), slot.getId()));  // 广告位
                content.put("url", ad.getResourceUrl());  // URL
                // 投放对象
                Map<Integer, List<AdvertisementTarget>> targetMap = advertisementLoaderClient.loadAdTargetsGroupByType(adId);
                List<String> adTarget = new ArrayList<>();
                targetMap.entrySet().forEach(entry -> {
                    AdvertisementTargetType targetType = AdvertisementTargetType.of(entry.getKey());
                    String target = "[" + targetType.getDesc();
                    if (targetType != TARGET_TYPE_ALL && CollectionUtils.isNotEmpty(entry.getValue())) {
                        target += ": " + entry.getValue().get(0).getTargetStr() + "等一共" + entry.getValue().size() + "条";
                    }
                    target += "]";
                    adTarget.add(target);
                });
                content.put("target", StringUtils.join(adTarget, " , "));  // 投放对象

                // 投放策略
                Map<String, AdvertisementTag> tagMap = advertisementLoaderClient.loadAdTagsGroupByType(adId);
                List<String> adTag = new ArrayList<>();
                tagMap.values().forEach(tagVal -> {
                    String tag = StringUtils.formatMessage("[{}({}) : {}]", tagVal.getTagName(), tagVal.getTagComment(), tagVal.getTagValue());
                    adTag.add(tag);
                });
                content.put("tag", StringUtils.join(adTag, " , "));  // 投放策略
                emailServiceClient.createTemplateEmail(EmailTemplate.advertiseonline)
                        .to(receiveEmails)
                        .subject(ad.getName() + " 广告上线通知(来自：" + RuntimeMode.getCurrentStage() + "环境)")
                        .content(content)
                        .send();
            }
            saveOperationLog(adId, "广告上线", returnMsg.isSuccess() ? "操作成功" : returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("Failed to online advertisement:id={},ex={}", adId, ex.getMessage(), ex);
            return MapMessage.errorMessage("上线广告失败:{}", ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "adoffline.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage adDetailOffline(@RequestParam Long adId) {
        try {
            // 操作之前做状态检查, 防止重复操作
            AdvertisementDetail ad = crmConfigService.$loadAdvertisementDetail(adId);
            if (ad == null || ad.isDisabledTrue()) {
                return MapMessage.errorMessage("该广告已经被删除，请刷新重新加载页面！");
            }
            if (ad.getAuditStatus() < AdDetailAuditStatus.PROCESSED.getStatus()) {
                return MapMessage.errorMessage("该广告尚未通过审核！");
            }
            // 不是自己创建的广告不允许下线
            if (!checkUserPrivilege(ad.getCreator(), ad)) {
                return MapMessage.errorMessage("对不起，你不是该广告的创建者");
            }
            if (AdDetailStatus.OFFLINE.getStatus().equals(ad.getStatus())) {
                return MapMessage.successMessage();
            }
            // 下线的广告清空审核信息 By Wyc 2016-12-13
            ad.setAuditStatus(AdDetailAuditStatus.DRAFT.getStatus()); // 状态变为草稿
            ad.setAuditor("");
            ad.setAuditorName("");
            ad.setAuditDatetime(ad.getCreateDatetime());
            ad.setStatus(AdDetailStatus.OFFLINE.getStatus());
            MapMessage returnMsg = advertisementServiceClient.getAdvertisementService().updateAdvertisement(adId, ad);

            // 广告下线成功 记录指定用户
            if (!returnMsg.isSuccess()) {
                return returnMsg;
            }

            List<AdvertisementTarget> targetList = advertisementLoaderClient.loadAdTargetByType(adId, TARGET_TYPE_USER);
            if (CollectionUtils.isNotEmpty(targetList)) {
                // 修改用户UserTag
                for (AdvertisementTarget target : targetList) {
                    User user = userLoaderClient.loadUser(SafeConverter.toLong(target.getTargetStr()));
                    if (user != null) {
                        UserTag userTag = userTagLoaderClient.loadUserTag(user.getId(), UserTagType.USER_TARGET_ADS.name());
                        if (userTag != null) {
                            String hisAdIds = userTag.getTags().get(UserTagType.USER_TARGET_ADS.name()).getValue();
                            if (StringUtils.isNotBlank(hisAdIds)) {
                                List<String> adIdList = Arrays.asList(StringUtils.split(hisAdIds, ","));
                                List<String> realList = adIdList.stream().filter(aid -> !StringUtils.equals(aid, adId.toString())).collect(Collectors.toList());
                                // 修改
                                UserTag.Tag tag = new UserTag.Tag(UserTagType.USER_TARGET_ADS, StringUtils.join(realList.toArray(), ","), new Date());
                                userManagementClient.updateTag(user.getId(), UserType.PARENT, tag);
                            }
                        }

                        AdvertisementCache.removeAdTargetUser(adId, user.getId());
                    }
                }
            }

            targetList = advertisementLoaderClient.loadAdTargetByType(adId, AdvertisementTargetType.TARGET_TYPE_SCHOOL);
            if (CollectionUtils.isNotEmpty(targetList)) {
                for (AdvertisementTarget target : targetList) {
                    Long schoolId = SafeConverter.toLong(target.getTargetStr());
                    AdvertisementCache.removeAdTargetSchool(adId, schoolId);
                }
            }

            targetList = advertisementLoaderClient.loadAdTargetByType(adId, AdvertisementTargetType.TARGET_TYPE_REGION);
            if (CollectionUtils.isNotEmpty(targetList)) {
                for (AdvertisementTarget target : targetList) {
                    Integer regionCode = SafeConverter.toInt(target.getTargetStr());
                    AdvertisementCache.removeAdTargetRegion(adId, regionCode);
                }
            }

            saveOperationLog(adId, "广告下线", "操作成功");
            return returnMsg;
        } catch (Exception ex) {
            logger.error("Failed to offline advertisement:id={},ex={}", adId, ex.getMessage(), ex);
            return MapMessage.errorMessage("下线广告失败:{}", ex.getMessage(), ex);
        }

    }

    //-----------------------------------------------------------------------------
    //--------------              PRIVATE METHODS                     -------------
    //-----------------------------------------------------------------------------
    private String uploadFile(MultipartFile file, String slotId) {
        try {
            if (file != null && !file.isEmpty()) {
                String prefix = "be-" + DateUtils.dateToString(new Date(), "yyyyMMdd") + "-" + slotId;
                String originalFileName = file.getOriginalFilename();
                @Cleanup InputStream inStream = file.getInputStream();
                return crmImageUploader.upload(prefix, originalFileName, inStream);
            }
        } catch (IOException ex) {
            logger.error("上传文件失败： " + ex.getMessage());
        }
        return null;
    }

    private MapMessage validateDetail(AdvertisementDetail detail) {
        StringBuilder errorMsg = new StringBuilder();
        AdvertisementSlot slot = crmConfigService.$loadAdvertisementSlot(detail.getAdSlotId());
        if (slot == null) {
            return MapMessage.errorMessage("广告位信息异常！");
        }
        AdvertisementSlotType slotType = AdvertisementSlotType.safeParse(slot.getType());
        if (slotType == null) {
            return MapMessage.errorMessage("无效的广告位类型!");
        }
        if (StringUtils.isNotBlank(detail.getAdContent())) {
            String badWord = badWordCheckerClient.checkConversationBadWord(detail.getAdContent());
            if (StringUtils.isNotBlank(badWord)) {
                return MapMessage.errorMessage("广告内容包含敏感词：{}, 请酌情修改！", badWord);
            }
        }
        // FIXME add 404 check for resourceUrl item
        String resourceUrl = detail.getResourceUrl();
        if (StringUtils.isNoneBlank(resourceUrl) && resourceUrl.toLowerCase().startsWith("http")) {
            // 自动替换http为https，站外的不管
            if(resourceUrl.contains("17zuoye")) {
                resourceUrl = resourceUrl.replaceFirst("^http://", "https://");
            }
            // FIXME 测试环境HTTPS证书有问题，先不验了，正式环境才验证
            if (RuntimeMode.isProduction()) {
                try {
                    AlpsHttpResponse execute = HttpRequestExecutor.defaultInstance().get(resourceUrl).execute();
                    if (execute.hasHttpClientException() || execute.getStatusCode() > 400) {
                        return MapMessage.errorMessage("无效的跳转URL, 请确认跳转URL是否正确!");
                    }
                } catch (Exception ex) {
                    return MapMessage.errorMessage("无效的跳转URL: " + ex.getMessage());
                }
            }
        }
        // Popup消息类型的时候，内容以及按钮都不能为空
        if (slotType == Popup消息) {
            if (StringUtils.isBlank(detail.getAdContent()) || StringUtils.isBlank(detail.getBtnContent())) {
                errorMsg.append("广告内容以及按钮内容都不能为空！\n");
            }
        }
        if (slotType == 闪屏) {
            if (detail.getDisplayDuration() < 3 || detail.getDisplayDuration() > 4) {
                errorMsg.append("闪屏持续时间只能是3或4秒!\n");
            }
        }
        if (detail.getShowTimeStart() == null || detail.getShowTimeEnd() == null) {
            errorMsg.append("请填写广告的投放时间区间！\n");
        }
        if (detail.getShowTimeStart() != null && detail.getShowTimeEnd() != null && detail.getShowTimeStart().after(detail.getShowTimeEnd())) {
            errorMsg.append("投放开始时间不能晚于结束时间！\n");
        }
        // 校验时间段的信息
        List<AdDisplayPeriod> periods = detail.fetchPeriod();
        if (CollectionUtils.isNotEmpty(periods)) {
            if (periods.size() > 5) {
                errorMsg.append("时间段不能超过五个！\n");
            } else {
                // 1. E[i] - S[i] >= 1h, 每段时间不小于1小时
                // 2 S[i+1] - E[i] >= 1h, 时间段时间间隔至少1小时
                for (int i = 0; i < periods.size(); ++i) {
                    Date start1 = periods.get(i).getStartTime();
                    Date end1 = periods.get(i).getEndTime();
                    if (DateUtils.minuteDiff(end1, start1) < 60) {
                        errorMsg.append(StringUtils.formatMessage("时间区间{}小于1小时\n", periods.get(i).toString()));
                    }
                    if (i < periods.size() - 1) {
                        Date start2 = periods.get(i + 1).getStartTime();
                        if (DateUtils.minuteDiff(start2, end1) < 60) {
                            errorMsg.append(StringUtils.formatMessage("{}与{}间隔至少1小时\n", periods.get(i).toString(), periods.get(i + 1).toString()));
                        }
                    }
                }
            }
        }

        if (errorMsg.length() > 0) {
            return MapMessage.errorMessage(errorMsg.toString());
        }
        // 警告信息
        StringBuilder warningMsg = new StringBuilder();
        DateRange range = new DateRange(detail.getShowTimeStart(), detail.getShowTimeEnd());
        // 检查当前时间段内在线的广告
        long validAds = advertisementLoaderClient.getLocalAdvertisementBuffer().loadAvailableAds(slot.getId())
                .stream()
                .filter(AdvertisementDetail::isAvailableAdvertisement)
                .filter(ad -> {
                    if (ad.getShowTimeStart() == null || ad.getShowTimeEnd() == null) {
                        return true;
                    }
                    if (ad.getShowTimeStart() == null && ad.getShowTimeEnd() != null) {
                        return ad.getShowTimeEnd().after(detail.getShowTimeStart());
                    }
                    if (ad.getShowTimeStart() != null && ad.getShowTimeEnd() == null) {
                        return ad.getShowTimeStart().before(detail.getShowTimeEnd());
                    }
                    return detail.getShowTimeStart().before(ad.getShowTimeEnd()) && detail.getShowTimeEnd().after(ad.getShowTimeStart());
                }).count();
        if (validAds >= slot.getCapacity()) {
            warningMsg.append("您所选的广告位在投放日期的有排期冲突，确定仍要保存？\n")
                    .append("广告位容量: ").append(slot.getCapacity()).append("，同时期广告数: ").append(validAds);
        }
        return MapMessage.successMessage(warningMsg.toString());
    }

    private MapMessage validateImg(MultipartFile file, String slotId, String type) {
        AdvertisementSlot slot = crmConfigService.$loadAdvertisementSlot(slotId);
        if (slot == null) {
            return MapMessage.errorMessage("广告位信息异常！");
        }
        AdvertisementSlotType slotType = AdvertisementSlotType.safeParse(slot.getType());
        if (slotType == null) {
            return MapMessage.errorMessage("无效的广告位类型!");
        }
        if (slotType == 纯文本) {
            return MapMessage.errorMessage("纯文本广告位无需上传素材！");
        }
        String ext = StringUtils.substringAfterLast(file.getOriginalFilename(), ".").toLowerCase();
        if (!IMG_SUFFIX.contains(ext)) {
            return MapMessage.errorMessage("图片格式只能是" + Arrays.toString(IMG_SUFFIX.toArray()));
        } else {
            try {
                BufferedImage image = ImageIO.read(file.getInputStream());
                int height = image.getHeight();
                int width = image.getWidth();
                // 弹窗的类型仅仅校验图片的最大尺寸
                if (slotType == 弹窗 || slotType == Popup消息) {
                    if (height > slot.getHeight() || width != image.getWidth()) {
                        return MapMessage.errorMessage("图片大小与广告位不匹配，请重新选择图片！");
                    }
                } else if (slotType == 闪屏 && "gif".equalsIgnoreCase(type)) {
                    // FIXME 闪屏类型的第二张图校验尺寸为 640*960 这里暂时写死吧
                    if (width != 640 && height != 960) {
                        return MapMessage.errorMessage("闪屏类型广告第二张图片尺寸为 640px × 960px！");
                    }
                } else if ("221001".equals(slotId) && "gif".equalsIgnoreCase(type)) {
                    // FIXME 机构导流第二张图校验尺寸为 710*200 这里暂时写死吧
                    if (width != 710 && height != 200) {
                        return MapMessage.errorMessage("机构导流运营位第二张图片尺寸为 710px × 200px！");
                    }
                } else if ("220703".equals(slotId) && "gif".equalsIgnoreCase(type)) {
                    // FIXME 家长APP上课了组合广告位第二张图校验尺寸为 325*200 这里暂时写死吧
                    if (width != 325 && height != 200) {
                        return MapMessage.errorMessage("机构导流运营位第二张图片尺寸为 325px × 200px！");
                    }
                } else if (("220107".equals(slotId) || "220108".equals(slotId) || "220109".equals(slotId) || "220110".equals(slotId)) && "gif".equalsIgnoreCase(type)) {
                    // FIXME 家长APP 首页热门活动广告位 340*170 这里暂时写死吧
                    if (width != 340 || height != 170) {
                        return MapMessage.errorMessage("家长APP-首页-热门活动图片尺寸为 340px × 170px！");
                    }
                } else if ("220111".equals(slotId) && "gif".equalsIgnoreCase(type)) {
                    // FIXME 家长APP端-首页（2.2.3起）-直播导流广告位 130*46 这里暂时写死吧
                    if (width != 130 || height != 46) {
                        return MapMessage.errorMessage("家长APP-首页-热门活动图片尺寸为 130px × 46px！");
                    }
                } else if (slotType == 闪屏 && "ext".equalsIgnoreCase(type)) {
                    // FIXME 闪屏类型的第三张图校验尺寸为 818*1792 这里暂时写死吧
                    if (width != 818 && height != 1792) {
                        return MapMessage.errorMessage("闪屏类型广告第三张图片尺寸为 818px × 1792px！");
                    }
                } else {
                    if (height != slot.getHeight() || width != slot.getWidth()) {
                        return MapMessage.errorMessage("图片大小与广告位不匹配，请重新选择图片！");
                    }
                }
                return MapMessage.successMessage();
            } catch (Exception ex) {
                logger.error("Failed validate Img, ex={}", ex);
                return MapMessage.errorMessage("图片校验异常！");
            }
        }
    }

    private MapMessage validateBeforeSubmit(AdvertisementDetail ad) {
        AdvertisementSlot slot = crmConfigService.$loadAdvertisementSlot(ad.getAdSlotId());
        if (slot == null) {
            return MapMessage.errorMessage("广告位信息异常！");
        }
        AdvertisementSlotType slotType = AdvertisementSlotType.safeParse(slot.getType());
        if (slotType == null) {
            return MapMessage.errorMessage("无效的广告位类型!");
        }
        // 纯文本广告位不需要校验图片
        if (slotType != 纯文本 && StringUtils.isBlank(ad.getImgUrl())) {
            return MapMessage.errorMessage("广告投放素材尚未配置完备!");
        }
        if (slotType == 闪屏 && StringUtils.isBlank(ad.getGifUrl())) {
            return MapMessage.errorMessage("广告投放素材尚未配置完备!");
        }
        if (slotType == 闪屏 && StringUtils.isBlank(ad.getExtUrl())) {
            return MapMessage.errorMessage("广告投放素材尚未配置完备!");
        }
        return MapMessage.successMessage();
    }

    /**
     * 1. 提交的时候发送给1级管理员审核 (submitad.vpage)
     * 2. 1级管理员转上级的时候发送给2级管理员审核 (raiseup.vpage)
     * 3. 2级管理员转上级的时候发送给3级管理员 (raiseup.vpage)
     * 4. 管理员审批/驳回，发送给创建人 (approvesad.vpage/rejectad.vpage)
     */
    private void sendAdvertiseNotify(AdvertisementDetail adDetail, AdDetailAuditStatus status, String comment) {
        // 测试环境发邮件有点Annoying... 取消掉...
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            return;
        }
        String receiver;
        String operation;
        Map<String, String> configValues = new LinkedHashMap<>();
        crmConfigService.$loadCommonConfigs().stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .filter(e -> ADVERTISE_AUDITOR_CATEGORY.equals(e.getCategoryName()))
                .sorted((o1, o2) -> {
                    long u1 = SafeConverter.toLong(o1.getUpdateDatetime());
                    long u2 = SafeConverter.toLong(o2.getUpdateDatetime());
                    return Long.compare(u2, u1);
                })
                .forEach(e -> configValues.put(e.getConfigKeyName(), e.getConfigKeyValue()));

        try {
            switch (status) {
                case PENDING_LV3:
                    receiver = configValues.containsKey(ADVERTISE_AUDITOR_LV3) ? configValues.get(ADVERTISE_AUDITOR_LV3) : null;
                    operation = "点击链接前往审核";
                    break;
                case PENDING_LV2:
                    receiver = configValues.containsKey(ADVERTISE_AUDITOR_LV2) ? configValues.get(ADVERTISE_AUDITOR_LV2) : null;
                    operation = "点击链接前往审核";
                    break;
                case PENDING_LV1:
                    receiver = configValues.containsKey(ADVERTISE_AUDITOR_LV1) ? configValues.get(ADVERTISE_AUDITOR_LV1) : null;
                    operation = "点击链接前往审核";
                    break;
                case APPROVED:
                    receiver = adDetail.getCreator();
                    operation = "已通过审核";
                    break;
                case REJECTED:
                    receiver = adDetail.getCreator();
                    operation = "未通过审核，拒绝原因：" + comment;
                    break;
                default:
                    return;
            }
            if (StringUtils.isBlank(receiver)) {
                // 发生异常
                emailServiceClient.createTemplateEmail(EmailTemplate.office)
                        .to("yuechen.wang@17zuoye.com")
                        .subject("运营平台通知(来自：" + RuntimeMode.getCurrentStage() + "环境)")
                        .content(MiscUtils.m("info", "Please Check CommonConfig Of Advertisement Auditor!"))
                        .send();
                throw new UtopiaRuntimeException("Please Check CommonConfig Of Advertisement Auditor!");
            }
            String receiveEmails = StringUtils.join(receiver.split(","), MAIL_SUFFIX).concat(MAIL_SUFFIX);
            receiveEmails = receiveEmails.substring(0, receiveEmails.length() - 1);

            String ccUsers = crmConfigService.$loadCommonConfigValue(ADVERTISE_AUDITOR_CATEGORY, ADVERTISE_MAIL_CC);
            String ccEmails = StringUtils.join(ccUsers.split(","), MAIL_SUFFIX).concat(MAIL_SUFFIX);
            ccEmails = ccEmails.substring(0, ccEmails.length() - 1);
            if (StringUtils.isBlank(ccEmails)) {
                ccEmails = "yuechen.wang@17zuoye.com";
            }
            String url = getAdminBaseUrl() + "/opmanager/advertisement/adindex.vpage";
            Map<String, Object> content = new HashMap<>();
            content.put("creator", adDetail.getCreatorName());
            content.put("url", url);
            content.put("operation", operation);
            content.put("text", adDetail.getDescription());
            emailServiceClient.createTemplateEmail(EmailTemplate.advertiseaudit)
                    .to(receiveEmails)
                    .cc(ccEmails)
                    .subject("运营平台通知(来自：" + RuntimeMode.getCurrentStage() + "环境)")
                    .content(content)
                    .send();
        } catch (Exception ex) {
            // 发生异常
            emailServiceClient.createTemplateEmail(EmailTemplate.office)
                    .to("yuechen.wang@17zuoye.com")
                    .subject("运营平台通知(来自：" + RuntimeMode.getCurrentStage() + "环境)")
                    .content(MiscUtils.m("info", ex))
                    .send();
        }
    }
}

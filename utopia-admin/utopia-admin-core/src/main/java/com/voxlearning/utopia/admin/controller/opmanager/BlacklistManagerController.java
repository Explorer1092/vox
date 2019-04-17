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

package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.constant.GlobalTagName;
import com.voxlearning.utopia.service.config.api.entity.GlobalTag;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.region.api.constant.RegionConstants;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.config.api.constant.ConfigCategory.PRIMARY_PLATFORM_GENERAL;
import static com.voxlearning.utopia.service.config.api.constant.GlobalTagName.*;

/**
 * @author peng.zhang.a
 * @since 16-10-25
 */
@Controller
@RequestMapping("/opmanager/blacklist")
public class BlacklistManagerController extends OpManagerAbstractController {

    final private static String RECEIVER_VALUE = "fairyland_email_receiver";

    @Inject private RaikouSystem raikouSystem;
    @Inject private GlobalTagServiceClient globalTagServiceClient;
    @Inject private EmailServiceClient emailServiceClient;

    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String school(Model model) {

        List<GlobalTag> tagsList = globalTagServiceClient.getGlobalTagService()
                .loadAllGlobalTagsFromDB()
                .getUninterruptibly();
        Set<Long> schoolIds = tagsList.stream()
                .filter(p -> GlobalTagName.valueOf(p.getTagName()) == AfentiBlackListSchools || GlobalTagName.valueOf(p.getTagName()) == PaymentGrayListSchools)
                .map(p -> SafeConverter.toLong(p.getTagValue()))
                .collect(Collectors.toSet());
        Set<Long> userIds = tagsList.stream()
                .filter(p -> GlobalTagName.valueOf(p.getTagName()) == AfentiBlackListUsers || GlobalTagName.valueOf(p.getTagName()) == ParentBlackListUsers
                        || GlobalTagName.valueOf(p.getTagName()) == PaymentWhiteListUsers || GlobalTagName.valueOf(p.getTagName()) == PaymentLimitWhiteListUsers
                        || GlobalTagName.valueOf(p.getTagName()) == PaymentGrayListUsers)
                .map(p -> SafeConverter.toLong(p.getTagValue()))
                .collect(Collectors.toSet());

        Map<Long, User> userMap = raikouSystem.loadUsers(userIds);
        Map<Long, School> schoolMap = raikouSystem.loadSchools(schoolIds);
        Map<Integer, ExRegion> allRegions = raikouSystem.getRegionBuffer().loadAllRegions();

        List<Map<String, Object>> parentUserIds = tagsList.stream()
                .filter(p -> GlobalTagName.valueOf(p.getTagName()) == ParentBlackListUsers)
                .map(p -> MapMessage.successMessage()
                        .add("tagId", p.getId())
                        .add("userId", p.getTagValue())
                        .add("desc", p.getTagComment())
                        .add("createTime", p.getCreateDatetime())
                        .add("username", userMap.getOrDefault(SafeConverter.toLong(p.getTagValue()), new User()).fetchRealname())
                ).collect(Collectors.toList());

        List<Map<String, Object>> parentAdUserIds = tagsList.stream()
                .filter(p -> Objects.equals(p.getTagName(), ParentAdBlackUsers.name()))
                .map(p -> MapMessage.successMessage()
                        .add("tagId", p.getId())
                        .add("userId", p.getTagValue())
                        .add("desc", p.getTagComment())
                        .add("createTime", p.getCreateDatetime())
                        .add("username", userMap.getOrDefault(SafeConverter.toLong(p.getTagValue()), new User()).fetchRealname())
                ).collect(Collectors.toList());

        List<Map<String, Object>> studentUserIds = tagsList.stream()
                .filter(p -> GlobalTagName.valueOf(p.getTagName()) == AfentiBlackListUsers)
                .map(p -> MapMessage.successMessage()
                        .add("tagId", p.getId())
                        .add("userId", p.getTagValue())
                        .add("desc", p.getTagComment())
                        .add("createTime", p.getCreateDatetime())
                        .add("username", userMap.getOrDefault(SafeConverter.toLong(p.getTagValue()), new User()).fetchRealname())
                ).collect(Collectors.toList());


        List<Map<String, Object>> whiteUserIds = tagsList.stream()
                .filter(p -> GlobalTagName.valueOf(p.getTagName()) == PaymentWhiteListUsers)
                .map(p -> MapMessage.successMessage()
                        .add("tagId", p.getId())
                        .add("userId", p.getTagValue())
                        .add("desc", p.getTagComment())
                        .add("createTime", p.getCreateDatetime())
                        .add("username", userMap.getOrDefault(SafeConverter.toLong(p.getTagValue()), new User()).fetchRealname())
                ).collect(Collectors.toList());

        List<Map<String, Object>> paymentLimitWhiteUserIds = tagsList.stream()
                .filter(p -> GlobalTagName.valueOf(p.getTagName()) == PaymentLimitWhiteListUsers)
                .map(p -> MapMessage.successMessage()
                        .add("tagId", p.getId())
                        .add("userId", p.getTagValue())
                        .add("desc", p.getTagComment())
                        .add("createTime", p.getCreateDatetime())
                        .add("username", userMap.getOrDefault(SafeConverter.toLong(p.getTagValue()), new User()).fetchRealname())
                ).collect(Collectors.toList());

        List<Map<String, Object>> schools = tagsList.stream()
                .filter(p -> GlobalTagName.valueOf(p.getTagName()) == AfentiBlackListSchools)
                .map(p -> {
                    Map<String, Object> mid = new HashMap<>();
                    mid.put("tagId", p.getId());
                    mid.put("schoolId", p.getTagValue());
                    mid.put("desc", p.getTagComment());
                    mid.put("createTime", p.getCreateDatetime());
                    School school = schoolMap.getOrDefault(SafeConverter.toLong(p.getTagValue()), new School());
                    ExRegion exRegion = allRegions.getOrDefault(SafeConverter.toInt(school.getRegionCode()), new ExRegion());
                    mid.put("schoolName", school.getShortName());
                    mid.put("provinceName", exRegion.getProvinceName());
                    mid.put("cityName", exRegion.getCityName());
                    return mid;

                }).collect(Collectors.toList());

        List<Map<String, Object>> graySchools = tagsList.stream()
                .filter(p -> GlobalTagName.valueOf(p.getTagName()) == PaymentGrayListSchools)
                .map(p -> {
                    Map<String, Object> mid = new HashMap<>();
                    mid.put("tagId", p.getId());
                    mid.put("schoolId", p.getTagValue());
                    mid.put("desc", p.getTagComment());
                    mid.put("createTime", p.getCreateDatetime());
                    School school = schoolMap.getOrDefault(SafeConverter.toLong(p.getTagValue()), new School());
                    ExRegion exRegion = allRegions.getOrDefault(SafeConverter.toInt(school.getRegionCode()), new ExRegion());
                    mid.put("schoolName", school.getShortName());
                    mid.put("provinceName", exRegion.getProvinceName());
                    mid.put("cityName", exRegion.getCityName());
                    return mid;

                }).collect(Collectors.toList());

        List<Map<String, Object>> grayUsers = tagsList.stream()
                .filter(p -> GlobalTagName.valueOf(p.getTagName()) == PaymentGrayListUsers)
                .map(p -> MapMessage.successMessage()
                        .add("tagId", p.getId())
                        .add("userId", p.getTagValue())
                        .add("desc", p.getTagComment())
                        .add("createTime", p.getCreateDatetime())
                        .add("username", userMap.getOrDefault(SafeConverter.toLong(p.getTagValue()), new User()).fetchRealname())
                ).collect(Collectors.toList());


        model.addAttribute("studentUserIds", studentUserIds);
        model.addAttribute("parentUserIds", parentUserIds);
        model.addAttribute("parentAdUserIds", parentAdUserIds);
        model.addAttribute("whiteUserIds", whiteUserIds);
        model.addAttribute("schools", schools);
        model.addAttribute("graySchools", graySchools);
        model.addAttribute("paymentLimitWhiteUserIds", paymentLimitWhiteUserIds);
        model.addAttribute("grayUsers", grayUsers);
        return "opmanager/blacklist/index";
    }

    @RequestMapping(value = "regions.vpage", method = RequestMethod.GET)
    public String region(Model model) {

        Collection<Integer> parentRegions = raikouSystem.getRegionBuffer().findByTag(RegionConstants.TAG_PARENT_PAYMENT_BLACKLIST_REGIONS);
        Collection<Integer> studentRegions = raikouSystem.getRegionBuffer().findByTag(RegionConstants.TAG_PAYMENT_BLACKLIST_REGIONS);
        Collection<Integer> grayRegions = raikouSystem.getRegionBuffer().findByTag(RegionConstants.TAG_PAYMENT_GRAY_LIST_REGIONS);
        List<Map<String, Object>> parentList = crmRegionService.buildRegionTree(parentRegions);
        List<Map<String, Object>> studentList = crmRegionService.buildRegionTree(studentRegions);
        List<Map<String, Object>> grayList = crmRegionService.buildRegionTree(grayRegions);
        model.addAttribute("parentBlacklistRegion", JsonUtils.toJson(parentList));
        model.addAttribute("studentBlacklistRegion", JsonUtils.toJson(studentList));
        model.addAttribute("grayRegion", JsonUtils.toJson(grayList));

        return "opmanager/blacklist/regions";
    }

    @RequestMapping(value = "deleteTag.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteTag() {
        String tagId = getRequestString("tagId");
        if (StringUtils.isBlank(tagId)) {
            return MapMessage.errorMessage("传值为空");
        }
        GlobalTag tag = globalTagServiceClient.getGlobalTagService()
                .loadGlobalTagFromDB(tagId)
                .getUninterruptibly();
        if (tag != null && globalTagServiceClient.getGlobalTagService().removeGlobalTag(tagId).getUninterruptibly()) {
            String title;
            //发邮件
            StringBuilder sendText = new StringBuilder();
            if (GlobalTagName.valueOf(tag.getTagName()) == GlobalTagName.AfentiBlackListSchools) {
                sendText.append("删除黑名单学校：schoolId=").append(tag.getTagValue());
                title = "学校黑名单";
            } else if (GlobalTagName.valueOf(tag.getTagName()) == PaymentWhiteListUsers) {
                sendText.append("删除白名单用户：userId=").append(tag.getTagValue());
                title = "个人白名单用户";
            } else {
                sendText.append("删除黑名单用户：userId=").append(tag.getTagValue());
                title = "个人用户黑名单";
            }
            sendEmail(sendText.toString(), title);
            return MapMessage.successMessage();

        }
        return MapMessage.errorMessage("删除失败");

    }

    @RequestMapping(value = "addTagValue.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage addTagValue() {
        String blackType = getRequestString("blackType");
        String tagValue = getRequestString("tagValue");
        String tagComment = getRequestString("tagComment");

        if (StringUtils.isBlank(blackType) || StringUtils.isBlank(tagValue)) {
            return MapMessage.errorMessage("传值为空");
        }
        GlobalTagName globalTagName = GlobalTagName.valueOf(blackType);
        String[] tagValues = tagValue.split("[,| ]");

        for (String tag : tagValues) {
            GlobalTag globalTag = new GlobalTag();

            if (globalTagName == AfentiBlackListUsers || globalTagName == ParentBlackListUsers || globalTagName == ParentAdBlackUsers
                    || globalTagName == PaymentWhiteListUsers) {

                User user = userLoaderClient.loadUser(SafeConverter.toLong(tag.trim()));
                if (user == null) {
                    return MapMessage.errorMessage(tag + "用户不存在");
                }
            } else if (globalTagName == AfentiBlackListSchools) {
                School school = raikouSystem.loadSchool(SafeConverter.toLong(tag.trim()));
                if (school == null) {
                    return MapMessage.errorMessage("填写学校失败,学校不存在");
                }
            }
            globalTag.setTagComment(tagComment);
            globalTag.setTagName(globalTagName.name());
            globalTag.setTagValue(tag);
            globalTagServiceClient.getGlobalTagService().insertGlobalTag(globalTag).awaitUninterruptibly();
        }


        //发邮件
        String sendText;
        String title;
        if (globalTagName == GlobalTagName.AfentiBlackListSchools) {
            sendText = "<br/>增加黑名单学校：" + ",schoolIds=" + tagValue;
            title = "学校黑名单";
        } else if (globalTagName == PaymentWhiteListUsers) {

            sendText = "<br/>增加付费白名单用户：" + "userIds=" + tagValue;
            title = "个人付费白名单";
        } else {
            sendText = "<br/>增加黑名单用户：" + "userIds=" + tagValue;
            title = "个人用户黑名单";
        }
        sendEmail(sendText, title);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "saveregionconfig.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveRegionConfig(String product, String regionList) {
        if (StringUtils.isEmpty(product)) {
            return MapMessage.errorMessage("无效的参数!");
        }

        if (!Objects.equals(product, RegionConstants.TAG_PAYMENT_BLACKLIST_REGIONS)
                && !Objects.equals(product, RegionConstants.TAG_PARENT_PAYMENT_BLACKLIST_REGIONS)
                && !Objects.equals(product, RegionConstants.TAG_PAYMENT_GRAY_LIST_REGIONS)) {
            return MapMessage.errorMessage("无效的参数!");
        }
        // 根据用户管辖区域的不同来确定要取消设置的区域列表和要设置的区域列表
        List<Integer> untagedRegions = new ArrayList<>();
        List<Integer> tagedRegions = new ArrayList<>();

        String[] regionCodeList = regionList.split(",");

        Collection<Integer> alreadyTaggedRegions = raikouSystem.getRegionBuffer().findByTag(product);
        for (Integer taggedRegion : alreadyTaggedRegions) {
            String strRegionCode = String.valueOf(taggedRegion);
            if (!regionList.contains(strRegionCode)) {
                untagedRegions.add(taggedRegion);
            }
        }

        for (String regionCode : regionCodeList) {
            if (StringUtils.isEmpty(regionCode)) {
                continue;
            }

            Integer regionCodeInt = Integer.parseInt(regionCode);
            tagedRegions.add(regionCodeInt);
        }

        StringBuffer text = new StringBuffer();

        if (untagedRegions.size() > 0) {
            Set<Integer> tobeUntagRegions = new HashSet<>();
            tobeUntagRegions.addAll(untagedRegions);
            raikouSystem.getRegionService().detachRegionTag(tobeUntagRegions, product);
        }
        if (tagedRegions.size() > 0) {
            raikouSystem.getRegionService().attachRegionTag(tagedRegions, product);
        }

        Map<Integer, ExRegion> allRegions = raikouSystem.getRegionBuffer().loadAllRegions();

        //发送邮件
        Set<Integer> validRegions = new HashSet<>();
        Set<Integer> hasUntagedRegionsChild = new HashSet<>();
        for (int i = untagedRegions.size() - 1; i >= 0; i--) {
            Integer code = untagedRegions.get(i);
            Integer parentCode = allRegions.get(code).getParentRegionCode();
            hasUntagedRegionsChild.add(parentCode);
            if (!hasUntagedRegionsChild.contains(code)) {
                validRegions.add(code);
            }

        }
        if (CollectionUtils.isNotEmpty(validRegions)) {

            text.append("<br/>开放黑名单地区：");
            raikouSystem.getRegionBuffer().loadRegions(validRegions)
                    .values()
                    .forEach(p -> text.append(p.getProvinceName())
                            .append(p.getCityName())
                            .append(p.getCountyName())
                            .append(","));
        }
        validRegions = tagedRegions.stream()
                .filter(p -> !tagedRegions.contains(allRegions.get(p).getParentRegionCode()))
                .filter(p -> !alreadyTaggedRegions.contains(p))
                .collect(Collectors.toSet());

        if (CollectionUtils.isNotEmpty(validRegions)) {
            text.append("<br/>增加黑名单地区：");
            raikouSystem.getRegionBuffer().loadRegions(validRegions)
                    .values()
                    .stream()
                    .forEach(p -> text.append(p.getProvinceName())
                            .append(p.getCityName())
                            .append(p.getCountyName())
                            .append(","));
        }
        String title = "";
        if (Objects.equals(product, RegionConstants.TAG_PAYMENT_GRAY_LIST_REGIONS)) {
            title = "区域灰名单";
        } else if (Objects.equals(product, RegionConstants.TAG_PAYMENT_BLACKLIST_REGIONS)) {
            title = "学生区域黑名单";
        } else if (Objects.equals(product, RegionConstants.TAG_PARENT_PAYMENT_BLACKLIST_REGIONS)) {
            title = "家长区域黑名单";
        }
        sendEmail(text.toString(), title);
        return MapMessage.successMessage();


    }

    private Boolean sendEmail(String text, String title) {
        try {
            String receivers = crmConfigService.$loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.getType(), RECEIVER_VALUE);
            if (!StringUtils.isBlank(receivers)) {
                Map<String, Object> content = new HashMap<>();
                content.put("user", getCurrentAdminUser());
                content.put("time", DateUtils.getNowSqlDatetime());
                content.put("text", text);
                emailServiceClient.createTemplateEmail(EmailTemplate.blacklistregionchange)
                        .to(receivers)
                        .subject(title + "变更(来自：" + RuntimeMode.current().name() + "环境)")
                        .content(content)
                        .send();
            }
        } catch (Exception e) {
            logger.error("sendEmail failed");
            return false;
        }
        return true;
    }

}




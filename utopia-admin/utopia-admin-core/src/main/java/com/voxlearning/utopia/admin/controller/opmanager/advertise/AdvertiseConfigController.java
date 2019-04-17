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

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.athena.bean.AdPlatform;
import com.voxlearning.athena.bean.AdRealData;
import com.voxlearning.athena.cenum.AdEnum;
import com.voxlearning.utopia.admin.athena.AdPlatformServiceClient;
import com.voxlearning.utopia.admin.athena.AdRealServiceClient;
import com.voxlearning.utopia.admin.athena.SearchEngineServiceClient;
import com.voxlearning.utopia.admin.data.AdExcelData;
import com.voxlearning.utopia.admin.data.AdSummaryData;
import com.voxlearning.utopia.admin.data.AdSummaryDetailData;
import com.voxlearning.utopia.admin.service.crm.CrmAdvertiseRegionService;
import com.voxlearning.utopia.admin.util.HighchartsUtil;
import com.voxlearning.utopia.admin.util.HssfUtils;
import com.voxlearning.utopia.service.config.api.constant.AdDetailStatus;
import com.voxlearning.utopia.service.config.api.constant.AdvertisementTagType;
import com.voxlearning.utopia.service.config.api.constant.AdvertisementTargetType;
import com.voxlearning.utopia.service.config.api.entity.AdvertisementDetail;
import com.voxlearning.utopia.service.config.api.entity.AdvertisementSlot;
import com.voxlearning.utopia.service.config.api.entity.AdvertisementTag;
import com.voxlearning.utopia.service.config.api.entity.AdvertisementTarget;
import lombok.Cleanup;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 广告投放策略，从之前的 AdvertiseManageController 里抽出来
 * Created by Yuechen Wang on 2016-06-27.
 */
@Controller
@RequestMapping("/opmanager/advertisement/config")
public class AdvertiseConfigController extends AbstractAdvertiseController {

    @Inject private AdPlatformServiceClient adPlatformServiceClient;
    @Inject private AdRealServiceClient adRealServiceClient;
    @Inject private CrmAdvertiseRegionService crmAdvertiseRegionService;
    @Inject private SearchEngineServiceClient searchEngineServiceClient; // 大数据标签接口...

    //-----------------------------------------------------------------------------
    //--------------             广告投放目标相关操作                   -------------
    //-----------------------------------------------------------------------------
    @RequestMapping(value = "adconfig.vpage", method = RequestMethod.GET)
    public String adTarget(Model model) {
        Long adId = getRequestLong("adId");
        model.addAttribute("adId", adId);
        AdvertisementDetail detail = crmConfigService.$loadAdvertisementDetail(adId);
        if (detail == null || detail.isDisabledTrue()) {
            model.addAttribute("error", "无效的广告信息");
            return "opmanager/advertisement/adconfig";
        }
        if (!checkUserPrivilege(detail.getCreator(), detail)) {
            return "redirect: /opmanager/advertisement/adindex.vpage";
        }
        AdvertisementSlot adSlot = crmConfigService.$loadAdvertisementSlot(detail.getAdSlotId());
        if (adSlot == null) {
            model.addAttribute("error", "无效的广告位信息");
            return "opmanager/advertisement/adconfig";
        }
        model.addAttribute("adDetail", detail);
        model.addAttribute("adSlot", adSlot);
        model.addAttribute("editable", AdDetailStatus.of(detail.getStatus()) != AdDetailStatus.ONLINE);
        generateDetailTargets(adId, model);
        generateDetailTags(adId, adSlot, model);
        return "opmanager/advertisement/adconfig";
    }

    @RequestMapping(value = "regiontree.vpage", method = RequestMethod.GET)
    @ResponseBody
    public String regionTree() {
        Long adId = getRequestLong("adId");
        List<Integer> regions = null;
        if (adId != 0L) {
            int type = AdvertisementTargetType.TARGET_TYPE_REGION.getType();
            Map<Integer, List<AdvertisementTarget>> targetMap = advertisementLoaderClient.loadAdTargetsGroupByType(adId);
            if (targetMap.get(type) != null) {
                regions = targetMap.get(type).stream().map(t -> SafeConverter.toInt(t.getTargetStr())).collect(Collectors.toList());
            }
        }
        return JsonUtils.toJson(crmRegionService.buildRegionTree(regions));
    }

    @RequestMapping(value = "getlabelhits.vpage", method = RequestMethod.POST)
    @ResponseBody
    public long getNumTotalHits() {
        String labels = getRequestString("labels");
        if (StringUtils.isBlank(labels)) {
            return 0;
        }
        Set<String> labelSet = CollectionUtils.toLinkedHashSet(Arrays.asList(labels.split(",")));
        return getUserTotalHits(Collections.singletonList(labelSet));
    }

    @RequestMapping(value = "gettotallabelhits.vpage", method = RequestMethod.POST)
    @ResponseBody
    public long getTotalNumTotalHits() {
        Long adId = getRequestLong("adId");
        if (adId == 0L) {
            return 0;
        }
        Map<Integer, List<AdvertisementTarget>> targetMap = advertisementLoaderClient.loadAdTargetsGroupByType(adId);
        int type = AdvertisementTargetType.TARGET_TYPE_LABEL.getType();
        if (!targetMap.containsKey(type)) return 0;
        List<Set<String>> labels = targetMap.get(type).stream()
                .filter(t -> StringUtils.isNotBlank(t.getTargetStr()))
                .map(target -> Arrays.stream(target.getTargetStr().split(",")).collect(Collectors.toSet()))
                .collect(Collectors.toList());
        return getUserTotalHits(labels);
    }

    @RequestMapping(value = "finduserlabel.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage findUserLabel() {
        String uid = getRequestString("uid");
        if (StringUtils.isBlank(uid)) {
            return MapMessage.errorMessage();
        }
        try {
            Set<Long> uidSet = Arrays.stream(uid.split(",")).map(SafeConverter::toLong).filter(t -> t != 0L).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(uidSet)) {
                return MapMessage.errorMessage();
            }
            Map<Long, String> labelMap = searchEngineServiceClient.getLabelMap();
            List<Map<String, Object>> labels = new ArrayList<>();
            uidSet.forEach(t -> {
                Map<String, Object> info = new HashMap<>();
                info.put("uid", t);
                Set<String> userLabelSet = searchEngineServiceClient.getUserLabelSet(t).stream()
                        .map(l -> labelMap.get(SafeConverter.toLong(l)))
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toSet());
                info.put("label", CollectionUtils.isEmpty(userLabelSet) ? "该用户无标签" : StringUtils.join(userLabelSet, "、"));
                labels.add(info);
            });
            return MapMessage.successMessage().add("labels", labels);
        } catch (Exception ex) {
            logger.error("Failed load user label, uid={}, ex={}", uid, ex.getMessage(), ex);
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(value = "saveregion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTargetRegion() {
        Long adId = getRequestLong("adId");
        Integer type = getRequestInt("type");
        String regions = getRequestString("regionList");
        boolean append = getRequestBool("append");
        if (AdvertisementTargetType.of(type) != AdvertisementTargetType.TARGET_TYPE_REGION) {
            return MapMessage.errorMessage("无效的参数！");
        }
        if (StringUtils.isBlank(regions)) {
            return MapMessage.errorMessage("选择地区不能为空！");
        }
        try {
            MapMessage returnMsg;
            if (!checkBeforeTargetSave(adId, AdvertisementTargetType.TARGET_TYPE_REGION)) {
                return MapMessage.errorMessage("暂时只能允许选择一种类型的投放对象！");
            }
            // 没有校验用户输入是否符合规范
            List<String> regionList = new ArrayList<>();
            for (String s : regions.split(",")) {
                regionList.add(s);
            }

            // 不利用isAppend 追加, 考虑可能会有重复
            if (append) {
                Map<Integer, List<AdvertisementTarget>> targetMap = advertisementLoaderClient.loadAdTargetsGroupByType(adId);

                if (targetMap.get(AdvertisementTargetType.TARGET_TYPE_REGION.getType()) != null) {
                    type = AdvertisementTargetType.TARGET_TYPE_REGION.getType();
                    for (AdvertisementTarget ad : targetMap.get(type)) {
                        regionList.add(ad.getTargetStr());
                    }
                }
            }

            //去重
            Set<String> set = new LinkedHashSet<>(regionList);
            regionList = new ArrayList<>(set);

            returnMsg = advertisementServiceClient.getAdvertisementService().saveAdvertisementTargets(adId, type, regionList, false);
            saveOperationLog(adId, "修改广告投放区域", returnMsg.isSuccess() ? "操作成功" : returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("保存投放地区失败! id={},type={}, ex={}", adId, type, ex.getMessage(), ex);
            return MapMessage.errorMessage("保存投放地区失败:" + ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "saveids.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTargetIds() {
        Long adId = getRequestLong("adId");
        Integer type = getRequestInt("type");
        String targetIds = getRequestString("targetIds");
        Boolean append = getRequestBool("append");
        AdvertisementTargetType targetType = AdvertisementTargetType.of(type);
        if (targetType != AdvertisementTargetType.TARGET_TYPE_USER
                && targetType != AdvertisementTargetType.TARGET_TYPE_SCHOOL
                && targetType != AdvertisementTargetType.TARGET_TYPE_ALL) {
            return MapMessage.errorMessage("无效的参数！");
        }
        if (StringUtils.isBlank(targetIds)) {
            return MapMessage.errorMessage("请输入有效的内容！");
        }
        // 没有校验用户输入是否符合规范
        List<String> targetList = Arrays.stream(targetIds.split(DEFAULT_LINE_SEPARATOR)).map(t -> t.replaceAll("\\s", ""))
                .filter(StringUtils::isNotBlank).collect(Collectors.toList()); // /r/n may occur
        if (targetType == AdvertisementTargetType.TARGET_TYPE_USER) {
            // 高峰时段不让推送按用户的广告 用户ID大于5个的时候限制
            int hours = SafeConverter.toInt(DateUtils.dateToString(new Date(), "HH"));
            if (hours >= 17 && hours < 22 && targetList.size() > 5) {
                return MapMessage.errorMessage("高峰时段不支持按用户配置广告，请在17:00-22:00之外的时间配置。");
            }
        }
        try {
            MapMessage returnMsg;
            if (!checkBeforeTargetSave(adId, targetType)) {
                return MapMessage.errorMessage("暂时只能允许选择一种类型的投放对象！");
            }
            returnMsg = advertisementServiceClient.getAdvertisementService().saveAdvertisementTargets(adId, type, targetList, append);
            saveOperationLog(adId, "修改广告投放对象(" + targetType.getDesc() + ")", returnMsg.isSuccess() ? "操作成功" : returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("保存投放用户失败:id={},type={},ex={}", adId, type, ex.getMessage(), ex);
            return MapMessage.errorMessage("保存投放用户失败:" + ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "cleartargets.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clearTargets() {
        Long adId = getRequestLong("adId");
        Integer type = getRequestInt("type");
        AdvertisementTargetType targetType = AdvertisementTargetType.of(type);
        if (targetType != AdvertisementTargetType.TARGET_TYPE_USER
                && targetType != AdvertisementTargetType.TARGET_TYPE_SCHOOL
                && targetType != AdvertisementTargetType.TARGET_TYPE_REGION
                && targetType != AdvertisementTargetType.TARGET_TYPE_LABEL
                && targetType != AdvertisementTargetType.TARGET_TYPE_ALL) {
            return MapMessage.errorMessage("无效的参数！");
        }
        try {
            return advertisementServiceClient.getAdvertisementService().clearAdvertisementTargets(adId, type);
        } catch (Exception ex) {
            logger.error("清空投放对象失败:id={},type={},ex={}", adId, type, ex.getMessage(), ex);
            return MapMessage.errorMessage("清空投放对象失败:" + ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "savelabel.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTargetLabels() {
        Long adId = getRequestLong("adId");
        Integer type = getRequestInt("type");
        String labels = getRequestString("labelList");
        AdvertisementTargetType targetType = AdvertisementTargetType.of(type);
        if (targetType != AdvertisementTargetType.TARGET_TYPE_LABEL) {
            return MapMessage.errorMessage("无效的参数！");
        }
        if (StringUtils.isBlank(labels)) {
            return MapMessage.errorMessage("选择标签不能为空！");
        }
        try {
            MapMessage returnMsg;
            if (!checkBeforeTargetSave(adId, targetType)) {
                return MapMessage.errorMessage("暂时只能允许选择一种类型的投放对象！");
            }
            // 没有校验用户输入是否符合规范
            // 每个ID长度 17, 做多限制在20个左右
            if (labels.length() > 500) {
                return MapMessage.errorMessage("选择条目过多！请重新规划策略");
            }
            returnMsg = advertisementServiceClient.getAdvertisementService().saveAdvertisementLabel(adId, labels);
            saveOperationLog(adId, "修改广告投放标签", returnMsg.isSuccess() ? "操作成功" : returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("保存投放Label失败! id={},type={}, ex={}", adId, type, ex.getMessage(), ex);
            return MapMessage.errorMessage("保存投放Label失败:" + ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "deletelabel.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteTargetLabels() {
        Long adId = getRequestLong("adId");
        Long targetId = getRequestLong("labelId");
        if (adId == 0L || targetId == 0L) {
            return MapMessage.errorMessage("无效的参数！");
        }
        try {
            return advertisementServiceClient.getAdvertisementService().deleteAdvertisementLabel(adId, targetId);
        } catch (Exception ex) {
            logger.error("删除投放Label失败! id={},targetId={}", adId, targetId, ex);
            return MapMessage.errorMessage("删除投放Label失败:" + ex.getMessage(), ex);
        }
    }

    private void generateDetailTargets(Long adId, Model model) {
        Map<Integer, List<AdvertisementTarget>> targetMap = advertisementLoaderClient.loadAdTargetsGroupByType(adId);
        int type = 5;
        List<Integer> regions = new ArrayList<>();
        String targetUser = null;
        String targetSchool = null;
        if (targetMap.get(AdvertisementTargetType.TARGET_TYPE_REGION.getType()) != null) {
            type = AdvertisementTargetType.TARGET_TYPE_REGION.getType();
            regions = targetMap.get(type).stream().map(ad -> SafeConverter.toInt(ad.getTargetStr())).collect(Collectors.toList());
        }
        if (targetMap.get(AdvertisementTargetType.TARGET_TYPE_USER.getType()) != null) {
            type = AdvertisementTargetType.TARGET_TYPE_USER.getType();
            List<String> users = targetMap.get(type).stream().map(AdvertisementTarget::getTargetStr).collect(Collectors.toList());
            int size = users.size() > 2000 ? 2000 : users.size();
            targetUser = StringUtils.join(users.subList(0, size), DEFAULT_LINE_SEPARATOR);
            model.addAttribute("userSize", users.size());
        }
        if (targetMap.get(AdvertisementTargetType.TARGET_TYPE_SCHOOL.getType()) != null) {
            type = AdvertisementTargetType.TARGET_TYPE_SCHOOL.getType();
            List<String> schools = targetMap.get(type).stream().map(AdvertisementTarget::getTargetStr).collect(Collectors.toList());
            int size = schools.size() > 2000 ? 2000 : schools.size();
            targetSchool = StringUtils.join(schools.subList(0, size), DEFAULT_LINE_SEPARATOR);
            model.addAttribute("schoolSize", schools.size());
        }
        if (targetMap.get(AdvertisementTargetType.TARGET_TYPE_LABEL.getType()) != null) {
            type = AdvertisementTargetType.TARGET_TYPE_LABEL.getType();
            model.addAttribute("labelGroupList", parseLabel(targetMap.get(type)));
        }
        List<KeyValuePair<Integer, String>> targetTypes = AdvertisementTargetType.toKeyValuePairs();
        for (KeyValuePair<Integer, String> target : targetTypes) {
            model.addAttribute("has_" + target.getKey(), targetMap.containsKey(target.getKey()));
        }
        model.addAttribute("targetType", type);
        model.addAttribute("targetRegion", JsonUtils.toJson(crmAdvertiseRegionService.buildRegionTree(regions)));
        model.addAttribute("targetRegionCode", StringUtils.join(regions,"\n"));
        model.addAttribute("targetRegionCodeSize", regions.size());
        model.addAttribute("targetUser", targetUser);
        model.addAttribute("targetSchool", targetSchool);
        model.addAttribute("labelTree", JsonUtils.toJson(searchEngineServiceClient.getLabelTree()));
    }

    /**
     * 保存Target之前校验一下是否存在其他类型的Target
     * 原则上只允许一种target存在于投放对象中
     */
    private boolean checkBeforeTargetSave(Long adId, AdvertisementTargetType targetType) {
        if (AdvertisementTargetType.TARGET_TYPE_LABEL == targetType) {
            return true;
        }
        Map<Integer, List<AdvertisementTarget>> targetMap = advertisementLoaderClient.loadAdTargetsGroupByType(adId);
        if (MapUtils.isEmpty(targetMap)) {
            return true;
        }
        for (AdvertisementTargetType type : AdvertisementTargetType.values()) {
            if (AdvertisementTargetType.TARGET_TYPE_LABEL == type) {
                continue;
            }
            if (type != targetType && targetMap.containsKey(type.getType())) {
                return false;
            }
        }
        return true;
    }

    private List<Map<String, Object>> parseLabel(List<AdvertisementTarget> labelGroups) {
        Map<Long, String> labelMap = searchEngineServiceClient.getLabelMap();
        List<Map<String, Object>> result = new ArrayList<>();
        for (AdvertisementTarget target : labelGroups) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("targetId", target.getId());
            List<String> tagNameList = Arrays.stream(target.getTargetStr().split(",")).map(SafeConverter::toLong).map(labelMap::get).collect(Collectors.toList());
            temp.put("targetStr", StringUtils.join(tagNameList, ","));
            result.add(temp);
        }
        return result;
    }

    //-----------------------------------------------------------------------------
    //--------------             广告投放约束相关操作                   -------------
    //-----------------------------------------------------------------------------
    @RequestMapping(value = "savetag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTag() {
        Long adId = getRequestLong("adId");
        String tagName = getRequestString("tagName");
        String tagVal = getRequestString("tagVal").trim();
        String tagComment = getRequestString("tagComment");
        try {
            if (adId == 0L || StringUtils.isBlank(tagName)) {
                return MapMessage.errorMessage("参数异常");
            }
            AdvertisementTagType tagType = AdvertisementTagType.valueOf(tagName);
            if (tagType.getValueType() == 2 && StringUtils.isBlank(tagVal)) {
                return MapMessage.errorMessage("参数异常");
            }
            if (tagType.getValueType() == 1) {
                tagVal = "true";
            }
            if (StringUtils.isBlank(tagComment)) {
                tagComment = tagType.getDesc();
            }
            if (tagType == AdvertisementTagType.MOBILE_SYSTEM) {
                if (!"iOS".equalsIgnoreCase(tagVal) && !"Android".equalsIgnoreCase(tagVal)) {
                    return MapMessage.errorMessage("请填写 iOS 或者 Android");
                }
            }
            if (tagType == AdvertisementTagType.APP_VERSION) {
                if (!tagVal.matches("(>|>=|=|<|<=|!=)\\d+(\\.)\\d+(\\.)\\d+(\\.)\\d+")) {
                    return MapMessage.errorMessage("必须以 >|>=|=|<|<=|!= 开头, 比如>1.5.1.18");
                }
            }

            // 查出当用户的TagList 暂时全部覆盖了吧。。。
            Map<String, AdvertisementTag> tagMap = advertisementLoaderClient.loadAdTagsGroupByType(adId);
            AdvertisementTag tag = tagMap.get(tagName);
            if (tag == null) {
                tag = new AdvertisementTag(adId, tagName, tagVal, tagComment);
            } else {
                tag.setTagName(tagName);
                tag.setTagValue(tagVal);
                tag.setTagComment(tagComment);
            }
            MapMessage returnMsg = advertisementServiceClient.getAdvertisementService().saveAdvertisementTag(adId, tag);
            saveOperationLog(adId, "修改广告约束:" + tagName, returnMsg.isSuccess() ? "操作成功" : returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("保存广告约束失败：id={},tag={},ex={}", adId, tagName, ex.getMessage(), ex);
            return MapMessage.errorMessage("保存广告约束失败:" + ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "deltag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteTag() {
        Long adId = getRequestLong("adId");
        String tagName = getRequestString("tagName");
        try {
            if (adId == 0L || StringUtils.isBlank(tagName)) {
                return MapMessage.errorMessage("参数异常");
            }

            Map<String, AdvertisementTag> tagMap = advertisementLoaderClient.loadAdTagsGroupByType(adId);
            AdvertisementTag tag = tagMap.get(tagName);
            if (tag == null) {
                return MapMessage.errorMessage("Tag已经被删除，请勿重复操作！");
            }
            tag.setDisabled(true);
            MapMessage returnMsg = advertisementServiceClient.getAdvertisementService().saveAdvertisementTag(adId, tag);
            saveOperationLog(adId, "删除广告约束:" + tagName, returnMsg.isSuccess() ? "操作成功" : returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("删除广告约束失败：id={},tag={},ex={}", adId, tagName, ex.getMessage(), ex);
            return MapMessage.errorMessage("删除广告约束失败:" + ex.getMessage(), ex);
        }
    }

    private void generateDetailTags(Long adId, AdvertisementSlot slot, Model model) {
        UserType userType = UserType.of(slot.getUserType());
        // 一个类型的Tag理论上应该只有一条与之对应
        Map<String, AdvertisementTag> tagMap = advertisementLoaderClient.loadAdTagsGroupByType(adId);
        List<AdvertisementTagType> tagList = AdvertisementTagType.loadByUserType(userType);
        model.addAttribute("tagMap", tagMap);
        model.addAttribute("tagList", generateTagInfo(tagList, tagMap.keySet()));
    }

    private List<Map<String, Object>> generateTagInfo(List<AdvertisementTagType> tagList, Set<String> exists) {
        boolean exist = CollectionUtils.isNotEmpty(exists);
        List<Map<String, Object>> results = new ArrayList<>();
        for (AdvertisementTagType tag : tagList) {
            Map<String, Object> info = new HashMap<>();
            info.put("tagName", tag.getName());
            info.put("tagDesc", tag.getDesc());
            info.put("tagType", tag.getValueType());
            info.put("exist", exist && exists.contains(tag.getName()));
            info.put("instruction", tag.getInstruction());
            results.add(info);
        }
        return results;
    }

    /**
     * 实时数据
     */
    @RequestMapping(value = "realtimedata.vpage", method = RequestMethod.GET)
    public String realtimeData(Model model) {
        Long adId = getRequestLong("adId");
        String address = "opmanager/advertisement/realtimedata";
        model.addAttribute("adId", adId);
        AdvertisementDetail ad = crmConfigService.$loadAdvertisementDetail(adId);
        if (ad == null || ad.isDisabledTrue()) {
            model.addAttribute("error", "无效的广告信息!");
            return address;
        }
        if (!checkUserPrivilege(ad.getCreator(), ad)) {
            return "redirect: /opmanager/advertisement/adindex.vpage";
        }
        AdvertisementSlot slot = crmConfigService.$loadAdvertisementSlot(ad.getAdSlotId());
        if (slot == null) {
            model.addAttribute("error", "无效的广告位信息!");
            return address;
        }
        model.addAttribute("adDetail", ad);
        model.addAttribute("adSlot", slot);
        return address;
    }

    @RequestMapping(value = "realtimedatadetail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage realtimedatadetail() {
        Long adId = getRequestLong("adId");
        String dateType = getRequestString("dateType");
        AdvertisementDetail ad = crmConfigService.$loadAdvertisementDetail(adId);
        AdvertisementSlot slot = ad == null ? null : crmConfigService.$loadAdvertisementSlot(ad.getAdSlotId());
        if (ad == null || slot == null) {
            return MapMessage.errorMessage("无效的广告位信息!");
        }

        try {
            String yAxisTitle;
            String dataFormat;
            List<AdRealData> clickList;
            List<AdRealData> showList;
            if (dateType == null || dateType.equals("hour")) {
                yAxisTitle = "次数／每1Hour";
                dataFormat = "yyyy-MM-dd HH";
                Map<AdEnum, List<AdRealData>> data;
                if (RuntimeMode.le(Mode.DEVELOPMENT)) {
                    data = adRealServiceClient.mockData(); // 本地开发环境用来测试
                } else {
                    data = adRealServiceClient.getAdRealService().getDataByHour(DateUtils.dateToString(new Date(), "yyyy-MM-dd HH:mm"), adId.intValue());
                }
                clickList = data == null || CollectionUtils.isEmpty(data.get(AdEnum.HOUR_CLICK)) ? Collections.emptyList() : data.get(AdEnum.HOUR_CLICK);
                showList = data == null || CollectionUtils.isEmpty(data.get(AdEnum.HOUR_SHOW)) ? Collections.emptyList() : data.get(AdEnum.HOUR_SHOW);
            } else {
                yAxisTitle = "次数／每5Min";
                dataFormat = "yyyy-MM-dd HH:mm";

                Map<AdEnum, List<AdRealData>> data;
                if (RuntimeMode.le(Mode.DEVELOPMENT)) {
                    data = adRealServiceClient.mockData(); // 本地开发环境用来测试
                } else {
                    data = adRealServiceClient.getAdRealService().getDataByMinute(DateUtils.dateToString(new Date(), "yyyy-MM-dd HH:mm"), adId.intValue());
                }
                clickList = data == null || CollectionUtils.isEmpty(data.get(AdEnum.MINUTE_CLICK)) ? Collections.emptyList() : data.get(AdEnum.MINUTE_CLICK);
                showList = data == null || CollectionUtils.isEmpty(data.get(AdEnum.MINUTE_SHOW)) ? Collections.emptyList() : data.get(AdEnum.MINUTE_SHOW);
            }
            HighchartsUtil highcharsUtil = HighchartsUtil.create();
            highcharsUtil.init("广告实时数据", "(点击图下方Title，取消展示)", yAxisTitle);
            highcharsUtil.addSerieName("click_pv", "点击量");
            highcharsUtil.addSerieName("click_uv", "独立用户点击量");
            clickList.forEach((p) -> {
                Date date = DateUtils.stringToDate(p.getDate(), dataFormat);
                String dateStr = DateUtils.dateToString(date, "MM-dd HH:mm");
                highcharsUtil.addData("click_pv", dateStr, p.getPv());
                highcharsUtil.addData("click_uv", dateStr, p.getUv());
            });
            highcharsUtil.addSerieName("show_pv", "曝光量");
            highcharsUtil.addSerieName("show_uv", "独立用户曝光量");
            showList.forEach((p) -> {
                Date date = DateUtils.stringToDate(p.getDate(), dataFormat);
                String dateStr = DateUtils.dateToString(date, "MM-dd HH:mm");
                highcharsUtil.addData("show_pv", dateStr, p.getPv());
                highcharsUtil.addData("show_uv", dateStr, p.getUv());
            });
            highcharsUtil.calRate("点击率", "show_pv", "click_pv");
            highcharsUtil.calRate("独立用户点击率", "show_uv", "click_uv");
            return MapMessage.successMessage().set("highchartsResult", highcharsUtil.toResult());
        } catch (Exception e) {
            logger.error("adRealService failed", e);
            return MapMessage.errorMessage("请求数据错误" + e.getMessage());
        }
    }

    @RequestMapping(value = "dataindex.vpage", method = RequestMethod.GET)
    public String dataIndex(Model model) {
        Long adId = getRequestLong("adId");
        Date upToDate = DateUtils.calculateDateDay(new Date(), -1);
        model.addAttribute("upToDate", upToDate);
        model.addAttribute("adId", adId <= 0L ? null : adId);
        AdvertisementDetail ad = crmConfigService.$loadAdvertisementDetail(adId);
        if (ad == null || ad.isDisabledTrue()) {
            model.addAttribute("error", "无效的广告信息!");
            return "opmanager/advertisement/dataindex";
        }
        if (!checkUserPrivilege(ad.getCreator(), ad)) {
            return "redirect: /opmanager/advertisement/adindex.vpage";
        }
        AdvertisementSlot slot = crmConfigService.$loadAdvertisementSlot(ad.getAdSlotId());
        if (slot == null) {
            model.addAttribute("error", "无效的广告位信息!");
            return "opmanager/advertisement/dataindex";
        }
        model.addAttribute("adDetail", ad);
        model.addAttribute("adSlot", slot);
        int dateVal = Integer.valueOf(DateUtils.dateToString(upToDate, "yyyyMMdd"));
        try {
            MapMessage resultMsg = adPlatformServiceClient.getAdPlatformService().summaryQueryByDate(adId, dateVal);
            if (!resultMsg.isSuccess()) {
                model.addAttribute("error", "查询数据异常：" + resultMsg.get("info"));
                return "opmanager/advertisement/dataindex";
            }
            AdPlatform originData = (AdPlatform) resultMsg.get("summaryQueryByDate");
            if (originData == null) {
                model.addAttribute("error", "查询数据格式异常！");
                return "opmanager/advertisement/dataindex";
            }
            // 数据查询成功，处理数据
            AdSummaryData summary = AdSummaryData.generateByAdPlatform(originData, 1);
            model.addAttribute("summary", summary);
            return "opmanager/advertisement/dataindex";
        } catch (Exception ex) {
            model.addAttribute("error", "错误:" + ex.getMessage());
            return "opmanager/advertisement/dataindex";
        }
    }

    @RequestMapping(value = "querybydate.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage querySummaryDataByDate() {
        Long adId = getRequestLong("adId");
        String endDate = getRequestString("endDate");
        if (adId == 0L || StringUtils.isBlank(endDate) || DateUtils.stringToDate(endDate, DateUtils.FORMAT_SQL_DATE) == null) {
            return MapMessage.errorMessage("参数异常！");
        }
        try {
            int dateVal = Integer.valueOf(endDate.replaceAll("-", ""));
            MapMessage resultMsg = adPlatformServiceClient.getAdPlatformService().summaryQueryByDate(adId, dateVal);
            if (!resultMsg.isSuccess()) {
                return resultMsg;
            }
            AdPlatform originData = (AdPlatform) resultMsg.get("summaryQueryByDate");
            if (originData == null) {
                return MapMessage.errorMessage("查询数据格式异常！");
            }
            AdSummaryData summary = AdSummaryData.generateByAdPlatform(originData, 1);
            return MapMessage.successMessage().add("summary", summary);
        } catch (Exception ex) {
            logger.error("Failed to query data by date, adId={}, date={}, ex={}", adId, endDate, ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "querybyregion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage querySummaryDataByRegion() {
        Long adId = getRequestLong("adId");
        String endDate = getRequestString("endDate");
        if (adId == 0L || StringUtils.isBlank(endDate) || DateUtils.stringToDate(endDate, DateUtils.FORMAT_SQL_DATE) == null) {
            return MapMessage.errorMessage("参数异常！");
        }
        try {
            int dateVal = Integer.valueOf(endDate.replaceAll("-", ""));
            MapMessage resultMsg = adPlatformServiceClient.getAdPlatformService().summaryQueryByRegion(adId, dateVal);
            if (!resultMsg.isSuccess()) {
                return resultMsg;
            }
            AdPlatform originData = (AdPlatform) resultMsg.get("summaryQueryByRegion");
            if (originData == null) {
                return MapMessage.errorMessage("查询数据格式异常！");
            }
            AdSummaryData summary = AdSummaryData.generateByAdPlatform(originData, 2);
            return MapMessage.successMessage().add("summary", summary);
        } catch (Exception ex) {
            logger.error("Failed to query data by region, adId={}, date={}, ex={}", adId, endDate, ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "downloadsummarydata.vpage", method = RequestMethod.POST)
    public void topAuthSumExport(HttpServletResponse response) {
        Long adId = getRequestLong("adId");
        String endDate = getRequestString("endDate");
        String dataType = getRequestString("dataType");
        if (adId == 0L || StringUtils.isBlank(endDate) || StringUtils.isBlank(dataType)) {
            return;
        }
        int dateVal = Integer.valueOf(endDate.replaceAll("-", ""));
        HSSFWorkbook hssfWorkbook;
        String filename;
        try {
            if ("date".equals(dataType)) {
                MapMessage resultMsg = adPlatformServiceClient.getAdPlatformService().summaryQueryByDate(adId, dateVal);
                if (!resultMsg.isSuccess()) {
                    return;
                }
                AdPlatform originData = (AdPlatform) resultMsg.get("summaryQueryByDate");
                if (originData == null) {
                    return;
                }
                AdSummaryData summary = AdSummaryData.generateByAdPlatform(originData, 1);
                hssfWorkbook = convertDateDataToHSSfWorkbook(summary);
                filename = "按日期查看-";
            } else if ("region".equals(dataType)) {
                MapMessage resultMsg = adPlatformServiceClient.getAdPlatformService().summaryQueryByRegion(adId, dateVal);
                if (!resultMsg.isSuccess()) {
                    return;
                }
                AdPlatform originData = (AdPlatform) resultMsg.get("summaryQueryByRegion");
                if (originData == null) {
                    return;
                }
                AdSummaryData summary = AdSummaryData.generateByAdPlatform(originData, 2);
                hssfWorkbook = convertRegionDataToHSSfWorkbook(summary);
                filename = "按地域查看-";
            } else {
                return;
            }
            if (hssfWorkbook == null) {
                return;
            }
            filename = filename.concat(adId + "-" + endDate + ".xls");
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            hssfWorkbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (Exception ex) {
            logger.error("下载失败!", ex.getMessage(), ex);
            getAlertMessageManager().addMessageError("下载失败: " + ex.getMessage());
        }
    }

    @RequestMapping(value = "downloadrealtimedata.vpage", method = RequestMethod.POST)
    public void realTimeDataExport(HttpServletResponse response) {
        Long adId = getRequestLong("adId");
        String dateType = getRequestString("dateType");
        AdvertisementDetail ad = crmConfigService.$loadAdvertisementDetail(adId);
        AdvertisementSlot slot = ad == null ? null : crmConfigService.$loadAdvertisementSlot(ad.getAdSlotId());
        if (ad == null || StringUtils.isBlank(dateType) || slot == null) {
            return;
        }
        String dataFormat = "yyyy-MM-dd HH:mm";
        Map<String, Object> exportData = new HashMap<>();
        // 表头数据
        exportData.put("adId", ad.getId());
        exportData.put("adName", ad.getName());
        exportData.put("adCode", ad.getAdCode());
        exportData.put("slotId", slot.getId());
        exportData.put("slotName", slot.getName());
        List<AdRealData> clickList;
        List<AdRealData> showList;
        Map<String, AdExcelData> dataMap = new HashMap<>();
        String title;
        try {
            if (dateType.equals("hour")) {
                title = "按小时查看";
                Map<AdEnum, List<AdRealData>> data = adRealServiceClient.getAdRealService().getDataByHour(DateUtils.dateToString(new Date(), "yyyy-MM-dd HH:mm"), adId.intValue());
                clickList = data == null || CollectionUtils.isEmpty(data.get(AdEnum.HOUR_CLICK)) ? Collections.emptyList() : data.get(AdEnum.HOUR_CLICK);
                showList = data == null || CollectionUtils.isEmpty(data.get(AdEnum.HOUR_SHOW)) ? Collections.emptyList() : data.get(AdEnum.HOUR_SHOW);
            } else {
                title = "按分钟查看";
                Map<AdEnum, List<AdRealData>> data = adRealServiceClient.getAdRealService().getDataByMinute(DateUtils.dateToString(new Date(), "yyyy-MM-dd HH:mm"), adId.intValue());
                clickList = data == null || CollectionUtils.isEmpty(data.get(AdEnum.MINUTE_CLICK)) ? Collections.emptyList() : data.get(AdEnum.MINUTE_CLICK);
                showList = data == null || CollectionUtils.isEmpty(data.get(AdEnum.MINUTE_SHOW)) ? Collections.emptyList() : data.get(AdEnum.MINUTE_SHOW);
            }
            clickList.stream().filter(p -> StringUtils.isNotBlank(p.getDate())).forEach((p) -> {
                Date date = DateUtils.stringToDate(p.getDate(), dataFormat);
                AdExcelData excelData;
                if (!dataMap.containsKey(p.getDate())) {
                    excelData = new AdExcelData();
                    excelData.setDate(date);
                    excelData.setDateStr(p.getDate());
                } else {
                    excelData = dataMap.get(p.getDate());
                }
                excelData.setTotalClickPv(p.getPv());
                excelData.setTotalClickUv(p.getUv());
                dataMap.put(p.getDate(), excelData);
            });
            showList.stream().filter(p -> StringUtils.isNotBlank(p.getDate())).forEach((p) -> {
                Date date = DateUtils.stringToDate(p.getDate(), dataFormat);
                AdExcelData excelData;
                if (!dataMap.containsKey(p.getDate())) {
                    excelData = new AdExcelData();
                    excelData.setDate(date);
                    excelData.setDateStr(p.getDate());
                } else {
                    excelData = dataMap.get(p.getDate());
                }
                excelData.setTotalShowPv(p.getPv());
                excelData.setTotalShowUv(p.getUv());
                dataMap.put(p.getDate(), excelData);
            });
            List<AdExcelData> dataList = dataMap.values().stream()
                    .filter(t -> t.getDate() != null)
                    .sorted(Comparator.comparingLong(t -> t.getDate().getTime()))
                    .collect(Collectors.toList());
            exportData.put("dataList", dataList);
            HSSFWorkbook hssfWorkbook = convertExportDataToHSSfWorkbook(exportData);
            if (hssfWorkbook == null) {
                return;
            }
            String filename = StringUtils.formatMessage("广告实时数据-{}-{}-{}.xls", adId, title, DateUtils.dateToString(new Date(), "yyyyMMddHHmmss"));
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            hssfWorkbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (Exception ex) {
            logger.error("下载失败!", ex.getMessage(), ex);
            getAlertMessageManager().addMessageError("下载失败: " + ex.getMessage());
        }
    }

    //-----------------------------------------------------------------------------
    //--------------              PRIVATE METHODS                     -------------
    //-----------------------------------------------------------------------------

    private long getUserTotalHits(List<Set<String>> tagsGroup) {
        if (CollectionUtils.isEmpty(tagsGroup)) {
            return 0;
        }
        try {
            return searchEngineServiceClient.getRemoteReference().getNumTotalHitsGroup(tagsGroup);
        } catch (Exception ex) {
            logger.error("Failed to get Label User Hits, ex={}", ex);
            return 0;
        }
    }

    private HSSFWorkbook convertDateDataToHSSfWorkbook(AdSummaryData summary) {
        String[] dateDataTitle = new String[]{
                "广告编码", "广告ID", "广告位ID",
                "日期",
                "曝光量", "独立用户曝光量",
                "点击量", "点击率(%)",
                "独立用户点击量", "独立用户点击率(%)"
        };
        int[] dateDataWidth = new int[]{
                5000, 5000, 5000,
                5000,
                4000, 4000,
                4000, 4000,
                7000, 7000
        };
        HSSFWorkbook hssfWorkbook = generateWorkBookFirstRow(dateDataTitle, dateDataWidth);
        if (hssfWorkbook == null) {
            return null;
        }
        // 文本单元格边框样式
        HSSFCellStyle stringStyle = hssfWorkbook.createCellStyle();
        stringStyle.setBorderBottom(CellStyle.BORDER_THIN);
        stringStyle.setBorderTop(CellStyle.BORDER_THIN);
        stringStyle.setBorderLeft(CellStyle.BORDER_THIN);
        stringStyle.setBorderRight(CellStyle.BORDER_THIN);
        // 数字单元格边框样式
        HSSFCellStyle numberStyle = hssfWorkbook.createCellStyle();
        numberStyle.setBorderBottom(CellStyle.BORDER_THIN);
        numberStyle.setBorderTop(CellStyle.BORDER_THIN);
        numberStyle.setBorderLeft(CellStyle.BORDER_THIN);
        numberStyle.setBorderRight(CellStyle.BORDER_THIN);
        numberStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
        int rowNum = 1;
        HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);
        for (AdSummaryDetailData detailData : summary.getDetailDataList()) {
            HSSFRow row = HssfUtils.createRow(hssfSheet, rowNum++, 9, stringStyle);
            HssfUtils.setCellValue(row, 0, stringStyle, detailData.getAdCode());
            HssfUtils.setCellValue(row, 1, stringStyle, detailData.getAdId());
            HssfUtils.setCellValue(row, 2, stringStyle, detailData.getAdSlot());
            HssfUtils.setCellValue(row, 3, stringStyle, detailData.getDateStr());
            HssfUtils.setCellValue(row, 4, numberStyle, detailData.getShowPv());
            HssfUtils.setCellValue(row, 5, numberStyle, detailData.getShowUv());
            HssfUtils.setCellValue(row, 6, numberStyle, detailData.getClickPv());
            HssfUtils.setCellValue(row, 7, stringStyle, detailData.getClickRatePv());
            HssfUtils.setCellValue(row, 8, numberStyle, detailData.getClickUv());
            HssfUtils.setCellValue(row, 9, stringStyle, detailData.getClickRateUv());
        }
        // 统计数据
        HSSFRow totalRow = HssfUtils.createRow(hssfSheet, rowNum, 9, stringStyle);
        CellRangeAddress totalDataRange = new CellRangeAddress(rowNum, rowNum, 0, 3);
        hssfSheet.addMergedRegion(totalDataRange);
        HssfUtils.setCellValue(totalRow, 0, stringStyle, "合计:");
        HssfUtils.setCellValue(totalRow, 4, numberStyle, summary.getTotalShowPv());
        HssfUtils.setCellValue(totalRow, 5, numberStyle, summary.getTotalShowUv());
        HssfUtils.setCellValue(totalRow, 6, numberStyle, summary.getTotalClickPv());
        HssfUtils.setCellValue(totalRow, 7, stringStyle, summary.getTotalClickRatePv());
        HssfUtils.setCellValue(totalRow, 8, numberStyle, summary.getTotalClickUv());
        HssfUtils.setCellValue(totalRow, 9, stringStyle, summary.getTotalClickRateUv());
        return hssfWorkbook;
    }

    private HSSFWorkbook convertRegionDataToHSSfWorkbook(AdSummaryData summary) {
        String[] regionDataTitle = new String[]{
                "广告编码", "广告ID", "广告位ID",
                "省", "市", "区",
                "曝光量", "独立用户曝光量",
                "点击量", "点击率(%)",
                "独立用户点击量", "独立用户点击率(%)"
        };
        int[] regionDataWidth = new int[]{
                5000, 5000, 5000,
                3000, 3000, 3000,
                4000, 4000,
                4000, 4000,
                7000, 7000
        };
        HSSFWorkbook hssfWorkbook = generateWorkBookFirstRow(regionDataTitle, regionDataWidth);
        if (hssfWorkbook == null) {
            return null;
        }
        // 文本单元格边框样式
        HSSFCellStyle stringStyle = hssfWorkbook.createCellStyle();
        stringStyle.setBorderBottom(CellStyle.BORDER_THIN);
        stringStyle.setBorderTop(CellStyle.BORDER_THIN);
        stringStyle.setBorderLeft(CellStyle.BORDER_THIN);
        stringStyle.setBorderRight(CellStyle.BORDER_THIN);
        // 数字单元格边框样式
        HSSFCellStyle numberStyle = hssfWorkbook.createCellStyle();
        numberStyle.setBorderBottom(CellStyle.BORDER_THIN);
        numberStyle.setBorderTop(CellStyle.BORDER_THIN);
        numberStyle.setBorderLeft(CellStyle.BORDER_THIN);
        numberStyle.setBorderRight(CellStyle.BORDER_THIN);
        numberStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
        int rowNum = 1;
        HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);
        for (AdSummaryDetailData detailData : summary.getDetailDataList()) {
            HSSFRow row = HssfUtils.createRow(hssfSheet, rowNum++, 11, stringStyle);
            HssfUtils.setCellValue(row, 0, stringStyle, detailData.getAdCode());
            HssfUtils.setCellValue(row, 1, stringStyle, detailData.getAdId());
            HssfUtils.setCellValue(row, 2, stringStyle, detailData.getAdSlot());
            HssfUtils.setCellValue(row, 3, stringStyle, detailData.getProvName());
            HssfUtils.setCellValue(row, 4, stringStyle, detailData.getCityName());
            HssfUtils.setCellValue(row, 5, stringStyle, detailData.getCountyName());
            HssfUtils.setCellValue(row, 6, numberStyle, detailData.getShowPv());
            HssfUtils.setCellValue(row, 7, numberStyle, detailData.getShowUv());
            HssfUtils.setCellValue(row, 8, numberStyle, detailData.getClickPv());
            HssfUtils.setCellValue(row, 9, stringStyle, detailData.getClickRatePv());
            HssfUtils.setCellValue(row, 10, numberStyle, detailData.getClickUv());
            HssfUtils.setCellValue(row, 11, stringStyle, detailData.getClickRateUv());
        }
        // 统计数据
        HSSFRow totalRow = HssfUtils.createRow(hssfSheet, rowNum, 11, stringStyle);
        CellRangeAddress totalDataRange = new CellRangeAddress(rowNum, rowNum, 0, 5);
        hssfSheet.addMergedRegion(totalDataRange);
        HssfUtils.setCellValue(totalRow, 0, stringStyle, "合计:");
        HssfUtils.setCellValue(totalRow, 6, numberStyle, summary.getTotalShowPv());
        HssfUtils.setCellValue(totalRow, 7, numberStyle, summary.getTotalShowUv());
        HssfUtils.setCellValue(totalRow, 8, numberStyle, summary.getTotalClickPv());
        HssfUtils.setCellValue(totalRow, 9, numberStyle, summary.getTotalClickRatePv());
        HssfUtils.setCellValue(totalRow, 10, numberStyle, summary.getTotalClickUv());
        HssfUtils.setCellValue(totalRow, 11, stringStyle, summary.getTotalClickRateUv());
        return hssfWorkbook;
    }

    private HSSFWorkbook convertExportDataToHSSfWorkbook(Map<String, Object> summary) {
        String[] dateDataTitle = new String[]{"时间", "曝光量", "点击量", "独立用户曝光量", "独立用户点击量"};
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        // 文本单元格边框样式
        HSSFCellStyle stringStyle = hssfWorkbook.createCellStyle();
        stringStyle.setBorderBottom(CellStyle.BORDER_THIN);
        stringStyle.setBorderTop(CellStyle.BORDER_THIN);
        stringStyle.setBorderLeft(CellStyle.BORDER_THIN);
        stringStyle.setBorderRight(CellStyle.BORDER_THIN);
        // 数字单元格边框样式
        HSSFCellStyle numberStyle = hssfWorkbook.createCellStyle();
        numberStyle.setBorderBottom(CellStyle.BORDER_THIN);
        numberStyle.setBorderTop(CellStyle.BORDER_THIN);
        numberStyle.setBorderLeft(CellStyle.BORDER_THIN);
        numberStyle.setBorderRight(CellStyle.BORDER_THIN);
        numberStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));

        HSSFSheet hssfSheet = hssfWorkbook.createSheet();
        int rowNum = 7;
        HSSFRow firstRow = HssfUtils.createRow(hssfSheet, rowNum++, 4, stringStyle);
        for (int i = 0; i < dateDataTitle.length; ++i) {
            hssfSheet.setColumnWidth(i, 10000);
            HssfUtils.setCellValue(firstRow, i, stringStyle, dateDataTitle[i]);
        }
        HSSFRow adIdRow = HssfUtils.createRow(hssfSheet, 0, 1, stringStyle);
        HssfUtils.setCellValue(adIdRow, 0, stringStyle, "广告ID");
        HssfUtils.setCellValue(adIdRow, 1, stringStyle, SafeConverter.toString(summary.get("adId")));

        HSSFRow adNameRow = HssfUtils.createRow(hssfSheet, 1, 1, stringStyle);
        HssfUtils.setCellValue(adNameRow, 0, stringStyle, "广告名称");
        HssfUtils.setCellValue(adNameRow, 1, stringStyle, SafeConverter.toString(summary.get("adName")));

        HSSFRow adCodeRow = HssfUtils.createRow(hssfSheet, 2, 1, stringStyle);
        HssfUtils.setCellValue(adCodeRow, 0, stringStyle, "广告编码");
        HssfUtils.setCellValue(adCodeRow, 1, stringStyle, SafeConverter.toString(summary.get("adCode")));

        HSSFRow slotIdRow = HssfUtils.createRow(hssfSheet, 3, 1, stringStyle);
        HssfUtils.setCellValue(slotIdRow, 0, stringStyle, "广告位ID");
        HssfUtils.setCellValue(slotIdRow, 1, stringStyle, SafeConverter.toString(summary.get("slotId")));

        HSSFRow slotNameRow = HssfUtils.createRow(hssfSheet, 4, 1, stringStyle);
        HssfUtils.setCellValue(slotNameRow, 0, stringStyle, "广告位名称");
        HssfUtils.setCellValue(slotNameRow, 1, stringStyle, SafeConverter.toString(summary.get("slotName")));

        @SuppressWarnings("unchecked")
        List<AdExcelData> dataList = (List<AdExcelData>) summary.get("dataList");
        if (CollectionUtils.isEmpty(dataList)) {
            HSSFRow tipRow = HssfUtils.createRow(hssfSheet, rowNum, 4, stringStyle);
            CellRangeAddress totalDataRange = new CellRangeAddress(rowNum, rowNum, 0, 4);
            hssfSheet.addMergedRegion(totalDataRange);
            HssfUtils.setCellValue(tipRow, 0, stringStyle, "当前时间段没有数据");
        } else {
            for (AdExcelData detailData : dataList) {
                HSSFRow row = HssfUtils.createRow(hssfSheet, rowNum++, 4, stringStyle);
                HssfUtils.setCellValue(row, 0, stringStyle, detailData.getDateStr());
                HssfUtils.setCellValue(row, 1, numberStyle, SafeConverter.toInt(detailData.getTotalShowPv()));
                HssfUtils.setCellValue(row, 2, numberStyle, SafeConverter.toInt(detailData.getTotalClickPv()));
                HssfUtils.setCellValue(row, 3, numberStyle, SafeConverter.toInt(detailData.getTotalShowUv()));
                HssfUtils.setCellValue(row, 4, numberStyle, SafeConverter.toInt(detailData.getTotalClickUv()));
            }
        }
        return hssfWorkbook;
    }

    private HSSFWorkbook generateWorkBookFirstRow(String[] title, int[] width) {
        if (title.length == 0 || width.length == 0 || title.length != width.length) {
            return null;
        }
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        // 文本单元格边框样式
        HSSFCellStyle stringStyle = hssfWorkbook.createCellStyle();
        stringStyle.setBorderBottom(CellStyle.BORDER_THIN);
        stringStyle.setBorderTop(CellStyle.BORDER_THIN);
        stringStyle.setBorderLeft(CellStyle.BORDER_THIN);
        stringStyle.setBorderRight(CellStyle.BORDER_THIN);

        HSSFSheet hssfSheet = hssfWorkbook.createSheet();
        HSSFRow firstRow = HssfUtils.createRow(hssfSheet, 0, title.length, stringStyle);
        for (int i = 0; i < title.length; ++i) {
            hssfSheet.setColumnWidth(i, width[i]);
            HssfUtils.setCellValue(firstRow, i, stringStyle, title[i]);
        }
        return hssfWorkbook;
    }

}
